package com.pod.iam.sys.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pod.common.core.domain.BaseEntity;
import com.pod.common.core.exception.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDate;

@TableName("fx_rate")
public class FxRate extends BaseEntity {

    public static final String STATUS_ENABLED = "ENABLED";
    public static final String STATUS_DISABLED = "DISABLED";

    @TableField("base_currency")
    private String baseCurrency;
    @TableField("quote_currency")
    private String quoteCurrency;
    private BigDecimal rate;
    @TableField("effective_date")
    private LocalDate effectiveDate;
    private String source;
    private String status;

    public void enable() {
        if (STATUS_DISABLED.equals(this.status)) this.status = STATUS_ENABLED;
    }

    public void disable() {
        if (STATUS_ENABLED.equals(this.status)) this.status = STATUS_DISABLED;
    }

    public void validateForCreate() {
        if (baseCurrency == null || baseCurrency.isBlank()) throw new BusinessException("base_currency required");
        if (quoteCurrency == null || quoteCurrency.isBlank()) throw new BusinessException("quote_currency required");
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) throw new BusinessException("rate must be > 0");
        if (effectiveDate == null) throw new BusinessException("effective_date required");
        this.status = status != null && STATUS_DISABLED.equals(status) ? STATUS_DISABLED : STATUS_ENABLED;
    }

    public void validateForUpdate() {
        if (rate != null && rate.compareTo(BigDecimal.ZERO) <= 0) throw new BusinessException("rate must be > 0");
    }

    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String baseCurrency) { this.baseCurrency = baseCurrency; }
    public String getQuoteCurrency() { return quoteCurrency; }
    public void setQuoteCurrency(String quoteCurrency) { this.quoteCurrency = quoteCurrency; }
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
