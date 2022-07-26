package com.jtyjy.finance.manager.ws;

import com.jtyjy.ecology.webservice.workflow.WorkflowBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
public class WSBudgetMonthAgentAddDetail extends WorkflowBase {

    /**
     * 追加动因
     */
    private String zjdy;

    /**
     * 追加金额
     */
    private BigDecimal zjje;

    /**
     * 追加科目
     */
    private String zjkm;

    /**
     * 追加原因
     */
    private String zjyy;

    /**
     * 年度预算结余
     */
    private BigDecimal ndysjy;

    /**
     * 月初预算总额
     */
    private BigDecimal ycysje;

    /**
     * 月度已追加
     */
    private BigDecimal yzj;

    /**
     * 月度已使用
     */
    private BigDecimal ysy;

    /**
     * 月度预算结余
     */
    private BigDecimal ydysjy;

    /**
     * 本次追加后月度预算结余
     */
    private BigDecimal zjhjy;
}
