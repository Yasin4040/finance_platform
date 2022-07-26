package com.jtyjy.finance.manager.bean;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Admin
 */
@TableName(value = "budget_billing_unit_account")
@Data
@ApiModel(description = "单位账户表")
public class BudgetBillingUnitAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id
     */
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "单位账户id（修改必送）")
    private Long id;

    /**
     * 单位Id
     */
    @NotNull(message = "开票单位id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "billingunitid")
    @ApiModelProperty(value = "开票单位ID", required = true)
    private Long billingunitid;

    /**
     * 银行关联Id
     */
    @NotBlank(message = "银行关联id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "branchcode")
    @ApiModelProperty(value = "电子联行号", required = true)
    private String branchcode;

    /**
     * 排序号
     */
    @NotNull(message = "排序号不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "orderno")
    @ApiModelProperty(value = "排序号", required = true)
    private Integer orderno;

    /**
     * 停用标识（0：启用【默认】 1：停用）
     */
    @NotNull(message = "停用标识不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "stopflag")
    @ApiModelProperty(value = "停用标识 false：启用 true：停用")
    private Boolean stopflag;

    /**
     * 备注
     */
    @ApiParam(hidden = true)
    @TableField(value = "remark")
    @ApiModelProperty(value = "备注")
    private String remark;

    /**
     * 银行账户
     */
    @NotBlank(message = "银行账户不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "bankaccount")
    @ApiModelProperty(value = "银行账户", required = true)
    private String bankaccount;

    /**
     * 是否默认账户（0 ：非默认账户  1：默认账户）
     */
    @NotNull(message = "是否默认账户不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "defaultflag")
    @ApiModelProperty(value = "是否默认账户 false：非默认账户  true：默认账户")
    private Boolean defaultflag;

}
