package com.pod.iam.application;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pod.common.core.exception.BusinessException;
import com.pod.iam.domain.IamUser;
import com.pod.iam.domain.IamUserRole;
import com.pod.iam.dto.UserDto;
import com.pod.iam.dto.UserPageQuery;
import com.pod.iam.mapper.IamUserMapper;
import com.pod.iam.mapper.IamUserRoleMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IamUserApplicationService {

    private final IamUserMapper userMapper;
    private final IamUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public IamUserApplicationService(IamUserMapper userMapper, IamUserRoleMapper userRoleMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public IPage<IamUser> page(UserPageQuery query) {
        Page<IamUser> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<IamUser> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.like(StrUtil.isNotBlank(query.getUsername()), IamUser::getUsername, query.getUsername());
        wrapper.like(StrUtil.isNotBlank(query.getRealName()), IamUser::getRealName, query.getRealName());
        wrapper.eq(StrUtil.isNotBlank(query.getStatus()), IamUser::getStatus, query.getStatus());
        wrapper.orderByDesc(IamUser::getCreatedAt);

        return userMapper.selectPage(page, wrapper);
    }

    public IamUser get(Long id) {
        return userMapper.selectById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(UserDto dto) {
        // Check username uniqueness (Tenant aware by default due to MyBatis Plus Tenant Handler)
        Long count = userMapper.selectCount(new LambdaQueryWrapper<IamUser>().eq(IamUser::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("Username already exists");
        }

        IamUser user = new IamUser();
        BeanUtil.copyProperties(dto, user, "id", "password");
        
        // Default password if not provided
        String rawPassword = StrUtil.isBlank(dto.getPassword()) ? "123456" : dto.getPassword();
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        
        if (StrUtil.isBlank(user.getStatus())) {
            user.setStatus("ENABLED");
        }

        userMapper.insert(user);
        
        // Assign Roles
        assignRoles(user.getId(), dto.getRoleIds());
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(UserDto dto) {
        IamUser user = userMapper.selectById(dto.getId());
        if (user == null) {
            throw new BusinessException("User not found");
        }

        user.setRealName(dto.getRealName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(dto.getStatus());
        // Do not update password here

        userMapper.updateById(user);
        
        // Update Roles
        if (dto.getRoleIds() != null) {
             // Clear existing roles
             userRoleMapper.delete(new LambdaQueryWrapper<IamUserRole>().eq(IamUserRole::getUserId, user.getId()));
             // Assign new roles
             assignRoles(user.getId(), dto.getRoleIds());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        userMapper.deleteById(id);
        userRoleMapper.delete(new LambdaQueryWrapper<IamUserRole>().eq(IamUserRole::getUserId, id));
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id, String newPassword) {
        IamUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("User not found");
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    private void assignRoles(Long userId, List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) return;
        
        List<IamUserRole> userRoles = new ArrayList<>();
        for (Long roleId : roleIds) {
            IamUserRole ur = new IamUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            userRoles.add(ur);
        }
        // Batch insert? Or loop insert. MyBatis Plus Service has saveBatch. Mapper doesn't.
        // For simplicity loop insert.
        for (IamUserRole ur : userRoles) {
            userRoleMapper.insert(ur);
        }
    }
    
    public List<Long> getRoleIds(Long userId) {
        List<IamUserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<IamUserRole>().eq(IamUserRole::getUserId, userId));
        return userRoles.stream().map(IamUserRole::getRoleId).collect(Collectors.toList());
    }
}
