package com.pod.infra.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.context.TenantIgnoreContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.stereotype.Component;

@Component
public class PodTenantLineHandler implements TenantLineHandler {
    @Override
    public Expression getTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return new LongValue(0); // Default or throw error?
        }
        return new LongValue(tenantId);
    }

    @Override
    public String getTenantIdColumn() {
        return "tenant_id";
    }

    @Override
    public boolean ignoreTable(String tableName) {
        // Explicitly ignore via ThreadLocal
        if (TenantIgnoreContext.isIgnored()) {
            return true;
        }
        // Ignore system tables or tables without tenant_id
        if ("iam_tenant".equalsIgnoreCase(tableName)) return true;
        if ("sys_tenant".equalsIgnoreCase(tableName)) return true;
        if ("sys_dict".equalsIgnoreCase(tableName)) return true;
        if ("sys_dict_item".equalsIgnoreCase(tableName)) return true;
        if ("sys_idempotent".equalsIgnoreCase(tableName)) return true; // Idempotent table usually shared or specific handling
        return false;
    }
}
