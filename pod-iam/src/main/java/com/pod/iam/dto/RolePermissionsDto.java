package com.pod.iam.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 角色已授权权限 ID 列表响应 / 授权请求体。
 */
public class RolePermissionsDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Long> permIds;

    public RolePermissionsDto() {
    }

    public RolePermissionsDto(List<Long> permIds) {
        this.permIds = permIds;
    }

    public List<Long> getPermIds() {
        return permIds;
    }

    public void setPermIds(List<Long> permIds) {
        this.permIds = permIds;
    }
}
