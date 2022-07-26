package com.jtyjy.finance.manager.ws;

import com.jtyjy.ecology.webservice.workflow.WorkflowBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 年度预算拆借
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BudgetYearAgentLending extends WorkflowBase {

    /**
     * 流程编号
     */
    private String lcbh;

    /**
     * 预算员
     */
    private String sqr;

    /**
     * 申请人所属部门id
     */
    private String ssbm;

    /**
     * 预算单位（拆进）
     * 需求变更：跨部门拆借，原来的预算单位字段现在当做拆进的预算单位
     */
    private String ysdw;

    /**
     * 预算单位(拆出)
     */
    private String ccysdw;

    /**
     * 预算届别
     */
    private String ysjb;

    /**
     * 申请日期
     */
    private String sqrq;

    /**
     * 本届第几次拆借
     */
    private Integer cjcs;

    /**
     * 拆借金额
     */
    private BigDecimal cjje;

    /**
     * 拆进科目
     */
    private String cjkm;

    /**
     * 拆出科目
     */
    private String cckm;

    /**
     * 拆进动因
     */
    private String cjdy;

    /**
     * 附件
     */
    private String fj;

    /**
     * 拆出动因
     */
    private String ccdy;

    /**
     * 拆借后拆进科目年度预算余额
     */
    private BigDecimal cjkmye;

    /**
     * 拆借后拆出科目年度预算余额
     */
    private BigDecimal cckmye;

    /**
     * 拆借原因
     */
    private String cjyy;

}
