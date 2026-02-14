package com.pod.iam.dto;

import java.io.Serializable;

/**
 * 权限分页查询参数。tenant_id/factory_id 由上下文注入。
 */
public class PermissionPageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long current = 1L;
    private Long size = 10L;
    private String keyword;
    private String permType;

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

    public String getPermType() {
        return permType;
    }

    public void setPermType(String permType) {
        this.permType = permType;
    }
}
