package com.pmcaff.nework.manager.controller.sys;

import com.pmcaff.nework.core.common.CommonResult;
import com.pmcaff.nework.core.common.Constants;
import com.pmcaff.nework.core.common.Page;
import com.pmcaff.nework.core.controller.BaseController;
import com.pmcaff.nework.core.utils.JsonUtils;
import com.pmcaff.nework.manager.domain.SysPermission;
import com.pmcaff.nework.manager.service.SysPermissionService;
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
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/sysPermission", method = RequestMethod.POST)
public class SysPermissionController extends BaseController {

    @Resource
    SysPermissionService permissionService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "listPermissionByParam", method = RequestMethod.GET)
    public JSONObject listPermissionByParam(@RequestParam(required = false) String name,
                                            @RequestParam(required = false) String available,
                                            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                            @RequestParam(required = false, defaultValue = "0") Integer start,
                                            @RequestParam(required = false, defaultValue = "20") Integer limit) {
        JSONObject retJson = new JSONObject();
        Map<String, Object> param = new HashedMap();
        param.put("name", name);
        param.put("available", available);
        param.put(Constants.PAGE_NUMBER, pageNum);
        param.put(Constants.PAGE_START, start);
        int totalCount = permissionService.listPermissionByParam(param).size();
        Page page = assemblePage(pageNum, start, limit, totalCount);
        if (limit == -1) {
            param.put(Constants.PAGE_LIMIT, totalCount);
        } else {
            param.put(Constants.PAGE_LIMIT, limit);
        }
        try {
            List<SysPermission> permissions = permissionService.listPermissionByParam(param);
            retJson = assembleSuccResponseWithPage(retJson, JsonUtils.parseObject(permissions), page);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            retJson = assembleFailResponse(retJson, Constants.EXCEPTION_CODE, Constants.EXCEPTION_DESC, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "getPermissionById", method = RequestMethod.GET)
    public JSONObject getPermissionById(@RequestParam String permissionId) {
        JSONObject retJson = new JSONObject();
        try {
            String operator = (String) SecurityUtils.getSubject().getPrincipal();
            SysPermission permission = permissionService.selectByPrimaryKey(permissionId);
            retJson = assembleSuccessResponse(retJson, JsonUtils.fromObject(permission));
        } catch (Exception e) {
            logger.error(" error happens : ", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping("addPermission")
    public JSONObject addPermission(SysPermission permission) {
        JSONObject retJson = new JSONObject();
        try {
            Subject subject = SecurityUtils.getSubject();
            if (!subject.isPermitted("sysPermission:add")) {
                return assembleFailResponse(retJson, 500, "no permission", "");
            }
            if (!StringUtils.isEmpty(permission.getName()) && permissionService.checkPermName(permission)) {
                return assembleFailResponse(retJson, 500, "权限名已经存在！", "");
            }
            CommonResult result = permissionService.insertSelective(permission);
            retJson = assembleResponse(retJson, result.getStatus(), result.getMsg(), "");
        } catch (Exception e) {
            logger.error("error happens : {}", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping("updatePermission")
    public JSONObject updatePermission(SysPermission permission, HttpServletRequest request) {
        JSONObject retJson = new JSONObject();
        try {
            Subject subject = SecurityUtils.getSubject();
            if (!subject.isPermitted("sysPermission:update")) {
                return assembleFailResponse(retJson, 500, "no permission", "");
            }
            if (!StringUtils.isEmpty(permission.getName()) && permissionService.checkPermName(permission)) {
                return assembleFailResponse(retJson, 500, "权限名已经存在！", "");
            }
            CommonResult result = permissionService.updateByPrimaryKeySelective(permission);
            retJson = assembleResponse(retJson, result.getStatus(), result.getMsg(), "");
        } catch (Exception e) {
            logger.error("error happens : {}", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "deletePermission", method = RequestMethod.GET)
    public JSONObject deletePermission(@RequestParam Long permissionId) {
        JSONObject retJson = new JSONObject();
        try {
            Subject subject = SecurityUtils.getSubject();
            if (!subject.isPermitted("sysPermission:delete")) {
                return assembleFailResponse(retJson, 500, "no permission", "");
            }
            CommonResult res = permissionService.deleteByPrimaryKey(permissionId);
            retJson = assembleResponse(retJson, res.getStatus(), res.getMsg(), "");
        } catch (Exception e) {
            logger.error(" delete permission happens : ", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }
}
