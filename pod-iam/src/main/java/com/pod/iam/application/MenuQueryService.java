package com.pod.iam.application;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pod.common.core.context.TenantContext;
import com.pod.iam.domain.IamPermission;
import com.pod.iam.mapper.IamPermissionMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuQueryService {

    private final IamPermissionMapper permissionMapper;

    public MenuQueryService(IamPermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    /**
     * Query Menu Tree for specific context
     */
    public List<Tree<Long>> queryMenus(Long tenantId, Long factoryId, Long userId) {
        // 1. Get all permissions for user
        // Note: selectByUserId typically returns all permissions across roles.
        // We must filter by current factory/tenant context.
        List<IamPermission> allPerms = permissionMapper.selectByUserId(userId);

        // 2. Filter: Tenant + Factory + Status + Deleted(handled by mapper usually) + Type(MENU/DIR)
        // factory_id=0 or null 视为全局菜单，任意工厂上下文均展示
        List<IamPermission> menuPerms = allPerms.stream()
                .filter(p -> Objects.equals(p.getTenantId(), tenantId))
                .filter(p -> Objects.equals(p.getFactoryId(), factoryId)
                        || p.getFactoryId() == null
                        || Long.valueOf(0L).equals(p.getFactoryId()))
                .filter(p -> "ENABLED".equals(p.getStatus()))
                .filter(p -> "MENU".equals(p.getPermType()) || "DIR".equals(p.getPermType()))
                .sorted(Comparator.comparingInt(p -> p.getSortNo() == null ? 0 : p.getSortNo()))
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(menuPerms)) {
            return new ArrayList<>();
        }

        // 3. Build Tree
        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        treeNodeConfig.setIdKey("id");
        treeNodeConfig.setParentIdKey("parentId");
        treeNodeConfig.setWeightKey("sortNo");
        treeNodeConfig.setNameKey("name");
        // Vben requires children key to be 'children'
        treeNodeConfig.setChildrenKey("children");

        List<Tree<Long>> treeNodes = TreeUtil.build(menuPerms, 0L, treeNodeConfig, (node, tree) -> {
            tree.setId(node.getId());
            tree.setParentId(node.getParentId());
            tree.setWeight(node.getSortNo());
            
            // Name: Generate PascalCase name from path (e.g. /system/user -> SystemUser)
            // Fallback to permCode if path is empty, then ID
            String name = generateRouteName(node.getMenuPath());
            if (StrUtil.isBlank(name)) {
                name = StrUtil.isNotBlank(node.getPermCode()) ? node.getPermCode() : String.valueOf(node.getId());
            }
            tree.setName(name);

            // Path: must start with /
            String path = node.getMenuPath();
            if (StrUtil.isNotBlank(path) && !path.startsWith("/") && !path.startsWith("http")) {
                path = "/" + path;
            }
            tree.put("path", path);

            // Component: Placeholder, will be refined in post-processing
            tree.put("component", node.getComponent()); 
            
            if (StrUtil.isNotBlank(node.getRedirect())) {
                tree.put("redirect", node.getRedirect());
            }

            Map<String, Object> meta = new HashMap<>();
            meta.put("title", node.getPermName());
            meta.put("icon", node.getIcon());
            meta.put("order", node.getSortNo());
            meta.put("hideInMenu", Boolean.TRUE.equals(node.getHidden()));
            meta.put("keepAlive", Boolean.TRUE.equals(node.getKeepAlive()));
            if (Boolean.TRUE.equals(node.getAlwaysShow())) {
                // For Vben, some versions use meta.alwaysShow, some use root property.
                // Safest to put in meta.
                meta.put("alwaysShow", true);
            }
            tree.put("meta", meta);
        });

        // 4. Post-process for Component Logic (The "Smart Fallback")
        for (Tree<Long> node : treeNodes) {
            applyComponentRules(node);
        }

        return treeNodes;
    }

    /**
     * Query Permission Codes (BUTTON/API)
     */
    public Set<String> queryPermissionCodes(Long tenantId, Long factoryId, Long userId) {
        List<IamPermission> allPerms = permissionMapper.selectByUserId(userId);

        return allPerms.stream()
                .filter(p -> Objects.equals(p.getTenantId(), tenantId))
                .filter(p -> Objects.equals(p.getFactoryId(), factoryId))
                .filter(p -> "ENABLED".equals(p.getStatus()))
                .filter(p -> "BUTTON".equals(p.getPermType()) || "API".equals(p.getPermType()))
                .map(IamPermission::getPermCode)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
    }

    // --- Helper ---

    private String generateRouteName(String path) {
        if (StrUtil.isBlank(path)) return null;
        String clean = StrUtil.strip(path, "/");
        if (StrUtil.isBlank(clean)) return null;
        
        String[] parts = clean.split("/");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            // Handle parameterized paths like :id -> Id? Or just strip colon?
            // Usually menus don't have params.
            if (part.startsWith(":")) {
                part = part.substring(1);
            }
            sb.append(StrUtil.upperFirst(part));
        }
        return sb.toString();
    }

    private void applyComponentRules(Tree<Long> node) {
        // 1. Recursive first (DFS)
        List<Tree<Long>> children = node.getChildren();
        if (CollUtil.isNotEmpty(children)) {
            for (Tree<Long> child : children) {
                applyComponentRules(child);
            }
        }

        // 2. Determine Component
        // Rule: If has children -> LAYOUT
        
        String currentComponent = (String) node.get("component");
        boolean hasChildren = CollUtil.isNotEmpty(children);
        String path = (String) node.get("path");

        if (hasChildren) {
            // Directory / Parent Node -> LAYOUT
            if (StrUtil.isBlank(currentComponent) || "LAYOUT".equalsIgnoreCase(currentComponent)) {
                node.put("component", "LAYOUT");
            }
        } else {
            // Leaf Node
            String normalized = normalizeComponent(currentComponent, path);
            node.put("component", normalized);
        }
    }

    private String normalizeComponent(String component, String menuPath) {
        // Rule: if component == 'LAYOUT' return 'LAYOUT'
        if ("LAYOUT".equalsIgnoreCase(component)) {
            return "LAYOUT";
        }

        // Rule: if component is blank: return path + '/index' (no leading slash)
        if (StrUtil.isBlank(component)) {
            if (StrUtil.isBlank(menuPath)) {
                return "_core/fallback/not-implemented";
            }
            String cleanPath = StrUtil.strip(menuPath, "/");
            return cleanPath + "/index";
        }

        String normalized = component.trim();
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.startsWith("api") || normalized.contains("..") || normalized.contains("\\")
            || normalized.contains(":") || normalized.contains("http")) {
            return "_core/fallback/not-implemented";
        }
        if (!normalized.endsWith("/index") && !normalized.endsWith(".vue")) {
            normalized = normalized + "/index";
        }
        return normalized;
    }
}

