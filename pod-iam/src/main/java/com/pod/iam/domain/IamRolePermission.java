package com.pod.iam.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;

@TableName("iam_role_permission")
public class IamRolePermission extends BaseEntity {
    private Long roleId;
    private Long permId;

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public Long getPermId() { return permId; }
    public void setPermId(Long permId) { this.permId = permId; }
}
