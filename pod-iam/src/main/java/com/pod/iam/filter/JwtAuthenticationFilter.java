package com.pod.iam.filter;

import com.pod.common.core.context.TenantContext;
import com.pod.iam.domain.IamPermission;
import com.pod.iam.mapper.IamDataScopeMapper;
import com.pod.iam.mapper.IamPermissionMapper;
import com.pod.iam.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final IamDataScopeMapper dataScopeMapper; // Check DB for factory access
    private final IamPermissionMapper permissionMapper;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, IamDataScopeMapper dataScopeMapper, IamPermissionMapper permissionMapper) {
        this.jwtUtils = jwtUtils;
        this.dataScopeMapper = dataScopeMapper;
        this.permissionMapper = permissionMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
                try {
                    username = jwtUtils.extractUsername(jwt);
                } catch (Exception e) {
                    // Token invalid
                }
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Simple validation using username match
                if (jwtUtils.validateToken(jwt, username)) {
                    // 1. Extract Context
                    Long tenantId = jwtUtils.extractTenantId(jwt);
                    Long userId = jwtUtils.extractUserId(jwt);
                    
                    if (tenantId != null) TenantContext.setTenantId(tenantId);
                    if (userId != null) TenantContext.setUserId(userId);

                    // 2. Determine Factory Context
                    // Priority 1: Header (Explicit switch)
                    // Priority 2: Token (Default factory)
                    String factoryHeader = request.getHeader("X-Factory-Id");
                    Long targetFactoryId = null;

                    if (factoryHeader != null && !factoryHeader.isEmpty()) {
                        try {
                            targetFactoryId = Long.parseLong(factoryHeader);
                        } catch (NumberFormatException e) {
                            // Invalid format
                        }
                    } else {
                         // Fallback to token factoryId
                         targetFactoryId = jwtUtils.extractFactoryId(jwt);
                    }

                    if (targetFactoryId != null && userId != null) {
                        // Validate user has access to this factory
                        // NOTE: We must set the Factory Context temporarily to query the iam_data_scope table 
                        // because it is partitioned by factory_id.
                        TenantContext.setFactoryId(targetFactoryId);
                        
                        if (!checkFactoryAccess(userId, targetFactoryId)) {
                             // Access Denied
                             TenantContext.setFactoryId(null); // Clear context
                             
                            if (factoryHeader != null) {
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or unauthorized Factory ID");
                                return; // Stop chain
                            }
                            // If fallback failed (e.g. factory access revoked), we just don't set context.
                        }
                    }

                    // 3. Load Permissions (Authorities)
                    // Now that Context is set, Mybatis Interceptors should scope the query.
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    if (userId != null) {
                        try {
                            // Query permissions. Interceptor will apply tenant_id and factory_id filtering if configured.
                            // IamPermissionMapper.selectByUserId uses a JOIN query. 
                            // If Interceptor fails on custom SQL, we might need manual handling.
                            // Assuming MP Interceptor handles it or logic is safe.
                            List<IamPermission> perms = permissionMapper.selectByUserId(userId);
                            if (perms != null) {
                                perms.stream()
                                     .map(IamPermission::getPermCode)
                                     .filter(code -> code != null && !code.isEmpty())
                                     .forEach(code -> authorities.add(new SimpleGrantedAuthority(code)));
                            }
                        } catch (Exception e) {
                            logger.error("Failed to load permissions", e);
                        }
                    }

                    // 4. Create Authentication Token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            username, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
    
    private boolean checkFactoryAccess(Long userId, Long factoryId) {
        // Optimized check using count
        Integer count = dataScopeMapper.countFactoryAccess(userId, factoryId);
        return count != null && count > 0;
    }
}
