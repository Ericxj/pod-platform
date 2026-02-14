package com.pod.common.core.context;

public class TenantIgnoreContext {
    private static final ThreadLocal<Boolean> IGNORE_TENANT = new ThreadLocal<>();

    public static void setIgnore(boolean ignore) {
        if (ignore) {
            IGNORE_TENANT.set(true);
        } else {
            IGNORE_TENANT.remove();
        }
    }

    public static boolean isIgnored() {
        return Boolean.TRUE.equals(IGNORE_TENANT.get());
    }

    public static void clear() {
        IGNORE_TENANT.remove();
    }
}
