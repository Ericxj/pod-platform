package com.pod.iam.application;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.context.TenantIgnoreContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.common.utils.TraceIdUtils;
import com.pod.iam.domain.IamPermission;
import com.pod.iam.dto.PermissionCreateDto;
import com.pod.iam.dto.PermissionPageQuery;
import com.pod.iam.dto.PermissionTreeDto;
import com.pod.iam.dto.PermissionUpdateDto;
import com.pod.iam.dto.PermissionValidateResultDto;
import com.pod.iam.mapper.IamPermissionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class IamPermissionApplicationService {

    private static final List<String> PERM_TYPES = List.of("MENU", "BUTTON", "API");
    private static final List<String> ALLOWED_STATUS = List.of("ENABLED", "DISABLED");

    private final IamPermissionMapper permissionMapper;

    public IamPermissionApplicationService(IamPermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    /**
     * 树结构：按 parent_id 组树，sort_no 升序。
     * permType=MENU 只返回 MENU；ALL 返回所有类型（BUTTON/API 可挂在 MENU 下）。
     */
    public List<PermissionTreeDto> tree(String permType) {
        Long tenantId = TenantContext.getTenantId();
        Long factoryId = TenantContext.getFactoryId();
        if (tenantId == null || factoryId == null) {
            throw new BusinessException("Tenant and factory context required");
        }
        LambdaQueryWrapper<IamPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(IamPermission::getSortNo);
        List<IamPermission> list = permissionMapper.selectList(wrapper);
        final List<IamPermission> sourceList = list == null ? new ArrayList<>() : list;

        List<IamPermission> filtered = sourceList.stream()
                .filter(p -> "ALL".equalsIgnoreCase(permType) || (permType != null && permType.equalsIgnoreCase(p.getPermType())))
                .collect(Collectors.toList());

        List<PermissionTreeDto> roots = buildTree(filtered, null);
        roots.sort(Comparator.comparingInt(n -> n.getSortNo() != null ? n.getSortNo() : 0));
        return roots;
    }

    private List<PermissionTreeDto> buildTree(List<IamPermission> flat, Long parentId) {
        Long pid = parentId == null ? 0L : parentId;
        List<IamPermission> children = flat.stream()
                .filter(p -> Objects.equals(p.getParentId() == null ? 0L : p.getParentId(), pid))
                .sorted(Comparator.comparingInt(p -> p.getSortNo() == null ? 0 : p.getSortNo()))
                .collect(Collectors.toList());
        List<PermissionTreeDto> result = new ArrayList<>();
        for (IamPermission p : children) {
            PermissionTreeDto node = toTreeDto(p);
            node.setChildren(buildTree(flat, p.getId()));
            result.add(node);
        }
        return result;
    }

    private static PermissionTreeDto toTreeDto(IamPermission p) {
        PermissionTreeDto dto = new PermissionTreeDto();
        dto.setId(p.getId());
        dto.setTenantId(p.getTenantId());
        dto.setFactoryId(p.getFactoryId());
        dto.setPermCode(p.getPermCode());
        dto.setPermName(p.getPermName());
        dto.setPermType(p.getPermType());
        dto.setMenuPath(p.getMenuPath());
        dto.setComponent(p.getComponent());
        dto.setIcon(p.getIcon());
        dto.setRedirect(p.getRedirect());
        dto.setApiMethod(p.getApiMethod());
        dto.setApiPath(p.getApiPath());
        dto.setParentId(p.getParentId());
        dto.setSortNo(p.getSortNo());
        dto.setStatus(p.getStatus());
        dto.setHidden(p.getHidden());
        dto.setKeepAlive(p.getKeepAlive());
        dto.setAlwaysShow(p.getAlwaysShow());
        return dto;
    }

    public IamPermission get(Long id) {
        return permissionMapper.selectById(id);
    }

    public IPage<IamPermission> page(PermissionPageQuery query) {
        Page<IamPermission> page = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<IamPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StrUtil.isNotBlank(query.getKeyword()), w -> w
                .like(IamPermission::getPermCode, query.getKeyword())
                .or().like(IamPermission::getPermName, query.getKeyword())
                .or().like(IamPermission::getMenuPath, query.getKeyword()));
        wrapper.eq(StrUtil.isNotBlank(query.getPermType()), IamPermission::getPermType, query.getPermType());
        wrapper.orderByAsc(IamPermission::getSortNo).orderByAsc(IamPermission::getId);
        return permissionMapper.selectPage(page, wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(PermissionCreateDto dto) {
        validatePermType(dto.getPermType());
        validateStatus(dto.getStatus());
        Long tenantId = TenantContext.getTenantId();
        Long factoryId = TenantContext.getFactoryId();
        if (tenantId == null || factoryId == null) {
            throw new BusinessException("Tenant and factory context required");
        }
        if (StrUtil.isBlank(dto.getPermCode())) {
            throw new BusinessException("permCode is required");
        }
        PermissionValidateResultDto vr = validate(
                dto.getPermCode(), dto.getMenuPath(), dto.getApiMethod(), dto.getApiPath(),
                dto.getPermType(), null);
        if (!vr.isValid()) {
            throw new BusinessException(vr.getMessage());
        }
        IamPermission p = new IamPermission();
        p.setPermCode(dto.getPermCode().trim());
        p.setPermName(StrUtil.isNotBlank(dto.getPermName()) ? dto.getPermName().trim() : dto.getPermCode());
        p.setPermType(StrUtil.isNotBlank(dto.getPermType()) ? dto.getPermType() : "BUTTON");
        p.setMenuPath(StrUtil.isNotBlank(dto.getMenuPath()) ? dto.getMenuPath().trim() : null);
        p.setComponent(StrUtil.isNotBlank(dto.getComponent()) ? dto.getComponent().trim() : null);
        p.setIcon(StrUtil.isNotBlank(dto.getIcon()) ? dto.getIcon().trim() : null);
        p.setRedirect(StrUtil.isNotBlank(dto.getRedirect()) ? dto.getRedirect().trim() : null);
        p.setApiMethod(StrUtil.isNotBlank(dto.getApiMethod()) ? dto.getApiMethod().trim() : null);
        p.setApiPath(StrUtil.isNotBlank(dto.getApiPath()) ? dto.getApiPath().trim() : null);
        p.setParentId(dto.getParentId());
        p.setSortNo(dto.getSortNo() != null ? dto.getSortNo() : 0);
        p.setStatus(StrUtil.isNotBlank(dto.getStatus()) ? dto.getStatus() : "ENABLED");
        p.setHidden(dto.getHidden() != null && dto.getHidden());
        p.setKeepAlive(dto.getKeepAlive() == null || dto.getKeepAlive());
        p.setAlwaysShow(dto.getAlwaysShow() != null && dto.getAlwaysShow());
        permissionMapper.insert(p);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, PermissionUpdateDto dto) {
        IamPermission p = permissionMapper.selectById(id);
        if (p == null) {
            throw new BusinessException("Permission not found");
        }
        if (StrUtil.isNotBlank(dto.getPermName())) p.setPermName(dto.getPermName().trim());
        if (StrUtil.isNotBlank(dto.getPermType())) {
            validatePermType(dto.getPermType());
            p.setPermType(dto.getPermType());
        }
        if (dto.getMenuPath() != null) p.setMenuPath(dto.getMenuPath().trim().isEmpty() ? null : dto.getMenuPath().trim());
        if (dto.getComponent() != null) p.setComponent(dto.getComponent().trim().isEmpty() ? null : dto.getComponent().trim());
        if (dto.getIcon() != null) p.setIcon(dto.getIcon().trim().isEmpty() ? null : dto.getIcon().trim());
        if (dto.getRedirect() != null) p.setRedirect(dto.getRedirect().trim().isEmpty() ? null : dto.getRedirect().trim());
        if (dto.getApiMethod() != null) p.setApiMethod(dto.getApiMethod().trim().isEmpty() ? null : dto.getApiMethod().trim());
        if (dto.getApiPath() != null) p.setApiPath(dto.getApiPath().trim().isEmpty() ? null : dto.getApiPath().trim());
        if (dto.getParentId() != null) p.setParentId(dto.getParentId());
        if (dto.getSortNo() != null) p.setSortNo(dto.getSortNo());
        if (StrUtil.isNotBlank(dto.getStatus())) {
            validateStatus(dto.getStatus());
            p.setStatus(dto.getStatus());
        }
        if (dto.getHidden() != null) p.setHidden(dto.getHidden());
        if (dto.getKeepAlive() != null) p.setKeepAlive(dto.getKeepAlive());
        if (dto.getAlwaysShow() != null) p.setAlwaysShow(dto.getAlwaysShow());

        PermissionValidateResultDto vr = validate(
                p.getPermCode(), p.getMenuPath(), p.getApiMethod(), p.getApiPath(),
                p.getPermType(), id);
        if (!vr.isValid()) {
            throw new BusinessException(vr.getMessage());
        }
        permissionMapper.updateById(p);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        IamPermission p = permissionMapper.selectById(id);
        if (p == null) {
            throw new BusinessException("Permission not found");
        }
        p.setDeleted(1);
        p.setTraceId(TraceIdUtils.getTraceId());
        permissionMapper.updateById(p);
    }

    /**
     * 校验是否冲突（tenant 维度 + deleted=0）。
     * perm_code: tenant 内唯一；menu_path: 仅 MENU，tenant+factory 唯一；api_method+api_path: 仅 API，tenant 内唯一。
     */
    public PermissionValidateResultDto validate(String permCode, String menuPath, String apiMethod, String apiPath, String permType, Long excludeId) {
        Long tenantId = TenantContext.getTenantId();
        Long factoryId = TenantContext.getFactoryId();
        if (tenantId == null) {
            return PermissionValidateResultDto.conflict("Tenant context required");
        }

        if (StrUtil.isNotBlank(permCode)) {
            try {
                TenantIgnoreContext.setIgnore(true);
                long count = permissionMapper.selectCount(
                        new LambdaQueryWrapper<IamPermission>()
                                .eq(IamPermission::getTenantId, tenantId)
                                .eq(IamPermission::getPermCode, permCode)
                                .ne(excludeId != null, IamPermission::getId, excludeId));
                if (count > 0) {
                    return PermissionValidateResultDto.conflict("permCode conflict in tenant");
                }
            } finally {
                TenantIgnoreContext.clear();
            }
        }

        if ("MENU".equalsIgnoreCase(permType) && StrUtil.isNotBlank(menuPath)) {
            LambdaQueryWrapper<IamPermission> w = new LambdaQueryWrapper<>();
            w.eq(IamPermission::getMenuPath, menuPath).ne(excludeId != null, IamPermission::getId, excludeId);
            long count = permissionMapper.selectCount(w);
            if (count > 0) {
                return PermissionValidateResultDto.conflict("menuPath conflict in tenant+factory");
            }
        }

        if ("API".equalsIgnoreCase(permType) && StrUtil.isNotBlank(apiMethod) && StrUtil.isNotBlank(apiPath)) {
            try {
                TenantIgnoreContext.setIgnore(true);
                long count = permissionMapper.selectCount(
                        new LambdaQueryWrapper<IamPermission>()
                                .eq(IamPermission::getTenantId, tenantId)
                                .eq(IamPermission::getPermType, "API")
                                .eq(IamPermission::getApiMethod, apiMethod)
                                .eq(IamPermission::getApiPath, apiPath)
                                .ne(excludeId != null, IamPermission::getId, excludeId));
                if (count > 0) {
                    return PermissionValidateResultDto.conflict("api_method+api_path conflict in tenant");
                }
            } finally {
                TenantIgnoreContext.clear();
            }
        }

        return PermissionValidateResultDto.ok();
    }

    private void validatePermType(String type) {
        if (StrUtil.isBlank(type)) return;
        if (!PERM_TYPES.contains(type)) {
            throw new BusinessException("permType must be MENU, BUTTON or API");
        }
    }

    private void validateStatus(String status) {
        if (StrUtil.isBlank(status)) return;
        if (!ALLOWED_STATUS.contains(status)) {
            throw new BusinessException("status must be ENABLED or DISABLED");
        }
    }
}
