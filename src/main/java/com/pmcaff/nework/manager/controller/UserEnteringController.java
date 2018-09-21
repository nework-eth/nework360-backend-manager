package com.pmcaff.nework.manager.controller;

import com.pmcaff.nework.core.common.Constants;
import com.pmcaff.nework.core.common.Page;
import com.pmcaff.nework.core.controller.BaseController;
import com.pmcaff.nework.core.domain.UserEntering;
import com.pmcaff.nework.core.domain.VO.UserEnteringVO;
import com.pmcaff.nework.core.service.UserEnteringService;
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
import java.util.Objects;
import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/userEntering")
public class UserEnteringController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    UserEnteringService userEnteringService;

    @RequestMapping(value = "/listItemByPeriod", method = RequestMethod.GET)
    public JSONObject listLogByParam(@RequestParam(required = false) Long startDate,
                                     @RequestParam(required = false) Long endDate) {
        JSONObject retJson = new JSONObject();
        Map<String, Object> param = new HashedMap();
        param.put("startDate", Objects.isNull(startDate) ? DateUtil.getBefroeDate(9) : startDate);
        param.put("endDate", Objects.isNull(endDate) ? DateUtil.getCurDate() : endDate);
        try {
            List<UserEnteringVO> items = userEnteringService.listItemByPeriod(param);
            retJson = assembleSuccessResponse(retJson, JsonUtils.parseObject(items));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            retJson = assembleFailResponse(retJson, Constants.EXCEPTION_CODE, Constants.EXCEPTION_DESC, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "/getItemByDate", method = RequestMethod.GET)
    public JSONObject getItemByDate(@RequestParam Long date) {
        JSONObject retJson = new JSONObject();
        try {
            UserEnteringVO item = userEnteringService.getItemByDate(date);
            retJson = assembleSuccessResponse(retJson, JsonUtils.fromObject(item));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            retJson = assembleFailResponse(retJson, Constants.EXCEPTION_CODE, Constants.EXCEPTION_DESC, e.getMessage());
        }
        return retJson;
    }
}
