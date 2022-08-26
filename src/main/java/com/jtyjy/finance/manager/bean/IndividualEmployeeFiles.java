package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 员工个体户档案
 * @TableName budget_individual_employee_files
 */
@TableName(value ="budget_individual_employee_files")
@Data
public class IndividualEmployeeFiles implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 员工工号
     */
    @TableField(value = "employee_job_num")
    private Integer employeeJobNum;

    /**
     * 联系电话

     */
    @TableField(value = "phone")
    private Integer phone;

    /**
     * 账户类型  1个人 2 公户
     */
    @TableField(value = "account_type")
    private Integer accountType;

    /**
     * 户名

     */
    @TableField(value = "account_name")
    private String accountName;

    /**
     * 开户行
     */
    @TableField(value = "deposit_bank")
    private String depositBank;

    /**
     * 发放单位
     */
    @TableField(value = "issued_unit")
    private String issuedUnit;

    /**
     * 发放意见
     */
    @TableField(value = "release_opinions")
    private String releaseOpinions;

    /**
     * 社保停发日期
     */
    @TableField(value = "social_security_stop_date")
    private Date socialSecurityStopDate;

    /**
     * 离职日期
     */
    @TableField(value = "leave_date")
    private Date leaveDate;

    /**
     * 服务协议
     */
    @TableField(value = "service_agreement")
    private String serviceAgreement;

    /**
     * 自办还是代办  1自办 2 代办
     */
    @TableField(value = "self_or_agency")
    private Integer selfOrAgency;

    /**
     * 平台公司
     */
    @TableField(value = "platform_company")
    private String platformCompany;

    /**
     * 核定/查账
     */
    @TableField(value = "verification_audit")
    private String verificationAudit;

    /**
     * 年额度
     */
    @TableField(value = "annual_quota")
    private Integer annualQuota;

    /**
     * 创建人
     */
    @TableField(value = "create_by")
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新人
     */
    @TableField(value = "update_by")
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 状态 1 正常  2停用
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 批次
     */
    @TableField(value = "batch_no")
    private String batchNo;

    /**
     * 部门
     */
    @TableField(value = "department_no")
    private String departmentNo;

    /**
     * 部门名称
     */
    @TableField(value = "department_name")
    private String departmentName;

    /**
     * 省区/大区
     */
    @TableField(value = "province_or_region")
    private String provinceOrRegion;

    /**
     * 员工名称
     */
    @TableField(value = "employee_name")
    private String employeeName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}