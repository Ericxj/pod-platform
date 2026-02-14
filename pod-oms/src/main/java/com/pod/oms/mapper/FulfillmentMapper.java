package com.pod.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pod.oms.domain.Fulfillment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface FulfillmentMapper extends BaseMapper<Fulfillment> {

    @Update("UPDATE oms_fulfillment SET status = #{newStatus}, version = version + 1, updated_at = NOW(), updated_by = #{updatedBy} " +
            "WHERE id = #{id} AND status = #{oldStatus} AND version = #{version} AND deleted = 0")
    int updateStatusWithLock(@Param("id") Long id, 
                             @Param("newStatus") String newStatus, 
                             @Param("oldStatus") String oldStatus, 
                             @Param("version") Integer version,
                             @Param("updatedBy") Long updatedBy);
}
