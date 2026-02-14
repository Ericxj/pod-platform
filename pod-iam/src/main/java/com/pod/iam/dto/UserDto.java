package com.pod.iam.dto;

import java.io.Serializable;
import java.util.List;

public class UserDto implements Serializable {
    private Long id;
    private String username;
    private String password; // Only for create/reset
    private String realName;
    private String email;
    private String phone;
    private String status;
    private List<Long> roleIds; // For assigning roles
    private Long defaultFactoryId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<Long> getRoleIds() { return roleIds; }
    public void setRoleIds(List<Long> roleIds) { this.roleIds = roleIds; }
    public Long getDefaultFactoryId() { return defaultFactoryId; }
    public void setDefaultFactoryId(Long defaultFactoryId) { this.defaultFactoryId = defaultFactoryId; }
}
