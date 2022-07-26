package com.jtyjy.finance.manager.dto;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorder;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderAllocated;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderCash;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderDetail;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderEntertain;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderPayment;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderTrans;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderTravel;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审核通过请求参数
 * @author User
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckPassRequest {
    
    @NotNull(message = "报销单主键不能为空")
    @ApiModelProperty(value = "报销单主键",required = true)
    private Long orderId;
    
	//明细表
    @NotNull(message = "报销明细不能为空")
	@ApiModelProperty(value = "报销明细",required = true)
	private List<BudgetReimbursementorderDetail> orderDetail;
	
	//转账表
	@ApiModelProperty(value = "转账信息",required = true)
	private List<BudgetReimbursementorderTrans> orderTrans;
    
    @ApiModelProperty(value = "执行操作，报销审核必送（1：票面通过；2：预算通过）")
    private String option;

    @ApiModelProperty(value="删除的转账单id")
    private List<Long> deletedTranList = new ArrayList<>();
}
