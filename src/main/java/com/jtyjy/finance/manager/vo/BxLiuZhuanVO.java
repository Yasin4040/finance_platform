package com.jtyjy.finance.manager.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author User
 */
@ApiModel(description = "报销流转信息VO")
@Data
public class BxLiuZhuanVO {


    @ApiModelProperty(value = "报销单主键")
    private Long reimbursementid;

    @ApiModelProperty(value = "报销单编号", hidden = false, required = false)
    private String reimcode;

    @ApiModelProperty(value = "接收人")
    private String scanername;

    @ApiModelProperty(value = "操作时间")
    private String scantime;

    @ApiModelProperty(value = "操作情况")
    private String scantype;

    @ApiModelProperty(value = "处理情况")
    private String verifyflag;
    
    @ApiModelProperty(value = "处理信息")
    private String scanresult;
    
}
