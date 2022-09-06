package com.jtyjy.finance.manager.vo.application;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author 李子耀
 * @Date 2022/9/6 10:35
 */
@Data
public class BudgetSubjectVO {

    @ApiModelProperty(value = "预算单位Id")
    private Long unitId;

    @ApiModelProperty(value = "预算单位名称")
    private String unitName;

    @ApiModelProperty(value = "预算科目Id")
    private Long subjectId;
    @ApiModelProperty(value = "预算科目编码")
    private String subjectCode;
    @ApiModelProperty(value = "预算科目金蝶编码")
    private String jindiecode;
    @ApiModelProperty(value = "预算科目金蝶名称")
    private String jindieName;

    @ApiModelProperty(value = "预算科目名称")
    private String subjectName;

    @ApiModelProperty(value = "动因Id")
    private Long agentId;

    @ApiModelProperty(value = "动因名称")
    private String agentName;
}
