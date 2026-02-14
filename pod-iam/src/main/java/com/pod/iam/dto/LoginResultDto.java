package com.pod.iam.dto;

import com.pod.iam.domain.IamUser;
import java.util.List;

public class LoginResultDto {
    private String token;
    private IamUser user;
    private List<Long> factoryIds;
    private Long currentFactoryId;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public IamUser getUser() { return user; }
    public void setUser(IamUser user) { this.user = user; }
    public List<Long> getFactoryIds() { return factoryIds; }
    public void setFactoryIds(List<Long> factoryIds) { this.factoryIds = factoryIds; }
    public Long getCurrentFactoryId() { return currentFactoryId; }
    public void setCurrentFactoryId(Long currentFactoryId) { this.currentFactoryId = currentFactoryId; }
}
