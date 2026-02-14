package com.pod.mes.domain.routing;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Routing Template (Value Object)
 * Represents a predefined sequence of operations.
 */
public class RoutingTemplate implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String code;
    private final String name;
    private final List<RoutingStepDef> steps;

    public RoutingTemplate(String code, String name, List<RoutingStepDef> steps) {
        this.code = code;
        this.name = name;
        this.steps = steps != null ? Collections.unmodifiableList(steps) : Collections.emptyList();
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public List<RoutingStepDef> getSteps() {
        return steps;
    }
}
