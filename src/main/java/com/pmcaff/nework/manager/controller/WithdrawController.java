package com.pmcaff.nework.manager.controller;

import com.pmcaff.nework.core.common.Constants;
import com.pmcaff.nework.core.common.OrderStatusConstants;
import com.pmcaff.nework.core.common.Page;
import com.pmcaff.nework.core.controller.BaseController;
import com.pmcaff.nework.core.domain.UserAccount;
import com.pmcaff.nework.core.domain.WithdrawRecord;
import com.pmcaff.nework.core.service.MessageService;
import com.pmcaff.nework.core.service.UserAccountService;
import com.pmcaff.nework.core.service.WithdrawService;
import com.pmcaff.nework.core.utils.DateUtil;
import com.pmcaff.nework.core.utils.JsonUtils;
import com.pmcaff.nework.core.utils.pingPlusPlus.TransferUtil;
import com.pmcaff.nework.manager.common.ManagerConstants;
import com.pmcaff.nework.manager.service.OptLogService;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 提现记录
 *
 * @author skd
 * @CREATE 18/7/1
 */
@RestController
@RequestMapping(value = "/withdraw")
public class WithdrawController extends BaseController {
    @Resource
    WithdrawService withdrawRecordService;
    @Resource
    UserAccountService userAccountService;
    @Resource
    OptLogService logService;
    @Resource
    MessageService messageService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/listWithdrawByParam", method = RequestMethod.GET)
    public JSONObject listOrderNeedByParam(@RequestParam(required = false) String withdrawId,
                                           @RequestParam(required = false) Long userId,
                                           @RequestParam(required = false) String username,
                                           @RequestParam(required = false) String type,
                                           @RequestParam(required = false) String address,
                                           @RequestParam(required = false) String statusAudit,
                                           //@RequestParam(required = false) String statusPass,
                                           @RequestParam(required = false) String statusWithdraw,
                                           @RequestParam(required = false) String userAudit,
                                           @RequestParam(required = false) String startTime,
                                           @RequestParam(required = false) String endTime,
                                           @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                           @RequestParam(required = false, defaultValue = "0") Integer start,
                                           @RequestParam(required = false, defaultValue = "20") Integer limit,
                                           HttpServletRequest request) {
        JSONObject retJson = new JSONObject();
        Map<String, Object> param = new HashedMap();
        param.put("withdrawId", withdrawId);
        param.put("userId", userId);
        param.put("userName", username);
        param.put("type", type);
        param.put("address", address);
        param.put("statusAudit", statusAudit);
        //param.put("statusPass", statusPass);
        param.put("statusWithdraw", statusWithdraw);
        param.put("userAudit", userAudit);
        if (!StringUtils.isEmpty(startTime)) {
            param.put("startTime", DateUtil.stampToDate(startTime));
        }
        if (!StringUtils.isEmpty(endTime)) {
            param.put("endTime", DateUtil.stampToDate(endTime));
        }

        param.put(Constants.PAGE_NUMBER, pageNum);
        param.put(Constants.PAGE_START, start);

        int totalCount = withdrawRecordService.getRecords(param).size();

        Page page = assemblePage(pageNum, start, limit, totalCount);
        if (limit == -1) {
            param.put(Constants.PAGE_LIMIT, totalCount);
        } else {
            param.put(Constants.PAGE_LIMIT, limit);
        }
        try {
            List<WithdrawRecord> orderCules = withdrawRecordService.getRecords(param);
            retJson = assembleSuccResponseWithPage(retJson, JsonUtils.parseObject(orderCules), page);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            retJson = assembleFailResponse(retJson, Constants.EXCEPTION_CODE, Constants.EXCEPTION_DESC, e.getMessage());
        }
        return retJson;
    }

    /**
     * 提现审核
     *
     * @param withdrawId
     * @param statusAudit yes/no   审核通过没通过
     * @return
     */
    @RequestMapping(value = "/update", method = RequestMethod.GET)
    public JSONObject update(@RequestParam(required = true) String withdrawId,
                             @RequestParam(required = true) String statusAudit) {

        JSONObject retJson = new JSONObject();

        String operator = (String) SecurityUtils.getSubject().getPrincipal();

        try {
            WithdrawRecord record = withdrawRecordService.selectByPrimaryKey(withdrawId);
            if (record == null) {
                retJson.put("code", 400);
                retJson.put("desc", "no found");
                return retJson;
            }

            String audit = record.getStatusAudit();
            if (OrderStatusConstants.WITHDRAW_STATUS_AUDIT_YES.equals(audit)) {
                retJson.put("code", 401);
                retJson.put("desc", "already audit");
                return retJson;
            }

            if (OrderStatusConstants.WITHDRAW_STATUS_AUDIT_YES.equals(statusAudit)) {

                long amountFinal = record.getAmountFinal();
                long userId = record.getUserid();
                UserAccount userAccount = userAccountService.selectByPrimaryKey(userId);
                String openId = userAccount.getOpenid();
                if (openId == null) {
                    retJson.put("code", 401);
                    retJson.put("desc", "openId error");
                    return retJson;
                }
                record.setStatusAudit(OrderStatusConstants.WITHDRAW_STATUS_AUDIT_YES);
                //record.setStatusPass(statusPass);
                record.setStatusWithdraw(OrderStatusConstants.WITHDRAW_STATUS_WAIT);
                record.setUserAudit(operator);
                withdrawRecordService.updateByPrimaryKeySelective(record);
                //// TODO: 18/7/7  发起提现
                TransferUtil.create(withdrawId, amountFinal, "wx_pub", openId);
            } else {
                undo(record, operator);

            }

            JSONObject logJson = new JSONObject();
            logJson.put("withdrawId", withdrawId);
            logJson.put("statusAudit", statusAudit);
            logService.write(ManagerConstants.MODULE_WITHDRAW, withdrawId,
                    ManagerConstants.OPT_TYPE_UPDATE, logJson.toString());

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            retJson = assembleFailResponse(retJson, Constants.EXCEPTION_CODE, Constants.EXCEPTION_DESC, e.getMessage());
            return retJson;
        }
        retJson.put("code", 200);
        retJson.put("desc", "success");
        return retJson;

    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class}, isolation = Isolation.DEFAULT)
    public void undo(WithdrawRecord record, String operator) {

        record.setStatusAudit(OrderStatusConstants.WITHDRAW_STATUS_AUDIT_YES);
        //record.setStatusPass(statusPass);
        record.setStatusWithdraw(OrderStatusConstants.WITHDRAW_STATUS_REJECT);
        record.setUserAudit(operator);
        withdrawRecordService.updateByPrimaryKeySelective(record);

        long userId = record.getUserid();

        // 冻结余额的部分
        UserAccount userAccount = userAccountService.selectByPrimaryKey(userId);

        long money = userAccount.getMoney();
        long amount = record.getAmount();

        userAccount.setMoney(money + amount);
        userAccountService.updateByPrimaryKey(userAccount);

        // 发消息 提现被驳回
        messageService.sendWithdrawReject(record);
    }
}
