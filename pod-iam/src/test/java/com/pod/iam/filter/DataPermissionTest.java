package com.pod.iam.filter;

import com.pod.common.core.context.TenantContext;
import com.pod.iam.mapper.IamDataScopeMapper;
import com.pod.iam.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DataPermissionTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private IamDataScopeMapper dataScopeMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @Test
    void testBypass_InvalidFactoryId_ShouldFail() throws ServletException, IOException {
        // Arrange
        String token = "valid.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.extractUsername(token)).thenReturn("user");
        when(jwtUtils.validateToken(token, "user")).thenReturn(true);
        when(jwtUtils.extractUserId(token)).thenReturn(1L);
        when(jwtUtils.extractTenantId(token)).thenReturn(1L);

        // User requests Factory 999
        when(request.getHeader("X-Factory-Id")).thenReturn("999");

        // DB says NO access (count = 0)
        when(dataScopeMapper.countFactoryAccess(1L, 999L)).thenReturn(0);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or unauthorized Factory ID");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void testNormalAccess_ValidFactoryId_ShouldPass() throws ServletException, IOException {
        // Arrange
        String token = "valid.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.extractUsername(token)).thenReturn("user");
        when(jwtUtils.validateToken(token, "user")).thenReturn(true);
        when(jwtUtils.extractUserId(token)).thenReturn(1L);
        
        when(request.getHeader("X-Factory-Id")).thenReturn("1");
        when(dataScopeMapper.countFactoryAccess(1L, 1L)).thenReturn(1);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        verify(chain).doFilter(request, response);
    }

    @Test
    void testNoFactoryHeader_ShouldPassWithNullFactoryContext() throws ServletException, IOException {
        // Arrange
        String token = "valid.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.extractUsername(token)).thenReturn("user");
        when(jwtUtils.validateToken(token, "user")).thenReturn(true);
        
        when(request.getHeader("X-Factory-Id")).thenReturn(null);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        verify(chain).doFilter(request, response);
        // TenantContext.getFactoryId() should be null
    }
}
