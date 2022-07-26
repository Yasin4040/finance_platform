package com.jtyjy.finance.manager.dto;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: gll
 * @create: 2021-12-15 17:02
 */
public class ReimBursementDTO {

    @ApiModelProperty(value = "登录唯一标识",required = true)
    private String token;

    @ApiModelProperty(value = "是否预算员",required = true)
    private Boolean budgeterflag;

    @ApiModelProperty("报销单号")
    private String reimcode;

    @ApiModelProperty("报销单状态")
    private Long reuqeststatus;

    @ApiModelProperty("界别")
    private Long yearid;

    @ApiModelProperty("月份")
    private Long monthid;

    @ApiModelProperty("预算单位名称（模糊查询）")
    private String ysdw;

    @ApiModelProperty("报销人（模糊查询）")
    private String bxr;

    @ApiModelProperty("报销日期（yyyy-mm-dd）")
    private String bxrq;

    @ApiModelProperty("报销金额")
    private Double bxje;

    @ApiModelProperty("冲账金额")
    private Double czje;

    @ApiModelProperty("转账金额")
    private Double zzje;

    @ApiModelProperty("现金金额")
    private Double xjje;

    @ApiModelProperty("划拨金额")
    private Double hbje;

    @ApiModelProperty("其他金额")
    private Double othermoney;

    @ApiModelProperty("提交日期（yyyy-mm-dd）")
    private String submittime;

    @ApiModelProperty("申请日期（yyyy-mm-dd）")
    private String applicanttime;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getBudgeterflag() {
        return budgeterflag;
    }

    public void setBudgeterflag(Boolean budgeterflag) {
        this.budgeterflag = budgeterflag;
    }

    public String getReimcode() {
        return reimcode;
    }

    public void setReimcode(String reimcode) {
        this.reimcode = reimcode;
    }

    public Long getReuqeststatus() {
        return reuqeststatus;
    }

    public void setReuqeststatus(Long reuqeststatus) {
        this.reuqeststatus = reuqeststatus;
    }

    public Long getYearid() {
        return yearid;
    }

    public void setYearid(Long yearid) {
        this.yearid = yearid;
    }

    public Long getMonthid() {
        return monthid;
    }

    public void setMonthid(Long monthid) {
        this.monthid = monthid;
    }

    public String getYsdw() {
        return ysdw;
    }

    public void setYsdw(String ysdw) {
        this.ysdw = ysdw;
    }

    public String getBxr() {
        return bxr;
    }

    public void setBxr(String bxr) {
        this.bxr = bxr;
    }

    public String getBxrq() {
        return bxrq;
    }

    public void setBxrq(String bxrq) {
        this.bxrq = bxrq;
    }

    public Double getBxje() {
        return bxje;
    }

    public void setBxje(Double bxje) {
        this.bxje = bxje;
    }

    public Double getCzje() {
        return czje;
    }

    public void setCzje(Double czje) {
        this.czje = czje;
    }

    public Double getZzje() {
        return zzje;
    }

    public void setZzje(Double zzje) {
        this.zzje = zzje;
    }

    public Double getXjje() {
        return xjje;
    }

    public void setXjje(Double xjje) {
        this.xjje = xjje;
    }

    public Double getHbje() {
        return hbje;
    }

    public void setHbje(Double hbje) {
        this.hbje = hbje;
    }

    public Double getOthermoney() {
        return othermoney;
    }

    public void setOthermoney(Double othermoney) {
        this.othermoney = othermoney;
    }

    public String getSubmittime() {
        return submittime;
    }

    public void setSubmittime(String submittime) {
        this.submittime = submittime;
    }

    public String getApplicanttime() {
        return applicanttime;
    }

    public void setApplicanttime(String applicanttime) {
        this.applicanttime = applicanttime;
    }

    public Map<String,Object> toMap(){
        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("reuqeststatus", reuqeststatus);
        conditionMap.put("reimcode", reimcode);
        conditionMap.put("yearid", yearid);
        conditionMap.put("monthid", monthid);
        conditionMap.put("bxr", bxr);
        conditionMap.put("bxrq", bxrq);
        conditionMap.put("bxje", bxje);
        conditionMap.put("czje", czje);
        conditionMap.put("zzje", zzje);
        conditionMap.put("xjje", xjje);
        conditionMap.put("hbje", hbje);
        conditionMap.put("othermoney", othermoney);
        conditionMap.put("ysdw", ysdw);
        conditionMap.put("submittime", submittime);
        conditionMap.put("applicanttime", applicanttime);
        return conditionMap;
    }
}
