package com.jtyjy.finance.manager.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author 袁前兼
 * @Date 2021/6/11 14:32
 */
@Data
public class YearAgentLendVO {


    @ApiModelProperty(value = "主键Id")
    private Long id;

    @ApiModelProperty(value = "届别id")
    private Long yearId;

    @ApiModelProperty(value = "届别")
    private String yearName;

    @ApiModelProperty(value = "部门Id(非跨部门时必传)")
    private Long unitId;

    @ApiModelProperty(value = "部门名称")
    private String unitName;

    @ApiModelProperty(value = "文件URL")
    private String fileUrl;

    @ApiModelProperty(value = "文件原名称")
    private String fileOriginName;

    @ApiModelProperty(value = "oa密码")
    private String oaPassword;

    @ApiModelProperty(value = "拆借明细")
    private List<YearAgentLendDetailVO> details;

}
