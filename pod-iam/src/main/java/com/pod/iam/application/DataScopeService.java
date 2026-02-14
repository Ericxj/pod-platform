package com.pod.iam.application;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.exception.BusinessException;
import com.pod.common.utils.TraceIdUtils;
import com.pod.iam.domain.IamDataScope;
import com.pod.iam.mapper.IamDataScopeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DataScopeService {

    private static final String SUBJECT_USER = "USER";
    private static final String SCOPE_FACTORY = "FACTORY";
    private static final String STATUS_ENABLED = "ENABLED";

    private final IamDataScopeMapper dataScopeMapper;

    public DataScopeService(IamDataScopeMapper dataScopeMapper) {
        this.dataScopeMapper = dataScopeMapper;
    }

    /**
     * Get list of Factory IDs that the user has access to (USER + ROLE scopes).
     * When list is empty, falls back to currentFactoryId for backward compatibility (e.g. getMyFactories).
     */
    public List<Long> getAccessibleFactoryIds(Long userId, Long currentFactoryId) {
        List<IamDataScope> scopes = dataScopeMapper.selectFactoryScopesByUserId(userId);
        final List<Long> factoryIds = scopes.stream()
                .filter(s -> STATUS_ENABLED.equals(s.getStatus()))
                .map(IamDataScope::getScopeId)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(factoryIds) && currentFactoryId != null) {
            List<Long> fallback = new ArrayList<>();
            fallback.add(currentFactoryId);
            return fallback;
        }
        return factoryIds;
    }

    /**
     * Strict list for switchFactory: only factories present in iam_data_scope (USER or ROLE), no fallback.
     * 用于切换工厂强校验：未授权工厂返回 403。
     */
    public List<Long> getAccessibleFactoryIdsStrict(Long userId) {
        List<IamDataScope> scopes = dataScopeMapper.selectFactoryScopesByUserId(userId);
        return scopes.stream()
                .filter(s -> STATUS_ENABLED.equals(s.getStatus()))
                .map(IamDataScope::getScopeId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 用户维度可访问工厂 ID 列表（仅 USER 主体：subject_type=USER, subject_id=userId, scope_type=FACTORY, status=ENABLED）。
     * 用于 GET /api/iam/users/{id}/factoryScopes。
     */
    public List<Long> getUserFactoryScopeIds(Long userId) {
        LambdaQueryWrapper<IamDataScope> w = new LambdaQueryWrapper<>();
        w.eq(IamDataScope::getSubjectType, SUBJECT_USER)
                .eq(IamDataScope::getSubjectId, userId)
                .eq(IamDataScope::getScopeType, SCOPE_FACTORY)
                .eq(IamDataScope::getStatus, STATUS_ENABLED);
        List<IamDataScope> list = dataScopeMapper.selectList(w);
        return list == null ? Collections.emptyList() : list.stream().map(IamDataScope::getScopeId).collect(Collectors.toList());
    }

    /**
     * 全量覆盖用户可访问工厂范围：先软删该用户全部 USER+FACTORY 记录，再插入新列表。
     * 落表：subject_type=USER, subject_id=userId, scope_type=FACTORY, scope_id=factoryId, status=ENABLED。
     */
    @Transactional(rollbackFor = Exception.class)
    public void setUserFactoryScopes(Long userId, List<Long> factoryIds) {
        Long tenantId = TenantContext.getTenantId();
        Long factoryId = TenantContext.getFactoryId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context required");
        }
        LambdaQueryWrapper<IamDataScope> w = new LambdaQueryWrapper<>();
        w.eq(IamDataScope::getSubjectType, SUBJECT_USER)
                .eq(IamDataScope::getSubjectId, userId)
                .eq(IamDataScope::getScopeType, SCOPE_FACTORY);
        dataScopeMapper.delete(w);

        final List<Long> scopeIds = factoryIds == null ? Collections.emptyList() : factoryIds;
        for (Long scopeId : scopeIds) {
            if (scopeId == null) continue;
            IamDataScope ds = new IamDataScope();
            ds.setSubjectType(SUBJECT_USER);
            ds.setSubjectId(userId);
            ds.setScopeType(SCOPE_FACTORY);
            ds.setScopeId(scopeId);
            ds.setStatus(STATUS_ENABLED);
            dataScopeMapper.insert(ds);
        }
    }

    /**
     * 通用：按主体与范围类型查询 scope_id 列表（仅 ENABLED 且未软删）。
     */
    public List<Long> getScopeIds(String subjectType, Long subjectId, String scopeType) {
        if (StrUtil.isBlank(subjectType) || subjectId == null || StrUtil.isBlank(scopeType)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<IamDataScope> w = new LambdaQueryWrapper<>();
        w.eq(IamDataScope::getSubjectType, subjectType)
                .eq(IamDataScope::getSubjectId, subjectId)
                .eq(IamDataScope::getScopeType, scopeType)
                .eq(IamDataScope::getStatus, STATUS_ENABLED);
        List<IamDataScope> list = dataScopeMapper.selectList(w);
        return list == null ? Collections.emptyList() : list.stream().map(IamDataScope::getScopeId).distinct().collect(Collectors.toList());
    }

    /**
     * 通用：全量覆盖某主体某范围类型的 scope_id；先软删旧记录再插入新记录，幂等。
     */
    @Transactional(rollbackFor = Exception.class)
    public void setScopeIds(String subjectType, Long subjectId, String scopeType, List<Long> scopeIds) {
        Long tenantId = TenantContext.getTenantId();
        Long factoryId = TenantContext.getFactoryId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context required");
        }
        if (StrUtil.isBlank(subjectType) || subjectId == null || StrUtil.isBlank(scopeType)) {
            throw new BusinessException("subjectType, subjectId, scopeType required");
        }
        LambdaUpdateWrapper<IamDataScope> update = new LambdaUpdateWrapper<>();
        update.eq(IamDataScope::getSubjectType, subjectType)
                .eq(IamDataScope::getSubjectId, subjectId)
                .eq(IamDataScope::getScopeType, scopeType)
                .set(IamDataScope::getDeleted, 1)
                .set(IamDataScope::getTraceId, TraceIdUtils.getTraceId());
        dataScopeMapper.update(null, update);

        List<Long> ids = scopeIds == null ? Collections.emptyList() : scopeIds.stream().distinct().filter(id -> id != null).collect(Collectors.toList());
        for (Long scopeId : ids) {
            IamDataScope ds = new IamDataScope();
            ds.setSubjectType(subjectType);
            ds.setSubjectId(subjectId);
            ds.setScopeType(scopeType);
            ds.setScopeId(scopeId);
            ds.setStatus(STATUS_ENABLED);
            dataScopeMapper.insert(ds);
        }
    }
}
