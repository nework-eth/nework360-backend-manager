package com.pmcaff.nework.manager.controller.sys;

import com.pmcaff.nework.core.common.CommonResult;
import com.pmcaff.nework.core.common.Constants;
import com.pmcaff.nework.core.common.Page;
import com.pmcaff.nework.core.controller.BaseController;
import com.pmcaff.nework.core.utils.JsonUtils;
import com.pmcaff.nework.manager.domain.SysRole;
import com.pmcaff.nework.manager.service.SysRoleService;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
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
@RequestMapping(value = "/sysRole", method = RequestMethod.POST)
public class SysRoleController extends BaseController {
    @Resource
    SysRoleService roleService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/listRoleWithPerm", method = RequestMethod.GET)
    public JSONObject listRoleWithPermByParam(@RequestParam(required = false) String roleName,
                                              @RequestParam(required = false) String available,
                                              @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                              @RequestParam(required = false, defaultValue = "0") Integer start,
                                              @RequestParam(required = false, defaultValue = "20") Integer limit) {
        JSONObject retJson = new JSONObject();
        Map<String, Object> param = new HashedMap();
        param.put("role_name", roleName);
        param.put("available", available);
        param.put(Constants.PAGE_NUMBER, pageNum);
        param.put(Constants.PAGE_START, start);
        int totalCount = roleService.listRoleByParam(param).size();
        Page page = assemblePage(pageNum, start, limit, totalCount);
        if (limit == -1) {
            param.put(Constants.PAGE_LIMIT, totalCount);
        } else {
            param.put(Constants.PAGE_LIMIT, limit);
        }
        try {
            List<SysRole> roles = roleService.listRoleByParam(param);
            retJson = assembleSuccResponseWithPage(retJson, roleService.getPermsByRoles(roles), page);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            retJson = assembleFailResponse(retJson, Constants.EXCEPTION_CODE, Constants.EXCEPTION_DESC, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "getRoleById", method = RequestMethod.GET)
    public JSONObject getRoleById(@RequestParam Long roleId) {
        JSONObject retJson = new JSONObject();
        try {
            SysRole role = roleService.selectByPrimaryKey(roleId);
            retJson = assembleSuccessResponse(retJson, JsonUtils.fromObject(role));
        } catch (Exception e) {
            logger.error(" error happens : ", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }

    @RequiresPermissions("sysRole:add")
    @RequestMapping("addRole")
    public JSONObject addRole(SysRole role, HttpServletRequest request) {
        JSONObject retJson = new JSONObject();
        try {
            Subject subject = SecurityUtils.getSubject();
            if (!subject.isPermitted("sysRole:add")) {
                return assembleFailResponse(retJson, 500, "no permission", "");
            }
            if (!StringUtils.isEmpty(role.getRoleName()) && roleService.checkRoleName(role)) {
                return assembleFailResponse(retJson, 500, "角色名已经存在！", "");
            }
            CommonResult result = roleService.insertSelective(role);
            retJson = assembleResponse(retJson, result.getStatus(), result.getMsg(), "");
        } catch (Exception e) {
            logger.error("error happens : {}", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping("updateRole")
    public JSONObject updateRole(SysRole role, String permIds) {
        JSONObject retJson = new JSONObject();
        try {
            Subject subject = SecurityUtils.getSubject();
            if (!subject.isPermitted("sysRole:update")) {
                return assembleFailResponse(retJson, 500, "no permission", "");
            }
            if (!StringUtils.isEmpty(role.getRoleName()) && roleService.checkRoleName(role)) {
                return assembleFailResponse(retJson, 500, "角色名已经存在！", "");
            }
            CommonResult result = roleService.update(role, permIds);
            retJson = assembleResponse(retJson, result.getStatus(), result.getMsg(), "");
        } catch (Exception e) {
            logger.error("error happens : {}", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "deleteRole", method = RequestMethod.GET)
    public JSONObject deleteRole(@RequestParam Long roleId) {
        JSONObject retJson = new JSONObject();
        try {
            Subject subject = SecurityUtils.getSubject();
            if (!subject.isPermitted("sysRole:delete")) {
                return assembleFailResponse(retJson, 500, "no permission", "");
            }
            CommonResult res = roleService.deleteByPrimaryKey(roleId);
            retJson = assembleResponse(retJson, res.getStatus(), res.getMsg(), "");
        } catch (Exception e) {
            logger.error(" delete role happens : ", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "deletePermForRole", method = RequestMethod.GET)
    public JSONObject deletePermForRole(@RequestParam String roleId, @RequestParam String permId) {
        JSONObject retJson = new JSONObject();
        try {
            CommonResult res = roleService.deletePermForRole(roleId, permId);
            retJson = assembleResponse(retJson, res.getStatus(), res.getMsg(), "");
        } catch (Exception e) {
            logger.error(" delete role happens : ", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "addPermForRole", method = RequestMethod.GET)
    public JSONObject addPermForRole(@RequestParam Long roleId, @RequestParam Long permId) {
        JSONObject retJson = new JSONObject();
        try {
            CommonResult res = roleService.addPermForRole(roleId, permId);
            retJson = assembleResponse(retJson, res.getStatus(), res.getMsg(), "");
        } catch (Exception e) {
            logger.error(" delete role happens : ", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }
}
