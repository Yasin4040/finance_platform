package com.jtyjy.finance.manager.ws;

import com.jtyjy.ecology.webservice.workflow.WorkflowBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * webservice预算追加参数类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WSBudgetYearAgentAdd extends WorkflowBase {

    /**
     * 预算员
     */
    private Integer sqr;

    /**
     * 预算单位
     */
    private String ysdw;

    /**
     * 预算届别
     */
    private String ysjb;

    /**
     * 申请日期
     */
    private String sqrq;

    /**
     * 追加科目
     */
    private String zjkm;

    /**
     * 本届别第几次追加
     */
    private String zjcs;

    /**
     * 年初预算(科目)
     */
    private String ncys;

    /**
     * 累计追加(科目)
     */
    private String ljzj;

    /**
     * 累计执行(科目)
     */
    private String ljzx;

    /**
     * 是否商务部审批（权限下放用）
     */
    private int isswsp;

    /**
     * 本次追加后年度预算总额(科目)
     */
    private String bczjhndysze;

    /**
     * 本次追加后年度预算余额(科目)
     */
    private String bczjhndysye;

    /**
     * 申请人所属部门id
     */
    private String ssbm;


    private String fj;

}
