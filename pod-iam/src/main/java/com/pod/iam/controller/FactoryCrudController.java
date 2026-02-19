package com.pod.iam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pod.common.core.annotation.RequirePerm;
import com.pod.common.core.context.TenantContext;
import com.pod.common.core.domain.Result;
import com.pod.iam.application.DataScopeService;
import com.pod.iam.application.IamFactoryApplicationService;
import com.pod.iam.domain.IamFactory;
import com.pod.iam.domain.IamUser;
import com.pod.iam.dto.FactoryAllItemDto;
import com.pod.iam.dto.FactoryPageQuery;
import com.pod.iam.mapper.IamUserMapper;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/iam/factories")
public class FactoryCrudController {

    private final IamFactoryApplicationService factoryService;
    private final DataScopeService dataScopeService;
    private final IamUserMapper userMapper;

    public FactoryCrudController(IamFactoryApplicationService factoryService,
                                 DataScopeService dataScopeService,
                                 IamUserMapper userMapper) {
        this.factoryService = factoryService;
        this.dataScopeService = dataScopeService;
        this.userMapper = userMapper;
    }

    @GetMapping("/my")
    @RequirePerm("iam:factory:page")
    public Result<List<Long>> getMyFactories() {
        Long userId = TenantContext.getUserId();
        IamUser user = userMapper.selectById(userId);
        if (user == null) return Result.success(List.of());
        List<Long> ids = dataScopeService.getAccessibleFactoryIds(userId, user.getFactoryId());
        return Result.success(ids);
    }

    @GetMapping
    @RequirePerm("iam:factory:page")
    public Result<IPage<IamFactory>> page(FactoryPageQuery query) {
        return Result.success(factoryService.page(query));
    }

    /** 当前租户下全部 ENABLED 工厂，供数据权限页等下拉使用；tenant_id 来自上下文。 */
    @GetMapping("/all")
    @RequirePerm("iam:factory:page")
    public Result<List<FactoryAllItemDto>> all(@RequestParam(value = "tenantId", required = false) Long tenantId) {
        Long tid = tenantId != null ? tenantId : TenantContext.getTenantId();
        List<FactoryAllItemDto> list = factoryService.listAllEnabled(tid);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @RequirePerm("iam:factory:page")
    public Result<IamFactory> get(@PathVariable("id") Long id) {
        IamFactory f = factoryService.get(id);
        if (f == null) return Result.error("Factory not found");
        return Result.success(f);
    }

    @PostMapping
    @RequirePerm("iam:factory:create")
    public Result<Void> create(@RequestBody IamFactory entity) {
        factoryService.create(entity);
        return Result.success();
    }

    @PutMapping("/{id}")
    @RequirePerm("iam:factory:update")
    public Result<Void> update(@PathVariable("id") Long id, @RequestBody IamFactory entity) {
        factoryService.update(id, entity);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequirePerm("iam:factory:delete")
    public Result<Void> delete(@PathVariable("id") Long id) {
        factoryService.delete(id);
        return Result.success();
    }
}
