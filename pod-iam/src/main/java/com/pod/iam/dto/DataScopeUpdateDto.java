package com.pod.iam.dto;

import java.io.Serializable;
import java.util.List;

/** 数据权限范围全量覆盖请求体。 */
public class DataScopeUpdateDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String subjectType;
    private Long subjectId;
    private String scopeType;
    private List<Long> scopeIds;

    public String getSubjectType() { return subjectType; }
    public void setSubjectType(String subjectType) { this.subjectType = subjectType; }
    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
    public String getScopeType() { return scopeType; }
    public void setScopeType(String scopeType) { this.scopeType = scopeType; }
    public List<Long> getScopeIds() { return scopeIds; }
    public void setScopeIds(List<Long> scopeIds) { this.scopeIds = scopeIds; }
}
