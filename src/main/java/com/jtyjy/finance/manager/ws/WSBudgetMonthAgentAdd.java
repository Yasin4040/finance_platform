package com.jtyjy.finance.manager.ws;

import com.jtyjy.ecology.webservice.workflow.WorkflowBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 月度预算追加提交OA
 *
 * @author User
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WSBudgetMonthAgentAdd extends WorkflowBase {

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
     * 追加月份
     */
    private String zjyf;

    /**
     * 追加科目
     */
    private String zjkm;

    /**
     * 本月第几次追加
     */
    private String byzjcs;

    /**
     * 流程编号
     */
    private BigDecimal lcbh;

    /**
     * 申请人所属部门id
     */
    private String ssbm;

    /**
     * 是否商务部审批（权限下放用）
     */
    private int isswsp;

    /**
     * 附件
     */
    private String fj;

}
