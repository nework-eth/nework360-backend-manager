package com.pmcaff.nework.manager.domain;

import java.util.Date;

public class SysRolePermission {
    private Long id;

    private Long sysRoleId;

    private Long sysPermissionId;

    private Date createTime;

    private Date updateTme;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSysRoleId() {
        return sysRoleId;
    }

    public void setSysRoleId(Long sysRoleId) {
        this.sysRoleId = sysRoleId;
    }

    public Long getSysPermissionId() {
        return sysPermissionId;
    }

    public void setSysPermissionId(Long sysPermissionId) {
        this.sysPermissionId = sysPermissionId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTme() {
        return updateTme;
    }

    public void setUpdateTme(Date updateTme) {
        this.updateTme = updateTme;
    }
}