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
@TableName(value = "budget_month_subject_his")
@Data
public class BudgetMonthSubjectHis implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 届别id
     */
    @NotNull(message = "届别id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 预算单位id
     */
    @NotNull(message = "预算单位id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "unitid")
    private Long unitid;

    /**
     * 月度期间id
     */
    @NotNull(message = "月度期间id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "monthid")
    private Long monthid;

    /**
     * 预算科目id
     */
    @NotNull(message = "预算科目id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "subjectid")
    private Long subjectid;

    /**
     * 0:初始化，1：年度追加 ,2:报销执行
     */
    @ApiParam(hidden = true)
    @TableField(value = "type")
    private Integer type;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "updatetime")
    private Date updatetime;

    /**
     * 月初动因金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "total")
    private BigDecimal total;

    /**
     * 前月度追加金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "beforeaddmoney")
    private BigDecimal beforeaddmoney;

    /**
     * 前月度拆出金额（目前同科目里面的动因可以拆借）
     */
    @ApiParam(hidden = true)
    @TableField(value = "beforelendoutmoney")
    private BigDecimal beforelendoutmoney;

    /**
     * 前月度拆进金额（目前同科目里面的动因可以拆借）
     */
    @ApiParam(hidden = true)
    @TableField(value = "beforelendinmoney")
    private BigDecimal beforelendinmoney;

    /**
     * 前月度执行金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "beforeexecutemoney")
    private BigDecimal beforeexecutemoney;

    /**
     * 后月度追加金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "afteraddmoney")
    private BigDecimal afteraddmoney;

    /**
     * 后月度拆出金额（目前同科目里面的动因可以拆借）
     */
    @ApiParam(hidden = true)
    @TableField(value = "afterlendoutmoney")
    private BigDecimal afterlendoutmoney;

    /**
     * 后月度拆进金额（目前同科目里面的动因可以拆借）
     */
    @ApiParam(hidden = true)
    @TableField(value = "afterlendinmoney")
    private BigDecimal afterlendinmoney;

    /**
     * 后月度执行金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "afterexecutemoney")
    private BigDecimal afterexecutemoney;

}
