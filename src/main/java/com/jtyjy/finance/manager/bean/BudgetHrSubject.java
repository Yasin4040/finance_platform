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
@TableName(value = "budget_hr_subject")
@Data
public class BudgetHrSubject implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 员工id
     */
    @NotBlank(message = "员工id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "empid")
    private String empid;

    /**
     * 员工工号
     */
    @NotBlank(message = "员工工号不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "empno")
    private String empno;

    /**
     * 员工姓名
     */
    @NotBlank(message = "员工姓名不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "empname")
    private String empname;

    /**
     * 部门id
     */
    @NotBlank(message = "部门id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "deptid")
    private String deptid;

    /**
     * 部门名称
     */
    @NotBlank(message = "部门名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "deptname")
    private String deptname;

    /**
     * 所有上级部门id
     */
    @NotBlank(message = "所有上级部门id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "deptpids")
    private String deptpids;

    /**
     * 部门全称
     */
    @NotBlank(message = "部门全称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "fulldeptname")
    private String fulldeptname;

    /**
     * 一级部门名称
     */
    @NotBlank(message = "一级部门名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "topdeptname")
    private String topdeptname;

    /**
     * 开票单位id
     */
    @NotNull(message = "开票单位id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "bunitid")
    private Long bunitid;

    /**
     * 开票单位名称
     */
    @NotBlank(message = "开票单位名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "bunitname")
    private String bunitname;

    /**
     * 届别id
     */
    @NotNull(message = "届别id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 月份id
     */
    @NotNull(message = "月份id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "monthid")
    private Long monthid;

    /**
     * 月份，例如201908
     */
    @NotBlank(message = "月份，例如201908不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "month")
    private String month;

    /**
     * //map 数据格式
     */
    @NotBlank(message = "//map 数据格式不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "valuestr")
    private String valuestr;

    /**
     * 创建时间
     */
    @NotNull(message = "创建时间不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "createdate")
    private Date createdate;

}
