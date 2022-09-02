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
public class CommissionDetailsVO {
    // `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    //    `application_id` bigint(20) NOT NULL  COMMENT '申请单id',
    // `commission_type` varchar(20) DEFAULT NULL COMMENT '提成类型 取导入数据中的“提成类型”',
    //  `category` varchar(256) DEFAULT NULL COMMENT '归属类别 取导入数据中的“界别”',
    //`apply_for_commission`  decimal(10,2) DEFAULT NULL COMMENT '申请提成',
    //`deduction_amount`  decimal(10,2) DEFAULT NULL COMMENT '扣款金额 申请提成”-“实发金额”',
    //`actual_amount`  decimal(10,2) DEFAULT NULL COMMENT '实发金额 取导入数据中的“实发提成”',
    // `create_by` varchar(20) DEFAULT NULL COMMENT '创建人',
    @ApiModelProperty(value = "id")
    private Long id;
    @ApiModelProperty(value = "提成类型名称")
    private String commissionTypeName;
    @ApiModelProperty(value = "归属届别")
    private String yearId;
    @ApiModelProperty(value = "申请提成")
    private BigDecimal applyAmount;
    @ApiModelProperty(value = "扣款金额")
    private BigDecimal deductionAmount;
    @ApiModelProperty(value = "实发金额")
    private BigDecimal actualAmount;
}
