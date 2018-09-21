package com.pmcaff.nework.manager.mapper;

import com.pmcaff.nework.manager.domain.OptLog;

import java.util.List;
import java.util.Map;

public interface OptLogMapper {

    List<OptLog> listItemByParam(Map<String, Object> paramMap);

    int deleteByPrimaryKey(Long id);

    int insert(OptLog record);

    int insertSelective(OptLog record);

    OptLog selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(OptLog record);

    int updateByPrimaryKey(OptLog record);
}