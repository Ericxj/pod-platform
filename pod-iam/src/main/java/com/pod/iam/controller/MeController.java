package com.pod.iam.controller;

import cn.hutool.core.lang.tree.Tree;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.domain.Result;
import com.pod.iam.application.MenuQueryService;
import com.pod.iam.domain.IamUser;
import com.pod.iam.domain.IamDataScope;
import com.pod.iam.mapper.IamDataScopeMapper;
import com.pod.iam.mapper.IamUserMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/iam/me")
public class MeController {

    private final MenuQueryService menuQueryService;
    private final IamUserMapper userMapper;
    private final IamDataScopeMapper dataScopeMapper;

    public MeController(MenuQueryService menuQueryService, IamUserMapper userMapper, IamDataScopeMapper dataScopeMapper) {
        this.menuQueryService = menuQueryService;
        this.userMapper = userMapper;
        this.dataScopeMapper = dataScopeMapper;
    }

    @GetMapping
    public Result<IamUser> me() {
        Long userId = TenantContext.getUserId();
        if (userId == null) return Result.success(null);
        return Result.success(userMapper.selectById(userId));
    }

    /**
     * Get user menus for current factory context
     */
    @GetMapping("/menus")
    public Result<Map<String, Object>> menus() {
        Long userId = TenantContext.getUserId();
        Long tenantId = TenantContext.getTenantId();
        Long factoryId = resolveFactoryId(userId);

        if (userId == null || tenantId == null || factoryId == null) {
            return Result.success(Map.of("menus", List.of(), "permissionCodes", List.of()));
        }

        List<Tree<Long>> menus = menuQueryService.queryMenus(tenantId, factoryId, userId);
        Set<String> codes = menuQueryService.queryPermissionCodes(tenantId, factoryId, userId);
        
        // Construct Data Scopes
        List<IamDataScope> scopes = dataScopeMapper.selectFactoryScopesByUserId(userId);
        List<Long> factoryIds = scopes.stream().map(IamDataScope::getScopeId).collect(Collectors.toList());
        if (!factoryIds.contains(factoryId)) {
             factoryIds.add(factoryId);
        }

        Map<String, Object> dataScopes = new HashMap<>();
        dataScopes.put("factoryIds", factoryIds);
        dataScopes.put("defaultFactoryId", factoryId);

        Map<String, Object> result = new HashMap<>();
        result.put("menus", menus);
        result.put("permissionCodes", codes);
        result.put("dataScopes", dataScopes);

        return Result.success(result);
    }

    /**
     * Get user permission codes for current factory context
     */
    @GetMapping("/perms")
    public Result<Map<String, Object>> perms() {
        Long userId = TenantContext.getUserId();
        Long tenantId = TenantContext.getTenantId();
        Long factoryId = resolveFactoryId(userId);

        if (userId == null || tenantId == null || factoryId == null) {
            return Result.success(Map.of("permissionCodes", Set.of()));
        }

        Set<String> codes = menuQueryService.queryPermissionCodes(tenantId, factoryId, userId);
        return Result.success(Map.of("permissionCodes", codes));
    }

    // --- Helper ---

    private Long resolveFactoryId(Long userId) {
        Long fid = TenantContext.getFactoryId();
        if (fid != null) return fid;
        
        // Fallback to user's default factory
        if (userId != null) {
            IamUser user = userMapper.selectById(userId);
            if (user != null) {
                return user.getFactoryId();
            }
        }
        return null;
    }
}
