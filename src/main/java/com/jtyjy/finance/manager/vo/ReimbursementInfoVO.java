package com.jtyjy.finance.manager.vo;

import com.baomidou.mybatisplus.annotation.TableField;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

/**
 * @author User
 */
@ApiModel(description = "报销信息VO")
@Data
public class ReimbursementInfoVO {

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "二维码")
    private String qrcode;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "报销主表id")
    private Long id;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="报销单号")
    private String reimcode;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="报销状态")
    private Integer reuqeststatus;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="报销状态中文")
    private String reuqeststatus_dictname;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="届别id")
    private Integer yearid;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="月份id")
    private Integer monthid;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="届别名称")
    private String yearname;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="月份名称")
    private String monthname;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="预算单位id")
    private Long unitid;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="预算单位名称")
    private String ysdw;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="报销人id")
    private String reimperonsid;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="报销人名称")
    private String bxr;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="报销日期（yyyy-mm-dd）")
    private String bxrq;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="报销金额")
    private Double bxje;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="不计入执行报销金额")
    private Double nonreimmoney;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="冲账金额")
    private Double czje;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="转账金额")
    private Double zzje;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="现金金额")
    private Double xjje;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="划拨金额")
    private Double hbje;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="其他金额")
    private Double othermoney;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="附件张数")
    private Integer fjzs;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="月度动因名称")
    private String monthagentname;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="备注")
    private String remark;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="基础单位名称")
    private Long baseunitid;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="创建时间")
    private String createtime;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="更新时间")
    private String updatetime;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="申请人id")
    private String applicantid;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="申请人名称")
    private String applicantame;
        
    @ApiParam(hidden = true)
    @ApiModelProperty(value="报销类型")
    private Integer bxtype;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="报销类型名称")
    private String bxtype_dictname;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="出差人员")
    private String traveler;    
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="出差理由")
    private String travelreason;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="当前接收状态名称")
    private String curscanstatusname;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="当前接收人")
    private String curscanername;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="审核通过时间")
    private String verifytime;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="提交时间")
    private String submittime;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value = "接收状态")
    private Integer receivestatus;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="接收时间")
    private String receivetime; 
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="申请时间")
    private String applicanttime;

    @ApiParam(hidden = true)
    @ApiModelProperty(value = "票面审核状态")
    private Boolean parverifystatus;
    
    @ApiModelProperty(value = "预算审核状态")
    private Boolean budgetverifystatus;
    
    @ApiModelProperty(value = "分单扫描状态")
    private Boolean fdstatus;
    
    @ApiModelProperty(value = "分单确认状态")
    private Boolean accountstatus;
    
    
    @ApiModelProperty(value = "出纳付款状态")
    private Boolean cashierpaymentreceivestatus;
    
    @ApiModelProperty(value = "会计做账状态")
    private Boolean account1receivestatus;
    
    @ApiModelProperty(value = "凭证审核状态")
    private Boolean voucherauditreceivestatus;
    
    @ApiModelProperty(value = "法人公司抽单状态")
    private Boolean drawbillreceivestatus;
    
    @ApiModelProperty(value = "结束流转状态", hidden = false, required = false)
    @TableField(value = "endreceivestatus")
    private Boolean endreceivestatus;
    
    @ApiModelProperty(hidden = true)
    private Boolean financialmanagestatus;
    @ApiModelProperty(hidden = true)
    private Boolean financialmanagereceivestatus;
    @ApiModelProperty(hidden = true)
    private Boolean generalmanagestatus;
    @ApiModelProperty(hidden = true)
    private Boolean generalmanagereceivestatus;
    @ApiModelProperty(hidden = true)
    private String workFlowStep;
    @ApiModelProperty(value = "财务总监审核接收状态。-1：不需此环节 0：否 1：是", hidden = false, required = false)
    private Integer financialManagerVerifyType;
    @ApiModelProperty(value = "总经理审核接收状态。-1：不需此环节 0：否 1：是", hidden = false, required = false)
    private Integer generalManagerVerifyType;
}
