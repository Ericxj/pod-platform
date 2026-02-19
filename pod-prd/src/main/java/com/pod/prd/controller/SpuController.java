package com.pod.prd.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.annotation.RequirePerm;
import com.pod.common.core.domain.Result;
import com.pod.prd.domain.Spu;
import com.pod.prd.service.PrdApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prd/spu")
public class SpuController {
    @Autowired
    private PrdApplicationService prdService;

    @GetMapping("/page")
    @RequirePerm("prd:spu:page")
    public Result<IPage<Spu>> page(
            @RequestParam(name = "current", defaultValue = "1") Integer current,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status) {
        return Result.success(prdService.pageSpu(new Page<>(current, size), keyword, status));
    }

    @GetMapping("/{id}")
    @RequirePerm("prd:spu:get")
    public Result<Spu> get(@PathVariable("id") Long id) {
        return Result.success(prdService.getSpu(id));
    }

    @PostMapping
    @RequirePerm("prd:spu:create")
    public Result<Spu> create(@RequestBody SpuCreateRequest req) {
        return Result.success(prdService.createSpu(req.getSpuCode(), req.getSpuName(), req.getCategoryCode(), req.getBrand()));
    }

    @PutMapping("/{id}")
    @RequirePerm("prd:spu:update")
    public Result<Void> update(@PathVariable("id") Long id, @RequestBody SpuUpdateRequest req) {
        prdService.updateSpu(id, req.getSpuName(), req.getCategoryCode(), req.getBrand());
        return Result.success();
    }

    public static class SpuCreateRequest {
        private String spuCode;
        private String spuName;
        private String categoryCode;
        private String brand;
        public String getSpuCode() { return spuCode; }
        public void setSpuCode(String spuCode) { this.spuCode = spuCode; }
        public String getSpuName() { return spuName; }
        public void setSpuName(String spuName) { this.spuName = spuName; }
        public String getCategoryCode() { return categoryCode; }
        public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
    }

    public static class SpuUpdateRequest {
        private String spuName;
        private String categoryCode;
        private String brand;
        public String getSpuName() { return spuName; }
        public void setSpuName(String spuName) { this.spuName = spuName; }
        public String getCategoryCode() { return categoryCode; }
        public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
    }
}
