package com.pod.oms.domain;

import com.pod.common.core.exception.BusinessException;
import java.util.EnumSet;
import java.util.Set;

/**
 * Fulfillment 状态枚举与合法迁移矩阵。
 * CREATED -> RESERVED | HOLD_INVENTORY | CANCELLED
 * RESERVED -> ART_READY | CANCELLED  (P1.3: 全部 line 生产图 READY 后推进)
 * ART_READY -> READY_TO_SHIP | CANCELLED  (P1.4: 工单 DONE 完工入库后推进)
 * READY_TO_SHIP -> CANCELLED
 * HOLD_INVENTORY -> RESERVED | CANCELLED（retryReserve 后可到 RESERVED）
 * RELEASED 保留兼容；CANCELLED 终态
 */
public enum FulfillmentStatus {

    CREATED,
    RESERVED,
    ART_READY,
    READY_TO_SHIP,
    HOLD_INVENTORY,
    RELEASED,
    CANCELLED;

    private static final Set<FulfillmentStatus> ALLOW_RESERVE = EnumSet.of(CREATED, HOLD_INVENTORY);
    private static final Set<FulfillmentStatus> ALLOW_RELEASE = EnumSet.of(CREATED, RESERVED);
    private static final Set<FulfillmentStatus> ALLOW_CANCEL = EnumSet.of(CREATED, RESERVED, ART_READY, READY_TO_SHIP, HOLD_INVENTORY, RELEASED);
    private static final Set<FulfillmentStatus> ALLOW_CONFIRM = EnumSet.of(CREATED);
    private static final Set<FulfillmentStatus> ALLOW_ART_READY = EnumSet.of(RESERVED);
    private static final Set<FulfillmentStatus> ALLOW_READY_TO_SHIP = EnumSet.of(ART_READY);

    public void requireAllowReserve() {
        if (!ALLOW_RESERVE.contains(this)) {
            throw new BusinessException("Fulfillment can only reserve from CREATED or HOLD_INVENTORY. Current: " + this);
        }
    }

    public void requireAllowRelease() {
        if (!ALLOW_RELEASE.contains(this)) {
            throw new BusinessException("Fulfillment can only be released from CREATED or RESERVED. Current: " + this);
        }
    }

    public void requireAllowCancel() {
        if (!ALLOW_CANCEL.contains(this)) {
            throw new BusinessException("Fulfillment cannot be cancelled from " + this);
        }
    }

    public void requireAllowConfirm() {
        if (!ALLOW_CONFIRM.contains(this)) {
            throw new BusinessException("Fulfillment can only be confirmed from CREATED. Current: " + this);
        }
    }

    public void requireAllowArtReady() {
        if (!ALLOW_ART_READY.contains(this)) {
            throw new BusinessException("Fulfillment can only move to ART_READY from RESERVED. Current: " + this);
        }
    }

    public void requireAllowReadyToShip() {
        if (!ALLOW_READY_TO_SHIP.contains(this)) {
            throw new BusinessException("Fulfillment can only move to READY_TO_SHIP from ART_READY. Current: " + this);
        }
    }

    public static FulfillmentStatus from(String status) {
        if (status == null || status.isEmpty()) {
            throw new BusinessException("Fulfillment status cannot be null or empty");
        }
        try {
            return valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid fulfillment status: " + status);
        }
    }
}
