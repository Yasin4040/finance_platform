package com.jtyjy.finance.manager.vo.application;

import com.jtyjy.finance.manager.bean.BudgetCommonAttachment;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Description:
 * Created by ZiYao Lee on 2022/08/26.
 * Time: 14:42
 */
@Data
public class CommissionApplicationInfoUpdateVO {
    /**
     * 申请单id
     */
    @ApiModelProperty(value = "申请单id")
    private Long applicationId;
    /**
     * 提成id主表
     */
    @ApiModelProperty(value = "提成主表订单id")
    private Long extractSumId;
    /**
     * 提成编码
     */
    @ApiModelProperty(value = "提成编码")
    private String extractSumNo;


    @ApiModelProperty(value = "支付事由")
    private String paymentReason;

    @ApiModelProperty(value = "备注")
    private String remarks;

    //预算明细
    List<BudgetDetailsVO> budgetList;

    //预算明细
    List<BudgetCommonAttachmentVO> attachmentList;


}
