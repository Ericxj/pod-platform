package com.pod.iam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pod.common.core.annotation.RequirePerm;
import com.pod.common.core.domain.Result;
import com.pod.iam.application.IamRoleApplicationService;
import com.pod.iam.domain.IamRole;
import com.pod.iam.dto.GrantPermissionsDto;
import com.pod.iam.dto.RoleCreateDto;
import com.pod.iam.dto.RolePageQuery;
import com.pod.iam.dto.RolePermissionsDto;
import com.pod.iam.dto.RoleUpdateDto;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/iam/roles")
public class RoleController {

    private final IamRoleApplicationService roleService;

    public RoleController(IamRoleApplicationService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @RequirePerm("iam:role:page")
    public Result<IPage<IamRole>> page(RolePageQuery query) {
        return Result.success(roleService.page(query));
    }

    @GetMapping("/{id}")
    @RequirePerm("iam:role:page")
    public Result<IamRole> get(@PathVariable Long id) {
        IamRole role = roleService.get(id);
        if (role == null) {
            return Result.error("Role not found");
        }
        return Result.success(role);
    }

    @PostMapping
    @RequirePerm("iam:role:create")
    public Result<Void> create(@RequestBody RoleCreateDto dto) {
        roleService.create(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    @RequirePerm("iam:role:update")
    public Result<Void> update(@PathVariable Long id, @RequestBody RoleUpdateDto dto) {
        roleService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequirePerm("iam:role:delete")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}/permissions")
    @RequirePerm("iam:role:page")
    public Result<RolePermissionsDto> getPermissions(@PathVariable Long id) {
        List<Long> permIds = roleService.getPermissionIds(id);
        return Result.success(new RolePermissionsDto(permIds));
    }

    @PutMapping("/{id}/permissions")
    @RequirePerm("iam:role:grant")
    public Result<Void> putPermissions(@PathVariable Long id, @RequestBody RolePermissionsDto dto) {
        roleService.grantPermissions(id, new GrantPermissionsDto(dto.getPermIds()));
        return Result.success();
    }

    @PostMapping("/{id}/grantPermissions")
    @RequirePerm("iam:role:grant")
    public Result<Void> grantPermissions(@PathVariable Long id, @RequestBody GrantPermissionsDto dto) {
        roleService.grantPermissions(id, dto);
        return Result.success();
    }
}
