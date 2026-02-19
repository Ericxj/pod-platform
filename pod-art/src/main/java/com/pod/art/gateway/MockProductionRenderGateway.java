package com.pod.art.gateway;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * P1.3 Mock 渲染：直接返回模拟 URL 与元数据，可替换为真实渲染器。
 */
@Component
public class MockProductionRenderGateway implements ProductionRenderGateway {

    @Override
    public RenderResult render(Long tenantId, Long factoryId, Long artJobId) {
        String path = "mock/art/" + tenantId + "/" + factoryId + "/" + artJobId + "-" + UUID.randomUUID().toString().substring(0, 8) + ".pdf";
        String url = "http://storage.example.com/" + path;
        String hash = "mock-hash-" + artJobId + "-" + System.currentTimeMillis();
        return new RenderResult(url, "PDF", "PDF", 300, 2480, 3508, hash);
    }
}
