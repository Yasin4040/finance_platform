package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_lendmoney_interest_new")
@Data
public class BudgetLendmoneyInterest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 借款id
     */
    @NotNull(message = "借款id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "lendmoneyid")
    private Long lendmoneyid;

    /**
     * 0 每天累计计算 1 一次性
     */
    @ApiParam(hidden = true)
    @TableField(value = "type")
    private Integer type;

    /**
     * 计息天数
     */
    @ApiParam(hidden = true)
    @TableField(value = "days")
    private Integer days;

    /**
     * 当时的借款金额
     */
    @NotNull(message = "当时的借款金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "curlendmoney")
    private BigDecimal curlendmoney;

    /**
     * 当时借款产生的总利息
     */
    @NotNull(message = "当时借款产生的总利息不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "curinterestmoney")
    private BigDecimal curinterestmoney;

    /**
     * 利率（千分之多少)
     */
    @NotNull(message = "利率（千分之多少)不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "interestrate")
    private Integer interestrate;

    /**
     * 产生利息的时间（某一天)
     */
    @NotNull(message = "产生利息的时间（某一天)不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "interestday")
    private Date interestday;

    /**
     * 本次产生的利息 （产生的利息 = 当时的借款金额  * 利率）  要更新借款单产生的利息
     */
    @NotNull(message = "本次产生的利息 （产生的利息 = 当时的借款金额  * 利率）  要更新借款单产生的利息不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "interestmoney")
    private BigDecimal interestmoney;

    /**
     * 本次产生的利息后的总利息 = 本次产生的利息 + 当时借款产生的总利息;
     */
    @NotNull(message = "本次产生的利息后的总利息 = 本次产生的利息 + 当时借款产生的总利息;不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "nowinterestmoney")
    private BigDecimal nowinterestmoney;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

}
