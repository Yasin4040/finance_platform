package com.jtyjy.finance.manager.vo;

import com.jtyjy.finance.manager.bean.BudgetYearAgent;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Admin
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BudgetYearAgentVO extends BudgetYearAgent {

    @ApiModelProperty(value = "审核状态，-1：退回，0：保存，1：已提交（待审核），2：审核通过")
    private Integer requestStatus;

    @ApiModelProperty(value = "产品名称")
    private String productName;

    @ApiModelProperty(value = "产品类别名称")
    private String cateName;

    @ApiModelProperty(value = "基础科目Id")
    private Long baseSubjectId;

    @ApiModelProperty(value = "基础科目名称")
    private String baseSubjectName;

}
