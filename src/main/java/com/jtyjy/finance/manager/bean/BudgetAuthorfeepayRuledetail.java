package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Admin
 */
@TableName(value = "budget_authorfeepay_ruledetail")
@Data
public class BudgetAuthorfeepayRuledetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @ApiModelProperty(value = "id(修改时需传)", dataType = "body")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 未知参数
     */
    @NotNull(message = "主规则id不能为空")
    @ApiModelProperty(value = "主规则id", required = true, dataType = "body")
    @TableField(value = "payruleid")
    private Long payruleid;

    /**
     * 最小金额
     */
    @NotNull(message = "最小金额不能为空")
    @ApiModelProperty(value = "最小金额", dataType = "body", required = true)
    @TableField(value = "min")
    private BigDecimal min;

    /**
     * 最大金额
     */
    @NotNull(message = "最大金额不能为空")
    @ApiModelProperty(value = "最大金额", dataType = "body", required = true)
    @TableField(value = "max")
    private BigDecimal max;

    /**
     * //多个银行，用逗号隔开；空表示其他银行
     */
    @ApiModelProperty(value = "银行类型(以,分隔)", dataType = "body")
    @TableField(value = "banks")
    private String banks;

    /**
     * 是否代发（默认 0 否    1是）
     */
    @ApiModelProperty(value = "是否代收", dataType = "body", required = true)
    @TableField(value = "isreplacesend")
    private Boolean isreplacesend;

    /**
     * 开票单位id
     */
    @NotNull(message = "开票单位id不能为空")
    @ApiModelProperty(value = "开票单位id", dataType = "body", required = true)
    @TableField(value = "billunitid")
    private Long billunitid;

    /**
     * 开票单位账户id
     */
    @NotNull(message = "开票单位账户id不能为空")
    @ApiModelProperty(value = "开票单位账户id", dataType = "body", required = true)
    @TableField(value = "billunitaccountid")
    private Long billunitaccountid;

    /**
     * 开票单位账户
     */
    @NotNull(message = "开票单位银行账号不能为空")
    @ApiModelProperty(value = "银行账号", dataType = "body", required = true)
    @TableField(value = "billunitaccount")
    private String billunitaccount;

    /**
     * 开票单位名字
     */
    @NotNull(message = "开票单位名称不能为空")
    @ApiModelProperty(value = "开票单位名称", dataType = "body", required = true)
    @TableField(value = "billunitname")
    private String billunitname;

    /**
     * 开户行
     */
    @ApiModelProperty(hidden = true, dataType = "body")
    @TableField(value = "billunitopenbank")
    private String billunitopenbank;

}
