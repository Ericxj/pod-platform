package com.pod.art.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;

@TableName("art_job_item")
public class ArtJobItem extends BaseEntity {

    private Long artJobId;
    private String surfaceCode;
    private String areaCode;
    private String editableType; // TEXT, IMAGE, MIXED
    private String inputPayloadJson;
    private String renderPayloadJson;

    public Long getArtJobId() { return artJobId; }
    public void setArtJobId(Long artJobId) { this.artJobId = artJobId; }
    public String getSurfaceCode() { return surfaceCode; }
    public void setSurfaceCode(String surfaceCode) { this.surfaceCode = surfaceCode; }
    public String getAreaCode() { return areaCode; }
    public void setAreaCode(String areaCode) { this.areaCode = areaCode; }
    public String getEditableType() { return editableType; }
    public void setEditableType(String editableType) { this.editableType = editableType; }
    public String getInputPayloadJson() { return inputPayloadJson; }
    public void setInputPayloadJson(String inputPayloadJson) { this.inputPayloadJson = inputPayloadJson; }
    public String getRenderPayloadJson() { return renderPayloadJson; }
    public void setRenderPayloadJson(String renderPayloadJson) { this.renderPayloadJson = renderPayloadJson; }
}
