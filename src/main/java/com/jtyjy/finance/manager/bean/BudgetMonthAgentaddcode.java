package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_month_agentaddcode")
@Data
public class BudgetMonthAgentaddcode implements Serializable {

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
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "infoid")
    private Long infoid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "beforecode")
    private String beforecode;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "currentcode")
    private String currentcode;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "aftercode")
    private String aftercode;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

}
