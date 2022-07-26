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
 * @author Admin
 */
@TableName(value = "budget_month_agentadd")
@Data
public class BudgetMonthAgentadd implements Serializable {

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
     * 动因名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "name")
    private String name;

    /**
     * 预算科目id
     */
    @NotNull(message = "预算科目id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "subjectid")
    private Long subjectid;

    /**
     * 月度动因id
     */
    @NotNull(message = "月度动因id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "monthagentid")
    private Long monthagentid;

    /**
     * 追加主表信息id
     */
    @NotNull(message = "追加主表信息id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "infoid")
    private Long infoid;

    /**
     * 更新时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "updatetime")
    private Date updatetime;

    /**
     * 创建时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 追加前动因月度预算金额（年初预算）
     */
    @NotNull(message = "追加前动因月度预算金额（年初预算）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearagentmoney")
    private BigDecimal yearagentmoney;

    /**
     * 追加前动因累计追加金额
     */
    @NotNull(message = "追加前动因累计追加金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearagentaddmoney")
    private BigDecimal yearagentaddmoney;

    /**
     * 追加前拆出金额（同科目里面的动因可以拆借）
     */
    @NotNull(message = "追加前拆出金额（同科目里面的动因可以拆借）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearagentlendoutmoney")
    private BigDecimal yearagentlendoutmoney;

    /**
     * yearagentlendinmoney
     */
    @NotNull(message = "yearagentlendinmoney不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearagentlendinmoney")
    private BigDecimal yearagentlendinmoney;

    /**
     * yearagentexcutemoney
     */
    @NotNull(message = "yearagentexcutemoney不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearagentexcutemoney")
    private BigDecimal yearagentexcutemoney;

    /**
     * 追加前动因月度预算金额（月初预算）
     */
    @NotNull(message = "追加前动因月度预算金额（月初预算）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "agentmoney")
    private BigDecimal agentmoney;

    /**
     * 追加前动因累计追加金额
     */
    @NotNull(message = "追加前动因累计追加金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "agentaddmoney")
    private BigDecimal agentaddmoney;

    /**
     * 追加前拆出金额（同科目里面的动因可以拆借）
     */
    @NotNull(message = "追加前拆出金额（同科目里面的动因可以拆借）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "agentlendoutmoney")
    private BigDecimal agentlendoutmoney;

    /**
     * 追加前拆进金额（同科目里面的动因可以拆借）
     */
    @NotNull(message = "追加前拆进金额（同科目里面的动因可以拆借）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "agentlendinmoney")
    private BigDecimal agentlendinmoney;

    /**
     * 追加前动因累计执行金额
     */
    @NotNull(message = "追加前动因累计执行金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "agentexcutemoney")
    private BigDecimal agentexcutemoney;

    /**
     * 追加金额
     */
    @NotNull(message = "追加金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "total")
    private BigDecimal total;

    /**
     * 追加原因
     */
    @NotBlank(message = "追加原因不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "remark")
    private String remark;

    @TableField(exist = false)
    private Long monthAgentId;

}
