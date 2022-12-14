package com.jtyjy.finance.manager.dto.individual;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Description: 新增实体类
 * Created by ZiYao Lee on 2022/08/29.
 * Time: 09:23
 */
@Data
@ApiModel(value = "新增实体类")
public class IndividualEmployeeFilesDTO {


    /**
     * 员工工号
     */
    @ApiModelProperty(value = "员工工号")
    private Integer employeeJobNum;

    /**
     * 联系电话

     */
    @ApiModelProperty(value = "联系电话")
    private String phone;

    /**
     * 账户类型  1个人 2 公户
     */
    @ApiModelProperty(value = "账户类型  1个人 2 公户")
    private Integer accountType;

    /**
     * 户名
     */
    @ApiModelProperty(value = "户名")
    private String accountName;

    /**
     * 开户行
     */
    @ApiModelProperty(value = "开户行")
    private String depositBank;

    /**
     * 发放单位
     */
    @ApiModelProperty(value = "发放单位")
    private String issuedUnit;

    /**
     * 发放意见
     */
    @ApiModelProperty(value = "发放意见")
    private String releaseOpinions;

    /**
     * 社保停发日期
     */
    @ApiModelProperty(value = "社保停发日期")
    private Date socialSecurityStopDate;

    /**
     * 离职日期
     */
    @ApiModelProperty(value = "离职日期")
    private Date leaveDate;

    /**
     * 服务协议
     */
    @ApiModelProperty(value = "服务协议")
    private String serviceAgreement;

    /**
     * 自办还是代办  1自办 2 代办
     */
    @ApiModelProperty(value = "自办还是代办  1自办 2 代办")
    private Integer selfOrAgency;

    /**
     * 平台公司
     */
    @ApiModelProperty(value = "platform_company")
    private String platformCompany;

    /**
     * 核定/查账
     */
    @ApiModelProperty(value = "核定/查账")
    private String verificationAudit;

    /**
     * 年额度
     */
    @ApiModelProperty(value = "年额度")
    private BigDecimal annualQuota;



    /**
     * 状态 1 正常  2停用
     */
    @ApiModelProperty(value = "状态 1 正常  2停用")
    private Integer status;

    /**
     * 批次
     */
    @ApiModelProperty(value = "批次")
    private String batchNo;

    /**
     * 部门
     */
    @ApiModelProperty(value = "部门Id")
    private String departmentNo;

    /**
     * 部门名称
     */
    @ApiModelProperty(value = "部门名称")
    private String departmentName;

    /**
     * 部门完整名称
     */
    @ApiModelProperty(value = "部门完整名称")
    private String departmentFullName;

    /**
     * 省区/大区
     */
    @ApiModelProperty(value = "省区/大区")
    private String provinceOrRegion;

    /**
     * 员工名称
     */
    @ApiModelProperty(value = "员工名称")
    private String employeeName;
    /**
     * account 账号
     */
    @ApiModelProperty(value = " account 账号")
    private String account;
    @ApiModelProperty(value = "备注")
    private String remarks;
}
