package com.pod.infra.outbox.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pod.infra.outbox.domain.OutboxEvent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OutboxMapper extends BaseMapper<OutboxEvent> {
}
