package com.jtyjy.finance.manager.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * @author shubo
 */
@ApiModel(description = "预算单位科目Vo")
@Data
public class BudgetUnitSubjectVO {


    @ApiParam(hidden = true)
    @ApiModelProperty(value = "预算科目id")
    private Long subid;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "科目名称", hidden = true)
    private String name;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "父id", hidden = true)
    private Long parentid;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "选中", hidden = true)
    private Boolean checked;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "层级", hidden = true)
    private Integer level;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "可追加 true:是，false:否", hidden = true)
    private Boolean addflag;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "费用追加 true:是，false:否", hidden = true)
    private Boolean costaddflag;

    @NotNull(message = "年度科目控制不能为空")
    @ApiParam(hidden = true)
    @ApiModelProperty(value = "年度科目控制", required = true)
    private Boolean yearsubjectcontrolflag;

    @NotNull(message = "可分解不能为空")
    @ApiParam(hidden = true)
    @ApiModelProperty(value = "可分解 true:是，false:否", required = true)
    private Boolean splitflag;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "费用分解 true:是，false:否")
    private Boolean costsplitflag;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "可拆借 true:是，false:否", hidden = true)
    private Boolean lendflag;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "费用拆借 true:是，false:否", hidden = true)
    private Boolean costlendflag;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "关联产品标识 true:是，false:否", hidden = true)
    private Boolean productagentflag;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "公式科目（通过公式计算） true:是，false:否", hidden = true)
    private Boolean formulaflag;

    @NotNull(message = "界面隐藏不能为空")
    @ApiParam(hidden = true)
    @ApiModelProperty(value = "界面隐藏 true:是，false:否", required = true)
    private Boolean hidden;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "操作", hidden = true)
    private String operator;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "本届码洋占比公式")
    private String ccratioformula;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "本届收入占比公式")
    private String revenueformula;

    @ApiModelProperty(value = "计算公式")
    private String formula;


    @ApiModelProperty(value = "本届码洋占比公式(只做展示)")
    private String showCcratioformula;

    @ApiModelProperty(value = "本届收入占比公式(只做展示)")
    private String showRevenueformula;

    @ApiModelProperty(value = "计算公式(只有展示)")
    private String showFormula;


    @ApiParam(hidden = true)
    @ApiModelProperty(value = "上届码洋占比公式", hidden = true)
    private String preccratioformula;

    @NotNull(message = "月度科目控制不能为空")
    @ApiParam(hidden = true)
    @ApiModelProperty(value = "月度科目控制", required = true)
    private Boolean monthcontrolflag;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "叶子节点标识默认为1", hidden = true)
    private Boolean leafflag;

    @NotNull(message = "年度动因控制不能为空")
    @ApiParam(hidden = true)
    @ApiModelProperty(value = "年度动因控制 true:是，false:否", required = true)
    private Boolean yearcontrolflag;

}
