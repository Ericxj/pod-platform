package com.pod.iam.dto;

import java.io.Serializable;

/**
 * GET /api/iam/factories/all 返回项，仅暴露必要字段。
 */
public class FactoryAllItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String factoryCode;
    private String factoryName;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFactoryCode() { return factoryCode; }
    public void setFactoryCode(String factoryCode) { this.factoryCode = factoryCode; }
    public String getFactoryName() { return factoryName; }
    public void setFactoryName(String factoryName) { this.factoryName = factoryName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
