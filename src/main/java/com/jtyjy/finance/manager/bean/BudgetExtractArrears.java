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

/**
 * @author Admin
 */
@TableName(value = "budget_extract_arrears")
@Data
public class BudgetExtractArrears implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 未知参数
     */
    @NotBlank(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "month")
    private String month;

    /**
     * 身份证号
     */
    @NotBlank(message = "身份证号不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "idnumber")
    private String idnumber;

    /**
     * 工号
     */
    @ApiParam(hidden = true)
    @TableField(value = "empno")
    private String empno;

    /**
     * 员工名称
     */
    @NotBlank(message = "员工名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "empname")
    private String empname;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "salary")
    private BigDecimal salary;

    /**
     * 累计实发工资
     */
    @ApiParam(hidden = true)
    @TableField(value = "salarylj")
    private BigDecimal salarylj;

    /**
     * 累计专项扣除(含本次)
     */
    @ApiParam(hidden = true)
    @TableField(value = "specialdeductionlj")
    private BigDecimal specialdeductionlj;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "fiveriskonefund")
    private BigDecimal fiveriskonefund;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "fiveriskonefundlj")
    private BigDecimal fiveriskonefundlj;

    /**
     * 累计起征点(含本次)
     */
    @ApiParam(hidden = true)
    @TableField(value = "thresholdlj")
    private BigDecimal thresholdlj;

    /**
     * 法人公司实发累计(不含本次)
     */
    @ApiParam(hidden = true)
    @TableField(value = "incorporatedcompanylj")
    private BigDecimal incorporatedcompanylj;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "realextract")
    private BigDecimal realextract;

    /**
     * 实发提成倒推金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "sfdtje")
    private BigDecimal sfdtje;

    /**
     * 上交个税，不体现负值【 （实发提成倒推 - 累计专项扣除(包含本次) - 累计起征点(包含本次)）*税率 - 扣除数 - 累计上交个税（不含本次）】
     */
    @ApiParam(hidden = true)
    @TableField(value = "payabletaxs")
    private BigDecimal payabletaxs;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitid")
    private Long bunitid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitname")
    private String bunitname;

}
