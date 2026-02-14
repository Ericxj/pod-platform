package com.pod.oms.domain;

import com.pod.common.core.exception.BusinessException;
import java.util.EnumSet;
import java.util.Set;

/**
 * Fulfillment 状态枚举与合法迁移矩阵。
 * CREATED -> RELEASED | CANCELLED
 * RELEASED -> CANCELLED（可选，业务允许时可关闭已释放单）
 * CANCELLED -> 终态
 */
public enum FulfillmentStatus {

    CREATED,
    RELEASED,
    CANCELLED;

    private static final Set<FulfillmentStatus> ALLOW_RELEASE = EnumSet.of(CREATED);
    private static final Set<FulfillmentStatus> ALLOW_CANCEL = EnumSet.of(CREATED, RELEASED);
    private static final Set<FulfillmentStatus> ALLOW_CONFIRM = EnumSet.of(CREATED);

    public void requireAllowRelease() {
        if (!ALLOW_RELEASE.contains(this)) {
            throw new BusinessException("Fulfillment can only be released from CREATED. Current: " + this);
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
