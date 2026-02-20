package com.pod.srm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pod.srm.domain.PurchaseOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrder> {
}
