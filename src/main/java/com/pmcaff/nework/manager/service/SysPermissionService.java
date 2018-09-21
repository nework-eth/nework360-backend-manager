package com.pmcaff.nework.manager.service;

import com.pmcaff.nework.core.common.CommonResult;
import com.pmcaff.nework.core.utils.JsonUtils;
import com.pmcaff.nework.manager.common.ManagerConstants;
import com.pmcaff.nework.manager.domain.SysPermission;
import com.pmcaff.nework.manager.mapper.SysPermissionMapper;
import com.pmcaff.nework.manager.mapper.SysRolePermissionMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;

@Service
public class SysPermissionService {

    @Resource
    SysPermissionMapper permissionMapper;
    @Resource
    OptLogService logService;
    @Resource
    private SysRolePermissionMapper rolePermissionMapper;

    public List<SysPermission> listPermissionByParam(Map<String, Object> paramMap) {
        return permissionMapper.listPermissionByParam(paramMap);
    }

    public List<SysPermission> listAllUsedPermission() {
        return permissionMapper.listAllUsedPermission();
    }

    public SysPermission selectByPrimaryKey(String id) {
        return permissionMapper.selectByPrimaryKey(Long.parseLong(id));
    }

    public boolean checkPermName(SysPermission perm) {
        SysPermission p = permissionMapper.findByPermName(perm.getName());
        if (perm.getId() == null) {
            return p != null;
        }
        if (p == null) {
            return false;
        } else {
            return p.getId() != perm.getId();
        }
    }

    public CommonResult insertSelective(SysPermission record) {
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        int effect = permissionMapper.insertSelective(record);
        if (effect > 0) {
            logService.write(ManagerConstants.MODULE_SYS_PERM, record.getId().toString(),
                    ManagerConstants.OPT_TYPE_ADD, JsonUtils.fromObject(record).toString());
            return CommonResult.ok();
        } else {
            return CommonResult.fail();
        }
    }

    public CommonResult updateByPrimaryKeySelective(SysPermission record) {
        record.setUpdateTime(new Date());
        int effect = permissionMapper.updateByPrimaryKeySelective(record);
        if (effect > 0) {
            logService.write(ManagerConstants.MODULE_SYS_PERM, record.getId().toString(),
                    ManagerConstants.OPT_TYPE_UPDATE, JsonUtils.fromObject(record).toString());
            return CommonResult.ok();
        } else {
            return CommonResult.fail();
        }
    }

    public CommonResult deleteByPrimaryKey(Long id) {
        SysPermission record = permissionMapper.selectByPrimaryKey(id);
        int effect = permissionMapper.deleteByPrimaryKey(id);
        if (effect > 0) {
            rolePermissionMapper.deleteByPermId(id);
            logService.write(ManagerConstants.MODULE_SYS_PERM, record.getId().toString(),
                    ManagerConstants.OPT_TYPE_DELETE, JsonUtils.fromObject(record).toString());
            return CommonResult.ok();
        } else {
            return CommonResult.fail();
        }
    }
}