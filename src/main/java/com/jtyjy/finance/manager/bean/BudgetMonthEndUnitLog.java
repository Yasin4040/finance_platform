package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_month_end_unit_log")
@Data
public class BudgetMonthEndUnitLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 届别
     */
    @NotNull(message = "届别不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "monthid")
    private Long monthid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "unitids")
    private String unitids;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "allunitids")
    private String allunitids;

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
    @TableField(value = "creater")
    private String creater;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "createname")
    private String createname;

}
