package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_hr_subject_sum")
@Data
public class BudgetHrSubjectSum implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 届别名称
     */
    @NotBlank(message = "届别名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "year")
    private String year;

    /**
     * 届别id
     */
    @NotNull(message = "届别id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 月份
     */
    @NotBlank(message = "月份不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "month")
    private String month;

    /**
     * 月份id
     */
    @NotNull(message = "月份id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "monthid")
    private Long monthid;

    /**
     * 未知参数
     */
    @NotBlank(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "unitname")
    private String unitname;

    /**
     * //0:人资，1：财务
     */
    @NotNull(message = "//0:人资，1：财务不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "type")
    private Integer type;

    /**
     * 人数
     */
    @NotNull(message = "人数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "count")
    private Integer count;

    /**
     * 未知参数
     */
    @NotBlank(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "valuestr")
    private String valuestr;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "createdate")
    private Date createdate;

}
