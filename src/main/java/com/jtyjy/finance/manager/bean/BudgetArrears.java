package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_arrears_new")
@Data
public class BudgetArrears implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 员工id(外部员工可以为空)
     */
    @ApiModelProperty(value = "员工id(外部员工可以为空)")
    @TableField(value = "empid")
    private String empid;

    /**
     * 工号（或者外部人员为编号）
     */
    @NotBlank(message = "工号（或者外部人员为编号）不能为空")
    @ApiModelProperty(value = "工号")
    @TableField(value = "empno")
    private String empno;

    /**
     * 员工姓名（或者外部人员名称）
     */
    @NotBlank(message = "员工姓名（或者外部人员名称）不能为空")
    @ApiModelProperty(value = "员工姓名（或者外部人员名称）")
    @TableField(value = "empname")
    private String empname;

    /**
     * 欠款金额
     */
    @NotNull(message = "欠款金额不能为空")
    @ApiModelProperty(value = "欠款金额")
    @TableField(value = "arrearsmoeny")
    private BigDecimal arrearsmoeny;

    /**
     * 借款金额
     */
    @NotNull(message = "借款金额不能为空")
    @ApiModelProperty(value = "借款金额")
    @TableField(value = "lendmoney")
    private BigDecimal lendmoney;

    /**
     * 还款金额
     */
    @NotNull(message = "还款金额不能为空")
    @ApiModelProperty(value = "还款金额")
    @TableField(value = "repaymoney")
    private BigDecimal repaymoney;

    /**
     * 利息总额
     */
    @NotNull(message = "利息总额不能为空")
    @ApiModelProperty(value = "利息总额")
    @TableField(value = "interestmoney")
    private BigDecimal interestmoney;

    /**
     * 逾期记录
     */
    @ApiModelProperty(value = "逾期记录")
    @TableField(value = "overduerecords")
    private Integer overduerecords;

    /**
     * 不良征信记录
     */
    @ApiModelProperty(value = "不良征信记录")
    @TableField(value = "badcredit")
    private Integer badcredit;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @TableField(value = "createtime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createtime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    @TableField(value = "updatetime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatetime;

    // --------------------------------------------------

    @ApiModelProperty(value = "部门全称")
    @TableField(exist = false)
    private String deptFullName;

    @ApiModelProperty(value = "还款状态 0未付清 1已付清")
    @TableField(exist = false)
    private Integer repaymentStatus;
}
