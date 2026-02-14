package com.pod.iam.dto;

import java.io.Serializable;

public class TenantPageQuery implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long current = 1L;
    private Long size = 10L;
    private String keyword;
    private String status;

    public Long getCurrent() { return current; }
    public void setCurrent(Long current) { this.current = current; }
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
