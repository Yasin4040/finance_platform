package com.jtyjy.finance.manager.vo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

/**
 * 预算科目信息
 * shubo
 */
@ApiModel(description="预算科目信息")
@Data
public class SubjectInfoVO {
    @ApiParam(hidden = true)
	@ApiModelProperty(value="预算科目id",required=true)
	private Long id;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="预算科目code",required=true)
	private String code;

    @NotNull(message = "基础科目id不能为空")
    @ApiParam(hidden = true)
    @ApiModelProperty(value="基础科目id",required=true)
	private Long subjectid;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "上级id（根节点为0）")
    private Long parentid;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "层级 最上层为1")
    private Integer level;

    @NotNull(message = "届别id不能为空")
    @ApiParam(hidden = true)
    @ApiModelProperty(value="届别id",required=true)
	private Long yearid;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="金蝶科目代码")
    private String jindiecode;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="金蝶科目名称")
    private String jindiename;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(hidden=true, value="届别名称")
	private String yearname;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="基础科目名称")
    private String basename;
	
    //默认和基础科目名称一样
    @ApiParam(hidden = true)
    @ApiModelProperty(hidden=true, value="预算科目名称")
	private String name;
	
	//辅助性指标标识 true:是，false:否
    @ApiParam(hidden = true)
    @ApiModelProperty(value="辅助性指标标识",required=true)
	private Boolean assistflag;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="辅助性指标标识中文",required=true)
    private String assistflagstr;
	
	//向上汇总标识  true:是，false:否
    @ApiParam(hidden = true)
    @ApiModelProperty(value="向上汇总标识",required=true)
	private Boolean upsumflag;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="向上汇总标识中文",required=true)
    private String upsumflagstr;
	
	//费用分解 true:是，false:否
    @ApiParam(hidden = true)
    @ApiModelProperty(value="费用分解标识",required=true)
	private Boolean costsplitflag;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="费用分解标识中文",required=true)
    private String costsplitflagstr;
	
	//费用拆借 true:是，false:否
    @ApiParam(hidden = true)
    @ApiModelProperty(value="费用拆借标识",required=true)
	private Boolean costlendflag;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="费用拆借标识中文",required=true)
    private String costlendflagstr;
	
	//关联产品标识true:是，false:否
    @ApiParam(hidden = true)
    @ApiModelProperty(value="关联产品标识",required=true)
	private Boolean jointproductflag;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="关联产品标识中文",required=true)
    private String jointproductflagstr;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="公式标识")
    private Boolean formulaflag;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="公式标识中文")
    private String formulaflagstr;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(hidden=true, value="产品分类id")
	private String procategoryid;

    @ApiParam(hidden = true)
    @ApiModelProperty(hidden=true, value="产品分类名称")
	private String procategoryname;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="费用追加标识")
    private Boolean costaddflag;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="费用追加中文")
    private String costaddflagstr;

    @NotNull(message = "届别id不能为空")
    @ApiParam(hidden = true)
    @ApiModelProperty(value="停用标识 0：启用 1：停用")
    private Boolean stopflag;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="停用标识中文")
    private String stopflagstr;
    
	//年度预算计划类型(链接数据)  1:码洋计划、2:收入计划、3:成本动因、4:发样计划、5:配赠计划
    @ApiParam(hidden = true)
    @ApiModelProperty(value="年度预算计划类型",required=true,allowableValues="1,2,3,4,5")
	private Integer yearplantype;
	
	//计算公式
    @ApiParam(hidden = true)
    @ApiModelProperty(value="计算公式")
	private String formula;
	
    //计算顺序
    @ApiParam(hidden = true)
    @ApiModelProperty(value="计算顺序")
	private Integer formulaorderno;

    @NotNull(message = "排序号不能为空")
    @ApiParam(hidden = true)
    @ApiModelProperty(value="排序号")
	private Integer orderno;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="备注")
	private String remark;
    

	
}
