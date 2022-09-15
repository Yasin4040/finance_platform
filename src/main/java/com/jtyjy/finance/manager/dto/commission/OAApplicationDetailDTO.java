package com.jtyjy.finance.manager.dto.commission;

import com.jtyjy.ecology.webservice.workflow.WorkflowBase;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Description:
 * Created by ZiYao Lee on 2022/09/15.
 * Time: 15:20
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OAApplicationDetailDTO extends WorkflowBase {
//    /**
//     * 申请单id
//     */
//    @ApiModelProperty(value = "申请人")
//    private String sqr;
//    @ApiModelProperty(value = "部门")
//    private String bm;
//    @ApiModelProperty(value = "制表日期")
//    private Date zbrq;
//    @ApiModelProperty(value = "支付事由")
//    private String zfsy;
//    @ApiModelProperty(value = "备注")
//    private String bz;
//    @ApiModelProperty(value = "提成编码")
//    private String bh;

    @ApiModelProperty(value = "提成类型")
    private String tclx;
    @ApiModelProperty(value = "归属类型")
    private String gslx;
    @ApiModelProperty(value = "申请提成")
    private BigDecimal sqtc;
    @ApiModelProperty(value = "扣款金额")
    private BigDecimal kkje;
    @ApiModelProperty(value = "实发金额")
    private BigDecimal sfje;

}
