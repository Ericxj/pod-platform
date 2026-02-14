package com.pod.iam.dto;

import cn.hutool.core.lang.tree.Tree;
import java.util.List;

public class MeMenusDto {
    private List<Tree<Long>> menus;
    private List<String> permissionCodes;
    private DataScopeDto dataScopes;

    public static class DataScopeDto {
        private List<Long> factoryIds;
        private Long defaultFactoryId;

        public List<Long> getFactoryIds() { return factoryIds; }
        public void setFactoryIds(List<Long> factoryIds) { this.factoryIds = factoryIds; }
        public Long getDefaultFactoryId() { return defaultFactoryId; }
        public void setDefaultFactoryId(Long defaultFactoryId) { this.defaultFactoryId = defaultFactoryId; }
    }

    public List<Tree<Long>> getMenus() { return menus; }
    public void setMenus(List<Tree<Long>> menus) { this.menus = menus; }
    public List<String> getPermissionCodes() { return permissionCodes; }
    public void setPermissionCodes(List<String> permissionCodes) { this.permissionCodes = permissionCodes; }
    public DataScopeDto getDataScopes() { return dataScopes; }
    public void setDataScopes(DataScopeDto dataScopes) { this.dataScopes = dataScopes; }
}
