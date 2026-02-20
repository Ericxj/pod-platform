package com.pod.oms.client.impl;

import com.pod.common.core.context.TenantContext;
import com.pod.common.utils.TraceIdUtils;
import com.pod.iam.sys.application.CredentialApplicationService;
import com.pod.iam.sys.application.ShopApplicationService;
import com.pod.iam.sys.domain.PlatApiCredential;
import com.pod.iam.sys.domain.PlatShop;
import com.pod.oms.client.SheinOrdersClient;
import com.pod.oms.dto.ChannelOrderDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Shein 拉单：从 plat_shop + plat_api_credential 驱动。
 * 当前为占位实现，返回空列表；对接 Shein API 后按分页与时间窗口拉取并写入 channel_order_raw（如有）。
 */
@Component
@ConditionalOnProperty(name = "oms.shein.client", havingValue = "real")
public class RealSheinOrdersClient implements SheinOrdersClient {

    private static final Logger log = LoggerFactory.getLogger(RealSheinOrdersClient.class);

    private final CredentialApplicationService credentialApplicationService;
    private final ShopApplicationService shopApplicationService;

    public RealSheinOrdersClient(CredentialApplicationService credentialApplicationService,
                                ShopApplicationService shopApplicationService) {
        this.credentialApplicationService = credentialApplicationService;
        this.shopApplicationService = shopApplicationService;
    }

    @Override
    public List<ChannelOrderDto> fetchOrders(String shopId, LocalDateTime from, LocalDateTime to) {
        Long shopIdLong = parseShopId(shopId);
        Long tenantId = TenantContext.getTenantId();
        Long factoryId = TenantContext.getFactoryId();
        String traceId = TraceIdUtils.getTraceId();
        log.info("fetchOrders SHEIN shopId={} tenantId={} factoryId={} traceId={}", shopIdLong, tenantId, factoryId, traceId);

        PlatShop shop = shopApplicationService.get(shopIdLong);
        if (!"SHEIN".equalsIgnoreCase(shop.getPlatformCode())) {
            throw new IllegalArgumentException("Shop is not SHEIN: " + shop.getPlatformCode());
        }
        PlatApiCredential cred = credentialApplicationService.getCredentialEntityForChannelPull("SHEIN", shopIdLong, "TOKEN");
        if (cred == null) cred = credentialApplicationService.getCredentialEntityForChannelPull("SHEIN", shopIdLong, "KEY_PAIR");
        if (cred == null || !PlatApiCredential.STATUS_ENABLED.equals(cred.getStatus())) {
            log.warn("No enabled SHEIN credential for shopId={} traceId={}", shopIdLong, traceId);
            return new ArrayList<>();
        }
        String payloadJson = credentialApplicationService.getDecryptedPayloadForChannelPull("SHEIN", shopIdLong, cred.getAuthType());
        if (payloadJson == null || payloadJson.isBlank()) {
            log.warn("SHEIN credential decrypt failed shopId={} traceId={}", shopIdLong, traceId);
            return new ArrayList<>();
        }
        return fetchOrdersFromSheinApi(shopIdLong, from, to, payloadJson, tenantId, factoryId, traceId);
    }

    private List<ChannelOrderDto> fetchOrdersFromSheinApi(Long shopId, LocalDateTime from, LocalDateTime to,
                                                         String payloadJson, Long tenantId, Long factoryId, String traceId) {
        return new ArrayList<>();
    }

    private static Long parseShopId(String shopId) {
        if (shopId == null || shopId.isBlank()) throw new IllegalArgumentException("shopId required");
        try {
            return Long.parseLong(shopId.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("shopId must be numeric: " + shopId);
        }
    }
}
