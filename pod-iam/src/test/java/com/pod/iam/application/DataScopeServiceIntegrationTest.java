package com.pod.iam.application;

import com.pod.common.core.context.TenantContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.iam.domain.IamUser;
import com.pod.iam.mapper.IamUserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
    "spring.main.allow-bean-definition-overriding=true",
    "iam.menu.validate-on-startup=false"
})
class DataScopeServiceIntegrationTest {

    private static final Long TENANT_ID = 1L;
    private static final Long FACTORY_ID = 1L;

    @Autowired
    private DataScopeService dataScopeService;
    @Autowired
    private IamUserMapper userMapper;

    private Long userId;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(TENANT_ID);
        TenantContext.setFactoryId(FACTORY_ID);
        IamUser user = new IamUser();
        user.setUsername("scopeuser_" + System.currentTimeMillis());
        user.setPasswordHash("hash");
        user.setStatus("ENABLED");
        userMapper.insert(user);
        this.userId = user.getId();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void getAccessibleFactoryIdsStrict_emptyWhenNoScopes() {
        List<Long> strict = dataScopeService.getAccessibleFactoryIdsStrict(userId);
        assertTrue(strict.isEmpty());
    }

    @Test
    void getUserFactoryScopeIds_and_setUserFactoryScopes_fullReplace() {
        List<Long> empty = dataScopeService.getUserFactoryScopeIds(userId);
        assertTrue(empty.isEmpty());

        dataScopeService.setUserFactoryScopes(userId, List.of(1L, 2L));
        List<Long> after = dataScopeService.getUserFactoryScopeIds(userId);
        assertEquals(2, after.size());
        assertTrue(after.contains(1L));
        assertTrue(after.contains(2L));

        dataScopeService.setUserFactoryScopes(userId, List.of(2L, 3L));
        List<Long> replaced = dataScopeService.getUserFactoryScopeIds(userId);
        assertEquals(2, replaced.size());
        assertTrue(replaced.contains(2L));
        assertTrue(replaced.contains(3L));
    }

    @Test
    void getAccessibleFactoryIdsStrict_includesUserScopes() {
        dataScopeService.setUserFactoryScopes(userId, List.of(1L, 2L));
        List<Long> strict = dataScopeService.getAccessibleFactoryIdsStrict(userId);
        assertTrue(strict.contains(1L));
        assertTrue(strict.contains(2L));
    }
}
