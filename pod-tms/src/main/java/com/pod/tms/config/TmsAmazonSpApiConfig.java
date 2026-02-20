package com.pod.tms.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 启用 tms.amazon.spapi 配置绑定。与 RealAmazonSpApiGateway 同用（gateway=real 时生效）。
 */
@Configuration
@EnableConfigurationProperties(AmazonSpApiProperties.class)
public class TmsAmazonSpApiConfig {
}
