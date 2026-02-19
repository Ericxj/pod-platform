package com.pod.iam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pod.common.core.annotation.RequirePerm;
import com.pod.common.core.domain.Result;
import com.pod.iam.application.IamTenantApplicationService;
import com.pod.iam.domain.IamTenant;
import com.pod.iam.dto.TenantPageQuery;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/iam/tenants")
public class TenantController {

    private final IamTenantApplicationService tenantService;

    public TenantController(IamTenantApplicationService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping
    @RequirePerm("iam:tenant:page")
    public Result<IPage<IamTenant>> page(TenantPageQuery query) {
        return Result.success(tenantService.page(query));
    }

    @GetMapping("/{id}")
    @RequirePerm("iam:tenant:page")
    public Result<IamTenant> get(@PathVariable("id") Long id) {
        IamTenant t = tenantService.get(id);
        if (t == null) return Result.error("Tenant not found");
        return Result.success(t);
    }

    @PostMapping
    @RequirePerm("iam:tenant:create")
    public Result<Void> create(@RequestBody IamTenant entity) {
        tenantService.create(entity);
        return Result.success();
    }

    @PutMapping("/{id}")
    @RequirePerm("iam:tenant:update")
    public Result<Void> update(@PathVariable("id") Long id, @RequestBody IamTenant entity) {
        tenantService.update(id, entity);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequirePerm("iam:tenant:delete")
    public Result<Void> delete(@PathVariable("id") Long id) {
        tenantService.delete(id);
        return Result.success();
    }
}
