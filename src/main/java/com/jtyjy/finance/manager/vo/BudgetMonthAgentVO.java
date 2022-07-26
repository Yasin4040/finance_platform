package com.jtyjy.finance.manager.vo;

import com.jtyjy.finance.manager.bean.BudgetMonthAgent;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * @author Admin
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BudgetMonthAgentVO extends BudgetMonthAgent {

    @ApiModelProperty(value = "预算科目名称")
    private String subjectName;

    @ApiModelProperty(value = "月份")
    private String period;

    @ApiModelProperty(value = "审核状态，-1：退回，0：保存，1：已提交（待审核），2：审核通过")
    private Integer requestStatus;

    @ApiModelProperty(value = "预算单位名称")
    private String unitName;

    @ApiModelProperty(value = "未执行")
    private BigDecimal unExecute;

    @ApiModelProperty(value = "年度合计")
    private BigDecimal yearMoneyTotal;

    @ApiModelProperty(value = "产品类别")
    private String productCate;

    @ApiModelProperty(value = "产品名称")
    private String productName;

}
