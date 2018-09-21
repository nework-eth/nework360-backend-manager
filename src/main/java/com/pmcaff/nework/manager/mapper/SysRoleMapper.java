package com.pmcaff.nework.manager.mapper;

import com.pmcaff.nework.manager.domain.SysRole;

import java.util.List;
import java.util.Map;

public interface SysRoleMapper {

    List<SysRole> listRoleByParam(Map<String, Object> paramMap);

    List<String> listAllUsedRoleNames();

    SysRole findByRoleName(String roleName);

    int deleteByPrimaryKey(Long roleId);

    int insert(SysRole record);

    int insertSelective(SysRole record);

    SysRole selectByPrimaryKey(Long roleId);

    int updateByPrimaryKeySelective(SysRole record);

    int updateByPrimaryKey(SysRole record);
}