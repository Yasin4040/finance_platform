package com.jtyjy.finance.manager.vo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.util.Date;

/**
 * @author shubo
 */
@ApiModel(description = "银行账户信息VO")
@Data
public class BankAccountVO {


    @ApiParam(hidden = true)
    @ApiModelProperty(value = "银行账户id")
    private Long id;

    @ApiParam(hidden = true)
    @NotBlank(message = "编号不能为空")
    @ApiModelProperty(value = "编号（对内账户为工号）")
    private String code;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "户名")
    private String accountName;

    @ApiParam(hidden = true)
    @NotBlank(message = "名称不能为空")
    @ApiModelProperty(value = "名称")
    private String pname;
    
    @ApiParam(hidden = true)
    @NotNull(message = "账户类型不能为空")
    @ApiModelProperty(value = "账户类型 1：对内，2：对外")
    private Integer accountType;

    @ApiParam(hidden = true)
    @NotBlank(message = "银行账号不能为空")
    @ApiModelProperty(value = "账号")
    private String bankAccount;

    @ApiParam(hidden = true)
    @NotNull(message = "工资账户不能为空")
    @ApiModelProperty(value = "工资账户 0：否 1：是")
    private Integer wagesFlag;

    @ApiParam(hidden = true)
    @NotBlank(message = "电子联行号不能为空")
    @ApiModelProperty(value = "电子联行号")
    private String branchCode;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "银行名称")
    private String branchName;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "省名称")
    private String province;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "城市名称")
    private String city;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "银行类型")
    private String bankName;

    @ApiParam(hidden = true)
    @NotNull(message = "停用标志不能为空")
    @ApiModelProperty(value = "停用标志 0：启用 1：停用")
    private Integer stopFlag;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiParam(hidden = true)
    @NotNull(message = "排序号不能为空")
    @ApiModelProperty(value = "排序号")
    private Integer orderNo;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "第三方系统id")
    private String outKey;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "部门id")
    private String deptId;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "部门名称")
    private String deptName;

    /**
     * 修改人
     */
    @ApiParam(value = "修改人")
    private String updateBy;

    /**
     * 更新时间
     */
    @ApiParam(value = "更新时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "查询条件")
    private String queryText;
}
