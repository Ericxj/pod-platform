package com.pod.iam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pod.iam.domain.IamTenant;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IamTenantMapper extends BaseMapper<IamTenant> {
}
