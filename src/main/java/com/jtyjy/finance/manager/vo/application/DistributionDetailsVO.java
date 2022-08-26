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
public class DistributionDetailsVO {
  //
  //  `Payer` varchar(20) DEFAULT NULL COMMENT '付款单位 当完成【超额导入】以及【确认完成】之后，根据【部门发放明细】、【员工个体户发放明细】取付款单位',
  //`expense_distribution_amount`  decimal(10,2) DEFAULT NULL COMMENT '(员工发放金额)费用发放  【超额导入】完成后，根据费用导入明细表中的数据获取该批次该事业部该发放单位的费用发放合计。',
  //`commission_distribution_amount`  decimal(10,2) DEFAULT NULL COMMENT '(员工发放金额)提成发放 【超额导入】完成后，根据部门发放明细表中的数据获取该批次该事业部该发放单位的提成发放合计。',
  //`individual_public_amount` decimal(10,2) DEFAULT NULL COMMENT '(员工个体户发放金额)-公户 【确认完成】后，根据发放明细表中取发放类型为“正常”且账户类型为“公户”的数据，统计该批次该部门该发放单位的当期发放总额的合计。',
  //`individual_person_amount`  decimal(10,2) DEFAULT NULL COMMENT '(员工个体户发放金额)-个卡 【确认完成】后，根据发放明细表中取发放类型为“正常”且账户类型为“个卡”的数据，统计该批次该部门该发放单位的当期发放总额的合计。',
  //`total_distribution_amount`  decimal(10,2) DEFAULT NULL COMMENT '费用发放+提成发放+公户+个卡',
  //`individual_unissued_amount` decimal(10,2) DEFAULT NULL COMMENT '员工个体户明细表中该批次该部门发放状态为“停发”的金额合计。。',
  //`outer_distribution_amount`  decimal(10,2) DEFAULT NULL COMMENT '员工个体户未发放金额【超额导入】完成后，根据部门发放明细表中的数据获取该批次该事业部该发放单位的提成发放合计。',  @ApiModelProperty(value = "科目编码")
    @ApiModelProperty(value = "付款单位")
    private String payer;
    @ApiModelProperty(value = "(员工发放金额)提成发放")
    private BigDecimal commissionDistributionAmount;
    @ApiModelProperty(value = "(员工发放金额)费用发放")
    private BigDecimal expenseDistributionAmount;
    @ApiModelProperty(value = "(员工个体户发放金额)-公户")
    private BigDecimal individualPublicAmount;
    @ApiModelProperty(value = "(员工个体户发放金额)-个卡")
    private BigDecimal individualPersonAmount;

    @ApiModelProperty(value = "合计发放 费用发放+提成发放+公户+个卡")
    private BigDecimal totalDistributionAmount;
    @ApiModelProperty(value = "员工个体户未发放金额")
    private BigDecimal individualUnissuedAmount;
    @ApiModelProperty(value = "外部户发放金额")
    private BigDecimal outerDistributionAmount;

}
