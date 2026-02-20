package com.pod.srm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pod.srm.domain.PurchaseOrderLine;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PurchaseOrderLineMapper extends BaseMapper<PurchaseOrderLine> {
}
