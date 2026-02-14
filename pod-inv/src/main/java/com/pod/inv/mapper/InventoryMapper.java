package com.pod.inv.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pod.inv.domain.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {
    
    @Select("SELECT * FROM inv_inventory WHERE sku_code = #{skuCode} AND deleted = 0 LIMIT 1")
    Inventory selectBySku(@Param("skuCode") String skuCode);
}
