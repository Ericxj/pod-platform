package com.pod.art.gateway;

import com.pod.art.domain.ProductionFile;

/**
 * 生产图渲染网关：可替换为真实渲染服务（Python/PS/外部服务）。
 * P1.3 使用 Mock 实现。
 */
public interface ProductionRenderGateway {

    /**
     * 为稿件任务生成生产图。成功返回生成的文件信息；失败抛异常或返回 null 由调用方处理。
     */
    RenderResult render(Long tenantId, Long factoryId, Long artJobId);
    
    record RenderResult(String storageUrl, String fileType, String format, Integer dpi, Integer widthPx, Integer heightPx, String fileHash) {
        public ProductionFile toProductionFile(Long artJobId, String fileNo) {
            ProductionFile f = ProductionFile.create(artJobId, fileNo, fileType, format, storageUrl, fileHash);
            f.setDpi(dpi);
            f.setWidthPx(widthPx);
            f.setHeightPx(heightPx);
            return f;
        }
    }
}
