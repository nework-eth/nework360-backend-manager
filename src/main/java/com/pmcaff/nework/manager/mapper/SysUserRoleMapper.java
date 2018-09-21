package com.pmcaff.nework.manager.mapper;

import com.pmcaff.nework.manager.domain.SysUserRole;

import java.util.List;
import java.util.Map;

public interface SysUserRoleMapper {

    List<Long> getRoleIdsByUserId(Long userId);

    int deleteByUserId(Long userId);

    int deleteByRoleId(Long roleId);

    int deleteRoleForUser(Map<String, String> param);

    int deleteByPrimaryKey(Long id);

    int insert(SysUserRole record);

    int insertSelective(SysUserRole record);

    SysUserRole selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SysUserRole record);

    int updateByPrimaryKey(SysUserRole record);
}