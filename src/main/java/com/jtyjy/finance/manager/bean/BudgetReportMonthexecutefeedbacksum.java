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
@TableName(value = "budget_report_monthexecutefeedbacksum")
@Data
public class BudgetReportMonthexecutefeedbacksum implements Serializable {

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
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearname")
    private String yearname;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "unitid")
    private Long unitid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "unitname")
    private String unitname;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "subjectid")
    private Long subjectid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "subjectname")
    private String subjectname;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "subjecttrrname")
    private String subjecttrrname;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthid")
    private Long monthid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "level")
    private Integer level;

    /**
     * 上届预估
     */
    @ApiParam(hidden = true)
    @TableField(value = "preestimate")
    private BigDecimal preestimate;

    /**
     * 占比科目金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "subjectyearagentmoney")
    private BigDecimal subjectyearagentmoney;

    /**
     * 执行金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthexecutemoney")
    private BigDecimal monthexecutemoney;

    /**
     * 收入占比(月度)
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthrevenueformula")
    private BigDecimal monthrevenueformula;

    /**
     * 累计执行
     */
    @ApiParam(hidden = true)
    @TableField(value = "executemoneysum")
    private BigDecimal executemoneysum;

    /**
     * 年初预算
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearagentmoney")
    private BigDecimal yearagentmoney;

    /**
     * 累计追加金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "addmoneysum")
    private BigDecimal addmoneysum;

    /**
     * 收入占比(年度)
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearrevenueformula")
    private BigDecimal yearrevenueformula;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "orderno")
    private Long orderno;

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

}
