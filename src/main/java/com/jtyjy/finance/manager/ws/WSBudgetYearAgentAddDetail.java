package com.jtyjy.finance.manager.ws;

import com.jtyjy.ecology.webservice.workflow.WorkflowBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
public class WSBudgetYearAgentAddDetail extends WorkflowBase {

    /**
     * 明细id
     */
    private String sjid;

    /**
     * 追加类型
     */
    private String zjlx;

    /**
     * 动因
     */
    private String dymc;

    /**
     * 追加科目
     */
    private String zjkm;

    /**
     * 追加金额
     */
    private BigDecimal zjje;

    /**
     * 追加理由
     */
    private String zjly;

    /**
     * 是否同时追加月度预算
     */
    private String sfzjydys;

    /**
     * 追加月份
     */
    private String zjyf;

    /**
     * 追加月度金额
     */
    private String zjydje;

    /**
     * 年初预算(科目)
     */
    private String ncys;

    /**
     * 累计执行(科目)
     */
    private String ljzx;

    /**
     * 本次追加后年度预算总额(科目)
     */
    private String bczjhndysye;

    /**
     * 是否免罚 false否 true是
     */
    private String sfsqmf;

    /**
     * 免罚原因
     */
    private String mfly;

    @Override
    public String getWfid() {
        //return "3084";
        return "";
    }
}
