package com.pod.tms.service;

/**
 * P1.6++ D: 对 confirmShipment 400 错误做可扩展分类，返回可执行的自愈动作。
 */
public enum ConfirmShipmentSelfHealStrategy {

    /** shipDate 晚于当前时间：调整为 nowUtc-2s */
    SHIP_DATE_AFTER_NOW("shipDate after now", "shipDate_after_now"),
    /** shipDate 早于订单日期：调整为 max(shipDate, purchaseDateUtc) 且 <= nowUtc-2s */
    SHIP_DATE_BEFORE_ORDER("shipDate before order date", "ship_date_before_order"),
    /** 承运商无效：降级 carrierCode=Other, carrierName=原 carrier */
    CARRIER_INVALID("carrier invalid", "carrier_other"),
    /** orderItemId 无效：强制刷新 orderItems 并回填后重试 */
    ORDER_ITEM_INVALID("orderItemId invalid", "order_item_refresh"),
    UNKNOWN(null, null);

    private final String actionCode;

    ConfirmShipmentSelfHealStrategy(String messageKeyword, String actionCode) {
        this.actionCode = actionCode;
    }

    public String getActionCode() {
        return actionCode;
    }

    /**
     * 根据 response body 或 error message 解析自愈类型（不区分大小写）。
     */
    public static ConfirmShipmentSelfHealStrategy classify(int httpStatus, String responseBody, String errorMessage) {
        if (httpStatus != 400) return UNKNOWN;
        String text = "";
        if (responseBody != null && !responseBody.isBlank()) text += " " + responseBody.toLowerCase();
        if (errorMessage != null && !errorMessage.isBlank()) text += " " + errorMessage.toLowerCase();
        if (text.contains("shipdate") && (text.contains("after") || text.contains("future") || text.contains("cannot be in the future"))) return SHIP_DATE_AFTER_NOW;
        if (text.contains("shipdate") && (text.contains("before") || text.contains("order date") || text.contains("purchase"))) return SHIP_DATE_BEFORE_ORDER;
        if (text.contains("carrier") && (text.contains("invalid") || text.contains("not supported") || text.contains("unknown"))) return CARRIER_INVALID;
        if (text.contains("orderitem") || text.contains("order item") || text.contains("orderItemId") && (text.contains("invalid") || text.contains("not found"))) return ORDER_ITEM_INVALID;
        return UNKNOWN;
    }
}
