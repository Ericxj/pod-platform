package com.pod.iam.application;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.common.utils.TraceIdUtils;
import com.pod.iam.domain.IamRole;
import com.pod.iam.domain.IamRolePermission;
import com.pod.iam.dto.GrantPermissionsDto;
import com.pod.iam.dto.RoleCreateDto;
import com.pod.iam.dto.RolePageQuery;
import com.pod.iam.dto.RoleUpdateDto;
import com.pod.iam.mapper.IamRoleMapper;
import com.pod.iam.mapper.IamRolePermissionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IamRoleApplicationService {

    private static final List<String> ALLOWED_STATUS = List.of("ENABLED", "DISABLED");

    private final IamRoleMapper roleMapper;
    private final IamRolePermissionMapper rolePermissionMapper;

    public IamRoleApplicationService(IamRoleMapper roleMapper, IamRolePermissionMapper rolePermissionMapper) {
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
    }

    public IPage<IamRole> page(RolePageQuery query) {
        Page<IamRole> page = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<IamRole> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w.like(IamRole::getRoleCode, query.getKeyword()).or().like(IamRole::getRoleName, query.getKeyword()));
        } else {
            wrapper.like(StrUtil.isNotBlank(query.getRoleCode()), IamRole::getRoleCode, query.getRoleCode());
            wrapper.like(StrUtil.isNotBlank(query.getRoleName()), IamRole::getRoleName, query.getRoleName());
        }
        wrapper.eq(StrUtil.isNotBlank(query.getStatus()), IamRole::getStatus, query.getStatus());
        wrapper.orderByDesc(IamRole::getCreatedAt);
        return roleMapper.selectPage(page, wrapper);
    }

    public IamRole get(Long id) {
        return roleMapper.selectById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(RoleCreateDto dto) {
        validateStatus(dto.getStatus());
        Long tenantId = TenantContext.getTenantId();
        Long factoryId = TenantContext.getFactoryId();
        if (tenantId == null || factoryId == null) {
            throw new BusinessException("Tenant and factory context required");
        }
        if (StrUtil.isBlank(dto.getRoleCode())) {
            throw new BusinessException("roleCode is required");
        }
        long count = roleMapper.selectCount(new LambdaQueryWrapper<IamRole>()
                .eq(IamRole::getRoleCode, dto.getRoleCode()));
        if (count > 0) {
            throw new BusinessException("role_code already exists in current tenant+factory scope");
        }
        IamRole role = new IamRole();
        role.setRoleCode(dto.getRoleCode().trim());
        role.setRoleName(StrUtil.isNotBlank(dto.getRoleName()) ? dto.getRoleName().trim() : dto.getRoleCode());
        role.setRoleType(StrUtil.isNotBlank(dto.getRoleType()) ? dto.getRoleType() : "BUSINESS");
        role.setStatus(StrUtil.isNotBlank(dto.getStatus()) ? dto.getStatus() : "ENABLED");
        role.setRemark(dto.getRemark());
        roleMapper.insert(role);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, RoleUpdateDto dto) {
        IamRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("Role not found");
        }
        if (StrUtil.isNotBlank(dto.getRoleName())) {
            role.setRoleName(dto.getRoleName().trim());
        }
        if (StrUtil.isNotBlank(dto.getStatus())) {
            validateStatus(dto.getStatus());
            role.setStatus(dto.getStatus());
        }
        if (dto.getRemark() != null) {
            role.setRemark(dto.getRemark());
        }
        roleMapper.updateById(role);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        IamRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("Role not found");
        }
        role.setDeleted(1);
        role.setTraceId(TraceIdUtils.getTraceId());
        roleMapper.updateById(role);
    }

    public List<Long> getPermissionIds(Long roleId) {
        IamRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("Role not found");
        }
        List<IamRolePermission> list = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<IamRolePermission>().eq(IamRolePermission::getRoleId, roleId));
        return list.stream().map(IamRolePermission::getPermId).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void grantPermissions(Long roleId, GrantPermissionsDto dto) {
        IamRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("Role not found");
        }
        List<Long> permIds = dto.getPermIds() != null ? dto.getPermIds() : Collections.emptyList();
        permIds = permIds.stream().distinct().collect(Collectors.toList());

        rolePermissionMapper.delete(new LambdaQueryWrapper<IamRolePermission>()
                .eq(IamRolePermission::getRoleId, roleId));

        for (Long permId : permIds) {
            if (permId == null) continue;
            IamRolePermission rp = new IamRolePermission();
            rp.setRoleId(roleId);
            rp.setPermId(permId);
            rolePermissionMapper.insert(rp);
        }
    }

    private void validateStatus(String status) {
        if (StrUtil.isBlank(status)) return;
        if (!ALLOWED_STATUS.contains(status)) {
            throw new BusinessException("status must be ENABLED or DISABLED");
        }
    }
}
