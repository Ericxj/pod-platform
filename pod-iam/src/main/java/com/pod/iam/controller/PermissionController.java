package com.pod.iam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pod.common.core.annotation.RequirePerm;
import com.pod.common.core.domain.Result;
import com.pod.iam.application.IamPermissionApplicationService;
import com.pod.iam.domain.IamPermission;
import com.pod.iam.dto.PermissionCreateDto;
import com.pod.iam.dto.PermissionPageQuery;
import com.pod.iam.dto.PermissionTreeDto;
import com.pod.iam.dto.PermissionUpdateDto;
import com.pod.iam.dto.PermissionValidateResultDto;
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
@RequestMapping("/api/iam/permissions")
public class PermissionController {

    private final IamPermissionApplicationService permissionService;

    public PermissionController(IamPermissionApplicationService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    @RequirePerm("iam:perm:page")
    public Result<IPage<IamPermission>> page(PermissionPageQuery query) {
        return Result.success(permissionService.page(query));
    }

    @GetMapping("/tree")
    @RequirePerm("iam:perm:page")
    public Result<List<PermissionTreeDto>> tree(@RequestParam(defaultValue = "ALL") String permType) {
        return Result.success(permissionService.tree(permType));
    }

    @GetMapping("/{id}")
    @RequirePerm("iam:perm:page")
    public Result<IamPermission> get(@PathVariable Long id) {
        IamPermission p = permissionService.get(id);
        if (p == null) {
            return Result.error("Permission not found");
        }
        return Result.success(p);
    }

    @PostMapping
    @RequirePerm("iam:perm:create")
    public Result<Void> create(@RequestBody PermissionCreateDto dto) {
        permissionService.create(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    @RequirePerm("iam:perm:update")
    public Result<Void> update(@PathVariable Long id, @RequestBody PermissionUpdateDto dto) {
        permissionService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequirePerm("iam:perm:delete")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return Result.success();
    }

    @GetMapping("/validate")
    @RequirePerm("iam:perm:page")
    public Result<PermissionValidateResultDto> validate(
            @RequestParam(required = false) String permCode,
            @RequestParam(required = false) String menuPath,
            @RequestParam(required = false) String apiMethod,
            @RequestParam(required = false) String apiPath,
            @RequestParam(required = false) Long excludeId,
            @RequestParam(required = false) String permType) {
        PermissionValidateResultDto result = permissionService.validate(
                permCode, menuPath, apiMethod, apiPath, permType, excludeId);
        return Result.success(result);
    }
}
