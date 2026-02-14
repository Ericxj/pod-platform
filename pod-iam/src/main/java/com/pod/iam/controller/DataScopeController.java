package com.pod.iam.controller;

import com.pod.common.core.annotation.RequirePerm;
import com.pod.common.core.domain.Result;
import com.pod.iam.application.DataScopeService;
import com.pod.iam.dto.DataScopeQueryDto;
import com.pod.iam.dto.DataScopeUpdateDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/iam/dataScopes")
public class DataScopeController {

    private final DataScopeService dataScopeService;

    public DataScopeController(DataScopeService dataScopeService) {
        this.dataScopeService = dataScopeService;
    }

    @GetMapping
    @RequirePerm("iam:scope:query")
    public Result<DataScopeQueryDto> get(
            @RequestParam String subjectType,
            @RequestParam Long subjectId,
            @RequestParam String scopeType) {
        List<Long> scopeIds = dataScopeService.getScopeIds(subjectType, subjectId, scopeType);
        return Result.success(new DataScopeQueryDto(scopeIds));
    }

    @PutMapping
    @RequirePerm("iam:scope:update")
    public Result<Void> put(@RequestBody DataScopeUpdateDto dto) {
        dataScopeService.setScopeIds(
                dto.getSubjectType(),
                dto.getSubjectId(),
                dto.getScopeType(),
                dto.getScopeIds());
        return Result.success();
    }
}
