package com.jtyjy.finance.manager.vo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

/**
 * @author User
 */
@ApiModel(description = "预算单位VO")
@Data
public class BudgetUnitVO {


    @ApiParam(hidden = true)
    @ApiModelProperty(value = "单位账户主键ID")
    private Long id;

    @NotNull(message = "基础单位id不能为空")
    @ApiParam(hidden = true)
    @ApiModelProperty(value="基础单位id")
    private Long baseUnitId;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="基础单位名称")
    private String baseUnitName;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="名称")
    private String name;

    @NotNull(message = "届别id不能为空")
    @ApiParam(hidden = true)
    @ApiModelProperty(value="届别id")
    private Long yearId;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="届别名称")
    private String yearName;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="上级id")
    private Long parentId;

    @NotNull(message = "预算体系不能为空")
    @ApiParam(hidden = true)
    @ApiModelProperty(value="是否为预算体系0：否，1：是")
    private Integer budgetFlag;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="树id")
    private String pids;

    @NotBlank(message = "预算员不能为空")
    @ApiParam(hidden = true)
    @ApiModelProperty(value="预算员id")
    private String managers;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="预算员名称")
    private String managersName;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="预算员工号")
    private String managersCode;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="部门id")
    private String budgetDepts;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="部门名称")
    private String budgetDeptsName;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="人员id")
    private String budgetUsers;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="人员名称")
    private String budgetUsersName;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="人员工号")
    private String budgetUsersCode;
    
    @NotNull(message = "排序号不能为空")
    @ApiParam(hidden = true)
    @ApiModelProperty(value="排序号")
    private Integer orderNo;

    @NotNull(message = "单位类型不能为空")
    @ApiParam(hidden = true)
    @ApiModelProperty(value="单位类型")
    private Integer unitType;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="单位类型名称")
    private String unitTypeName;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="备注")
    private String remark;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="本届码洋占比公式")
    private String ccratioFormula;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="本届收入占比公式")
    private String revenueFormula;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="月结时间")
    private String monthEndTime;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="月结标志")
    private Boolean monthEndFlag;
}
