package com.jtyjy.finance.manager.vo.application;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Description:
 * Created by ZiYao Lee on 2022/08/26.
 * Time: 16:56
 */
@Data
public class BudgetDetailsVO {
  // `subject_code` varchar(20) DEFAULT NULL COMMENT '科目编码',
  //`subject_name` varchar(20) DEFAULT NULL COMMENT '科目名称',
  //`deduction_amount`  decimal(10,2) DEFAULT NULL COMMENT '金额 根据提成类型+届别取提成明细所在行的“申请提成”',
    @ApiModelProperty(value = "id")
    private Long id;
    @ApiModelProperty(value = "科目主键")
    private Long subjectId;
    @ApiModelProperty(value = "科目编码")
    private String subjectCode;
    @ApiModelProperty(value = "科目名称")
    private String subjectName;
    @ApiModelProperty(value = "动因id")
    private Long motivationId;
    @ApiModelProperty(value = "动因名称")
    private String motivationName;
    @ApiModelProperty(value = "金额")
    private BigDecimal budgetAmount;

}
