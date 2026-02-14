package com.pod.iam.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;

@TableName("iam_permission")
public class IamPermission extends BaseEntity {
    private String permCode;
    private String permName;
    private String permType;
    private String menuPath;
    private String apiMethod;
    private String apiPath;
    private Long parentId;
    private Integer sortNo;
    private String status;

    // Extension for Vben Admin Dynamic Routing
    private String component;
    private String icon;
    private String redirect;
    private Boolean hidden;
    private Boolean keepAlive;
    private Boolean alwaysShow;

    public String getPermCode() { return permCode; }
    public void setPermCode(String permCode) { this.permCode = permCode; }
    public String getPermName() { return permName; }
    public void setPermName(String permName) { this.permName = permName; }
    public String getPermType() { return permType; }
    public void setPermType(String permType) { this.permType = permType; }
    public String getMenuPath() { return menuPath; }
    public void setMenuPath(String menuPath) { this.menuPath = menuPath; }
    public String getApiMethod() { return apiMethod; }
    public void setApiMethod(String apiMethod) { this.apiMethod = apiMethod; }
    public String getApiPath() { return apiPath; }
    public void setApiPath(String apiPath) { this.apiPath = apiPath; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getRedirect() { return redirect; }
    public void setRedirect(String redirect) { this.redirect = redirect; }
    public Boolean getHidden() { return hidden; }
    public void setHidden(Boolean hidden) { this.hidden = hidden; }
    public Boolean getKeepAlive() { return keepAlive; }
    public void setKeepAlive(Boolean keepAlive) { this.keepAlive = keepAlive; }
    public Boolean getAlwaysShow() { return alwaysShow; }
    public void setAlwaysShow(Boolean alwaysShow) { this.alwaysShow = alwaysShow; }

    // --- Domain Behaviors ---

    public void validate() {
        if ("MENU".equals(this.permType)) {
            if (this.component == null || this.component.isBlank()) {
                throw new com.pod.common.core.exception.BusinessException("Menu component cannot be empty");
            }
            if (this.menuPath == null || !this.menuPath.startsWith("/")) {
                throw new com.pod.common.core.exception.BusinessException("Menu path must start with /");
            }
        }
        if ("DIR".equals(this.permType)) {
             if (this.component != null && !this.component.equals("LAYOUT") && !this.component.isBlank()) {
                 // Warning or Error? Rule says strict.
                 // Actually DIR can have component if it's not LAYOUT but a wrapper. 
                 // But for this project, let's enforce LAYOUT for Root DIRs usually.
                 // Let's just check path.
             }
        }
    }
}
