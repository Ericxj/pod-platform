package com.pod.iam.dto;

import java.io.Serializable;

/**
 * 角色分页查询参数。tenant_id/factory_id 由上下文注入，不允许前端传入。
 */
public class RolePageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long current = 1L;
    private Long size = 10L;
    private String keyword;
    private String roleCode;
    private String roleName;
    private String status;

    public Long getCurrent() {
        return current;
    }

    public void setCurrent(Long current) {
        this.current = current;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
