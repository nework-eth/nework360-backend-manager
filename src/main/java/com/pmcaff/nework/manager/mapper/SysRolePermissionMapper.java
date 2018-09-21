package com.pmcaff.nework.manager.mapper;

import com.pmcaff.nework.manager.domain.SysRolePermission;

import java.util.List;
import java.util.Map;

public interface SysRolePermissionMapper {

    List<Long> getPermIdsByRoleId(Long roleId);

    int deletePermForRole(Map<String, String> param);

    int deleteByRoleId(Long roleId);

    int deleteByPermId(Long permId);

    int deleteByPrimaryKey(Long id);

    int insert(SysRolePermission record);

    int insertSelective(SysRolePermission record);

    SysRolePermission selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SysRolePermission record);

    int updateByPrimaryKey(SysRolePermission record);
}