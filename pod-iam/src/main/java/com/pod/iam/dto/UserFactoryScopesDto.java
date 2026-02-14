package com.pod.iam.dto;

import java.io.Serializable;
import java.util.List;

/** 用户可访问工厂范围：GET 响应 / PUT 请求体。 */
public class UserFactoryScopesDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Long> factoryIds;

    public UserFactoryScopesDto() {
    }

    public UserFactoryScopesDto(List<Long> factoryIds) {
        this.factoryIds = factoryIds;
    }

    public List<Long> getFactoryIds() {
        return factoryIds;
    }

    public void setFactoryIds(List<Long> factoryIds) {
        this.factoryIds = factoryIds;
    }
}
