package com.jtyjy.finance.manager.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author 袁前兼
 * @Date 2021/6/21 13:47
 */
@Data
public class BudgetLendMoneyVO {

    @ApiModelProperty(value = "主键Id")
    private Long id;

    @ApiModelProperty(value = "（借款人）员工id 外部人员为空")
    private String empId;

    @ApiModelProperty(value = "（借款人）员工工号 外部人为银行账户编号")
    private String empNo;

    @ApiModelProperty(value = "（借款人）员工姓名 外部人为银行账户户名")
    private String empName;

    @ApiModelProperty(value = "借款单号")
    private String lendMoneyCode;

    @ApiModelProperty(value = "是否备用金")
    private Boolean isbyj;

    @ApiModelProperty(value = "借款类型")
    private Integer lendType;

    @ApiModelProperty(value = "借款类型描述")
    private String lendTypeDesc;

    @ApiModelProperty(value = "借款金额（2位有效数字）(本金)")
    private BigDecimal lendMoney;

    @ApiModelProperty(value = "产生的利息 （每天计算利息，不是利滚利）")
    private BigDecimal interestMoney;

    @ApiModelProperty(value = "借款时间(到天)")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date lendDate;

    @ApiModelProperty(value = "计划还款日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date planPayDate;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "借款事由")
    private String remark;

    @ApiModelProperty(value = "流程id")
    private String requestId;

    @ApiModelProperty(value = "已还金额（已还本金 + 已还利息）")
    private BigDecimal repaidMoney;

    @ApiModelProperty(value = "未还金额（本金 + 利息 - 已还本金 - 已还利息）")
    private BigDecimal unpaidMoney;

    @ApiModelProperty(value = "还款状态 false(未还清) true(已还清)")
    private Boolean paymentStatus;

}
