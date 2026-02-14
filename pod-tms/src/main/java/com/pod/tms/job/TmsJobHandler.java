package com.pod.tms.job;

import com.pod.common.utils.TraceIdUtils;
import com.pod.tms.service.ShipmentApplicationService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TmsJobHandler {
    private static final Logger log = LoggerFactory.getLogger(TmsJobHandler.class);

    private final ShipmentApplicationService shipmentApplicationService;

    public TmsJobHandler(ShipmentApplicationService shipmentApplicationService) {
        this.shipmentApplicationService = shipmentApplicationService;
    }

    @XxlJob("tmsAckRetry")
    public void tmsAckRetry() {
        String traceId = TraceIdUtils.generateTraceId();
        TraceIdUtils.setTraceId(traceId);
        try {
            log.info("Job Start: tmsAckRetry");
            // Assuming method exists or I need to add it
            shipmentApplicationService.retryPlatformAcks(); 
            XxlJobHelper.handleSuccess("Finished");
        } catch (Exception e) {
            log.error("Job Failed", e);
            XxlJobHelper.handleFail(e.getMessage());
        } finally {
            TraceIdUtils.remove();
        }
    }
}
