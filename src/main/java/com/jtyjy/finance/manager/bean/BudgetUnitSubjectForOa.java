package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Admin
 */
@TableName(value = "budget_unit_subject_for_oa")
@Data
public class BudgetUnitSubjectForOa implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 名称(别名）
     */
    @ApiParam(hidden = true)
    @TableField(value = "biename")
    private String biename;

    /**
     * 届别id
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 预算单位id
     */
    @NotNull(message = "预算单位id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "unitid")
    private Long unitid;

    /**
     * 预算科目id
     */
    @NotNull(message = "预算科目id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "subjectid")
    private Long subjectid;

    /**
     * 是否隐藏
     */
    @NotNull(message = "是否隐藏不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "hidden")
    private Boolean hidden;

    /**
     * 年度动因控制
     */
    @NotNull(message = "年度动因控制不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearcontrolflag")
    private Boolean yearcontrolflag;

    /**
     * 月度科目控制标识
     */
    @NotNull(message = "月度科目控制标识不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "monthcontrolflag")
    private Boolean monthcontrolflag;

    /**
     * 多个一级产品分类
     */
    @ApiParam(hidden = true)
    @TableField(value = "procategoryid")
    private String procategoryid;

    /**
     * 可追加
     */
    @NotNull(message = "可追加不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "addflag")
    private Boolean addflag;

    /**
     * 可拆借
     */
    @NotNull(message = "可拆借不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "lendflag")
    private Boolean lendflag;

    /**
     * 分解权限标识
     */
    @NotNull(message = "分解权限标识不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "splitflag")
    private Boolean splitflag;

    /**
     * 本届码洋占比公式
     */
    @ApiParam(hidden = true)
    @TableField(value = "ccratioformula")
    private String ccratioformula;

    /**
     * 上届码洋占比公式
     */
    @ApiParam(hidden = true)
    @TableField(value = "preccratioformula")
    private String preccratioformula;

    /**
     * 本届收入占比公式
     */
    @ApiParam(hidden = true)
    @TableField(value = "revenueformula")
    private String revenueformula;

    /**
     * 计算公式
     */
    @ApiParam(hidden = true)
    @TableField(value = "formula")
    private String formula;

}
