package com.pod.iam.application;

import cn.hutool.core.lang.tree.Tree;
import com.pod.iam.domain.IamPermission;
import com.pod.iam.mapper.IamPermissionMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class MenuQueryServiceTest {

    @Mock
    private IamPermissionMapper permissionMapper;

    @InjectMocks
    private MenuQueryService menuQueryService;

    private final Long tenantId = 1L;
    private final Long factoryId = 100L;
    private final Long userId = 1000L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private IamPermission createPerm(Long id, Long parentId, String name, String code, String type, String path, String component, Long fId) {
        IamPermission p = new IamPermission();
        p.setId(id);
        p.setParentId(parentId);
        p.setPermName(name);
        p.setPermCode(code);
        p.setPermType(type);
        p.setMenuPath(path);
        p.setComponent(component);
        p.setSortNo(1);
        p.setHidden(false);
        p.setKeepAlive(true);
        p.setTenantId(tenantId);
        p.setFactoryId(fId);
        p.setStatus("ENABLED");
        return p;
    }

    @Test
    void testQueryMenus_Structure_And_Fallback() {
        // Mock Permissions
        List<IamPermission> perms = new ArrayList<>();
        // Root DIR (Factory Match)
        perms.add(createPerm(1L, 0L, "System", "sys", "DIR", "/sys", null, factoryId));
        
        // 1. Leaf MENU (Factory Match) - Component missing, should fallback to /sys/user/index
        perms.add(createPerm(2L, 1L, "User", "sys:user", "MENU", "/sys/user", null, factoryId));
        
        // 2. Leaf MENU - Component exists but no /index -> should append /index
        perms.add(createPerm(4L, 1L, "Invalid", "sys:norm", "MENU", "/sys/norm", "/sys/norm", factoryId));
        
        // 3. Leaf MENU - Invalid path (dots) -> fallback to fallback component
        perms.add(createPerm(5L, 1L, "Dots", "sys:dots", "MENU", "/sys/dots", "/../dots", factoryId));
        
        // 4. Leaf MENU - Component ends with .vue -> keep as is
        perms.add(createPerm(6L, 1L, "Vue", "sys:vue", "MENU", "/sys/vue", "/sys/vue/custom.vue", factoryId));

        // Button (Should be filtered out from menus)
        perms.add(createPerm(3L, 2L, "Add", "sys:user:add", "BUTTON", null, null, factoryId));

        when(permissionMapper.selectByUserId(anyLong())).thenReturn(perms);

        // Execute
        List<Tree<Long>> menus = menuQueryService.queryMenus(tenantId, factoryId, userId);

        // Verify
        Assertions.assertNotNull(menus);
        Assertions.assertEquals(1, menus.size()); // Only Root

        Tree<Long> root = menus.get(0);
        Assertions.assertEquals("sys", root.getName().toString());
        // Root has children -> Should be LAYOUT
        Assertions.assertEquals("LAYOUT", root.get("component"));
        
        List<Tree<Long>> children = root.getChildren();
        Assertions.assertNotNull(children);
        Assertions.assertEquals(4, children.size());
        
        // 1. Valid Fallback (menu_path + /index)
        Tree<Long> leaf = children.stream().filter(n -> n.getName().toString().equals("sys:user")).findFirst().get();
        Assertions.assertEquals("/sys/user/index", leaf.get("component"));
        
        // 2. Normalize (append /index)
        Tree<Long> norm = children.stream().filter(n -> n.getName().toString().equals("sys:norm")).findFirst().get();
        Assertions.assertEquals("/sys/norm/index", norm.get("component"));
        
        // 3. Dots -> Fallback (Invalid)
        Tree<Long> dots = children.stream().filter(n -> n.getName().toString().equals("sys:dots")).findFirst().get();
        Assertions.assertEquals("/_core/fallback/not-implemented", dots.get("component"));
        
        // 4. .vue -> Keep
        Tree<Long> vue = children.stream().filter(n -> n.getName().toString().equals("sys:vue")).findFirst().get();
        Assertions.assertEquals("/sys/vue/custom.vue", vue.get("component"));
        
        // Verify Meta
        Map<String, Object> meta = (Map<String, Object>) leaf.get("meta");
        Assertions.assertNotNull(meta);
        Assertions.assertEquals("User", meta.get("title"));
    }

    @Test
    void testQueryMenus_FactoryFilter() {
        // Mock Permissions
        List<IamPermission> perms = new ArrayList<>();
        // Factory Match
        perms.add(createPerm(1L, 0L, "Match", "match", "MENU", "/match", "MatchComp", factoryId));
        // Factory Mismatch
        perms.add(createPerm(2L, 0L, "Mismatch", "mismatch", "MENU", "/mismatch", "MismatchComp", 999L));

        when(permissionMapper.selectByUserId(anyLong())).thenReturn(perms);

        // Execute
        List<Tree<Long>> menus = menuQueryService.queryMenus(tenantId, factoryId, userId);

        // Verify
        Assertions.assertEquals(1, menus.size());
        Assertions.assertEquals("match", menus.get(0).getName().toString());
    }

    @Test
    void testQueryPermissionCodes() {
        // Mock Permissions
        List<IamPermission> perms = new ArrayList<>();
        perms.add(createPerm(1L, 0L, "Btn1", "btn:1", "BUTTON", null, null, factoryId));
        perms.add(createPerm(2L, 0L, "Btn2", "btn:2", "API", null, null, factoryId)); // API should also be included? Usually yes for frontend checks
        perms.add(createPerm(3L, 0L, "Menu", "menu:1", "MENU", "/menu", null, factoryId)); // Menu excluded

        when(permissionMapper.selectByUserId(anyLong())).thenReturn(perms);

        // Execute
        Set<String> codes = menuQueryService.queryPermissionCodes(tenantId, factoryId, userId);

        // Verify
        Assertions.assertEquals(2, codes.size());
        Assertions.assertTrue(codes.contains("btn:1"));
        Assertions.assertTrue(codes.contains("btn:2"));
    }
}
