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
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_lend_interest_rule_history_new")
@Data
public class BudgetLendInterestRuleHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id
     */
    @NotNull(message = "主键Id不能为空")
    @ApiModelProperty(value = "主键Id")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 未知参数
     */
    @ApiModelProperty(value = "未知参数")
    @TableField(value = "lendmoneyid")
    private Long lendmoneyid;

    /**
     * 当时借款金额
     */
    @ApiModelProperty(value = "当时借款金额")
    @TableField(value = "curlendmoney")
    private BigDecimal curlendmoney;

    /**
     * 当时还款金额
     */
    @ApiModelProperty(value = "当时还款金额")
    @TableField(value = "currepaidmoney")
    private BigDecimal currepaidmoney;

    /**
     * 当时利息
     */
    @ApiModelProperty(value = "当时利息")
    @TableField(value = "curinterestmoney")
    private BigDecimal curinterestmoney;

    /**
     * 当时已还利息
     */
    @ApiModelProperty(value = "当时已还利息")
    @TableField(value = "currepaidinterestmoney")
    private BigDecimal currepaidinterestmoney;

    /**
     * 当时月利率
     */
    @ApiModelProperty(value = "当时月利率")
    @TableField(value = "interestrateduringtheperiod")
    private BigDecimal interestrateduringtheperiod;

    /**
     * 当时逾期月利率
     */
    @ApiModelProperty(value = "当时逾期月利率")
    @TableField(value = "interestrateouttheperiod")
    private BigDecimal interestrateouttheperiod;

    /**
     * 当时借款期限（年）
     */
    @ApiModelProperty(value = "当时借款期限（年）")
    @TableField(value = "years")
    private Integer years;

    /**
     * 当时借款期限（月）
     */
    @ApiModelProperty(value = "当时借款期限（月）")
    @TableField(value = "months")
    private Integer months;

    /**
     * 本次产生的利息
     */
    @ApiModelProperty(value = "本次产生的利息")
    @TableField(value = "interestmoney")
    private BigDecimal interestmoney;

    /**
     * 当时生效日期
     */
    @ApiModelProperty(value = "当时生效日期")
    @TableField(value = "effectdate")
    private Date effectdate;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @TableField(value = "createtime")
    private Date createtime;

}
