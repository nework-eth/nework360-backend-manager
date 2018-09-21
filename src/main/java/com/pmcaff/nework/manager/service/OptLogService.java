package com.pmcaff.nework.manager.service;

import com.pmcaff.nework.core.common.CommonResult;
import com.pmcaff.nework.manager.domain.OptLog;
import com.pmcaff.nework.manager.mapper.OptLogMapper;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;

@Service
public class OptLogService {

    @Resource
    OptLogMapper logMapper;

    public List<OptLog> listLogByParam(Map<String, Object> paramMap) {
        return logMapper.listItemByParam(paramMap);
    }

    public CommonResult write(String module, String dataPK, String optType, String optValue) {
        String operator = (String) SecurityUtils.getSubject().getPrincipal();

        OptLog log = new OptLog();
        log.setOperator(operator);
        log.setModule(module);
        log.setDataPk(dataPK);
        log.setOptType(optType);
        log.setOptValue(optValue);
        log.setOptTime(new Date());

        int effect = logMapper.insertSelective(log);
        if (effect > 0) {
            return CommonResult.ok();
        } else {
            return CommonResult.fail();
        }
    }
}