package com.pod.infra.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.pod.common.core.context.TenantContext;
import com.pod.common.utils.TraceIdUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class MybatisMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "tenantId", Long.class, TenantContext.getTenantId());
        this.strictInsertFill(metaObject, "factoryId", Long.class, TenantContext.getFactoryId());
        this.strictInsertFill(metaObject, "createdBy", Long.class, TenantContext.getUserId());
        this.strictInsertFill(metaObject, "updatedBy", Long.class, TenantContext.getUserId());
        this.strictInsertFill(metaObject, "traceId", String.class, TraceIdUtils.getTraceId());
        this.strictInsertFill(metaObject, "version", Integer.class, 0);
        this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updatedBy", Long.class, TenantContext.getUserId());
    }
}
