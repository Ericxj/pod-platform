package com.pod.srm.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

@TableName("srm_supplier")
public class Supplier extends BaseEntity {

    public static final String STATUS_ENABLED = "ENABLED";
    public static final String STATUS_DISABLED = "DISABLED";

    @TableField("supplier_code")
    private String supplierCode;
    @TableField("supplier_name")
    private String supplierName;
    @TableField("contact_name")
    private String contactName;
    private String phone;
    private String email;
    private String address;
    private String status;
    private String remark;

    public void enable() {
        if (STATUS_DISABLED.equals(this.status)) this.status = STATUS_ENABLED;
    }

    public void disable() {
        if (STATUS_ENABLED.equals(this.status)) this.status = STATUS_DISABLED;
    }

    public void validateForCreate() {
        if (supplierCode == null || supplierCode.isBlank()) throw new BusinessException("supplier_code required");
        if (supplierName == null || supplierName.isBlank()) throw new BusinessException("supplier_name required");
        this.status = status != null && STATUS_DISABLED.equals(status) ? STATUS_DISABLED : STATUS_ENABLED;
    }

    public void validateForUpdate() {
        if (supplierName != null && supplierName.isBlank()) throw new BusinessException("supplier_name cannot be blank");
    }

    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
