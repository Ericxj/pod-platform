package com.pod.iam.application;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pod.common.core.exception.BusinessException;
import com.pod.iam.domain.IamDataScope;
import com.pod.iam.domain.IamUser;
import com.pod.iam.domain.LoginLog;
import com.pod.iam.dto.LoginDto;
import com.pod.iam.dto.LoginResultDto;
import com.pod.iam.mapper.IamDataScopeMapper;
import com.pod.iam.mapper.IamUserMapper;
import com.pod.iam.mapper.LoginLogMapper;
import com.pod.iam.utils.JwtUtils;
import com.pod.infra.idempotent.service.IdempotentService;
import org.slf4j.MDC;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthApplicationService {

    private final IamUserMapper userMapper;
    private final IamDataScopeMapper dataScopeMapper;
    private final LoginLogMapper loginLogMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final IdempotentService idempotentService;
    private final DataScopeService dataScopeService;

    public AuthApplicationService(IamUserMapper userMapper, IamDataScopeMapper dataScopeMapper, LoginLogMapper loginLogMapper, JwtUtils jwtUtils, PasswordEncoder passwordEncoder, IdempotentService idempotentService, DataScopeService dataScopeService) {
        this.userMapper = userMapper;
        this.dataScopeMapper = dataScopeMapper;
        this.loginLogMapper = loginLogMapper;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.idempotentService = idempotentService;
        this.dataScopeService = dataScopeService;
    }

    @Transactional(rollbackFor = Exception.class)
    public LoginResultDto login(LoginDto loginDto) {
        // ... (Idempotency check skipped as per previous logic)

        // 1. Find User
        IamUser user;
        try {
            // Explicitly ignore tenant filter for login lookup, just in case
            com.pod.common.core.context.TenantIgnoreContext.setIgnore(true);
            user = userMapper.selectByUsername(loginDto.getUsername());
        } finally {
            com.pod.common.core.context.TenantIgnoreContext.clear();
        }

        if (user == null) {
            recordLoginLog(null, loginDto.getUsername(), "FAIL", "User not found");
            throw new BusinessException("User not found");
        }

        // 2. Login Check
        try {
            user.login(loginDto.getPassword(), passwordEncoder);
        } catch (Exception e) {
            recordLoginLog(user, loginDto.getUsername(), "FAIL", e.getMessage());
            throw e;
        }
        
        userMapper.updateById(user); // Update lastLoginAt

        // 3. Get Data Scopes (Factories) using DataScopeService
        List<Long> allowedFactoryIds = dataScopeService.getAccessibleFactoryIds(user.getId(), user.getFactoryId());
        
        // 4. Validate Requested Factory (if any)
        Long targetFactoryId = loginDto.getFactoryId();
        if (targetFactoryId != null) {
            if (!allowedFactoryIds.contains(targetFactoryId)) {
                recordLoginLog(user, loginDto.getUsername(), "FAIL", "Factory Access Denied: " + targetFactoryId);
                throw new BusinessException("Access to Factory " + targetFactoryId + " denied");
            }
        } else {
            // Default to first available or user's default if in list
            if (user.getFactoryId() != null && allowedFactoryIds.contains(user.getFactoryId())) {
                targetFactoryId = user.getFactoryId();
            } else if (!allowedFactoryIds.isEmpty()) {
                targetFactoryId = allowedFactoryIds.get(0);
            }
        }

        // 5. Generate Token
        String token = jwtUtils.generateToken(user.getUsername(), user.getId(), user.getTenantId(), targetFactoryId);

        LoginResultDto result = new LoginResultDto();
        result.setToken(token);
        result.setUser(user);
        result.setFactoryIds(allowedFactoryIds);
        result.setCurrentFactoryId(targetFactoryId);
        
        // 6. Audit Log
        recordLoginLog(user, loginDto.getUsername(), "SUCCESS", "Login successful");
        
        return result;
    }
    
    private void recordLoginLog(IamUser user, String username, String status, String message) {
        LoginLog log = new LoginLog();
        log.setUsername(username);
        log.setStatus(status);
        log.setMessage(message);
        log.setCreatedAt(LocalDateTime.now());
        log.setTraceId(MDC.get("traceId")); // MDC key usually traceId or trace_id
        if (user != null) {
            log.setTenantId(user.getTenantId());
            log.setFactoryId(user.getFactoryId());
        }
        // Try to insert, catch exception just in case to not fail login? 
        // User said "IAM phase at least for login add audit record".
        // It should probably be part of transaction.
        loginLogMapper.insert(log);
    }
}
