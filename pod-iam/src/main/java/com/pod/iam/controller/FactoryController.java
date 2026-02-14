package com.pod.iam.controller;

import com.pod.common.core.context.TenantContext;
import com.pod.common.core.domain.Result;
import com.pod.common.core.exception.BusinessException;
import com.pod.iam.application.DataScopeService;
import com.pod.iam.domain.IamUser;
import com.pod.iam.mapper.IamUserMapper;
import com.pod.iam.utils.JwtUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/iam")
public class FactoryController {

    private final DataScopeService dataScopeService;
    private final IamUserMapper userMapper;
    private final JwtUtils jwtUtils;

    public FactoryController(DataScopeService dataScopeService, IamUserMapper userMapper, JwtUtils jwtUtils) {
        this.dataScopeService = dataScopeService;
        this.userMapper = userMapper;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/factories/my")
    public Result<List<Long>> getMyFactories() {
        Long userId = TenantContext.getUserId();
        // Since we are inside a factory context usually, we might need global view.
        // But scopes are Tenant-Level or Global-Level?
        // Scopes are stored in iam_data_scope, which has tenant_id=0 usually for global or tenant_id=X.
        // The Mapper handles it.
        // However, we need to know the User's "Default Factory" to include it if necessary.
        IamUser user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("User not found");
        
        List<Long> factories = dataScopeService.getAccessibleFactoryIds(userId, user.getFactoryId());
        return Result.success(factories);
    }

    @PostMapping("/me/switchFactory")
    public Result<Map<String, Object>> switchFactory(@RequestBody Map<String, Long> body) {
        Long targetFactoryId = body.get("factoryId");
        if (targetFactoryId == null) {
            throw new BusinessException("Target Factory ID is required");
        }
        
        Long userId = TenantContext.getUserId();
        IamUser user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("User not found");

        List<Long> allowedFactoryIds = dataScopeService.getAccessibleFactoryIds(userId, user.getFactoryId());
        
        if (!allowedFactoryIds.contains(targetFactoryId)) {
            throw new BusinessException("Access to Factory " + targetFactoryId + " denied");
        }
        
        // Generate new token
        String newToken = jwtUtils.generateToken(user.getUsername(), user.getId(), user.getTenantId(), targetFactoryId);
        
        Map<String, Object> res = new HashMap<>();
        res.put("token", newToken);
        res.put("currentFactoryId", targetFactoryId);
        
        return Result.success(res);
    }
}
