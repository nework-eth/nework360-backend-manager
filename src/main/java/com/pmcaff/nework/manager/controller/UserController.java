package com.pmcaff.nework.manager.controller;

import com.pmcaff.nework.core.common.CommonResult;
import com.pmcaff.nework.core.common.Constants;
import com.pmcaff.nework.core.common.Page;
import com.pmcaff.nework.core.controller.BaseController;
import com.pmcaff.nework.core.domain.User;
import com.pmcaff.nework.core.service.MessageService;
import com.pmcaff.nework.core.service.UserEnteringService;
import com.pmcaff.nework.core.service.UserService;
import com.pmcaff.nework.core.utils.JsonUtils;
import com.pmcaff.nework.core.utils.MD5Util;
import com.pmcaff.nework.manager.common.ManagerConstants;
import com.pmcaff.nework.manager.service.OptLogService;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
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
@RequestMapping(value = "/user", method = RequestMethod.POST)
public class UserController extends BaseController {

    @Resource
    UserService userService;
    @Resource
    OptLogService logService;
    @Resource
    MessageService msgService;
    @Resource
    UserEnteringService userEnteringService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/listUserByParam", method = RequestMethod.GET)
    public JSONObject listUserByParam(@RequestParam(required = false) String userId,
                                      @RequestParam(required = false) String nickName,
                                      @RequestParam(required = false) String phoneNumber,
                                      @RequestParam(required = false) Long districtId,
                                      @RequestParam(required = false) Byte checkStatus,
                                      @RequestParam(required = false) Boolean isPartyB,
                                      @RequestParam(required = false) Boolean isUsed,
                                      @RequestParam(required = false) Boolean isDeleted,
                                      @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                      @RequestParam(required = false, defaultValue = "0") Integer start,
                                      @RequestParam(required = false, defaultValue = "20") Integer limit) {
        JSONObject retJson = new JSONObject();
        Map<String, Object> param = new HashedMap();
        param.put("user_id", userId);
        param.put("nickname", nickName);
        param.put("phone_number", phoneNumber);
        param.put("district", districtId);
        param.put("check_status", checkStatus);
        param.put("is_party_b", isPartyB);
        param.put("is_used", isUsed);
        param.put("is_deleted", isDeleted);
        param.put(Constants.PAGE_NUMBER, pageNum);
        param.put(Constants.PAGE_START, start);
        int totalCount = userService.listAllUser(param).size();
        Page page = assemblePage(pageNum, start, limit, totalCount);
        if (limit == -1) {
            param.put(Constants.PAGE_LIMIT, totalCount);
        } else {
            param.put(Constants.PAGE_LIMIT, limit);
        }
        try {
            List<User> users = userService.listAllUser(param);
            retJson = assembleSuccResponseWithPage(retJson, JsonUtils.parseObject(users), page);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            retJson = assembleFailResponse(retJson, Constants.EXCEPTION_CODE, Constants.EXCEPTION_DESC, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping("updateUser")
    public JSONObject updateUser(User user) {
        JSONObject retJson = new JSONObject();
        try {
            Subject subject = SecurityUtils.getSubject();
            if (!subject.isPermitted("user:update")) {
                return assembleFailResponse(retJson, 500, "no permission", "");
            }
            if (!StringUtils.isEmpty(user.getPassword())) {
                user.setPassword(MD5Util.encode(user.getPassword()));
            }
            CommonResult result = userService.updateUser(user);
            retJson = assembleResponse(retJson, result.getStatus(), result.getMsg(), "");
        } catch (Exception e) {
            logger.error("error happens : {}", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "getUserById", method = RequestMethod.GET)
    public JSONObject getUserById(@RequestParam Long userId) {
        JSONObject retJson = new JSONObject();
        try {
            User user = userService.findUserById(userId);
            retJson = assembleSuccessResponse(retJson, JsonUtils.fromObject(user));
        } catch (Exception e) {
            logger.error(" error happens : ", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "deleteUser", method = RequestMethod.GET)
    public JSONObject deleteUser(@RequestParam Long userId) {
        JSONObject retJson = new JSONObject();
        try {
            Subject subject = SecurityUtils.getSubject();
            if (!subject.isPermitted("user:delete")) {
                return assembleFailResponse(retJson, 500, "no permission", "");
            }
            User user = userService.findUserById(userId);
            user.setIsDeleted(Boolean.TRUE);
            CommonResult res = userService.updateUser(user);
            retJson = assembleResponse(retJson, res.getStatus(), res.getMsg(), "");
        } catch (Exception e) {
            logger.error(" delete user happens : ", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "checkUser", method = RequestMethod.GET)
    public JSONObject checkUser(@RequestParam Long userId,
                                @RequestParam Byte checkStatus) {
        JSONObject retJson = new JSONObject();
        try {
            Subject subject = SecurityUtils.getSubject();
            if (!subject.isPermitted("user:check")) {
                return assembleFailResponse(retJson, 500, "no permission", "");
            }
            User user = userService.findUserById(userId);
            user.setCheckStatus(checkStatus);
            CommonResult result = userService.updateUser(user);
            if (result.isSuccess()) {
                try {
                    //记录日志
                    logService.write(ManagerConstants.MODULE_CHECK, user.getUserId().toString(),
                            ManagerConstants.OPT_TYPE_UPDATE, Constants.CHECK_STATUS.get(user.getCheckStatus()));
                    //发消息通知
                    if (checkStatus == Constants.CHECK_STATUS_CODE.get("审核未通过").intValue()) {
                        msgService.send(userId, Constants.MSG_TYPE.get("system"),
                                "根据您添加的证件照片，我们无法确认您的真实身份。为了帮助我们确认您的真实身份，请上传有效的身份证件照片",
                                Constants.MSG_ACTION.get("上传实名认证"));
                    } else if (checkStatus == Constants.CHECK_STATUS_CODE.get("已认证").intValue()) {
                        msgService.send(userId, Constants.MSG_TYPE.get("system"),
                                "实名认证已通过，现在可以进行投标了",
                                Constants.MSG_ACTION.get("无跳转"));
                        //记录入驻信息
                        userEnteringService.insert((String) SecurityUtils.getSubject().getPrincipal(), userId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            retJson = assembleResponse(retJson, result.getStatus(), result.getMsg(), "");
        } catch (Exception e) {
            logger.error("error happens : {}", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }
}
