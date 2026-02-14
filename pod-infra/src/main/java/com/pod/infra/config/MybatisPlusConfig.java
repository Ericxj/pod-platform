package com.pod.infra.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.pod.infra.handler.PodFactoryLineHandler;
import com.pod.infra.handler.PodTenantLineHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {
    
    private final PodTenantLineHandler tenantLineHandler;
    private final PodFactoryLineHandler factoryLineHandler;

    public MybatisPlusConfig(PodTenantLineHandler tenantLineHandler, PodFactoryLineHandler factoryLineHandler) {
        this.tenantLineHandler = tenantLineHandler;
        this.factoryLineHandler = factoryLineHandler;
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // Tenant Filter
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(tenantLineHandler));
        
        // Factory Filter
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(factoryLineHandler));

        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
}
