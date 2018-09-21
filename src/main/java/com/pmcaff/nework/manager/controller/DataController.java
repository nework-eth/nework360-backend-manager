package com.pmcaff.nework.manager.controller;

import com.pmcaff.nework.core.common.Constants;
import com.pmcaff.nework.core.controller.BaseController;
import com.pmcaff.nework.core.domain.VO.CommonResult;
import com.pmcaff.nework.core.domain.VO.UserEnteringVO;
import com.pmcaff.nework.core.service.OrderCuleService;
import com.pmcaff.nework.core.service.OrderNeedsService;
import com.pmcaff.nework.core.service.OrderQuoteService;
import com.pmcaff.nework.core.utils.DateUtil;
import com.pmcaff.nework.core.utils.JsonUtils;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author skd
 * @CREATE 18/9/13
 */
@RestController
@RequestMapping(value = "/data")
public class DataController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    OrderNeedsService orderNeedsService;

    @Resource
    OrderCuleService orderCuleService;

    @Resource
    OrderQuoteService orderQuoteService;

    @RequestMapping(value = "/orderNeeds", method = RequestMethod.GET)
    public JSONObject orderNeeds(@RequestParam(required = false) String startDate,
                                 @RequestParam(required = false) String endDate) {
        JSONObject retJson = new JSONObject();

        startDate = Objects.isNull(startDate) ? DateUtil.getBefroeDate(9) : startDate;
        endDate = Objects.isNull(endDate) ? DateUtil.getCurDate() : endDate;

        startDate = DateUtil.transDate(startDate);
        endDate = DateUtil.transDate(endDate);

        try {
            List<CommonResult> items = orderNeedsService.getCount(startDate, endDate);
            retJson = assembleSuccessResponse(retJson, JsonUtils.parseObject(items));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            retJson = assembleFailResponse(retJson, Constants.EXCEPTION_CODE, Constants.EXCEPTION_DESC, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "/ave", method = RequestMethod.GET)
    public JSONObject ave(@RequestParam(required = false) String startDate,
                          @RequestParam(required = false) String endDate) {
        JSONObject retJson = new JSONObject();

        startDate = Objects.isNull(startDate) ? DateUtil.getBefroeDate(9) : startDate;
        endDate = Objects.isNull(endDate) ? DateUtil.getCurDate() : endDate;

        startDate = DateUtil.transDate(startDate);
        endDate = DateUtil.transDate(endDate);

        try {
            // 获取报价数量
            List<CommonResult> quotes = orderQuoteService.getCount(startDate, endDate);
            Map<String, Long> quoteMap = new HashMap<>();
            for (CommonResult commonResult : quotes) {
                quoteMap.put(commonResult.getD(), commonResult.getC());
            }

            // 获取需求数量
            List<CommonResult> needs = orderNeedsService.getCount(startDate, endDate);

            List<CommonResult> items = new LinkedList<>();
            for (CommonResult commonResult : needs) {
                String d = commonResult.getD();
                long c = commonResult.getC();

                CommonResult item = new CommonResult();
                if (quoteMap.get(d) == null) {
                    item.setD(d);
                    item.setC(Long.valueOf(0));
                } else {
                    item.setD(d);
                    item.setC(quoteMap.get(d) / c);
                }
                items.add(item);
            }

            retJson = assembleSuccessResponse(retJson, JsonUtils.parseObject(items));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            retJson = assembleFailResponse(retJson, Constants.EXCEPTION_CODE, Constants.EXCEPTION_DESC, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "/orderNeedsBySucc", method = RequestMethod.GET)
    public JSONObject orderNeedsBySucc(@RequestParam(required = false) String startDate,
                                       @RequestParam(required = false) String endDate) {
        JSONObject retJson = new JSONObject();
        startDate = Objects.isNull(startDate) ? DateUtil.getBefroeDate(9) : startDate;
        endDate = Objects.isNull(endDate) ? DateUtil.getCurDate() : endDate;

        startDate = DateUtil.transDate(startDate);
        endDate = DateUtil.transDate(endDate);

        try {
            List<CommonResult> items = orderNeedsService.getSuccCount(startDate, endDate);
            retJson = assembleSuccessResponse(retJson, JsonUtils.parseObject(items));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            retJson = assembleFailResponse(retJson, Constants.EXCEPTION_CODE, Constants.EXCEPTION_DESC, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "/orderCule", method = RequestMethod.GET)
    public JSONObject orderCule(@RequestParam(required = false) String startDate,
                                @RequestParam(required = false) String endDate) {
        JSONObject retJson = new JSONObject();
        startDate = Objects.isNull(startDate) ? DateUtil.getBefroeDate(9) : startDate;
        endDate = Objects.isNull(endDate) ? DateUtil.getCurDate() : endDate;

        startDate = DateUtil.transDate(startDate);
        endDate = DateUtil.transDate(endDate);

        try {
            List<CommonResult> items = orderCuleService.getCount(startDate, endDate);
            retJson = assembleSuccessResponse(retJson, JsonUtils.parseObject(items));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            retJson = assembleFailResponse(retJson, Constants.EXCEPTION_CODE, Constants.EXCEPTION_DESC, e.getMessage());
        }
        return retJson;
    }

}
