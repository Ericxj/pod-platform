package com.pod.prd.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.annotation.RequirePerm;
import com.pod.common.core.domain.Result;
import com.pod.prd.domain.SkuMapping;
import com.pod.prd.service.PrdApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prd/mapping")
public class SkuMappingController {

    @Autowired
    private PrdApplicationService prdService;

    @GetMapping("/page")
    @RequirePerm("prd:mapping:page")
    public Result<IPage<SkuMapping>> page(
            @RequestParam(name = "current", defaultValue = "1") Integer current,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "channel", required = false) String channel,
            @RequestParam(name = "shopId", required = false) String shopId,
            @RequestParam(name = "externalSku", required = false) String externalSku,
            @RequestParam(name = "skuCode", required = false) String skuCode) {
        return Result.success(prdService.pageSkuMapping(new Page<>(current, size), channel, shopId, externalSku, skuCode));
    }

    @PostMapping
    @RequirePerm("prd:mapping:create")
    public Result<SkuMapping> create(@RequestBody SkuMappingCreateRequest req) {
        return Result.success(prdService.createSkuMapping(req.getChannel(), req.getShopId(), req.getExternalSku(), req.getExternalName(), req.getSkuCode(), req.getRemark()));
    }

    @PutMapping("/{id}")
    @RequirePerm("prd:mapping:update")
    public Result<Void> update(@PathVariable("id") Long id, @RequestBody SkuMappingUpdateRequest req) {
        prdService.updateSkuMapping(id, req.getExternalName(), req.getRemark());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequirePerm("prd:mapping:delete")
    public Result<Void> delete(@PathVariable("id") Long id) {
        prdService.deleteSkuMapping(id);
        return Result.success();
    }

    public static class SkuMappingCreateRequest {
        private String channel;
        private String shopId;
        private String externalSku;
        private String externalName;
        private String skuCode;
        private String remark;
        public String getChannel() { return channel; }
        public void setChannel(String channel) { this.channel = channel; }
        public String getShopId() { return shopId; }
        public void setShopId(String shopId) { this.shopId = shopId; }
        public String getExternalSku() { return externalSku; }
        public void setExternalSku(String externalSku) { this.externalSku = externalSku; }
        public String getExternalName() { return externalName; }
        public void setExternalName(String externalName) { this.externalName = externalName; }
        public String getSkuCode() { return skuCode; }
        public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }

    public static class SkuMappingUpdateRequest {
        private String externalName;
        private String remark;
        public String getExternalName() { return externalName; }
        public void setExternalName(String externalName) { this.externalName = externalName; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }
}
