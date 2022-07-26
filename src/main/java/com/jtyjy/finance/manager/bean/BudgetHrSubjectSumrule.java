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
@TableName(value = "budget_hr_subject_sumrule")
@Data
public class BudgetHrSubjectSumrule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 届别名称
     */
    @NotBlank(message = "届别名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "year")
    private String year;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 预算单位名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "unitname")
    private String unitname;

    /**
     * 预算单位id
     */
    @ApiParam(hidden = true)
    @TableField(value = "unitid")
    private Long unitid;

    /**
     * 备注
     */
    @NotBlank(message = "备注不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "remark")
    private String remark;

    /**
     * 多个部门id,用逗号隔开
     */
    @NotBlank(message = "多个部门id,用逗号隔开不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "deptids")
    private String deptids;

    /**
     * 多个部门名称,用逗号隔开
     */
    @NotBlank(message = "多个部门名称,用逗号隔开不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "deptanames")
    private String deptanames;

    /**
     * 多个用户id,用逗号隔开
     */
    @NotBlank(message = "多个用户id,用逗号隔开不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "userids")
    private String userids;

    /**
     * 多个用户名称,用逗号隔开
     */
    @NotBlank(message = "多个用户名称,用逗号隔开不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "usernames")
    private String usernames;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "createdate")
    private Date createdate;

    /**
     * 是否适用预算
     */
    @NotNull(message = "是否适用预算不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "budgetflag")
    private Boolean budgetflag;

    /**
     * 是否适用工资
     */
    @NotNull(message = "是否适用工资不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "salaryflag")
    private Boolean salaryflag;

}
