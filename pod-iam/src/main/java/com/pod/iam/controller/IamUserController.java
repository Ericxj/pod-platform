package com.pod.iam.controller;

import com.pod.common.core.annotation.RequirePerm;
import com.pod.common.core.domain.Result;
import com.pod.iam.application.IamUserApplicationService;
import com.pod.iam.domain.IamUser;
import com.pod.iam.dto.UserDto;
import com.pod.iam.dto.UserPageQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/iam/users")
public class IamUserController {

    private final IamUserApplicationService userService;

    public IamUserController(IamUserApplicationService userService) {
        this.userService = userService;
    }

    @GetMapping
    @RequirePerm("iam:user:list")
    public Result<IPage<IamUser>> page(UserPageQuery query) {
        return Result.success(userService.page(query));
    }

    @GetMapping("/{id}")
    @RequirePerm("iam:user:query")
    public Result<UserDto> get(@PathVariable Long id) {
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
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }
    
    @PutMapping("/{id}/password")
    @RequirePerm("iam:user:reset_pwd")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        userService.resetPassword(id, body.get("password"));
        return Result.success();
    }
}
