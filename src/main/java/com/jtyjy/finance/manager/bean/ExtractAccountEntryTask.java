package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 核算入账
 * @TableName budget_extract_account_entry_task
 */
@TableName(value ="budget_extract_account_entry_task")
@Data
public class ExtractAccountEntryTask implements Serializable {
    /**
     * 
     */
    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 提成id
     */
    @ApiModelProperty(value = "提成id")
    @TableField(value = "sum_id")
    private String sumId;

    /**
     * 提成单号
     */
    @ApiModelProperty(value = "提成单号")
    @TableField(value = "extract_code")
    private String extractCode;

    /**
     * 创建时间（任务接收时间）
     */
    @ApiModelProperty(value = "创建时间（任务接收时间）")
    @TableField(value = "create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 凭证号
     */
    @ApiModelProperty(value = "凭证号")
    @TableField(value = "voucher_no")
    private String voucherNo;

    /**
     * 届别id
     */
    @ApiModelProperty(value = "届别id")
    @TableField(value = "year_id")
    private String yearId;

    /**
     * 届别名称
     */
    @ApiModelProperty(value = "届别名称")
    @TableField(value = "year_name")
    private String yearName;

    /**
     * 月份id
     */
    @ApiModelProperty(value = "月份id")
    @TableField(value = "month_id")
    private String monthId;

    /**
     * 月份名称
     */
    @ApiModelProperty(value = "月份名称")
    @TableField(value = "month_name")
    private String monthName;

    /**
     * 提成批次
     */
    @ApiModelProperty(value = "提成批次")
    @TableField(value = "extract_month")
    private String extractMonth;

    /**
     * 部门id
     */
    @ApiModelProperty(value = "部门id")
    @TableField(value = "dept_id")
    private String deptId;

    /**
     * 部门名称
     */
    @ApiModelProperty(value = "部门名称")
    @TableField(value = "dept_name")
    private String deptName;

//    /**
//     * 预算单位
//     */
//    @ApiModelProperty(value = "预算单位id")
//    @TableField(value = "billing_unit_id")
//    private Long billingUnitId;
//
//    /**
//     * 预算单位名称
//     */
//    @ApiModelProperty(value = "预算单位名称")
//    @TableField(value = "billing_unit_name")
//    private String billingUnitName;

    /**
     * 发放金额
     */
    @ApiModelProperty(value = "发放金额")
    @TableField(value = "issued_amount")
    private BigDecimal issuedAmount;

    /**
     * 实际做账人(工号)
     */
    @ApiModelProperty(value = "实际做账人(工号)")
    @TableField(value = "accountant_emp_no")
    private String accountantEmpNo;

    /**
     * 实际做账人(名称)
     */
    @ApiModelProperty(value = "实际做账人(名称)")
    @TableField(value = "accountant_emp_name")
    private String accountantEmpName;

    /**
     * 做账完成时间
     */
    @ApiModelProperty(value = "做账完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "accountant_time")
    private Date accountantTime;

    /**
     * 状态  0接收 1完成
     */
    @ApiModelProperty(value = "状态  0接收 1完成")
    @TableField(value = "status")
    private Integer status;

    /**
     * 状态  0接收 1完成
     */
    @ApiModelProperty(value = "状态  0接收 1完成")
    @TableField(exist = false)
    private String statusName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}