package com.jtyjy.finance.manager.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author User
 */
@ApiModel(description = "开票单位信息VO")
@Data
public class BillingUnitVO {


    @ApiModelProperty(value = "开票单位主键ID")
    private Long id;

    @ApiModelProperty(value = "编号")
    private String code;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "全称")
    private String fullName;
    
    @ApiModelProperty(value = "公司发票(1)、无票(0)")
    private String billingUnitType;

    @ApiModelProperty(value = "是否法人单位 0：否 1：是")
    private Integer corporation;

    @ApiModelProperty(value = "内部单位标志 0：内部 1：外部【默认】")
    private Integer ownflag;
    
    @ApiModelProperty(value = "停用标识 0：启用 1：停用")
    private Integer stopFlag;
    
    @ApiModelProperty(value = "排序号")
    private String orderNo;
    
    @ApiModelProperty(value = "预算员ID（多个,隔开）")
    private String budgeters;
    
    @ApiModelProperty(value = "会计ID（多个,隔开）")
    private String accountants;

    @ApiModelProperty(value = "预算员名称（多个,隔开）")
    private String budgetersName;

    @ApiModelProperty(value = "会计名称（多个,隔开）")
    private String accountantsName;

    @ApiModelProperty(value = "预算员工号（多个,隔开）")
    private String budgetersCode;

    @ApiModelProperty(value = "会计工号（多个,隔开）")
    private String accountantsCode;
    
    @ApiModelProperty(value = "备注")
    private String remark;
    
    @ApiModelProperty(value = "名称首拼")
    private String firstspell;
    
    @ApiModelProperty(value = "名称全拼")
    private String fullSpell;
    
    @ApiModelProperty(value = "第三方系统Id")
    private String outkey;
    
}
