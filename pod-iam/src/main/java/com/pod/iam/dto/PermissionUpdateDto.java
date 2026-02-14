package com.pod.iam.dto;

import java.io.Serializable;

/**
 * 更新权限点请求体。
 */
public class PermissionUpdateDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String permName;
    private String permType;
    private String menuPath;
    private String component;
    private String icon;
    private String redirect;
    private String apiMethod;
    private String apiPath;
    private Long parentId;
    private Integer sortNo;
    private String status;
    private Boolean hidden;
    private Boolean keepAlive;
    private Boolean alwaysShow;

    public String getPermName() { return permName; }
    public void setPermName(String permName) { this.permName = permName; }
    public String getPermType() { return permType; }
    public void setPermType(String permType) { this.permType = permType; }
    public String getMenuPath() { return menuPath; }
    public void setMenuPath(String menuPath) { this.menuPath = menuPath; }
    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getRedirect() { return redirect; }
    public void setRedirect(String redirect) { this.redirect = redirect; }
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
    public Boolean getHidden() { return hidden; }
    public void setHidden(Boolean hidden) { this.hidden = hidden; }
    public Boolean getKeepAlive() { return keepAlive; }
    public void setKeepAlive(Boolean keepAlive) { this.keepAlive = keepAlive; }
    public Boolean getAlwaysShow() { return alwaysShow; }
    public void setAlwaysShow(Boolean alwaysShow) { this.alwaysShow = alwaysShow; }
}
