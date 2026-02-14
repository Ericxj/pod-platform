package com.pod.wms.job;

import com.pod.common.utils.TraceIdUtils;
import com.pod.wms.service.OutboundApplicationService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WmsJobHandler {
    private static final Logger log = LoggerFactory.getLogger(WmsJobHandler.class);

    private final OutboundApplicationService outboundApplicationService;

    public WmsJobHandler(OutboundApplicationService outboundApplicationService) {
        this.outboundApplicationService = outboundApplicationService;
    }

    @XxlJob("integrationPullOrdersMock")
    public void integrationPullOrdersMock() {
        String traceId = TraceIdUtils.generateTraceId();
        TraceIdUtils.setTraceId(traceId);
        try {
            log.info("Job Start: integrationPullOrdersMock");
            outboundApplicationService.pullOrdersFromOmsMock();
            log.info("Job End: integrationPullOrdersMock");
            
            XxlJobHelper.handleSuccess("Finished");
        } catch (org.springframework.jdbc.BadSqlGrammarException e) {
            log.warn("WMS tables missing, skipping mock logic. Error: {}", e.getMessage());
            XxlJobHelper.handleSuccess("Skipped: WMS tables missing");
        } catch (Exception e) {
            log.error("Job Failed", e);
            XxlJobHelper.handleFail(e.getMessage());
        } finally {
            TraceIdUtils.remove();
        }
    }
}
