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
@TableName(value = "budget_year_subject_his")
@Data
public class BudgetYearSubjectHis implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id
     */
    @NotNull(message = "主键Id不能为空")
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
     * 预算科目id
     */
    @NotNull(message = "预算科目id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "subjectid")
    private Long subjectid;

    /**
     * 0:初始化，1：年度追加 ,2:报销执行
     */
    @NotNull(message = "0:初始化，1：年度追加 ,2:报销执行不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "type")
    private Integer type;

    /**
     * 创建时间
     */
    @NotNull(message = "创建时间不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 修改时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "updatetime")
    private Date updatetime;

    /**
     * 年初动因金额
     */
    @NotNull(message = "年初动因金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "total")
    private BigDecimal total;

    /**
     * beforeaddmoney
     */
    @NotNull(message = "beforeaddmoney不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "beforeaddmoney")
    private BigDecimal beforeaddmoney;

    /**
     * 前年度拆出金额（目前同科目里面的动因可以拆借）
     */
    @NotNull(message = "前年度拆出金额（目前同科目里面的动因可以拆借）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "beforelendoutmoney")
    private BigDecimal beforelendoutmoney;

    /**
     * 前年度拆进金额（目前同科目里面的动因可以拆借）
     */
    @NotNull(message = "前年度拆进金额（目前同科目里面的动因可以拆借）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "beforelendinmoney")
    private BigDecimal beforelendinmoney;

    /**
     * 前年度执行金额
     */
    @NotNull(message = "前年度执行金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "beforeexecutemoney")
    private BigDecimal beforeexecutemoney;

    /**
     * 后年度追加金额
     */
    @NotNull(message = "后年度追加金额 不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "afteraddmoney")
    private BigDecimal afteraddmoney;

    /**
     * 后年度拆出金额（目前同科目里面的动因可以拆借）
     */
    @NotNull(message = "后年度拆出金额（目前同科目里面的动因可以拆借）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "afterlendoutmoney")
    private BigDecimal afterlendoutmoney;

    /**
     * 后年度拆进金额（目前同科目里面的动因可以拆借）
     */
    @NotNull(message = "后年度拆进金额（目前同科目里面的动因可以拆借）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "afterlendinmoney")
    private BigDecimal afterlendinmoney;

    /**
     * 后年度执行金额
     */
    @NotNull(message = "后年度执行金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "afterexecutemoney")
    private BigDecimal afterexecutemoney;

}
