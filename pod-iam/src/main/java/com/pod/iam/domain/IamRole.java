package com.pod.iam.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;

@TableName("iam_role")
public class IamRole extends BaseEntity {
    private String roleCode;
    private String roleName;
    private String roleType;
    private String status;
    private String remark;

    public void enable() {
        this.status = "ENABLED";
    }

    public void disable() {
        this.status = "DISABLED";
    }

    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getRoleType() { return roleType; }
    public void setRoleType(String roleType) { this.roleType = roleType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
