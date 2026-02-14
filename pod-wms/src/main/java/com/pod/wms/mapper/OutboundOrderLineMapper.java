package com.pod.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pod.wms.domain.OutboundOrderLine;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OutboundOrderLineMapper extends BaseMapper<OutboundOrderLine> {
}
