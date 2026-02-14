package com.pod.ai.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.pod.ai.domain.AiTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AiTaskMapper extends BaseMapper<AiTask> {

    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM ai_task ${ew.customSqlSegment}")
    IPage<AiTask> selectPageIgnoreTenant(IPage<AiTask> page, @Param(Constants.WRAPPER) Wrapper<AiTask> queryWrapper);
}
