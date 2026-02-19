package com.pod.oms.client;

import com.pod.oms.dto.ChannelOrderDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Temu 拉单客户端。预留接口，暂不实现业务细节。
 */
public interface TemuOrdersClient {

    List<ChannelOrderDto> fetchOrders(String shopId, LocalDateTime from, LocalDateTime to);
}
