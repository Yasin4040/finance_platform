package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_lend_interest_rule_new")
@Data
public class BudgetLendInterestRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id
     */
    @ApiModelProperty(value = "主键Id")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 借款类型
     */
    @ApiModelProperty(value = "借款类型")
    @TableField(value = "lendtype")
    private Integer lendtype;

    /**
     * 规则名称
     */
    @NotBlank(message = "规则名称不能为空")
    @ApiModelProperty(value = "规则名称", required = true)
    @TableField(value = "rulename")
    private String rulename;

    /**
     * 关联budget_projectlendsum
     */
    @NotNull(message = "projectlendsumid不能为空")
    @ApiModelProperty(value = "关联budget_projectlendsum ", required = true)
    @TableField(value = "projectlendsumid")
    private Long projectlendsumid;

    /**
     * 期内月利率
     */
    @NotNull(message = "期内月利率不能为空")
    @ApiModelProperty(value = "期内月利率", required = true)
    @TableField(value = "interestrateduringtheperiod")
    private BigDecimal interestrateduringtheperiod;

    /**
     * 期外月利率
     */
    @NotNull(message = "期外月利率不能为空")
    @ApiModelProperty(value = "期外月利率", required = true)
    @TableField(value = "interestrateouttheperiod")
    private BigDecimal interestrateouttheperiod;

    /**
     * 年
     */
    @NotNull(message = "期外月利率不能为空")
    @ApiModelProperty(value = "年", required = true)
    @TableField(value = "years")
    private Integer years;

    /**
     * 月
     */
    @NotNull(message = "期外月利率不能为空")
    @ApiModelProperty(value = "月", required = true)
    @TableField(value = "months")
    private Integer months;

    /**
     * 生效日期
     */
    @NotNull(message = "期外月利率不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "生效日期", required = true)
    @TableField(value = "effectdate")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date effectdate;

}
