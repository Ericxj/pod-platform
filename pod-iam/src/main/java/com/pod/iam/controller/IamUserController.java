package com.pod.iam.controller;

import com.pod.common.core.annotation.RequirePerm;
import com.pod.common.core.domain.Result;
import com.pod.iam.application.DataScopeService;
import com.pod.iam.application.IamUserApplicationService;
import com.pod.iam.domain.IamUser;
import com.pod.iam.dto.UserDto;
import com.pod.iam.dto.UserFactoryScopesDto;
import com.pod.iam.dto.UserPageQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/iam/users")
public class IamUserController {

    private final IamUserApplicationService userService;
    private final DataScopeService dataScopeService;

    public IamUserController(IamUserApplicationService userService, DataScopeService dataScopeService) {
        this.userService = userService;
        this.dataScopeService = dataScopeService;
    }

    @GetMapping
    @RequirePerm("iam:user:list")
    public Result<IPage<IamUser>> page(UserPageQuery query) {
        return Result.success(userService.page(query));
    }

    @GetMapping("/{id}")
    @RequirePerm("iam:user:query")
    public Result<UserDto> get(@PathVariable("id") Long id) {
        IamUser user = userService.get(id);
        if (user == null) return Result.error("User not found");
        
        UserDto dto = new UserDto();
        cn.hutool.core.bean.BeanUtil.copyProperties(user, dto);
        
        dto.setRoleIds(userService.getRoleIds(id));
        return Result.success(dto);
    }

    @PostMapping
    @RequirePerm("iam:user:create")
    public Result<Void> create(@RequestBody UserDto dto) {
        userService.create(dto);
        return Result.success();
    }

    @PutMapping
    @RequirePerm("iam:user:update")
    public Result<Void> update(@RequestBody UserDto dto) {
        userService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequirePerm("iam:user:delete")
    public Result<Void> delete(@PathVariable("id") Long id) {
        userService.delete(id);
        return Result.success();
    }
    
    @PutMapping("/{id}/password")
    @RequirePerm("iam:user:reset_pwd")
    public Result<Void> resetPassword(@PathVariable("id") Long id, @RequestBody java.util.Map<String, String> body) {
        userService.resetPassword(id, body.get("password"));
        return Result.success();
    }

    @GetMapping("/{id}/factoryScopes")
    @RequirePerm("iam:user:query")
    public Result<UserFactoryScopesDto> getFactoryScopes(@PathVariable("id") Long id) {
        IamUser user = userService.get(id);
        if (user == null) return Result.error("User not found");
        List<Long> factoryIds = dataScopeService.getUserFactoryScopeIds(id);
        return Result.success(new UserFactoryScopesDto(factoryIds));
    }

    @PutMapping("/{id}/factoryScopes")
    @RequirePerm("iam:user:update")
    public Result<Void> putFactoryScopes(@PathVariable("id") Long id, @RequestBody UserFactoryScopesDto dto) {
        IamUser user = userService.get(id);
        if (user == null) return Result.error("User not found");
        dataScopeService.setUserFactoryScopes(id, dto.getFactoryIds());
        return Result.success();
    }

    @GetMapping("/{id}/roles")
    @RequirePerm("iam:user:query")
    public Result<java.util.List<Long>> getRoleIds(@PathVariable("id") Long id) {
        IamUser user = userService.get(id);
        if (user == null) return Result.error("User not found");
        return Result.success(userService.getRoleIds(id));
    }

    @PutMapping("/{id}/roles")
    @RequirePerm("iam:user:update")
    public Result<Void> putRoleIds(@PathVariable("id") Long id, @RequestBody java.util.Map<String, java.util.List<Long>> body) {
        IamUser user = userService.get(id);
        if (user == null) return Result.error("User not found");
        java.util.List<Long> roleIds = body != null ? body.get("roleIds") : null;
        userService.setUserRoles(id, roleIds != null ? roleIds : java.util.Collections.emptyList());
        return Result.success();
    }
}
