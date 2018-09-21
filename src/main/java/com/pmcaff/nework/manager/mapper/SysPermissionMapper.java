package com.pmcaff.nework.manager.mapper;

import com.pmcaff.nework.manager.domain.SysPermission;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface SysPermissionMapper {

    List<SysPermission> listPermissionByParam(Map<String, Object> paramMap);

    @Select("select * from ne_sys_permission where available = 1")
    List<SysPermission> listAllUsedPermission();

    SysPermission findByPermName(String permName);

    int deleteByPrimaryKey(Long id);

    int insert(SysPermission record);

    int insertSelective(SysPermission record);

    SysPermission selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SysPermission record);

    int updateByPrimaryKey(SysPermission record);
}