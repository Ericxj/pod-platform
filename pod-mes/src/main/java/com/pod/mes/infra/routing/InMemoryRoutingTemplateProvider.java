package com.pod.mes.infra.routing;

import com.pod.mes.domain.routing.RoutingStepDef;
import com.pod.mes.domain.routing.RoutingTemplate;
import com.pod.mes.domain.routing.RoutingTemplateProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-Memory implementation of RoutingTemplateProvider.
 * Serves as a temporary solution until DB tables (mes_routing) are available.
 */
@Component
public class InMemoryRoutingTemplateProvider implements RoutingTemplateProvider {

    private final Map<String, RoutingTemplate> templateMap = new ConcurrentHashMap<>();

    public InMemoryRoutingTemplateProvider() {
        initTemplates();
    }

    private void initTemplates() {
        // Template 1: POD_DEFAULT_V1 (Existing logic: PRINT -> CUT -> PACK)
        List<RoutingStepDef> defaultSteps = new ArrayList<>();
        defaultSteps.add(new RoutingStepDef(1, "PRINT", "Printing", false, false));
        defaultSteps.add(new RoutingStepDef(2, "CUT", "Cutting", false, false));
        defaultSteps.add(new RoutingStepDef(3, "PACK", "Packing", false, false));
        
        RoutingTemplate defaultTemplate = new RoutingTemplate("POD_DEFAULT_V1", "Standard Process", defaultSteps);
        templateMap.put("POD_DEFAULT_V1", defaultTemplate);

        // Template 2: POD_TSHIRT_V1 (Demonstration: PRINT -> DRY -> QC -> PACK)
        List<RoutingStepDef> tshirtSteps = new ArrayList<>();
        tshirtSteps.add(new RoutingStepDef(1, "PRINT", "Printing", false, false));
        tshirtSteps.add(new RoutingStepDef(2, "DRY", "Drying", false, false));
        tshirtSteps.add(new RoutingStepDef(3, "QC", "Quality Check", false, true));
        tshirtSteps.add(new RoutingStepDef(4, "PACK", "Packing", false, false));
        
        RoutingTemplate tshirtTemplate = new RoutingTemplate("POD_TSHIRT_V1", "T-Shirt Process", tshirtSteps);
        templateMap.put("POD_TSHIRT_V1", tshirtTemplate);
    }

    @Override
    public RoutingTemplate getTemplateFor(Long tenantId, Long factoryId, String productType) {
        // Simple logic: if productType contains "SHIRT", use T-Shirt template, otherwise default.
        if (productType != null && productType.toUpperCase().contains("SHIRT")) {
            return templateMap.get("POD_TSHIRT_V1");
        }
        return templateMap.get("POD_DEFAULT_V1");
    }
}
