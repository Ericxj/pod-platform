package com.pod.iam.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pod.iam.domain.IamUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface IamUserMapper extends BaseMapper<IamUser> {
    
    @InterceptorIgnore(tenantLine = "true") // Ignore both Tenant and Factory filters for login lookup
    @Select("SELECT * FROM iam_user WHERE username = #{username} AND deleted = 0")
    IamUser selectByUsername(@Param("username") String username);
}
