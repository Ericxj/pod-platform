package com.pod.art.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;

@TableName("art_production_file")
public class ProductionFile extends BaseEntity {

    private Long artJobId;
    private String fileNo;
    private String fileType;
    private String fileUrl;
    private Long fileSize;
    private Integer widthPx;
    private Integer heightPx;
    private Integer dpi;
    private String iccProfile;
    private String whiteInkMode;
    private String metaJson;
    private String status;

    public static ProductionFile create(Long artJobId, String fileNo, String fileType, String fileUrl) {
        ProductionFile file = new ProductionFile();
        file.setArtJobId(artJobId);
        file.setFileNo(fileNo);
        file.setFileType(fileType);
        file.setFileUrl(fileUrl);
        file.setStatus("ACTIVE");
        return file;
    }

    public Long getArtJobId() { return artJobId; }
    public void setArtJobId(Long artJobId) { this.artJobId = artJobId; }
    public String getFileNo() { return fileNo; }
    public void setFileNo(String fileNo) { this.fileNo = fileNo; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public Integer getWidthPx() { return widthPx; }
    public void setWidthPx(Integer widthPx) { this.widthPx = widthPx; }
    public Integer getHeightPx() { return heightPx; }
    public void setHeightPx(Integer heightPx) { this.heightPx = heightPx; }
    public Integer getDpi() { return dpi; }
    public void setDpi(Integer dpi) { this.dpi = dpi; }
    public String getIccProfile() { return iccProfile; }
    public void setIccProfile(String iccProfile) { this.iccProfile = iccProfile; }
    public String getWhiteInkMode() { return whiteInkMode; }
    public void setWhiteInkMode(String whiteInkMode) { this.whiteInkMode = whiteInkMode; }
    public String getMetaJson() { return metaJson; }
    public void setMetaJson(String metaJson) { this.metaJson = metaJson; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
