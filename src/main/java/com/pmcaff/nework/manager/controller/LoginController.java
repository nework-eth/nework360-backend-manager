package com.pmcaff.nework.manager.controller;

import com.pmcaff.nework.core.common.Constants;
import com.pmcaff.nework.core.controller.BaseController;
import com.pmcaff.nework.manager.domain.SysUser;
import com.pmcaff.nework.manager.service.SysUserService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by megagao on 2018/6/2.
 */
@RestController
public class LoginController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private SysUserService sysUserService;

    @RequestMapping("/login")
    public JSONObject login(@RequestParam String username,
                            @RequestParam String password,
                            HttpServletRequest request) {
        JSONObject retJson = new JSONObject();
        try {
            if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
                return assembleFailResponse(retJson, 500, "parameter is empty", "");
            }
            if (sysUserService.varifyUser(username, password)) {
                SysUser user = sysUserService.findByUsername(username);
                JSONObject data = new JSONObject();
                data.put("token", request.getSession().getId());
                data.put("id", user.getId());
                data.put("username", user.getUsername());
                data.put("menus", sysUserService.getUserMenuPermsName(user));
                retJson = assembleSuccessResponse(retJson, data);
            } else {
                retJson = assembleFailResponse(retJson);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            retJson = assembleFailResponse(retJson, Constants.EXCEPTION_CODE, Constants.EXCEPTION_DESC, e.getMessage());
        }
        return retJson;
    }

    /**
     * 未登录，shiro应重定向到登录界面，此处返回未登录状态信息由前端控制跳转页面
     *
     * @return
     */
    @RequestMapping(value = "/unauth")
    public Object unauth() {
        Map<String, Object> map = new HashMap<>();
        map.put("code", "1000000");
        map.put("msg", "未登录");
        return map;
    }
}
