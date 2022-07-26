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
@TableName(value = "budget_reimbursementexecute")
@Data
public class BudgetReimbursementexecute implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 报销单id
     */
    @NotNull(message = "报销单id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "reimbursementid")
    private Long reimbursementid;

    /**
     * 0:报销，1：划拨
     */
    @NotNull(message = "0:报销，1：划拨不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "type")
    private Integer type;

    /**
     * 月度动因id
     */
    @NotNull(message = "月度动因id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "monthagentid")
    private Long monthagentid;

    /**
     * 执行金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "executemoney")
    private BigDecimal executemoney;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "reimflag")
    private Boolean reimflag;

    /**
     * 处理状态  true :已处理，false：未处理（默认）
     */
    @ApiParam(hidden = true)
    @TableField(value = "handleflag")
    private Boolean handleflag;

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
