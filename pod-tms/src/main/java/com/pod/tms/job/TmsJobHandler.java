package com.pod.tms.job;

import com.pod.common.core.context.TenantContext;
import com.pod.common.utils.TraceIdUtils;
import com.pod.tms.domain.ChannelShipmentAck;
import com.pod.tms.domain.Shipment;
import com.pod.tms.service.ChannelAckApplicationService;
import com.pod.tms.service.ShipmentApplicationService;
import com.pod.tms.service.TmsApplicationService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TmsJobHandler {
    private static final Logger log = LoggerFactory.getLogger(TmsJobHandler.class);

    private final ShipmentApplicationService shipmentApplicationService;
    private final TmsApplicationService tmsApplicationService;
    private final ChannelAckApplicationService channelAckApplicationService;

    public TmsJobHandler(ShipmentApplicationService shipmentApplicationService, TmsApplicationService tmsApplicationService, ChannelAckApplicationService channelAckApplicationService) {
        this.shipmentApplicationService = shipmentApplicationService;
        this.tmsApplicationService = tmsApplicationService;
        this.channelAckApplicationService = channelAckApplicationService;
    }

    @XxlJob("tmsAckRetry")
    public void tmsAckRetry() {
        String traceId = TraceIdUtils.generateTraceId();
        TraceIdUtils.setTraceId(traceId);
        try {
            log.info("Job Start: tmsAckRetry");
            shipmentApplicationService.retryPlatformAcks();
            XxlJobHelper.handleSuccess("Finished");
        } catch (Exception e) {
            log.error("Job Failed", e);
            XxlJobHelper.handleFail(e.getMessage());
        } finally {
            TraceIdUtils.remove();
        }
    }

    /** P1.6: 扫描 CREATED/FAILED 的 tms_shipment，重试 createLabel。参数可选: tenantId=1,factoryId=1 */
    @XxlJob("tmsCreateLabelJob")
    public void tmsCreateLabelJob() {
        if (TenantContext.getTenantId() == null) TenantContext.setTenantId(1L);
        if (TenantContext.getFactoryId() == null) TenantContext.setFactoryId(1L);
        String traceId = TraceIdUtils.generateTraceId();
        TraceIdUtils.setTraceId(traceId);
        try {
            log.info("Job Start: tmsCreateLabelJob");
            List<Shipment> list = tmsApplicationService.listForCreateLabelRetry(50);
            int ok = 0, fail = 0;
            for (Shipment s : list) {
                try {
                    tmsApplicationService.createLabel(s.getId());
                    ok++;
                } catch (Exception e) {
                    log.warn("createLabel failed shipmentId={}", s.getId(), e);
                    fail++;
                }
            }
            XxlJobHelper.handleSuccess("processed=" + list.size() + ", ok=" + ok + ", fail=" + fail);
        } catch (Exception e) {
            log.error("Job Failed", e);
            XxlJobHelper.handleFail(e.getMessage());
        } finally {
            TraceIdUtils.remove();
        }
    }

    /** P1.6: 扫描 LABEL_CREATED/HANDED_OVER 的 tms_shipment，重试 syncToChannel。参数可选: tenantId=1,factoryId=1 */
    @XxlJob("tmsSyncToChannelJob")
    public void tmsSyncToChannelJob() {
        if (TenantContext.getTenantId() == null) TenantContext.setTenantId(1L);
        if (TenantContext.getFactoryId() == null) TenantContext.setFactoryId(1L);
        String traceId = TraceIdUtils.generateTraceId();
        TraceIdUtils.setTraceId(traceId);
        try {
            log.info("Job Start: tmsSyncToChannelJob");
            List<Shipment> list = tmsApplicationService.listForSyncToChannelRetry(50);
            int ok = 0, fail = 0;
            for (Shipment s : list) {
                try {
                    tmsApplicationService.syncToChannel(s.getId());
                    ok++;
                } catch (Exception e) {
                    log.warn("syncToChannel failed shipmentId={}", s.getId(), e);
                    fail++;
                }
            }
            XxlJobHelper.handleSuccess("processed=" + list.size() + ", ok=" + ok + ", fail=" + fail);
        } catch (Exception e) {
            log.error("Job Failed", e);
            XxlJobHelper.handleFail(e.getMessage());
        } finally {
            TraceIdUtils.remove();
        }
    }

    /** P1.6 Amazon confirmShipment：扫描 CREATED/FAILED_RETRYABLE 且 next_retry_at<=now，分批 sendAck。参数可选: tenantId=1,factoryId=1 */
    @XxlJob("tmsAckAmazonShipmentJobHandler")
    public void tmsAckAmazonShipmentJobHandler() {
        if (TenantContext.getTenantId() == null) TenantContext.setTenantId(1L);
        if (TenantContext.getFactoryId() == null) TenantContext.setFactoryId(1L);
        String traceId = TraceIdUtils.generateTraceId();
        TraceIdUtils.setTraceId(traceId);
        try {
            log.info("Job Start: tmsAckAmazonShipmentJobHandler");
            List<ChannelShipmentAck> list = channelAckApplicationService.listForRetry(200);
            int ok = 0, fail = 0;
            for (ChannelShipmentAck a : list) {
                try {
                    channelAckApplicationService.sendAck(a.getId());
                    ok++;
                } catch (Exception e) {
                    log.warn("sendAck failed ackId={}", a.getId(), e);
                    fail++;
                }
            }
            XxlJobHelper.handleSuccess("processed=" + list.size() + ", ok=" + ok + ", fail=" + fail);
        } catch (Exception e) {
            log.error("Job Failed", e);
            XxlJobHelper.handleFail(e.getMessage());
        } finally {
            TraceIdUtils.remove();
        }
    }
}
