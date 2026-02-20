package com.pod.oms.service;

/**
 * P1.6++ Amazon 订单行 orderItemId 预回填门面。由 TMS 实现，OMS 拉单后调用。
 * 若存在缺失 external_order_item_id 的订单行，则调 getOrderItems（带缓存与限流）并匹配回填。
 */
public interface AmazonOrderItemBackfillFacade {

    /**
     * 拉单阶段预回填：对缺 external_order_item_id 的订单行拉取 getOrderItems 并回填。
     * 同一 amazonOrderId 同一轮内只应调用一次（由调用方去重或依赖缓存）。
     *
     * @param amazonOrderId 平台订单 ID
     * @param unifiedOrderId 统一订单 ID
     * @param marketplaceId 可选，用于缓存/限流
     * @param shopIdStr 可选，用于日志
     * @return 结果枚举；NOT_MATCHED 时 reasonMsg 可带未匹配 sku/asin 信息供 hold reason_msg
     */
    BackfillResultVo backfillOrderItemsForPull(String amazonOrderId, Long unifiedOrderId, String marketplaceId, String shopIdStr);
}

