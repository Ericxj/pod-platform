package com.pod.iam.controller;

import com.pod.common.core.context.TenantContext;
import com.pod.common.core.domain.Result;
import com.pod.common.core.exception.BusinessException;
import com.pod.iam.application.DataScopeService;
import com.pod.iam.domain.IamUser;
import com.pod.iam.mapper.IamUserMapper;
import com.pod.iam.utils.JwtUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/iam")
public class FactoryController {

    private final DataScopeService dataScopeService;
    private final IamUserMapper userMapper;
    private final JwtUtils jwtUtils;

    public FactoryController(DataScopeService dataScopeService, IamUserMapper userMapper, JwtUtils jwtUtils) {
        this.dataScopeService = dataScopeService;
        this.userMapper = userMapper;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/me/switchFactory")
    public Result<Map<String, Object>> switchFactory(@RequestBody Map<String, Long> body) {
        Long targetFactoryId = body.get("factoryId");
        if (targetFactoryId == null) {
            throw new BusinessException(400, "Target Factory ID is required");
        }
        Long userId = TenantContext.getUserId();
        IamUser user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(404, "User not found");

        List<Long> allowedFactoryIds = dataScopeService.getAccessibleFactoryIdsStrict(userId);
        if (!allowedFactoryIds.contains(targetFactoryId)) {
            throw new BusinessException(403, "Access to Factory " + targetFactoryId + " denied");
        }
        String newToken = jwtUtils.generateToken(user.getUsername(), user.getId(), user.getTenantId(), targetFactoryId);
        Map<String, Object> res = new HashMap<>();
        res.put("token", newToken);
        res.put("currentFactoryId", targetFactoryId);
        return Result.success(res);
    }
}
