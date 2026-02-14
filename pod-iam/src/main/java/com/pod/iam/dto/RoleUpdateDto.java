package com.pod.iam.dto;

import java.io.Serializable;

/**
 * 更新角色请求体。仅允许修改 roleName、status、remark；roleCode/roleType 不可改。
 */
public class RoleUpdateDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String roleName;
    private String status;
    private String remark;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
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
