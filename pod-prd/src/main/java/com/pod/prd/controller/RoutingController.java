package com.pod.prd.controller;

import com.pod.common.core.annotation.RequirePerm;
import com.pod.common.core.domain.Result;
import com.pod.prd.domain.Routing;
import com.pod.prd.domain.RoutingStep;
import com.pod.prd.service.PrdApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/prd/routing")
public class RoutingController {
    @Autowired
    private PrdApplicationService prdService;

    @GetMapping("/by-sku")
    @RequirePerm("prd:routing:get")
    public Result<Routing> getBySkuId(@RequestParam("skuId") Long skuId) {
        Routing routing = prdService.getRoutingBySkuId(skuId);
        return Result.success(routing);
    }

    @GetMapping("/{id}")
    @RequirePerm("prd:routing:get")
    public Result<Routing> get(@PathVariable("id") Long id) {
        return Result.success(prdService.getRouting(id));
    }

    @GetMapping("/{id}/steps")
    @RequirePerm("prd:routing:get")
    public Result<List<RoutingStep>> steps(@PathVariable("id") Long id) {
        return Result.success(prdService.listRoutingSteps(id));
    }

    @PostMapping("/save")
    @RequirePerm("prd:routing:save")
    public Result<Routing> save(@RequestBody RoutingSaveRequest req) {
        return Result.success(prdService.saveRouting(req.getSkuId(), req.getVersionNo(), req.getSteps()));
    }

    @PostMapping("/{id}/publish")
    @RequirePerm("prd:routing:publish")
    public Result<Void> publish(@PathVariable("id") Long id) {
        prdService.publishRouting(id);
        return Result.success();
    }

    @PostMapping("/{id}/unpublish")
    @RequirePerm("prd:routing:unpublish")
    public Result<Void> unpublish(@PathVariable("id") Long id) {
        prdService.unpublishRouting(id);
        return Result.success();
    }

    public static class RoutingSaveRequest {
        private Long skuId;
        private Integer versionNo;
        private List<RoutingStep> steps;
        public Long getSkuId() { return skuId; }
        public void setSkuId(Long skuId) { this.skuId = skuId; }
        public Integer getVersionNo() { return versionNo; }
        public void setVersionNo(Integer versionNo) { this.versionNo = versionNo; }
        public List<RoutingStep> getSteps() { return steps; }
        public void setSteps(List<RoutingStep> steps) { this.steps = steps; }
    }
}
