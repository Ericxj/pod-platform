package com.pod.infra.idempotent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pod.infra.idempotent.domain.IdempotentRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IdempotentMapper extends BaseMapper<IdempotentRecord> {
}
