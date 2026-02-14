package com.pod.infra.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.context.TenantIgnoreContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.stereotype.Component;

/**
 * 业务表 factory_id 过滤：对「非 iam_*」表强制追加 factory_id = TenantContext.getFactoryId()（当前会话工厂）。
 * 与 TenantLineInnerInterceptor 配合使用；deleted 由实体 @TableLogic 保证。
 * 白名单：iam_*、sys_* 等表不追加 factory 条件，仅租户表追加 tenant_id。
 */
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
        if ("iam_data_scope".equalsIgnoreCase(tableName)) return true;
        if (tableName != null && tableName.toLowerCase().startsWith("iam_")) return true;

        return false;
    }
}
