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
@TableName(value = "budget_otherlendsum")
@Data
public class BudgetOtherlendsum implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 导入批次号
     */
    @ApiParam(hidden = true)
    @TableField(value = "importbatchnumber")
    private String importbatchnumber;

    /**
     * 审核状态(1为已审核)
     */
    @ApiParam(hidden = true)
    @TableField(value = "status")
    private Integer status;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "importtime")
    private Date importtime;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "importor")
    private String importor;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "importorname")
    private String importorname;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "verifyor")
    private String verifyor;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "verifyname")
    private String verifyname;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "verifytime")
    private Date verifytime;

}
