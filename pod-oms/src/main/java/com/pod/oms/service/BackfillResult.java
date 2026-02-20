package com.pod.oms.service;

/**
 * P1.6++ 拉单阶段 getOrderItems 回填结果。供 OMS 拉单 Job 决定是否写 hold。
 */
public enum BackfillResult {
    /** 全部回填成功 */
    SUCCESS,
    /** 401/403 鉴权失败，应写 hold AMZ_AUTH_FAILED */
    AUTH_FAILED,
    /** 无法匹配订单行，应写 hold AMZ_ORDER_ITEM_NOT_MATCHED */
    NOT_MATCHED,
    /** 429/503 可重试，不写 hold */
    RETRYABLE
}

