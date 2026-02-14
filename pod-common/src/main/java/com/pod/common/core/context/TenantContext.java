package com.pod.common.core.context;

public class TenantContext {
    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> FACTORY_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    public static void setFactoryId(Long factoryId) {
        FACTORY_ID.set(factoryId);
    }

    public static Long getFactoryId() {
        return FACTORY_ID.get();
    }
    
    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void clear() {
        TENANT_ID.remove();
        FACTORY_ID.remove();
        USER_ID.remove();
    }
}
