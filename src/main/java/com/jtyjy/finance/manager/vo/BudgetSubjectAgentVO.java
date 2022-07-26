package com.jtyjy.finance.manager.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author 袁前兼
 * @Date 2021/6/3 10:35
 */
@Data
public class BudgetSubjectAgentVO {

    @ApiModelProperty(value = "预算单位Id")
    private Long unitId;

    @ApiModelProperty(value = "预算单位名称")
    private String unitName;

    @ApiModelProperty(value = "预算科目Id")
    private Long subjectId;

    @ApiModelProperty(value = "预算科目名称")
    private String subjectName;

    @ApiModelProperty(value = "动因Id")
    private Long agentId;

    @ApiModelProperty(value = "动因名称")
    private String agentName;
}
