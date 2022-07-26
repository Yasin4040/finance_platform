package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Admin
 */
@TableName(value = "budget_month_period")
@Data
public class BudgetMonthPeriod implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 期间名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "period")
    private String period;

    /**
     * 编号
     */
    @ApiParam(hidden = true)
    @TableField(value = "code")
    private String code;

    /**
     * 排序号
     */
    @ApiParam(hidden = true)
    @TableField(value = "orderno")
    private Integer orderno;

    /**
     * 主键
     */
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 当前月份(true:是）
     */
    @ApiParam(hidden = true)
    @TableField(value = "currentflag")
    private Boolean currentflag;

}
