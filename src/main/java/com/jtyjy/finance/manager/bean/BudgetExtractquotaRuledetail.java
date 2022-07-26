package com.jtyjy.finance.manager.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

/**
 * @author Admin
 */
@TableName(value = "budget_extractquota_ruledetail")
@Data
public class BudgetExtractquotaRuledetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @ApiModelProperty(hidden = false)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 限额规则主表id
     */
    @NotNull(message = "规则id不能为空")
    @ApiModelProperty(hidden = false, value = "规则主表id")
    @TableField(value = "extractquotaruleid")
    private Long extractquotaruleid;

    /**
     * 最小工资
     */
    @NotNull(message = "最小工资不能为空")
    @ApiModelProperty(hidden = false, value = "最小工资")
    @TableField(value = "minsalary")
    private BigDecimal minsalary;

    /**
     * 最大工资
     */
    @NotNull(message = "最大工资不能为空")
    @ApiModelProperty(hidden = false, value = "最大工资")
    @TableField(value = "maxsalary")
    private BigDecimal maxsalary;

    /**
     * 限额金额
     */
    @NotNull(message = "限额金额不能为空")
    @ApiModelProperty(hidden = false, value = "限额金额")
    @TableField(value = "quotamoney")
    private BigDecimal quotamoney;

    /**
     * 创建时间
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "createtime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createtime;

    /**
     * 更新时间
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "updatetime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updatetime;

}
