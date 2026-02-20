package com.pod.inv.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pod.inv.domain.InventoryBalance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface InventoryBalanceMapper extends BaseMapper<InventoryBalance> {
    
    @Update("UPDATE inv_balance SET allocated_qty = #{allocated}, available_qty = #{available}, version = version + 1 " +
            "WHERE id = #{id} AND version = #{version} AND tenant_id = #{tenantId} AND factory_id = #{factoryId}")
    int updateBalanceWithVersion(@Param("id") Long id,
                                 @Param("allocated") Integer allocated,
                                 @Param("available") Integer available,
                                 @Param("version") Integer version,
                                 @Param("tenantId") Long tenantId,
                                 @Param("factoryId") Long factoryId);

    @Update("UPDATE inv_balance SET on_hand_qty = #{onHand}, available_qty = #{available}, version = version + 1 " +
            "WHERE id = #{id} AND version = #{version} AND tenant_id = #{tenantId} AND factory_id = #{factoryId}")
    int updateOnHandWithVersion(@Param("id") Long id,
                                @Param("onHand") Integer onHand,
                                @Param("available") Integer available,
                                @Param("version") Integer version,
                                @Param("tenantId") Long tenantId,
                                @Param("factoryId") Long factoryId);
}
