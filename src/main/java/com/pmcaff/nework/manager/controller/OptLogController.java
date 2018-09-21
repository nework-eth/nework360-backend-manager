package com.pmcaff.nework.manager.controller;

import com.pmcaff.nework.core.common.Constants;
import com.pmcaff.nework.core.common.Page;
import com.pmcaff.nework.core.controller.BaseController;
import com.pmcaff.nework.core.utils.DateUtil;
import com.pmcaff.nework.core.utils.JsonUtils;
import com.pmcaff.nework.manager.domain.OptLog;
import com.pmcaff.nework.manager.service.OptLogService;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/optLog")
public class OptLogController extends BaseController {

    @Resource
    OptLogService logService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/listLogByParam", method = RequestMethod.GET)
    public JSONObject listLogByParam(@RequestParam(required = false) String module,
                                     @RequestParam(required = false) Long dataPK,
                                     @RequestParam(required = false) String operator,
                                     @RequestParam(required = false) String optType,
                                     @RequestParam(required = false) String startTime,
                                     @RequestParam(required = false) String endTime,
                                     @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                     @RequestParam(required = false, defaultValue = "0") Integer start,
                                     @RequestParam(required = false, defaultValue = "20") Integer limit) {
        JSONObject retJson = new JSONObject();
        Map<String, Object> param = new HashedMap();
        param.put("module", module);
        param.put("data_PK", dataPK);
        param.put("operator", operator);
        param.put("opt_type", optType);
        if (!StringUtils.isEmpty(startTime)) {
            param.put("startTime", DateUtil.stampToDate(startTime));
        }
        if (!StringUtils.isEmpty(endTime)) {
            param.put("endTime", DateUtil.stampToDate(endTime));
        }
        param.put(Constants.PAGE_NUMBER, pageNum);
        param.put(Constants.PAGE_START, start);
        int totalCount = logService.listLogByParam(param).size();
        Page page = assemblePage(pageNum, start, limit, totalCount);
        if (limit == -1) {
            param.put(Constants.PAGE_LIMIT, totalCount);
        } else {
            param.put(Constants.PAGE_LIMIT, limit);
        }
        try {
            List<OptLog> serviceTypes = logService.listLogByParam(param);
            retJson = assembleSuccResponseWithPage(retJson, JsonUtils.parseObject(serviceTypes), page);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            retJson = assembleFailResponse(retJson, Constants.EXCEPTION_CODE, Constants.EXCEPTION_DESC, e.getMessage());
        }
        return retJson;
    }
}
