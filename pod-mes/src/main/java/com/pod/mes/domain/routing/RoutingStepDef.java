package com.pod.mes.domain.routing;

import java.io.Serializable;

/**
 * Routing Step Definition (Value Object)
 * Represents a single step in a routing template.
 */
public class RoutingStepDef implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int stepNo;
    private final String opCode;
    private final String opName;
    private final boolean allowParallel;
    private final boolean isQc;

    public RoutingStepDef(int stepNo, String opCode, String opName, boolean allowParallel, boolean isQc) {
        this.stepNo = stepNo;
        this.opCode = opCode;
        this.opName = opName;
        this.allowParallel = allowParallel;
        this.isQc = isQc;
    }

    public int getStepNo() {
        return stepNo;
    }

    public String getOpCode() {
        return opCode;
    }

    public String getOpName() {
        return opName;
    }

    public boolean isAllowParallel() {
        return allowParallel;
    }

    public boolean isQc() {
        return isQc;
    }
}
