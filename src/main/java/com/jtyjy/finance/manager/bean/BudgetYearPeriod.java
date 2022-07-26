package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_year_period")
@Data
public class BudgetYearPeriod implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 届别名称，例如19届
     */
    @NotBlank(message = "届别名称，例如19届，不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "period")
    private String period;

    /**
     * 开始日期
     */
    @ApiParam(hidden = true)
    @TableField(value = "startdate")
    private Date startdate;

    /**
     * 结束日期
     */
    @ApiParam(hidden = true)
    @TableField(value = "enddate")
    private Date enddate;

    /**
     * 创建时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 当前期间
     */
    @ApiParam(hidden = true)
    @TableField(value = "currentflag")
    private Boolean currentflag;

    /**
     * 编号如2019
     */
    @ApiParam(hidden = true)
    @TableField(value = "code")
    private String code;

}
