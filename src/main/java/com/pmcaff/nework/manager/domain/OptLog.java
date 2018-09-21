package com.pmcaff.nework.manager.domain;

import java.util.Date;

public class OptLog {
    private Long id;

    private String operator;

    private String module;

    private String optType;

    private String dataPk;

    private String optValue;

    private Date optTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator == null ? null : operator.trim();
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module == null ? null : module.trim();
    }

    public String getOptType() {
        return optType;
    }

    public void setOptType(String optType) {
        this.optType = optType == null ? null : optType.trim();
    }

    public String getDataPk() {
        return dataPk;
    }

    public void setDataPk(String dataPk) {
        this.dataPk = dataPk == null ? null : dataPk.trim();
    }

    public String getOptValue() {
        return optValue;
    }

    public void setOptValue(String optValue) {
        this.optValue = optValue == null ? null : optValue.trim();
    }

    public Date getOptTime() {
        return optTime;
    }

    public void setOptTime(Date optTime) {
        this.optTime = optTime;
    }
}