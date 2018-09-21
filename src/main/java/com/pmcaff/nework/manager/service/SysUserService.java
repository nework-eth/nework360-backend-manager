package com.pmcaff.nework.manager.service;

import com.pmcaff.nework.core.common.CommonResult;
import com.pmcaff.nework.core.utils.JsonUtils;
import com.pmcaff.nework.manager.common.ManagerConstants;
import com.pmcaff.nework.manager.domain.SysRole;
import com.pmcaff.nework.manager.domain.SysUser;
import com.pmcaff.nework.manager.domain.SysUserRole;
import com.pmcaff.nework.manager.mapper.SysRoleMapper;
import com.pmcaff.nework.manager.mapper.SysUserMapper;
import com.pmcaff.nework.manager.mapper.SysUserRoleMapper;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;

@Service
public class SysUserService {

    @Resource
    OptLogService logService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysRoleMapper roleMapper;
    @Resource
    private SysUserRoleMapper userRoleMapper;
    @Resource
    private SysRoleService roleService;

    public List<SysUser> listUserByParam(Map<String, Object> paramMap) {
        return sysUserMapper.listUserByParam(paramMap);
    }

    public SysUser findByUsername(String username) {
        return sysUserMapper.findByUsername(username);
    }

    public SysUser selectByPrimaryKey(Long id) {
        return sysUserMapper.selectByPrimaryKey(id);
    }

    public boolean varifyUser(String name, String pwd) {
        UsernamePasswordToken token = new UsernamePasswordToken(name, pwd);
//        token.setRememberMe(true);
        // 获取当前的Subject
        Subject currentUser = SecurityUtils.getSubject();
        try {
            logger.debug("对用户[" + name + "]进行登录验证..验证开始");
            currentUser.login(token);
            logger.debug("对用户[" + name + "]进行登录验证..验证通过");
        } catch (UnknownAccountException uae) {
            logger.debug("对用户[" + name + "]进行登录验证..验证未通过,未知账户");
        } catch (IncorrectCredentialsException ice) {
            logger.debug("对用户[" + name + "]进行登录验证..验证未通过,错误的凭证");
        } catch (LockedAccountException lae) {
            logger.debug("对用户[" + name + "]进行登录验证..验证未通过,账户已锁定");
        } catch (ExcessiveAttemptsException eae) {
            logger.debug("对用户[" + name + "]进行登录验证..验证未通过,错误次数过多");
        } catch (AuthenticationException ae) {
            // 通过处理Shiro的运行时AuthenticationException就可以控制用户登录失败或密码错误时的情景
            logger.debug("对用户[" + name + "]进行登录验证..验证未通过,堆栈轨迹如下");
        }
        if (currentUser.isAuthenticated()) {
            logger.debug("用户[" + name + "]登录认证通过(这里可以进行一些认证通过后的一些系统参数初始化操作)");
            Session session = currentUser.getSession();
            session.setAttribute(ManagerConstants.CURRENT_USER, name);
            return true;
        } else {
            token.clear();
            return false;
        }
    }

    public JSONArray getRolesByUsers(List<SysUser> users) {
        if (users == null || users.size() == 0) {
            return new JSONArray();
        }
        List<JSONObject> userList = new ArrayList<>();
        for (SysUser user : users) {
            JSONObject data = JsonUtils.fromObject(user);
            data.put("roles", getUserRoles(user));
            userList.add(JsonUtils.fromObject(data));
        }
        return JsonUtils.parseObject(userList);
    }

    public JSONArray getUserRoles(SysUser user) {
        if (user == null) {
            return new JSONArray();
        }
        List<Long> roleIdList = userRoleMapper.getRoleIdsByUserId(user.getId());
        Set<SysRole> roleSet = new LinkedHashSet<>();
        for (Long id : roleIdList) {
            roleSet.add(roleMapper.selectByPrimaryKey(id));
        }
        if (roleSet == null || roleSet.size() == 0) {
            return null;
        }
        return JsonUtils.parseObject(roleSet);
    }

    public Set<SysRole> getRoles(SysUser user) {
        if (user == null) {
            return new LinkedHashSet<>();
        }
        List<Long> roleIdList = userRoleMapper.getRoleIdsByUserId(user.getId());
        Set<SysRole> roleSet = new LinkedHashSet<>();
        for (Long id : roleIdList) {
            roleSet.add(roleMapper.selectByPrimaryKey(id));
        }
        return roleSet;
    }

    public JSONObject getUserRoleAndPerms(SysUser user) {
        if (user == null) {
            return new JSONObject();
        }
        Map<String, JSONArray> rolePermMap = new HashMap<>();
        Set<SysRole> roles = getRoles(user);
        for (SysRole role : roles) {
            rolePermMap.put(role.getRoleName(), roleService.getRolePerms(role));
        }
        return JsonUtils.fromObject(rolePermMap);
    }

    public Set<String> getUserMenuPermsName(SysUser user) {
        if (user == null) {
            return new LinkedHashSet();
        }
        Set<String> menuPerms = new LinkedHashSet<>();
        Set<SysRole> roles = getRoles(user);
        for (SysRole role : roles) {
            Set<String> perms = roleService.getRoleMenuPermsName(role);
            if (perms != null && perms.size() > 0) {
                menuPerms.addAll(perms);
            }
        }
        return menuPerms;
    }

    public Set<String> getUserPermsCode(SysUser user) {
        if (user == null) {
            return new LinkedHashSet();
        }
        Set<String> permCodes = new LinkedHashSet<>();
        Set<SysRole> roles = getRoles(user);
        for (SysRole role : roles) {
            Set<String> codes = roleService.getRolePermissionCodes(role);
            if (codes != null && codes.size() > 0) {
                permCodes.addAll(codes);
            }
        }
        return permCodes;
    }

    public CommonResult insertSelective(SysUser record) {
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        int effect = sysUserMapper.insertSelective(record);
        if (effect > 0) {
            logService.write(ManagerConstants.MODULE_SYS_USER, record.getId().toString(),
                    ManagerConstants.OPT_TYPE_ADD, JsonUtils.fromObject(record).toString());
            return CommonResult.ok();
        } else {
            return CommonResult.fail();
        }
    }

    public CommonResult updateByPrimaryKeySelective(SysUser record) {
        record.setUpdateTime(new Date());
        int effect = sysUserMapper.updateByPrimaryKeySelective(record);
        if (effect > 0) {
            logService.write(ManagerConstants.MODULE_SYS_USER, record.getId().toString(),
                    ManagerConstants.OPT_TYPE_UPDATE, JsonUtils.fromObject(record).toString());
            return CommonResult.ok();
        } else {
            return CommonResult.fail();
        }
    }

    public CommonResult update(SysUser record, String roleIds) {
        record.setUpdateTime(new Date());
        int effect = sysUserMapper.updateByPrimaryKeySelective(record);
        if (roleIds != null) {
            deleteAllRolesForUser(record.getId().toString());
            if (!StringUtils.isEmpty(roleIds)) {
                String[] ids = roleIds.split(",");
                SysUserRole userRole = new SysUserRole();
                for (String id : ids) {
                    userRole.setSysUserId(record.getId());
                    userRole.setSysRoleId(Long.parseLong(id));
                    userRole.setCreateTime(new Date());
                    userRole.setUpdateTime(new Date());
                    userRoleMapper.insertSelective(userRole);
                }
            }
        }
        if (effect > 0) {
            logService.write(ManagerConstants.MODULE_SYS_USER, record.getId().toString(),
                    ManagerConstants.OPT_TYPE_UPDATE, "给该用户添加id为" + roleIds + "的角色");
            return CommonResult.ok();
        } else {
            return CommonResult.fail();
        }
    }

    public CommonResult addRoleForUser(Long userId, Long roleId) {
        SysUserRole userRole = new SysUserRole();
        userRole.setSysUserId(userId);
        userRole.setSysRoleId(roleId);
        userRole.setUpdateTime(new Date());
        int effect = userRoleMapper.insertSelective(userRole);
        if (effect > 0) {
            logService.write(ManagerConstants.MODULE_SYS_USER, roleId.toString(),
                    ManagerConstants.OPT_TYPE_ADD, "给该用户添加id为" + roleId + "的角色");
            return CommonResult.ok();
        } else {
            return CommonResult.fail();
        }
    }

    public CommonResult deleteRoleForUser(String userId, String roleId) {
        Map<String, String> param = new HashMap<>();
        param.put("userId", userId);
        param.put("roleId", roleId);
        int effect = userRoleMapper.deleteRoleForUser(param);
        if (effect > 0) {
            userRoleMapper.deleteByUserId(Long.parseLong(userId));
            logService.write(ManagerConstants.MODULE_SYS_USER, roleId.toString(),
                    ManagerConstants.OPT_TYPE_DELETE, "给该用户删除id为" + roleId + "的角色");
            return CommonResult.ok();
        } else {
            return CommonResult.fail();
        }
    }

    private CommonResult deleteAllRolesForUser(String userId) {
        Map<String, String> param = new HashMap<>();
        param.put("userId", userId);
        int effect = userRoleMapper.deleteRoleForUser(param);
        if (effect > 0) {
            logService.write(ManagerConstants.MODULE_SYS_USER, userId,
                    ManagerConstants.OPT_TYPE_DELETE, "删除该用户的所有角色");
            return CommonResult.ok();
        } else {
            return CommonResult.fail();
        }
    }

    public CommonResult deleteByPrimaryKey(Long id) {
        SysUser record = sysUserMapper.selectByPrimaryKey(id);
        int effect = sysUserMapper.deleteByPrimaryKey(id);
        if (effect > 0) {
            logService.write(ManagerConstants.MODULE_SYS_USER, id.toString(),
                    ManagerConstants.OPT_TYPE_DELETE, JsonUtils.fromObject(record).toString());
            return CommonResult.ok();
        } else {
            return CommonResult.fail();
        }
    }

    public boolean checkUserName(SysUser user) {
        SysUser u = sysUserMapper.findByUsername(user.getUsername());
        if (user.getId() == null) {
            return u != null;
        }
        if (u == null) {
            return false;
        } else {
            return u.getId() != user.getId();
        }
    }
}
