package com.pod.iam.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pod.iam.domain.IamDataScope;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface IamDataScopeMapper extends BaseMapper<IamDataScope> {
    
    @InterceptorIgnore(tenantLine = "true") // Ignore tenant filter to find scopes globally by user ID
    @Select("SELECT ds.* FROM iam_data_scope ds " +
            "WHERE ds.scope_type = 'FACTORY' " +
            "AND ds.deleted = 0 " +
            "AND (" +
            "  (ds.subject_type = 'USER' AND ds.subject_id = #{userId}) " +
            "  OR " +
            "  (ds.subject_type = 'ROLE' AND ds.subject_id IN (SELECT role_id FROM iam_user_role WHERE user_id = #{userId} AND deleted = 0))" +
            ")")
    List<IamDataScope> selectFactoryScopesByUserId(@Param("userId") Long userId);

    @InterceptorIgnore(tenantLine = "true") // Ignore tenant filter for access check
    @Select("SELECT COUNT(1) FROM iam_data_scope ds " +
            "WHERE ds.scope_type = 'FACTORY' " +
            "AND ds.scope_id = #{factoryId} " +
            "AND ds.deleted = 0 " +
            "AND (" +
            "  (ds.subject_type = 'USER' AND ds.subject_id = #{userId}) " +
            "  OR " +
            "  (ds.subject_type = 'ROLE' AND ds.subject_id IN (SELECT role_id FROM iam_user_role WHERE user_id = #{userId} AND deleted = 0))" +
            ")")
    Integer countFactoryAccess(@Param("userId") Long userId, @Param("factoryId") Long factoryId);
}
