package com.pod.iam.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pod.iam.domain.IamPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface IamPermissionMapper extends BaseMapper<IamPermission> {

    @Select("SELECT p.* FROM iam_permission p " +
            "INNER JOIN iam_role_permission rp ON p.id = rp.perm_id " +
            "INNER JOIN iam_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.deleted = 0 AND rp.deleted = 0 AND ur.deleted = 0")
    List<IamPermission> selectByUserId(@Param("userId") Long userId);

    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT tenant_id, factory_id FROM iam_permission WHERE deleted = 0 GROUP BY tenant_id, factory_id")
    List<IamPermission> selectDistinctTenantFactory();
}
