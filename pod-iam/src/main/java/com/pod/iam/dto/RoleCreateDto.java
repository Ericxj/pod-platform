package com.pod.iam.dto;

import java.io.Serializable;

/**
 * 创建角色请求体。tenant_id/factory_id 由上下文注入。
 */
public class RoleCreateDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String roleCode;
    private String roleName;
    private String roleType;
    private String status;
    private String remark;

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
