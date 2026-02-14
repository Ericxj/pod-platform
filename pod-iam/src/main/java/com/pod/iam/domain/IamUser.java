package com.pod.iam.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;

@TableName("iam_user")
public class IamUser extends BaseEntity {
    private String username;
    private String passwordHash;
    private String realName;
    private String email;
    private String phone;
    private String status;
    private LocalDateTime lastLoginAt;

    // Behavior Methods
    
    public void login(String rawPassword, PasswordEncoder encoder) {
        if (!"ENABLED".equals(this.status)) {
            throw new BusinessException("User is not active: " + this.status);
        }
        if (!encoder.matches(rawPassword, this.passwordHash)) {
            throw new BusinessException("Invalid password");
        }
        this.lastLoginAt = LocalDateTime.now();
    }

    public void changePassword(String newPassword, PasswordEncoder encoder) {
        this.passwordHash = encoder.encode(newPassword);
    }

    public void activate() {
        this.status = "ENABLED";
    }

    public void disable() {
        this.status = "DISABLED";
    }

    public void lock(String reason) {
        if ("DISABLED".equals(this.status)) {
             throw new BusinessException("Cannot lock a disabled user");
        }
        this.status = "LOCKED";
    }

    public void unlock() {
        if ("DISABLED".equals(this.status)) {
             throw new BusinessException("Cannot unlock a disabled user");
        }
        this.status = "ENABLED";
    }

    // Getters Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}
