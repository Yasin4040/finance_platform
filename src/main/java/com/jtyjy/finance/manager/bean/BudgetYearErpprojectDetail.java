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

/**
 * @author Admin
 */
@TableName(value = "budget_year_erpproject_detail")
@Data
public class BudgetYearErpprojectDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * erp项目id
     */
    @ApiParam(hidden = true)
    @TableField(value = "budgeterpproejctid")
    private Long budgeterpproejctid;

    /**
     * 届别id
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 预算单位id
     */
    @ApiParam(hidden = true)
    @TableField(value = "unitid")
    private Long unitid;

    /**
     * 预算科目id
     */
    @ApiParam(hidden = true)
    @TableField(value = "subjectid")
    private Long subjectid;

    /**
     * 年度预算动因id(拆出动因，已存在的)
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearagentid")
    private Long yearagentid;

    /**
     * 拆进动因，流程完成后新生成的
     */
    @ApiParam(hidden = true)
    @TableField(value = "newyearagentid")
    private Long newyearagentid;

    /**
     * 追加金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "total")
    private BigDecimal total;

    /**
     * 1月
     */
    @ApiParam(hidden = true)
    @TableField(value = "m1")
    private BigDecimal m1;

    /**
     * 2月
     */
    @ApiParam(hidden = true)
    @TableField(value = "m2")
    private BigDecimal m2;

    /**
     * 3月
     */
    @ApiParam(hidden = true)
    @TableField(value = "m3")
    private BigDecimal m3;

    /**
     * 4月
     */
    @ApiParam(hidden = true)
    @TableField(value = "m4")
    private BigDecimal m4;

    /**
     * 5月
     */
    @ApiParam(hidden = true)
    @TableField(value = "m5")
    private BigDecimal m5;

    /**
     * 6月
     */
    @ApiParam(hidden = true)
    @TableField(value = "m6")
    private BigDecimal m6;

    /**
     * 7月
     */
    @ApiParam(hidden = true)
    @TableField(value = "m7")
    private BigDecimal m7;

    /**
     * 8月
     */
    @ApiParam(hidden = true)
    @TableField(value = "m8")
    private BigDecimal m8;

    /**
     * 9月
     */
    @ApiParam(hidden = true)
    @TableField(value = "m9")
    private BigDecimal m9;

    /**
     * 10月
     */
    @ApiParam(hidden = true)
    @TableField(value = "m10")
    private BigDecimal m10;

    /**
     * 11月
     */
    @ApiParam(hidden = true)
    @TableField(value = "m11")
    private BigDecimal m11;

    /**
     * 12月
     */
    @ApiParam(hidden = true)
    @TableField(value = "m12")
    private BigDecimal m12;

    /**
     * 当前月id
     */
    @ApiParam(hidden = true)
    @TableField(value = "curmonthid")
    private Long curmonthid;

    /**
     * 当前月金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "curmonthmoney")
    private BigDecimal curmonthmoney;

    /**
     * 月度动因id（审核通过后添加一个月度动因）
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthagentid")
    private Long monthagentid;

    /**
     * 追加原因
     */
    @ApiParam(hidden = true)
    @TableField(value = "remark")
    private String remark;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearagentmoney")
    private BigDecimal yearagentmoney;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearagentaddmoney")
    private BigDecimal yearagentaddmoney;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearagentlendoutmoney")
    private BigDecimal yearagentlendoutmoney;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearagentlendinmoney")
    private BigDecimal yearagentlendinmoney;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearagentexecutemoney")
    private BigDecimal yearagentexecutemoney;

}
