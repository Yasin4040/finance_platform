package com.jtyjy.finance.manager.vo.application;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.jtyjy.finance.manager.vo.application.BudgetDetailsVO;
import com.jtyjy.finance.manager.vo.application.CommissionDetailsVO;
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
public class CommissionApplicationInfoVO {
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

    //基本信息
    /**
     * 部门名称
     */
    @ApiModelProperty(value = "部门名称")
    private String departmentName;

    /**
     * 支付事由 支付+“届别”+“月份”+“批次”+“提成/坏账”
     届别取“届别”字段；月份取“提成期间”中的月份；“提成/坏账”根据“坏账（是/否）”判断，若是则显示“坏账”；否则显示“提成”。
     */
    @ApiModelProperty(value = "支付事由")
    private String paymentReason;
    /**
     * 创建时间
     */
    @ApiModelProperty(value = "制表日期")
    private Date createTime;
    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remarks;

    //提成明细
    List<CommissionDetailsVO> commissionList;
    //预算明细
    List<BudgetDetailsVO> budgetList;

    @ApiModelProperty(value = "合计发放 费用发放+提成发放+公户+个卡")
    private BigDecimal totalDistributionAmount;
    @ApiModelProperty(value = "员工个体户未发放金额")
    private BigDecimal individualUnissuedAmount;
    @ApiModelProperty(value = "外部户发放金额")
    private BigDecimal outerDistributionAmount;
    //发放 明细
    List<DistributionDetailsVO> distributionList;
}
