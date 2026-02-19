package com.pod.oms.client;

import com.pod.oms.dto.ChannelOrderDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Amazon Orders API 2026 版拉单客户端。先 mock；真实对接按项目已有 sp-api client 实现。
 */
public interface AmazonOrdersClient {

    /**
     * 拉取指定店铺、时间范围内的订单（lastUpdatedAfter 或 from/to）。
     */
    List<ChannelOrderDto> fetchOrders(String shopId, LocalDateTime lastUpdatedAfter, LocalDateTime lastUpdatedBefore);
}
