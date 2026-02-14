package com.pod.iam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pod.common.core.exception.BusinessException;
import com.pod.iam.application.AuthApplicationService;
import com.pod.iam.domain.IamDataScope;
import com.pod.iam.domain.IamUser;
import com.pod.iam.dto.LoginDto;
import com.pod.iam.dto.LoginResultDto;
import com.pod.iam.mapper.IamDataScopeMapper;
import com.pod.iam.mapper.IamUserMapper;
import com.pod.iam.mapper.LoginLogMapper;
import com.pod.iam.utils.JwtUtils;
import com.pod.infra.idempotent.service.IdempotentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AuthApplicationServiceTest {

    @Mock
    private IamUserMapper userMapper;
    @Mock
    private IamDataScopeMapper dataScopeMapper;
    @Mock
    private LoginLogMapper loginLogMapper;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private IdempotentService idempotentService;

    @InjectMocks
    private AuthApplicationService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoginSuccess() {
        // Arrange
        String username = "admin";
        String password = "123";
        String hash = "hash123";
        
        IamUser user = new IamUser();
        user.setId(1L);
        user.setUsername(username);
        user.setPasswordHash(hash);
        user.setStatus("ENABLED");
        user.setTenantId(1L);
        user.setFactoryId(1L);

        when(userMapper.selectByUsername(username)).thenReturn(user);
        when(passwordEncoder.matches(password, hash)).thenReturn(true);
        
        IamDataScope scope = new IamDataScope();
        scope.setScopeId(1L);
        when(dataScopeMapper.selectFactoryScopesByUserId(1L)).thenReturn(List.of(scope));
        
        when(jwtUtils.generateToken(any(), any(), any(), any())).thenReturn("mock-token");

        LoginDto loginDto = new LoginDto();
        loginDto.setUsername(username);
        loginDto.setPassword(password);

        // Act
        LoginResultDto result = authService.login(loginDto);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals("mock-token", result.getToken());
        Assertions.assertEquals(1L, result.getCurrentFactoryId());
    }

    @Test
    void testLoginFail_UserDisabled() {
        // Arrange
        String username = "disabledUser";
        IamUser user = new IamUser();
        user.setUsername(username);
        user.setPasswordHash("hash");
        user.setStatus("DISABLED");

        when(userMapper.selectByUsername(username)).thenReturn(user);
        
        LoginDto loginDto = new LoginDto();
        loginDto.setUsername(username);
        loginDto.setPassword("123");

        // Act & Assert
        Assertions.assertThrows(BusinessException.class, () -> authService.login(loginDto));
    }

    @Test
    void testLoginFail_UserLocked() {
        // Arrange
        String username = "lockedUser";
        IamUser user = new IamUser();
        user.setUsername(username);
        user.setPasswordHash("hash");
        user.setStatus("LOCKED");

        when(userMapper.selectByUsername(username)).thenReturn(user);
        
        LoginDto loginDto = new LoginDto();
        loginDto.setUsername(username);
        loginDto.setPassword("123");

        // Act & Assert
        Assertions.assertThrows(BusinessException.class, () -> authService.login(loginDto));
    }
}
