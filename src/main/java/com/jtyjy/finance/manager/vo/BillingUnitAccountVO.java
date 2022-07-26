package com.jtyjy.finance.manager.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author User
 */
@ApiModel(description = "单位账户信息VO")
@Data
public class BillingUnitAccountVO {


    @ApiModelProperty(value = "单位账户主键ID")
    private Long id;

    @ApiModelProperty(value = "开票单位主键ID")
    private Long billlingUnitId;

    @ApiModelProperty(value = "开票单位名称")
    private String billingUnitName;

    @ApiModelProperty(value = "停用标志 0：否 1：是")
    private String stopFlag;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "账号")
    private String bankAccount;
    
    @ApiModelProperty(value = "默认账户 0：否 1：是")
    private String defaultFlag;
    
    @ApiModelProperty(value = "电子联行号")
    private String branchCode;
    
    @ApiModelProperty(value = "银行名称")
    private String branchName;
    
    @ApiModelProperty(value = "省名称")
    private String province;
    
    @ApiModelProperty(value = "城市名称")
    private String city;
    
    @ApiModelProperty(value = "银行类型")
    private String bankName;
    
    @ApiModelProperty(value = "排序号")
    private Integer orderNo;
}
