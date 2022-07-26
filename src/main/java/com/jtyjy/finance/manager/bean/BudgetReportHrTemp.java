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
@TableName(value = "budget_report_hr_temp")
@Data
public class BudgetReportHrTemp implements Serializable {

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
    @ApiParam(hidden = true)
    @TableField(value = "year")
    private String year;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "deptname")
    private String deptname;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "deptid")
    private String deptid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "empno")
    private String empno;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "empname")
    private String empname;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "createdate")
    private Date createdate;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "month")
    private String month;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "shebaomoney")
    private BigDecimal shebaomoney;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "gongjjmoney")
    private BigDecimal gongjjmoney;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "shuimoney")
    private BigDecimal shuimoney;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "shifamoney")
    private BigDecimal shifamoney;

}
