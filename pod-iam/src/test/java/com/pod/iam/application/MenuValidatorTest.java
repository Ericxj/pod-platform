package com.pod.iam.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pod.iam.domain.IamPermission;
import com.pod.iam.dto.MenuValidationResultDto;
import com.pod.iam.mapper.IamPermissionMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class MenuValidatorTest {

    @Mock
    private IamPermissionMapper permissionMapper;

    @InjectMocks
    private MenuValidator menuValidator;

    private Long tenantId = 100L;
    private Long factoryId = 200L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private IamPermission createMenu(Long id, String name, String path, String component, String type, Long parentId) {
        IamPermission p = new IamPermission();
        p.setId(id);
        p.setPermCode(name); // using permCode as Name
        p.setPermName(name);
        p.setPermType(type);
        p.setMenuPath(path);
        p.setComponent(component); // Using new component field
        p.setApiPath("/api/mock"); // Mock API path
        p.setParentId(parentId);
        p.setTenantId(tenantId);
        p.setFactoryId(factoryId);
        p.setDeleted(0);
        return p;
    }

    @Test
    void testValidateAll_LeafComponentMissing() {
        List<IamPermission> list = new ArrayList<>();
        // MENU type with null component
        list.add(createMenu(1L, "MissingComp", "/missing", null, "MENU", 0L));

        when(permissionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(list);

        MenuValidationResultDto result = menuValidator.validateAll(tenantId, factoryId);
        
        Assertions.assertFalse(result.isOk());
        Assertions.assertTrue(result.getErrors().stream()
                .anyMatch(e -> "MENU_COMPONENT_INVALID".equals(e.getCode()) && e.getMessage().contains("empty")));
    }

    @Test
    void testValidateAll_LeafComponentHttp() {
        List<IamPermission> list = new ArrayList<>();
        // MENU type with http component
        list.add(createMenu(1L, "HttpComp", "/http", "http://google.com", "MENU", 0L));

        when(permissionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(list);

        MenuValidationResultDto result = menuValidator.validateAll(tenantId, factoryId);
        
        Assertions.assertFalse(result.isOk());
        Assertions.assertTrue(result.getErrors().stream()
                .anyMatch(e -> "MENU_COMPONENT_INVALID".equals(e.getCode()) && e.getMessage().contains("URL")));
    }

    @Test
    void testValidateAll_Success() {
        List<IamPermission> list = new ArrayList<>();
        list.add(createMenu(1L, "Dashboard", "/dashboard", "/dashboard/index", "MENU", 0L));
        list.add(createMenu(2L, "System", "/system", null, "DIR", 0L));
        list.add(createMenu(3L, "User", "/system/user", "/sys/user/index", "MENU", 2L));

        when(permissionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(list);

        MenuValidationResultDto result = menuValidator.validateAll(tenantId, factoryId);
        
        Assertions.assertTrue(result.isOk());
        Assertions.assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void testValidateAll_PathConflict() {
        List<IamPermission> list = new ArrayList<>();
        list.add(createMenu(1L, "MenuA", "/conflict", "/view/a", "MENU", 0L));
        list.add(createMenu(2L, "MenuB", "/conflict", "/view/b", "MENU", 0L));

        when(permissionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(list);

        MenuValidationResultDto result = menuValidator.validateAll(tenantId, factoryId);
        
        Assertions.assertFalse(result.isOk());
        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals("MENU_PATH_CONFLICT", result.getErrors().get(0).getCode());
    }

    @Test
    void testValidateAll_ComponentInvalid() {
        List<IamPermission> list = new ArrayList<>();
        // Invalid component: contains ..
        list.add(createMenu(1L, "InvalidComp", "/test", "../invalid/path", "MENU", 0L));

        when(permissionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(list);

        MenuValidationResultDto result = menuValidator.validateAll(tenantId, factoryId);
        
        Assertions.assertFalse(result.isOk());
        Assertions.assertTrue(result.getErrors().stream()
                .anyMatch(e -> "MENU_COMPONENT_INVALID".equals(e.getCode())));
    }

    @Test
    void testValidateAll_DirComponentNotNull() {
        List<IamPermission> list = new ArrayList<>();
        // DIR with component (not in whitelist)
        list.add(createMenu(1L, "DirWithComp", "/dir", "/some/component", "DIR", 0L));

        when(permissionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(list);

        MenuValidationResultDto result = menuValidator.validateAll(tenantId, factoryId);
        
        Assertions.assertFalse(result.isOk());
        Assertions.assertEquals("MENU_COMPONENT_INVALID", result.getErrors().get(0).getCode());
    }
    
    @Test
    void testValidateAll_DirComponentWhitelist() {
        List<IamPermission> list = new ArrayList<>();
        // DIR with LAYOUT (allowed)
        list.add(createMenu(1L, "DirLayout", "/dir", "LAYOUT", "DIR", 0L));

        when(permissionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(list);

        MenuValidationResultDto result = menuValidator.validateAll(tenantId, factoryId);
        
        Assertions.assertTrue(result.isOk());
    }

    @Test
    void testValidateAll_TreeCycle() {
        List<IamPermission> list = new ArrayList<>();
        // A -> B -> A
        IamPermission a = createMenu(1L, "A", "/a", "/view/a", "MENU", 2L); // parent B
        IamPermission b = createMenu(2L, "B", "/b", "/view/b", "MENU", 1L); // parent A
        
        list.add(a);
        list.add(b);

        when(permissionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(list);

        MenuValidationResultDto result = menuValidator.validateAll(tenantId, factoryId);
        
        Assertions.assertFalse(result.isOk());
        // Parent not found might trigger first because we check parent existence.
        // But here A's parent B exists in list. B's parent A exists.
        // So detectCycles should find it.
        Assertions.assertEquals("MENU_TREE_CYCLE", result.getErrors().get(0).getCode());
    }
}
