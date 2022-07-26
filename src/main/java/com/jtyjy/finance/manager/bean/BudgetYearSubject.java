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
@TableName(value = "budget_year_subject")
@Data
public class BudgetYearSubject implements Serializable {

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
     * 6月
     */
    @NotNull(message = "6月不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "m6")
    private BigDecimal m6;

    /**
     * 7月
     */
    @NotNull(message = "7月不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "m7")
    private BigDecimal m7;

    /**
     * 8月
     */
    @NotNull(message = "8月不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "m8")
    private BigDecimal m8;

    /**
     * 9月
     */
    @NotNull(message = "9月不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "m9")
    private BigDecimal m9;

    /**
     * 10月
     */
    @NotNull(message = "10月不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "m10")
    private BigDecimal m10;

    /**
     * 11月
     */
    @NotNull(message = "11月不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "m11")
    private BigDecimal m11;

    /**
     * 12月
     */
    @NotNull(message = "12月不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "m12")
    private BigDecimal m12;

    /**
     * 1月
     */
    @NotNull(message = "1月不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "m1")
    private BigDecimal m1;

    /**
     * 2月
     */
    @NotNull(message = "2月不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "m2")
    private BigDecimal m2;

    /**
     * 3月
     */
    @NotNull(message = "3月不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "m3")
    private BigDecimal m3;

    /**
     * 4月
     */
    @NotNull(message = "4月不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "m4")
    private BigDecimal m4;

    /**
     * 5月
     */
    @NotNull(message = "5月不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "m5")
    private BigDecimal m5;

    /**
     * 本届总金额(12个月)
     */
    @NotNull(message = "本届总金额(12个月)不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "total")
    private BigDecimal total;

    /**
     * 上届执行总金额
     */
    @NotNull(message = "上届执行总金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "pretotal")
    private BigDecimal pretotal;

    /**
     * 上届预估
     */
    @NotNull(message = "上届预估不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "preestimate")
    private BigDecimal preestimate;

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
     * 预算科目id
     */
    @NotNull(message = "预算科目id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "subjectid")
    private Long subjectid;

    /**
     * 本届收入占比
     */
    @NotNull(message = "本届收入占比不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "revenueformula")
    private BigDecimal revenueformula;

    /**
     * 本届码洋占比
     */
    @NotNull(message = "本届码洋占比不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "ccratioformula")
    private BigDecimal ccratioformula;

    /**
     * 上届收入占比
     */
    @NotNull(message = "上届收入占比不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "prerevenueformula")
    private BigDecimal prerevenueformula;

    /**
     * 上届码洋占比
     */
    @NotNull(message = "上届码洋占比不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "preccratioformula")
    private BigDecimal preccratioformula;

    /**
     * 本届收入占比公式
     */
    @ApiParam(hidden = true)
    @TableField(value = "revenueformulastr")
    private String revenueformulastr;

    /**
     * 本届码洋占比公式
     */
    @ApiParam(hidden = true)
    @TableField(value = "ccratioformulastr")
    private String ccratioformulastr;

    /**
     * 上届码洋占比公式
     */
    @ApiParam(hidden = true)
    @TableField(value = "preccratioformulastr")
    private String preccratioformulastr;

    /**
     * 计算公式
     */
    @ApiParam(hidden = true)
    @TableField(value = "formula")
    private String formula;

    /**
     * 年度追加金额
     */
    @NotNull(message = "年度追加金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "addmoney")
    private BigDecimal addmoney;

    /**
     * 年度拆出金额（目前同科目里面的动因可以拆借）
     */
    @NotNull(message = "年度拆出金额（目前同科目里面的动因可以拆借）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "lendoutmoney")
    private BigDecimal lendoutmoney;

    /**
     * 年度拆进金额（目前同科目里面的动因可以拆借）
     */
    @NotNull(message = "年度拆进金额（目前同科目里面的动因可以拆借）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "lendinmoney")
    private BigDecimal lendinmoney;

    /**
     * 年度执行金额
     */
    @NotNull(message = "年度执行金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "executemoney")
    private BigDecimal executemoney;

    // ------------------------------------------------------------


}
