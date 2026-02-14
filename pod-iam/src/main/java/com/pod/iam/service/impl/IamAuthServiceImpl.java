package com.pod.iam.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.context.TenantIgnoreContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.iam.domain.IamDataScope;
import com.pod.iam.domain.IamPermission;
import com.pod.iam.domain.IamUser;
import com.pod.iam.dto.LoginDto;
import com.pod.iam.mapper.IamDataScopeMapper;
import com.pod.iam.mapper.IamPermissionMapper;
import com.pod.iam.mapper.IamUserMapper;
import com.pod.iam.service.IamAuthService;
import com.pod.iam.utils.JwtUtils;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IamAuthServiceImpl implements IamAuthService {

    private final IamUserMapper userMapper;
    private final IamPermissionMapper permissionMapper;
    private final IamDataScopeMapper dataScopeMapper;
    private final JwtUtils jwtUtils;

    public IamAuthServiceImpl(IamUserMapper userMapper, IamPermissionMapper permissionMapper, IamDataScopeMapper dataScopeMapper, JwtUtils jwtUtils) {
        this.userMapper = userMapper;
        this.permissionMapper = permissionMapper;
        this.dataScopeMapper = dataScopeMapper;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public String login(LoginDto loginDto) {
        // Find user by username
        // Note: In multi-tenant, username should be unique within tenant. 
        // But here we might not know tenantId yet unless passed in header or login param.
        // Assuming username is unique globally or we ignore tenant for login lookup.
        
        IamUser user;
        try {
            TenantIgnoreContext.setIgnore(true);
            user = userMapper.selectOne(new LambdaQueryWrapper<IamUser>()
                    .eq(IamUser::getUsername, loginDto.getUsername()));
        } finally {
            TenantIgnoreContext.clear();
        }

        if (user == null) {
            throw new BusinessException("User not found");
        }

        if (!BCrypt.checkpw(loginDto.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("Invalid password");
        }

        if (!"ENABLED".equals(user.getStatus())) {
            throw new BusinessException("User is disabled");
        }

        // Generate Token
        return jwtUtils.generateToken(user.getUsername(), user.getId(), user.getTenantId(), user.getFactoryId());
    }

    @Override
    public IamUser getCurrentUser() {
        Long userId = TenantContext.getUserId();
        if (userId == null) {
            throw new BusinessException("User not logged in");
        }
        return userMapper.selectById(userId);
    }

    @Override
    public List<IamPermission> getCurrentUserPermissions() {
        Long userId = TenantContext.getUserId();
        if (userId == null) {
            return List.of();
        }
        return permissionMapper.selectByUserId(userId);
    }

    @Override
    public List<Long> getCurrentUserFactoryIds() {
        Long userId = TenantContext.getUserId();
        if (userId == null) {
            return List.of();
        }
        List<IamDataScope> scopes = dataScopeMapper.selectFactoryScopesByUserId(userId);
        if (CollUtil.isEmpty(scopes)) {
            // If no explicit scope, maybe default to user's factoryId?
            // Or if admin, all? 
            // Let's return user's factoryId as default
            IamUser user = userMapper.selectById(userId);
            return List.of(user.getFactoryId());
        }
        return scopes.stream().map(IamDataScope::getScopeId).collect(Collectors.toList());
    }
}
