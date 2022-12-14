package com.jtyjy.finance.manager.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author 袁前兼
 * @Date 2021/6/11 14:32
 */
@Data
public class YearAgentLendDTO {

    @ApiModelProperty(value = "是否跨部门")
    private Boolean isAcross = false;

    @ApiModelProperty(value = "主键Id")
    private Long id;

    @ApiModelProperty(value = "届别id")
    @NotNull(message = "届别不能为空")
    private Long yearId;

    @ApiModelProperty(value = "部门Id(非跨部门时必传)")
    private Long unitId;

    @ApiModelProperty(value = "文件URL")
    private String fileUrl;

    @ApiModelProperty(value = "文件原名称")
    private String fileOriginName;

    @ApiModelProperty(value = "oa密码")
    private String oaPassword;

    @ApiModelProperty(value = "是否提交")
    private Boolean isSubmit = false;

    @NotEmpty(message = "拆借明细不能为空")
    @ApiModelProperty(value = "拆借明细")
    private List<YearAgentLendDetailDTO> details;

}
