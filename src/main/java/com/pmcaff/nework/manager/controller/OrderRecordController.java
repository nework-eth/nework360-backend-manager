package com.pmcaff.nework.manager.controller;

import com.pmcaff.nework.core.common.Constants;
import com.pmcaff.nework.core.common.OrderStatusConstants;
import com.pmcaff.nework.core.common.Page;
import com.pmcaff.nework.core.controller.BaseController;
import com.pmcaff.nework.core.domain.OrderRecord;
import com.pmcaff.nework.core.service.OrderRecordService;
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
@RequestMapping(value = "/orderRecord")
public class OrderRecordController extends BaseController {

    @Resource
    OrderRecordService orderRecordService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/listOrderRecordByParam", method = RequestMethod.GET)
    public JSONObject listOrderRecordByParam(@RequestParam(required = false) String recordOrderId,
                                             @RequestParam(required = false) String business,
                                             @RequestParam(required = false) String payWay,
                                             @RequestParam(required = false) String status,
                                             @RequestParam(required = false) String userAName,
                                             @RequestParam(required = false) String userBName,
                                             @RequestParam(required = false) String startTime,
                                             @RequestParam(required = false) String endTime,
                                             @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                             @RequestParam(required = false, defaultValue = "0") Integer start,
                                             @RequestParam(required = false, defaultValue = "20") Integer limit,
                                             HttpServletRequest request) {
        JSONObject retJson = new JSONObject();
        Map<String, Object> param = new HashedMap();
        param.put("recordOrderId", recordOrderId);
        param.put("business", business);
        param.put("status", status);
        if (status != null && !status.isEmpty() && status.equals("succ")) {
            param.put("statusA", "pay_success");
        }
        param.put("payWay", payWay);
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

        int totalCount = orderRecordService.getOrderAndWithdrawAndCuleRecord(param).size();

        Page page = assemblePage(pageNum, start, limit, totalCount);
        if (limit == -1) {
            param.put(Constants.PAGE_LIMIT, totalCount);
        } else {
            param.put(Constants.PAGE_LIMIT, limit);
        }
        try {
            List<OrderRecord> orderRecordList = orderRecordService.getOrderAndWithdrawAndCuleRecord(param);
            JSONArray array = new JSONArray();
            for (OrderRecord orderRecord : orderRecordList) {
                String b = orderRecord.getBusiness();
                if (b != null && b.equals("withdraw")) {
                    String s = orderRecord.getStatus();
                    if (!s.equals(OrderStatusConstants.WITHDRAW_STATUS_SUBMIT)) {
                        String aName = orderRecord.getUserAName();
                        String bName = orderRecord.getUserBName();
                        orderRecord.setUserAName(bName);
                        orderRecord.setUserBName(aName);
                        orderRecord.setPayWay("system");
                    }
                }

                String p = orderRecord.getPayWay();
                if (p != null && p.contains("wx")) {
                    orderRecord.setPayWay("wx");
                }

                String s = orderRecord.getStatus();
                if (s != null && s.equals("pay_success")) {
                    orderRecord.setStatus("succ");
                }

                array.add(JsonUtils.fromObject(orderRecord));

            }
            retJson = assembleSuccResponseWithPage(retJson, array, page);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            retJson = assembleFailResponse(retJson, Constants.EXCEPTION_CODE, Constants.EXCEPTION_DESC, e.getMessage());
        }
        return retJson;
    }
}
