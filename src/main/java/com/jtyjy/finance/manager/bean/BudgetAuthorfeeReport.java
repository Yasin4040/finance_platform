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
@TableName(value = "budget_authorfee_report")
@Data
public class BudgetAuthorfeeReport implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 届别id
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 届别
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearperiod")
    private String yearperiod;

    /**
     * 月份id
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthid")
    private Long monthid;

    /**
     * 稿费归属月份
     */
    @ApiParam(hidden = true)
    @TableField(value = "feemonth")
    private String feemonth;

    /**
     * 批次编号
     */
    @ApiParam(hidden = true)
    @TableField(value = "reportcode")
    private String reportcode;

    /**
     * 税前稿费合计
     */
    @ApiParam(hidden = true)
    @TableField(value = "copefeesum")
    private BigDecimal copefeesum;

    /**
     * 税费合计
     */
    @ApiParam(hidden = true)
    @TableField(value = "taxsum")
    private BigDecimal taxsum;

    /**
     * 实发稿费合计
     */
    @ApiParam(hidden = true)
    @TableField(value = "realfeesum")
    private BigDecimal realfeesum;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "times")
    private Integer times;

    /**
     * 创建时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

}
