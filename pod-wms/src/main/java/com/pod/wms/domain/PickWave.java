package com.pod.wms.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;

@TableName("wms_pick_wave")
public class PickWave extends BaseEntity {
    private String waveNo;
    private Long warehouseId;
    private String status;
    private String strategyJson;

    public String getWaveNo() {
        return waveNo;
    }

    public void setWaveNo(String waveNo) {
        this.waveNo = waveNo;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStrategyJson() {
        return strategyJson;
    }

    public void setStrategyJson(String strategyJson) {
        this.strategyJson = strategyJson;
    }
}
