package com.pmcaff.nework.manager.controller.sys;

import com.pmcaff.nework.core.common.CommonResult;
import com.pmcaff.nework.core.common.Constants;
import com.pmcaff.nework.core.common.Page;
import com.pmcaff.nework.core.controller.BaseController;
import com.pmcaff.nework.core.utils.JsonUtils;
import com.pmcaff.nework.core.utils.MD5Util;
import com.pmcaff.nework.manager.common.ManagerConstants;
import com.pmcaff.nework.manager.domain.SysPermission;
import com.pmcaff.nework.manager.domain.SysRole;
import com.pmcaff.nework.manager.domain.SysUser;
import com.pmcaff.nework.manager.service.SysPermissionService;
import com.pmcaff.nework.manager.service.SysRoleService;
import com.pmcaff.nework.manager.service.SysUserService;
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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/sysUser", method = RequestMethod.POST)
public class SysUserController extends BaseController {
    @Resource
    SysUserService sysUserService;
    @Resource
    SysRoleService sysRoleService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private SysPermissionService permissionService;

    @RequestMapping(value = "/listUserWithRole", method = RequestMethod.GET)
    public JSONObject listUserWithRole(@RequestParam(required = false) String username,
                                       @RequestParam(required = false) String locked,
                                       @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                       @RequestParam(required = false, defaultValue = "0") Integer start,
                                       @RequestParam(required = false, defaultValue = "20") Integer limit) {
        JSONObject retJson = new JSONObject();
        Map<String, Object> param = new HashedMap();
        param.put("username", username);
        param.put("locked", locked);
        param.put(Constants.PAGE_NUMBER, pageNum);
        param.put(Constants.PAGE_START, start);
        int totalCount = sysUserService.listUserByParam(param).size();
        Page page = assemblePage(pageNum, start, limit, totalCount);
        if (limit == -1) {
            param.put(Constants.PAGE_LIMIT, totalCount);
        } else {
            param.put(Constants.PAGE_LIMIT, limit);
        }
        try {
            List<SysUser> users = sysUserService.listUserByParam(param);
            retJson = assembleSuccResponseWithPage(retJson, sysUserService.getRolesByUsers(users), page);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            retJson = assembleFailResponse(retJson, Constants.EXCEPTION_CODE, Constants.EXCEPTION_DESC, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "getUserById", method = RequestMethod.GET)
    public JSONObject getUserById(@RequestParam Long userId) {
        JSONObject retJson = new JSONObject();
        try {
            SysUser user = sysUserService.selectByPrimaryKey(userId);
            if (user == null) {
                return assembleFailResponse(retJson, "用户不存在");
            }
            retJson = assembleSuccessResponse(retJson, JsonUtils.fromObject(user));
        } catch (Exception e) {
            logger.error(" error happens : ", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "getUserPerms", method = RequestMethod.GET)
    public JSONObject getUserPerms() {
        JSONObject retJson = new JSONObject();
        try {
            String username = (String) SecurityUtils.getSubject().getPrincipal();
            SysUser user = sysUserService.findByUsername(username);
            Set<SysRole> roles = sysUserService.getRoles(user);
            Set<SysPermission> perms = new LinkedHashSet<>();
            for (SysRole role : roles) {
                //超级管理员返回所有权限
                if (ManagerConstants.SUPER_ADMIN.equals(role.getRoleName())) {
                    List<SysPermission> ps = permissionService.listAllUsedPermission();
                    perms.addAll(ps);
                    break;
                }
                perms.addAll(sysRoleService.getPermsByRole(role));
            }
            retJson = assembleSuccessResponse(retJson, JsonUtils.parseObject(perms));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            retJson = assembleFailResponse(retJson, Constants.EXCEPTION_CODE, Constants.EXCEPTION_DESC, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping("addUser")
    public JSONObject addUser(SysUser user) {
        JSONObject retJson = new JSONObject();
        try {
            Subject subject = SecurityUtils.getSubject();
            if (!subject.isPermitted("sysUser:add")) {
                return assembleFailResponse(retJson, 500, "no permission", "");
            }
            if (!StringUtils.isEmpty(user.getUsername()) && sysUserService.checkUserName(user)) {
                return assembleFailResponse(retJson, 500, "用户名已经存在！", "");
            }
            if (!StringUtils.isEmpty(user.getPassword())) {
                user.setPassword(MD5Util.encode(user.getPassword()));
            }
            CommonResult result = sysUserService.insertSelective(user);
            retJson = assembleResponse(retJson, result.getStatus(), result.getMsg(), "");
        } catch (Exception e) {
            logger.error("error happens : {}", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping("updateUser")
    public JSONObject updateUser(SysUser user, String roleIds) {
        JSONObject retJson = new JSONObject();
        try {
            Subject subject = SecurityUtils.getSubject();
            if (!subject.isPermitted("sysUser:update")) {
                return assembleFailResponse(retJson, 500, "no permission", "");
            }
            if (!StringUtils.isEmpty(user.getUsername()) && sysUserService.checkUserName(user)) {
                return assembleFailResponse(retJson, 500, "用户名已经存在！", "");
            }
            if (!StringUtils.isEmpty(user.getPassword())) {
                user.setPassword(MD5Util.encode(user.getPassword()));
            }
            CommonResult result = sysUserService.update(user, roleIds);
            retJson = assembleResponse(retJson, result.getStatus(), result.getMsg(), "");
        } catch (Exception e) {
            logger.error("error happens : {}", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }

    @RequiresPermissions("sysUser:delete")
    @RequestMapping(value = "deleteUser", method = RequestMethod.GET)
    public JSONObject deleteUser(@RequestParam Long userId) {
        JSONObject retJson = new JSONObject();
        try {
            Subject subject = SecurityUtils.getSubject();
            if (!subject.isPermitted("sysUser:delete")) {
                return assembleFailResponse(retJson, 500, "no permission", "");
            }
            CommonResult res = sysUserService.deleteByPrimaryKey(userId);
            retJson = assembleResponse(retJson, res.getStatus(), res.getMsg(), "");
        } catch (Exception e) {
            logger.error(" delete user happens : ", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "deleteRoleForUser", method = RequestMethod.GET)
    public JSONObject deleteRoleForUser(@RequestParam String userId, @RequestParam String roleId) {
        JSONObject retJson = new JSONObject();
        try {
            CommonResult res = sysUserService.deleteRoleForUser(userId, roleId);
            retJson = assembleResponse(retJson, res.getStatus(), res.getMsg(), "");
        } catch (Exception e) {
            logger.error(" delete user happens : ", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }

    @RequestMapping(value = "addRoleForUser", method = RequestMethod.GET)
    public JSONObject addRoleForUser(@RequestParam Long userId, @RequestParam Long roleId) {
        JSONObject retJson = new JSONObject();
        try {
            CommonResult res = sysUserService.addRoleForUser(userId, roleId);
            retJson = assembleResponse(retJson, res.getStatus(), res.getMsg(), "");
        } catch (Exception e) {
            logger.error(" delete user happens : ", e);
            retJson = assembleFailResponse(retJson, e.getMessage());
        }
        return retJson;
    }
}
