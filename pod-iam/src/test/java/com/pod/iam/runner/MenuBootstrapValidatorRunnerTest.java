package com.pod.iam.runner;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pod.iam.application.MenuValidator;
import com.pod.iam.config.IamMenuProperties;
import com.pod.iam.domain.IamPermission;
import com.pod.iam.dto.MenuValidationResultDto;
import com.pod.iam.mapper.IamPermissionMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.ApplicationArguments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MenuBootstrapValidatorRunnerTest {

    @Mock
    private IamMenuProperties properties;

    @Mock
    private MenuValidator menuValidator;

    @Mock
    private IamPermissionMapper permissionMapper;

    @InjectMocks
    private MenuBootstrapValidatorRunner runner;

    @Mock
    private ApplicationArguments args;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRun_ValidationDisabled() throws Exception {
        when(properties.isValidateOnStartup()).thenReturn(false);

        runner.run(args);

        verify(menuValidator, never()).validateAll(any(), any());
    }

    @Test
    void testRun_ValidationPassed() throws Exception {
        when(properties.isValidateOnStartup()).thenReturn(true);
        when(properties.isFailFast()).thenReturn(true);
        
        // Use explicit configuration to bypass DB scan (and avoid MP LambdaQueryWrapper issues in mock env)
        when(properties.getValidateTenants()).thenReturn(Collections.singletonList(1L));
        when(properties.getValidateFactories()).thenReturn(Collections.singletonList(1L));

        // Mock Validator returning OK
        MenuValidationResultDto result = new MenuValidationResultDto();
        result.setOk(true);
        when(menuValidator.validateAll(1L, 1L)).thenReturn(result);

        assertDoesNotThrow(() -> runner.run(args));
        
        verify(menuValidator, times(1)).validateAll(1L, 1L);
    }

    @Test
    void testRun_ValidationFailed_FailFast() throws Exception {
        when(properties.isValidateOnStartup()).thenReturn(true);
        when(properties.isFailFast()).thenReturn(true);

        // Use explicit configuration
        when(properties.getValidateTenants()).thenReturn(Collections.singletonList(1L));
        when(properties.getValidateFactories()).thenReturn(Collections.singletonList(1L));

        // Mock Validator returning Error
        MenuValidationResultDto result = new MenuValidationResultDto();
        result.setOk(false);
        result.addError("PATH_CONFLICT", "Conflict detected", 1L, "Menu1", "/path", "Comp");
        when(menuValidator.validateAll(1L, 1L)).thenReturn(result);

        assertThrows(IllegalStateException.class, () -> runner.run(args));
    }

    @Test
    void testRun_ValidationFailed_NoFailFast() throws Exception {
        when(properties.isValidateOnStartup()).thenReturn(true);
        when(properties.isFailFast()).thenReturn(false); // Do not throw

        // Use explicit configuration
        when(properties.getValidateTenants()).thenReturn(Collections.singletonList(1L));
        when(properties.getValidateFactories()).thenReturn(Collections.singletonList(1L));

        // Mock Validator returning Error
        MenuValidationResultDto result = new MenuValidationResultDto();
        result.setOk(false);
        result.addError("PATH_CONFLICT", "Conflict detected", 1L, "Menu1", "/path", "Comp");
        when(menuValidator.validateAll(1L, 1L)).thenReturn(result);

        assertDoesNotThrow(() -> runner.run(args));
        
        verify(menuValidator, times(1)).validateAll(1L, 1L);
    }
}
