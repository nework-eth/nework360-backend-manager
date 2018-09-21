package com.pmcaff.nework.manager.controller;

import com.pmcaff.nework.core.common.Constants;
import com.pmcaff.nework.core.common.Page;
import com.pmcaff.nework.core.controller.BaseController;
import com.pmcaff.nework.core.domain.OrderCule;
import com.pmcaff.nework.core.service.OrderCuleService;
import com.pmcaff.nework.core.utils.DateUtil;
import com.pmcaff.nework.core.utils.JsonUtils;
import com.pmcaff.nework.manager.util.RedisUtil;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping(value = "/orderCule")
public class OrderCuleController extends BaseController {
    @Resource
    OrderCuleService orderCuleService;

    @Autowired
    private RedisUtil redisUtil;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/listOrderCuleByParam", method = RequestMethod.GET)
    public JSONObject listOrderNeedByParam(@RequestParam(required = false) String culeOrderId,
                                           @RequestParam(required = false) String type,
                                           @RequestParam(required = false) String status,
                                           @RequestParam(required = false) Long userId,
                                           @RequestParam(required = false) String userName,
                                           @RequestParam(required = false) String startTime,
                                           @RequestParam(required = false) String endTime,
                                           @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                           @RequestParam(required = false, defaultValue = "0") Integer start,
                                           @RequestParam(required = false, defaultValue = "20") Integer limit,
                                           HttpServletRequest request) {
        JSONObject retJson = new JSONObject();
        Map<String, Object> param = new HashedMap();
        param.put("clueOrderId", culeOrderId);
        param.put("type", type);
        param.put("status", status);
        param.put("userId", userId);
        param.put("userName", userName);
        if (!StringUtils.isEmpty(startTime)) {
            param.put("startTime", DateUtil.stampToDate(startTime));
        }
        if (!StringUtils.isEmpty(endTime)) {
            param.put("endTime", DateUtil.stampToDate(endTime));
        }

        param.put(Constants.PAGE_NUMBER, pageNum);
        param.put(Constants.PAGE_START, start);

        int totalCount = orderCuleService.getOrderCule(param).size();

        Page page = assemblePage(pageNum, start, limit, totalCount);
        if (limit == -1) {
            param.put(Constants.PAGE_LIMIT, totalCount);
        } else {
            param.put(Constants.PAGE_LIMIT, limit);
        }
        try {
            List<OrderCule> orderCules = orderCuleService.getOrderCule(param);
            retJson = assembleSuccResponseWithPage(retJson, JsonUtils.parseObject(orderCules), page);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            retJson = assembleFailResponse(retJson, Constants.EXCEPTION_CODE, Constants.EXCEPTION_DESC, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "/updateCulePrice", method = RequestMethod.GET)
    public JSONObject updateCulePrice(@RequestParam Integer price) {
        JSONObject retJson = new JSONObject();
        try {
            redisUtil.set(Constants.CULE_PRICE, price);
            retJson = assembleSuccessResponse(retJson, "");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            retJson = assembleFailResponse(retJson, Constants.EXCEPTION_CODE, Constants.EXCEPTION_DESC, e.getMessage());
        }
        return retJson;
    }
}
