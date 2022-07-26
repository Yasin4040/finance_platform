package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 月度科目预算表
 *
 * @author Admin
 */
@TableName(value = "budget_month_subject")
@Data
public class BudgetMonthSubject implements Serializable {

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
     * 月度id
     */
    @NotNull(message = "月度id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "monthid")
    private Long monthid;

    /**
     * 当时的年度动因金额
     */
    @NotNull(message = "当时的年度动因金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearagentmoney")
    private BigDecimal yearagentmoney;

    /**
     * 当时的年度追加金额
     */
    @NotNull(message = "当时的年度追加金额 不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearaddmoney")
    private BigDecimal yearaddmoney;

    /**
     * 当时的年度拆出金额（同科目里面的动因可以拆借）
     */
    @NotNull(message = "当时的年度拆出金额（同科目里面的动因可以拆借）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearlendoutmoney")
    private BigDecimal yearlendoutmoney;

    /**
     * 当时的年度当时的年度拆进金额（同科目里面的动因可以拆借）
     */
    @NotNull(message = "当时的年度当时的年度拆进金额（同科目里面的动因可以拆借）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearlendinmoney")
    private BigDecimal yearlendinmoney;

    /**
     * 当时的年度执行数量
     */
    @NotNull(message = "当时的年度执行数量不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearexecutemoney")
    private BigDecimal yearexecutemoney;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "m")
    private BigDecimal m;

    /**
     * 月度预算金额(可编辑)
     */
    @NotNull(message = "月度预算金额(可编辑)不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "total")
    private BigDecimal total = BigDecimal.ZERO;

    /**
     * 预算科目id
     */
    @NotNull(message = "预算科目id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "subjectid")
    private Long subjectid;

    /**
     * 动因内容
     */
    @ApiParam(hidden = true)
    @TableField(value = "remark")
    private String remark;

    /**
     * 月度追加金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "addmoney")
    private BigDecimal addmoney= BigDecimal.ZERO;

    /**
     * 月度拆出金额（目前同科目里面的动因可以拆借）
     */
    @ApiParam(hidden = true)
    @TableField(value = "lendoutmoney")
    private BigDecimal lendoutmoney= BigDecimal.ZERO;

    /**
     * 月度拆进金额（目前同科目里面的动因可以拆借）
     */
    @ApiParam(hidden = true)
    @TableField(value = "lendinmoney")
    private BigDecimal lendinmoney= BigDecimal.ZERO;

    /**
     * 月度执行金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "executemoney")
    private BigDecimal executemoney= BigDecimal.ZERO;

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
     * 本届收入占比
     */
    @ApiParam(hidden = true)
    @TableField(value = "revenueformula")
    private BigDecimal revenueformula= BigDecimal.ZERO;

    /**
     * 本届码洋占比
     */
    @ApiParam(hidden = true)
    @TableField(value = "ccratioformula")
    private BigDecimal ccratioformula= BigDecimal.ZERO;

    /**
     * 月度预算活动说明
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthbusiness")
    private String monthbusiness;

}
