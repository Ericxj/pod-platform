package com.pod.iam.aspect;

import cn.hutool.core.collection.CollUtil;
import com.pod.common.core.annotation.RequirePerm;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.iam.domain.IamPermission;
import com.pod.iam.mapper.IamPermissionMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Component
public class PermissionAspect {

    private final IamPermissionMapper permissionMapper;

    public PermissionAspect(IamPermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    @Before("@annotation(requirePerm)")
    public void checkPermission(JoinPoint joinPoint, RequirePerm requirePerm) {
        Long userId = TenantContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "Unauthorized");
        }
        
        // TODO: Cache permissions for performance
        List<IamPermission> perms = permissionMapper.selectByUserId(userId);
        Set<String> codes = perms.stream().map(IamPermission::getPermCode).collect(Collectors.toSet());
        
        if (!codes.contains(requirePerm.value())) {
            throw new BusinessException(403, "Permission Denied: " + requirePerm.value());
        }
    }
}
