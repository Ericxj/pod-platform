package com.pod.iam.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;

@TableName("iam_factory")
public class IamFactory extends BaseEntity {

    private String factoryCode;
    private String factoryName;
    private String countryCode;
    private String province;
    private String city;
    private String address;
    private String contactName;
    private String contactPhone;
    private String status;

    public String getFactoryCode() { return factoryCode; }
    public void setFactoryCode(String factoryCode) { this.factoryCode = factoryCode; }
    public String getFactoryName() { return factoryName; }
    public void setFactoryName(String factoryName) { this.factoryName = factoryName; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
