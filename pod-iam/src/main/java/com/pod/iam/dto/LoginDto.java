package com.pod.iam.dto;

import java.io.Serializable;

public class LoginDto implements Serializable {
    private String username;
    private String password;
    private Long factoryId;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Long getFactoryId() { return factoryId; }
    public void setFactoryId(Long factoryId) { this.factoryId = factoryId; }
}
