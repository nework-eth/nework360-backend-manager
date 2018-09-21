package com.pmcaff.nework.manager.controller;

import com.pmcaff.nework.core.common.Constants;
import com.pmcaff.nework.core.common.Page;
import com.pmcaff.nework.core.controller.BaseController;
import com.pmcaff.nework.core.domain.OrderNeeds;
import com.pmcaff.nework.core.domain.UserEvaluate;
import com.pmcaff.nework.core.service.OrderNeedsService;
import com.pmcaff.nework.core.service.UserEvaluateService;
import com.pmcaff.nework.core.utils.DateUtil;
import com.pmcaff.nework.core.utils.JsonUtils;
import net.sf.json.JSONArray;
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
import javax.servlet.http.HttpServletRequest;

/**
 * @author skd
 * @CREATE 18/6/23
 */
@RestController
@RequestMapping(value = "/orderNeed")
public class OrderNeedController extends BaseController {

    @Resource
    OrderNeedsService orderNeedsService;
    @Resource
    UserEvaluateService userEvaluateService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/listOrderNeedByParam", method = RequestMethod.GET)
    public JSONObject listOrderNeedByParam(@RequestParam(required = false) String needsId,
                                           @RequestParam(required = false) Long districtId,
                                           @RequestParam(required = false) Long serviceId,
                                           @RequestParam(required = false) String serviceName,
                                           @RequestParam(required = false) Long userAId,
                                           @RequestParam(required = false) Long userBId,
                                           @RequestParam(required = false) String userAName,
                                           @RequestParam(required = false) String userBName,
                                           @RequestParam(required = false) Integer status,
                                           @RequestParam(required = false) String startTime,
                                           @RequestParam(required = false) String endTime,
                                           @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                           @RequestParam(required = false, defaultValue = "0") Integer start,
                                           @RequestParam(required = false, defaultValue = "20") Integer limit,
                                           HttpServletRequest request) {
        JSONObject retJson = new JSONObject();
        Map<String, Object> param = new HashedMap();
        param.put("needsId", needsId);
        param.put("districtId", districtId);
        param.put("serviceId", serviceId);
        param.put("serviceName", serviceName);
        param.put("userAId", userAId);
        param.put("userBId", userBId);
        param.put("status", status);
        param.put("userAName", userAName);
        param.put("userBName", userBName);
        if (!StringUtils.isEmpty(startTime)) {
            param.put("startTime", DateUtil.stampToDate(startTime));
        }
        if (!StringUtils.isEmpty(endTime)) {
            param.put("endTime", DateUtil.stampToDate(endTime));
        }

        param.put(Constants.PAGE_NUMBER, pageNum);
        param.put(Constants.PAGE_START, start);

        int totalCount = orderNeedsService.getOrderNeeds(param).size();

        Page page = assemblePage(pageNum, start, limit, totalCount);
        if (limit == -1) {
            param.put(Constants.PAGE_LIMIT, totalCount);
        } else {
            param.put(Constants.PAGE_LIMIT, limit);
        }
        try {
            List<OrderNeeds> orderNeedsList = orderNeedsService.getOrderNeeds(param);
            JSONArray needsArray = new JSONArray();
            for (OrderNeeds orderNeeds : orderNeedsList) {
                JSONObject orderJson = JsonUtils.fromObject(orderNeeds);
                long userId_find = orderNeeds.getUserAId();
                String needsId_find = orderNeeds.getNeedsId();
                Map<String, Object> param_find = new HashedMap();
                param_find.put("userIdEvaluate", userId_find);
                param_find.put("needsId", needsId_find);
                List<UserEvaluate> userEvaluate = userEvaluateService.selectScoreBySelective(param_find);
                if (userEvaluate.size() != 0) {
                    int score = userEvaluate.get(0).getScore();
                    orderJson.put("score", score);
                }
                needsArray.add(orderJson);
            }
            retJson = assembleSuccResponseWithPage(retJson, needsArray, page);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            retJson = assembleFailResponse(retJson, Constants.EXCEPTION_CODE, Constants.EXCEPTION_DESC, e.getMessage());
        }
        return retJson;
    }
}
