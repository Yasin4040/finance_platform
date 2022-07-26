package com.jtyjy.finance.manager.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

/**
 * @author User
 */
@ApiModel(description = "报销明细VO")
@Data
public class BxDetailVO {

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "报销单位名称")
    private String bxunitname;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="报销类型")
    private Integer bxtype;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="报销单号")
    private String reimcode;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="报销状态")
    private Integer reuqeststatus;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="报销状态中文")
    private String reuqeststatusText;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="报销人")
    private String bxr;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="开票单位")
    private String bunitname;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="付款单位")
    private String payunitname;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="报销主表id")
    private Long reimbursementid;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="报销金额")
    private Double reimmoney;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="月度动因名称")
    private String monthagentname;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="摘要")
    private String remark;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="科目名称")
    private String subjectname;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="计入执行标志")
    private Boolean reimflag;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="报销种类")
    private String reimflagtype;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="备注")
    private String baseunitid;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="届别")
    private String yearname;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="月份")
    private String monthname;
        
    @ApiParam(hidden = true)
    @ApiModelProperty(value="预算单位")
    private String unitname;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="月度科目预算")
    private Double monthagentmoney;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="月度科目未执行金额")
    private Double monthagentunmoney;    
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="年度动因预算")
    private Double yearagentmoney;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="年度动因未执行金额")
    private Double yearagentunmoney;
}
