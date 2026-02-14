package com.pod.iam.application;

import cn.hutool.core.collection.CollUtil;
import com.pod.iam.domain.IamDataScope;
import com.pod.iam.mapper.IamDataScopeMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DataScopeService {

    private final IamDataScopeMapper dataScopeMapper;

    public DataScopeService(IamDataScopeMapper dataScopeMapper) {
        this.dataScopeMapper = dataScopeMapper;
    }

    /**
     * Get list of Factory IDs that the user has access to.
     * Logic:
     * 1. Query USER scopes
     * 2. Query ROLE scopes (via IamDataScopeMapper which handles the join/union logic in SQL)
     * 3. Merge and Deduplicate
     * 
     * @param userId User ID
     * @param currentFactoryId Current Factory ID (fallback)
     * @return List of accessible Factory IDs
     */
    public List<Long> getAccessibleFactoryIds(Long userId, Long currentFactoryId) {
        // The mapper method selectFactoryScopesByUserId already implements:
        // (subject_type = 'USER' AND subject_id = ?) OR (subject_type = 'ROLE' AND subject_id IN (user_roles))
        List<IamDataScope> scopes = dataScopeMapper.selectFactoryScopesByUserId(userId);
        
        List<Long> factoryIds = scopes.stream()
                .filter(s -> "ENABLED".equals(s.getStatus())) // Ensure scope is enabled
                .map(IamDataScope::getScopeId)
                .distinct()
                .collect(Collectors.toList());

        // If empty, user might not have explicit configuration.
        // Strategy: If currentFactoryId is provided (e.g. from User table), include it?
        // Or strictly follow DB.
        // User requirement: "If empty -> return currentFactoryId"
        if (CollUtil.isEmpty(factoryIds) && currentFactoryId != null) {
            factoryIds = new ArrayList<>();
            factoryIds.add(currentFactoryId);
        }
        
        return factoryIds;
    }
}
