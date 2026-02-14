package com.pod.iam.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 数据权限范围查询响应：scopeIds。
 */
public class DataScopeQueryDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Long> scopeIds;

    public DataScopeQueryDto() {
    }

    public DataScopeQueryDto(List<Long> scopeIds) {
        this.scopeIds = scopeIds;
    }

    public List<Long> getScopeIds() {
        return scopeIds;
    }

    public void setScopeIds(List<Long> scopeIds) {
        this.scopeIds = scopeIds;
    }
}
