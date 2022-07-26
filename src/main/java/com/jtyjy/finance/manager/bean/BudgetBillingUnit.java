package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Admin
 */
@TableName(value = "budget_billing_unit")
@Data
@ApiModel(description = "开票单位表")
public class BudgetBillingUnit implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 开票单位主键
     */
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "开票单位id（修改必送）")
    private Long id;

    /**
     * 编号
     */
    @ApiParam(hidden = true)
    @TableField(value = "code")
    @ApiModelProperty(value = "编号")
    private String code;

    /**
     * 名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "name")
    @ApiModelProperty(value = "名称", required = true)
    private String name;

    /**
     * 公司发票(1)、无票(0)
     */
    @ApiParam(hidden = true)
    @TableField(value = "billingunittype")
    @ApiModelProperty(value = "单位类型 0：无票 1：公司发票")
    private String billingUnitType;

    /**
     * 是否法人单位
     */
    @ApiParam(hidden = true)
    @TableField(value = "corporation")
    @ApiModelProperty(value = "是否法人单位 0：否【默认】 1：是", required = true)
    private Integer corporation;

    /**
     * 内部单位标志
     */
    @ApiParam(hidden = true)
    @TableField(value = "ownflag")
    @ApiModelProperty(value = "内部单位标志 0：内部 1：外部【默认】")
    private Integer ownFlag;

    /**
     * 停用标识（0：启用【默认】 1：停用）
     */
    @ApiParam(hidden = true)
    @TableField(value = "stopflag")
    @ApiModelProperty(value = "停用标识 0：启用【默认】 1：停用")
    private Integer stopFlag;

    /**
     * 排序号
     */
    @ApiParam(hidden = true)
    @TableField(value = "orderno")
    @ApiModelProperty(value = "排序号")
    private Integer orderNo;

    /**
     * 预算员（多个）要和预算单位中预算员一致（预算单位中的预算员变化了，这里面的也要变化）用户ids
     */
    @ApiParam(hidden = true)
    @TableField(value = "budgeters")
    @ApiModelProperty(value = "预算员（多个）ids")
    private String budgeters;

    /**
     * 会计（多个）用户ids
     */
    @ApiParam(hidden = true)
    @TableField(value = "accountants")
    @ApiModelProperty(value = "会计（多个）用户ids")
    private String accountants;

    /**
     * 备注
     */
    @ApiParam(hidden = true)
    @TableField(value = "remark")
    @ApiModelProperty(value = "备注")
    private String remark;

    /**
     * 名称首拼
     */
    @ApiParam(hidden = true)
    @TableField(value = "firstspell")
    @ApiModelProperty(value = "名称首拼")
    private String firstSpell;

    /**
     * 名称全拼
     */
    @ApiParam(hidden = true)
    @TableField(value = "fullspell")
    @ApiModelProperty(value = "名称全拼")
    private String fullSpell;

    /**
     * 第三方系统Id
     */
    @ApiParam(hidden = true)
    @TableField(value = "outkey")
    @ApiModelProperty(value = "第三方系统Id")
    private String outKey;

}
