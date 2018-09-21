package com.pmcaff.nework.manager.mapper;

import com.pmcaff.nework.manager.domain.SysUser;

import java.util.List;
import java.util.Map;

public interface SysUserMapper {

    List<SysUser> listUserByParam(Map<String, Object> paramMap);

    SysUser findByUsername(String username);

    int deleteByPrimaryKey(Long id);

    int insert(SysUser record);

    int insertSelective(SysUser record);

    SysUser selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SysUser record);

    int updateByPrimaryKey(SysUser record);
}