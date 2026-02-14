package com.pod.mes.domain.routing;

/**
 * Provider interface for retrieving routing templates.
 * This allows switching between In-Memory, DB, or External Service implementations.
 */
public interface RoutingTemplateProvider {
    
    /**
     * Get the appropriate routing template for a given context.
     * 
     * @param tenantId Tenant ID
     * @param factoryId Factory ID
     * @param productType Product Type or Category (can be null if using default)
     * @return The matching RoutingTemplate
     */
    RoutingTemplate getTemplateFor(Long tenantId, Long factoryId, String productType);
}
