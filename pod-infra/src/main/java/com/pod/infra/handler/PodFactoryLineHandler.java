package com.pod.infra.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.context.TenantIgnoreContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.stereotype.Component;

@Component
public class PodFactoryLineHandler implements TenantLineHandler {
    @Override
    public Expression getTenantId() {
        Long factoryId = TenantContext.getFactoryId();
        if (factoryId == null) {
            return new LongValue(0); 
        }
        return new LongValue(factoryId);
    }

    @Override
    public String getTenantIdColumn() {
        return "factory_id";
    }

    @Override
    public boolean ignoreTable(String tableName) {
        // Explicitly ignore via ThreadLocal
        if (TenantIgnoreContext.isIgnored()) {
            return true;
        }
        // Ignore tables without factory_id
        if ("iam_tenant".equalsIgnoreCase(tableName)) return true;
        if ("iam_factory".equalsIgnoreCase(tableName)) return true;
        if ("sys_tenant".equalsIgnoreCase(tableName)) return true;
        if ("sys_factory".equalsIgnoreCase(tableName)) return true;
        if ("sys_dict".equalsIgnoreCase(tableName)) return true;
        if ("sys_dict_item".equalsIgnoreCase(tableName)) return true;
        if ("sys_idempotent".equalsIgnoreCase(tableName)) return true;
        if ("iam_data_scope".equalsIgnoreCase(tableName)) return true; // Metadata table, accessed before context set
        
        // IAM Core tables are Tenant-Level, not Factory-Level (Factory ID is just for audit/home)
        if ("iam_user".equalsIgnoreCase(tableName)) return true;
        if ("iam_role".equalsIgnoreCase(tableName)) return true;
        if ("iam_menu".equalsIgnoreCase(tableName)) return true;
        if ("iam_permission".equalsIgnoreCase(tableName)) return true;
        if ("iam_user_role".equalsIgnoreCase(tableName)) return true;
        if ("iam_role_permission".equalsIgnoreCase(tableName)) return true;

        return false;
    }
}
