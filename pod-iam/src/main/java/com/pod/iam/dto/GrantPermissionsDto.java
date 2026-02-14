package com.pod.iam.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 角色授权请求体：全量覆盖式授权。
 */
public class GrantPermissionsDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Long> permIds;

    public GrantPermissionsDto() {
    }

    public GrantPermissionsDto(List<Long> permIds) {
        this.permIds = permIds;
    }

    public List<Long> getPermIds() {
        return permIds;
    }

    public void setPermIds(List<Long> permIds) {
        this.permIds = permIds;
    }
}
