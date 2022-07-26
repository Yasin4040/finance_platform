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
@TableName(value = "budget_project_interest_new")
@Data
public class BudgetProjectInterest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目Id
     */
    @NotNull(message = "项目Id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "projectid")
    private Long projectid;

    /**
     * 有无存款
     */
    @ApiParam(hidden = true)
    @TableField(value = "depositflag")
    private Integer depositflag;

    /**
     * 借款周期(1-12个月)
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthid")
    private Long monthid;

    /**
     * 是否从头计息
     */
    @ApiParam(hidden = true)
    @TableField(value = "isheadcalinterest")
    private Integer isheadcalinterest;

    /**
     * 前多少天按借款总额计息
     */
    @ApiParam(hidden = true)
    @TableField(value = "days")
    private Integer days;

    /**
     * 借款周期内利率
     */
    @ApiParam(hidden = true)
    @TableField(value = "insideinterestrate")
    private BigDecimal insideinterestrate;

    /**
     * 借款周期外利率
     */
    @ApiParam(hidden = true)
    @TableField(value = "outsideinterestrate")
    private BigDecimal outsideinterestrate;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

}
