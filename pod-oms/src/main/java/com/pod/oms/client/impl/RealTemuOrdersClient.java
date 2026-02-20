package com.pod.oms.client.impl;

import com.pod.common.core.context.TenantContext;
import com.pod.common.utils.TraceIdUtils;
import com.pod.iam.sys.application.CredentialApplicationService;
import com.pod.iam.sys.application.ShopApplicationService;
import com.pod.iam.sys.domain.PlatApiCredential;
import com.pod.iam.sys.domain.PlatShop;
import com.pod.oms.client.TemuOrdersClient;
import com.pod.oms.dto.ChannelOrderDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Temu 拉单：从 plat_shop + plat_api_credential 驱动。
 * 当前为占位实现，返回空列表；对接 Temu API 后按分页与时间窗口拉取。
 */
@Component
@ConditionalOnProperty(name = "oms.temu.client", havingValue = "real")
public class RealTemuOrdersClient implements TemuOrdersClient {

    private static final Logger log = LoggerFactory.getLogger(RealTemuOrdersClient.class);

    private final CredentialApplicationService credentialApplicationService;
    private final ShopApplicationService shopApplicationService;

    public RealTemuOrdersClient(CredentialApplicationService credentialApplicationService,
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
        log.info("fetchOrders TEMU shopId={} tenantId={} factoryId={} traceId={}", shopIdLong, tenantId, factoryId, traceId);

        PlatShop shop = shopApplicationService.get(shopIdLong);
        if (!"TEMU".equalsIgnoreCase(shop.getPlatformCode())) {
            throw new IllegalArgumentException("Shop is not TEMU: " + shop.getPlatformCode());
        }
        PlatApiCredential cred = credentialApplicationService.getCredentialEntityForChannelPull("TEMU", shopIdLong, "TOKEN");
        if (cred == null || !PlatApiCredential.STATUS_ENABLED.equals(cred.getStatus())) {
            log.warn("No enabled TEMU credential for shopId={} traceId={}", shopIdLong, traceId);
            return new ArrayList<>();
        }
        String payloadJson = credentialApplicationService.getDecryptedPayloadForChannelPull("TEMU", shopIdLong, "TOKEN");
        if (payloadJson == null || payloadJson.isBlank()) {
            log.warn("TEMU credential decrypt failed shopId={} traceId={}", shopIdLong, traceId);
            return new ArrayList<>();
        }
        return fetchOrdersFromTemuApi(shopIdLong, from, to, payloadJson, tenantId, factoryId, traceId);
    }

    private List<ChannelOrderDto> fetchOrdersFromTemuApi(Long shopId, LocalDateTime from, LocalDateTime to,
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
