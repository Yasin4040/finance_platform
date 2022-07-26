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
@ApiModel(description = "稿费作者VO")
@Data
public class BudgetAuthorVO {

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "稿费作者id")
    private Long id;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="编号")
    private String code;

    @NotBlank(message = "编号不能为空")
    @ApiParam(hidden = true)
    @ApiModelProperty(value="作者")
    private String author;

    @NotNull(message = "是否公司员工不能为空")
    @ApiParam(hidden = true)
    @ApiModelProperty(value="是否公司员工")
    private Boolean authortype;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="身份证号(个人作者)")
    private String idnumber;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="纳税人识别号(单位作者）")
    private String taxpayernumber;

    @NotBlank(message = "所在单位不能为空")
    @ApiParam(hidden = true)
    @ApiModelProperty(value="所在单位")
    private String company;

    @NotBlank(message = "电子联行号不能为空")
    @ApiParam(hidden = true)
    @ApiModelProperty(value="电子联行号")
    private String branchcode;

    @NotBlank(message = "银行账号不能为空")
    @ApiParam(hidden = true)
    @ApiModelProperty(value="银行账号")
    private String bankaccount;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="银行类型")
    private String banktype;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="省名称")
    private String province;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="市名称")
    private String city;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="开户行名称")
    private String bankname;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="备注")
    private String remark;
}
