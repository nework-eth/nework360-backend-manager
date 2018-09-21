package com.pmcaff.nework.manager.service;

import com.pmcaff.nework.core.common.CommonResult;
import com.pmcaff.nework.core.utils.JsonUtils;
import com.pmcaff.nework.manager.common.ManagerConstants;
import com.pmcaff.nework.manager.domain.SysPermission;
import com.pmcaff.nework.manager.domain.SysRole;
import com.pmcaff.nework.manager.domain.SysRolePermission;
import com.pmcaff.nework.manager.mapper.SysPermissionMapper;
import com.pmcaff.nework.manager.mapper.SysRoleMapper;
import com.pmcaff.nework.manager.mapper.SysRolePermissionMapper;
import com.pmcaff.nework.manager.mapper.SysUserRoleMapper;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
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
public class SysRoleService {
    @Resource
    OptLogService logService;
    @Resource
    private SysRoleMapper roleMapper;
    @Resource
    private SysRolePermissionMapper rolePermissionMapper;
    @Resource
    private SysUserRoleMapper userRoleMapper;
    @Resource
    private SysPermissionMapper permissionMapper;

    public List<SysRole> listRoleByParam(Map<String, Object> paramMap) {
        return roleMapper.listRoleByParam(paramMap);
    }

    public List<String> listAllUsedRoleNames() {
        return roleMapper.listAllUsedRoleNames();
    }

    public SysRole selectByPrimaryKey(Long id) {
        return roleMapper.selectByPrimaryKey(id);
    }

    public boolean checkRoleName(SysRole role) {
        SysRole r = roleMapper.findByRoleName(role.getRoleName());
        if (role.getRoleId() == null) {
            return r != null;
        }
        if (r == null) {
            return false;
        } else {
            return r.getRoleId() != role.getRoleId();
        }
    }

    public JSONArray getPermsByRoles(List<SysRole> roles) {
        if (roles == null || roles.size() == 0) {
            return new JSONArray();
        }
        List<JSONObject> roleList = new ArrayList<>();
        for (SysRole role : roles) {
            JSONObject data = JsonUtils.fromObject(role);
            data.put("permissions", getRolePerms(role));
            roleList.add(data);
        }
        return JsonUtils.parseObject(roleList);
    }

    public Set<SysPermission> getPermsByRole(SysRole role) {
        if (role == null) {
            return new LinkedHashSet<>();
        }
        List<Long> permIdList = new ArrayList<>();
        if (ManagerConstants.SUPER_ADMIN.equals(role.getRoleName())) {
            for (SysPermission p : permissionMapper.listAllUsedPermission()) {
                permIdList.add(p.getId());
            }
        } else {
            permIdList = rolePermissionMapper.getPermIdsByRoleId(role.getRoleId());
        }
        Set<SysPermission> permSet = new LinkedHashSet<>();
        for (Long id : permIdList) {
            permSet.add(permissionMapper.selectByPrimaryKey(id));
        }
        if (permSet == null || permSet.size() == 0) {
            return null;
        }
        return permSet;
    }

    public JSONArray getRolePerms(SysRole role) {
        if (role == null) {
            return new JSONArray();
        }
        List<Long> permIdList = new ArrayList<>();
        if (ManagerConstants.SUPER_ADMIN.equals(role.getRoleName())) {
            for (SysPermission p : permissionMapper.listAllUsedPermission()) {
                permIdList.add(p.getId());
            }
        } else {
            permIdList = rolePermissionMapper.getPermIdsByRoleId(role.getRoleId());
        }
        Set<SysPermission> permSet = new LinkedHashSet<>();
        for (Long id : permIdList) {
            permSet.add(permissionMapper.selectByPrimaryKey(id));
        }
        if (permSet == null || permSet.size() == 0) {
            return null;
        }
        return JsonUtils.parseObject(permSet);
    }

    public Set<String> getRoleMenuPermsName(SysRole role) {
        if (role == null) {
            return new LinkedHashSet<>();
        }
        List<Long> permIdList = new ArrayList<>();
        if (ManagerConstants.SUPER_ADMIN.equals(role.getRoleName())) {
            for (SysPermission p : permissionMapper.listAllUsedPermission()) {
                permIdList.add(p.getId());
            }
        } else {
            permIdList = rolePermissionMapper.getPermIdsByRoleId(role.getRoleId());
        }
        Set<String> permSet = new LinkedHashSet<>();
        for (Long id : permIdList) {
            SysPermission permission = permissionMapper.selectByPrimaryKey(id);
            if (permission != null && "menu".equals(permission.getType()) && !StringUtils.isEmpty(permission.getName())) {
                permSet.add(permission.getName());
            }
        }
        if (permSet == null || permSet.size() == 0) {
            return null;
        }
        return permSet;
    }

    public Set<String> getRolePermissionCodes(SysRole role) {
        if (role == null) {
            return new LinkedHashSet<>();
        }
        List<Long> permIdList = rolePermissionMapper.getPermIdsByRoleId(role.getRoleId());
        Set<String> permSet = new LinkedHashSet<>();
        for (Long id : permIdList) {
            SysPermission permission = permissionMapper.selectByPrimaryKey(id);
            if ("1".equals(permission.getAvailable()) && !StringUtils.isEmpty(permission.getPercode())) {
                permSet.add(permission.getPercode());
            }
        }
        return permSet;
    }

    public CommonResult insertSelective(SysRole record) {
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        int effect = roleMapper.insertSelective(record);
        if (effect > 0) {
            logService.write(ManagerConstants.MODULE_SYS_ROLE, record.getRoleId().toString(),
                    ManagerConstants.OPT_TYPE_ADD, JsonUtils.fromObject(record).toString());
            return CommonResult.ok();
        } else {
            return CommonResult.fail();
        }
    }

    public CommonResult updateByPrimaryKeySelective(SysRole record) {
        record.setUpdateTime(new Date());
        int effect = roleMapper.updateByPrimaryKeySelective(record);
        if (effect > 0) {
            logService.write(ManagerConstants.MODULE_SYS_ROLE, record.getRoleId().toString(),
                    ManagerConstants.OPT_TYPE_UPDATE, JsonUtils.fromObject(record).toString());
            return CommonResult.ok();
        } else {
            return CommonResult.fail();
        }
    }

    public CommonResult update(SysRole record, String permIds) {
        record.setUpdateTime(new Date());
        int effect = roleMapper.updateByPrimaryKeySelective(record);
        if (permIds != null) {
            deleteAllPermsForRole(record.getRoleId().toString());
            if (!StringUtils.isEmpty(permIds)) {
                String[] ids = permIds.split(",");
                SysRolePermission rolePermission = new SysRolePermission();
                for (String id : ids) {
                    rolePermission.setSysRoleId(record.getRoleId());
                    rolePermission.setSysPermissionId(Long.parseLong(id));
                    rolePermissionMapper.insertSelective(rolePermission);
                }
            }
        }
        if (effect > 0) {
            logService.write(ManagerConstants.MODULE_SYS_ROLE, record.getRoleId().toString(),
                    ManagerConstants.OPT_TYPE_UPDATE, "给该角色添加id为" + permIds + "的权限");
            return CommonResult.ok();
        } else {
            return CommonResult.fail();
        }
    }

    public CommonResult addPermForRole(Long roleId, Long permId) {
        SysRolePermission rolePermission = new SysRolePermission();
        rolePermission.setSysRoleId(roleId);
        rolePermission.setSysPermissionId(permId);
        rolePermission.setCreateTime(new Date());
        rolePermission.setUpdateTme(new Date());
        int effect = rolePermissionMapper.insertSelective(rolePermission);
        if (effect > 0) {
            logService.write(ManagerConstants.MODULE_SYS_ROLE, roleId.toString(),
                    ManagerConstants.OPT_TYPE_ADD, "给该角色添加id为" + permId + "的权限");
            return CommonResult.ok();
        } else {
            return CommonResult.fail();
        }
    }

    public CommonResult deletePermForRole(String roleId, String permId) {
        Map<String, String> param = new HashMap<>();
        param.put("roleId", roleId);
        param.put("permId", permId);
        int effect = rolePermissionMapper.deletePermForRole(param);
        if (effect > 0) {
            logService.write(ManagerConstants.MODULE_SYS_ROLE, roleId.toString(),
                    ManagerConstants.OPT_TYPE_DELETE, "给该角色删除id为" + permId + "的权限");
            return CommonResult.ok();
        } else {
            return CommonResult.fail();
        }
    }

    public CommonResult deleteAllPermsForRole(String roleId) {
        Map<String, String> param = new HashMap<>();
        param.put("roleId", roleId);
        int effect = rolePermissionMapper.deletePermForRole(param);
        if (effect > 0) {
            logService.write(ManagerConstants.MODULE_SYS_ROLE, roleId.toString(),
                    ManagerConstants.OPT_TYPE_DELETE, "删除该角色的所有权限");
            return CommonResult.ok();
        } else {
            return CommonResult.fail();
        }
    }

    public CommonResult deleteByPrimaryKey(Long id) {
        SysRole record = roleMapper.selectByPrimaryKey(id);
        int effect = roleMapper.deleteByPrimaryKey(id);
        if (effect > 0) {
            userRoleMapper.deleteByRoleId(id);
            rolePermissionMapper.deleteByRoleId(id);
            logService.write(ManagerConstants.MODULE_SYS_ROLE, record.getRoleId().toString(),
                    ManagerConstants.OPT_TYPE_ADD, JsonUtils.fromObject(record).toString());
            return CommonResult.ok();
        } else {
            return CommonResult.fail();
        }
    }
}