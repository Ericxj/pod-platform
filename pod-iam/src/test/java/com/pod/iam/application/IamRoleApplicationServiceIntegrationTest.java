package com.pod.iam.application;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pod.common.core.context.TenantContext;
import com.pod.iam.domain.IamRole;
import com.pod.iam.dto.GrantPermissionsDto;
import com.pod.iam.dto.RoleCreateDto;
import com.pod.iam.dto.RolePageQuery;
import com.pod.iam.dto.RoleUpdateDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
    "spring.main.allow-bean-definition-overriding=true",
    "iam.menu.validate-on-startup=false"
})
class IamRoleApplicationServiceIntegrationTest {

    private static final Long TENANT_ID = 1L;
    private static final Long FACTORY_ID = 1L;
    private static final Long OTHER_TENANT = 2L;
    private static final Long OTHER_FACTORY = 2L;

    @Autowired
    private IamRoleApplicationService roleService;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(TENANT_ID);
        TenantContext.setFactoryId(FACTORY_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void createRole_success() {
        RoleCreateDto dto = new RoleCreateDto();
        dto.setRoleCode("TEST_ROLE_" + System.currentTimeMillis());
        dto.setRoleName("Test Role");
        dto.setRoleType("BUSINESS");
        dto.setStatus("ENABLED");
        dto.setRemark("Integration test");

        roleService.create(dto);

        RolePageQuery query = new RolePageQuery();
        query.setCurrent(1L);
        query.setSize(10L);
        query.setRoleCode(dto.getRoleCode());
        IPage<IamRole> page = roleService.page(query);
        assertNotNull(page.getRecords());
        assertTrue(page.getTotal() >= 1);
        IamRole found = page.getRecords().stream()
                .filter(r -> dto.getRoleCode().equals(r.getRoleCode()))
                .findFirst()
                .orElse(null);
        assertNotNull(found);
        assertEquals("Test Role", found.getRoleName());
        assertEquals("ENABLED", found.getStatus());
        assertEquals(TENANT_ID, found.getTenantId());
        assertEquals(FACTORY_ID, found.getFactoryId());
    }

    @Test
    void grantPermissions_fullReplace_idempotent() {
        RoleCreateDto dto = new RoleCreateDto();
        dto.setRoleCode("GRANT_ROLE_" + System.currentTimeMillis());
        dto.setRoleName("Grant Test");
        dto.setStatus("ENABLED");
        roleService.create(dto);

        RolePageQuery query = new RolePageQuery();
        query.setRoleCode(dto.getRoleCode());
        IPage<IamRole> page = roleService.page(query);
        IamRole role = page.getRecords().stream()
                .filter(r -> dto.getRoleCode().equals(r.getRoleCode()))
                .findFirst()
                .orElseThrow();
        Long roleId = role.getId();

        List<Long> permIds = List.of(900L, 901L, 90201L);
        GrantPermissionsDto grant = new GrantPermissionsDto();
        grant.setPermIds(permIds);
        roleService.grantPermissions(roleId, grant);

        List<Long> first = roleService.getPermissionIds(roleId);
        assertEquals(3, first.size());
        assertTrue(first.containsAll(permIds));

        grant.setPermIds(List.of(90201L, 90202L));
        roleService.grantPermissions(roleId, grant);
        List<Long> second = roleService.getPermissionIds(roleId);
        assertEquals(2, second.size());
        assertTrue(second.contains(90201L));
        assertTrue(second.contains(90202L));

        roleService.grantPermissions(roleId, grant);
        List<Long> third = roleService.getPermissionIds(roleId);
        assertEquals(2, third.size());
    }

    @Test
    void pageQuery_tenantFactory_filtered() {
        RoleCreateDto dto = new RoleCreateDto();
        dto.setRoleCode("PAGE_ROLE_" + System.currentTimeMillis());
        dto.setRoleName("Page Test");
        dto.setStatus("ENABLED");
        roleService.create(dto);

        TenantContext.setTenantId(TENANT_ID);
        TenantContext.setFactoryId(FACTORY_ID);
        RolePageQuery query = new RolePageQuery();
        query.setCurrent(1L);
        query.setSize(10L);
        query.setRoleCode("PAGE_ROLE_");
        IPage<IamRole> page = roleService.page(query);
        assertNotNull(page.getRecords());
        for (IamRole r : page.getRecords()) {
            assertEquals(TENANT_ID, r.getTenantId());
            assertEquals(FACTORY_ID, r.getFactoryId());
        }
    }

    @Test
    void createRole_duplicateRoleCode_throws() {
        String code = "DUP_" + System.currentTimeMillis();
        RoleCreateDto dto = new RoleCreateDto();
        dto.setRoleCode(code);
        dto.setRoleName("First");
        dto.setStatus("ENABLED");
        roleService.create(dto);

        RoleCreateDto dto2 = new RoleCreateDto();
        dto2.setRoleCode(code);
        dto2.setRoleName("Second");
        dto2.setStatus("ENABLED");
        assertThrows(Exception.class, () -> roleService.create(dto2));
    }
}
