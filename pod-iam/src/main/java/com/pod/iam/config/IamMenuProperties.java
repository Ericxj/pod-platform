package com.pod.iam.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "iam.menu")
public class IamMenuProperties {

    /**
     * Whether to validate menus on application startup.
     * Default false: production must not depend on startup validation; enable in dev if needed.
     */
    private boolean validateOnStartup = false;

    /**
     * Whether to fail application startup if validation errors are found.
     * Default: true (for prod safety).
     */
    private boolean failFast = true;

    /**
     * List of tenant IDs to validate.
     * If empty, might default to a basic check or scan all (depending on implementation).
     */
    private List<Long> validateTenants = new ArrayList<>();

    /**
     * List of factory IDs to validate.
     * If empty, might default to a basic check or scan all.
     */
    private List<Long> validateFactories = new ArrayList<>();

    public boolean isValidateOnStartup() {
        return validateOnStartup;
    }

    public void setValidateOnStartup(boolean validateOnStartup) {
        this.validateOnStartup = validateOnStartup;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    public List<Long> getValidateTenants() {
        return validateTenants;
    }

    public void setValidateTenants(List<Long> validateTenants) {
        this.validateTenants = validateTenants;
    }

    public List<Long> getValidateFactories() {
        return validateFactories;
    }

    public void setValidateFactories(List<Long> validateFactories) {
        this.validateFactories = validateFactories;
    }
}
