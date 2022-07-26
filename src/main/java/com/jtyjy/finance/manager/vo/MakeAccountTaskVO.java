package com.jtyjy.finance.manager.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

/**
 * @author shubo
 */
@ApiModel(description = "做账任务VO")
@Data
public class MakeAccountTaskVO {

    @ApiParam(hidden = true)
    @ApiModelProperty(value="报销单号")
    private String reimcode;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="分单人")
    private String fdername;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="分单人id")
    private String fder;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="分单时间")
    private String fdtime;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="分单开票单位")
    private String bunitname;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="做账开票单位")
    private String planbunitname;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="单据接收人")
    private String curscanername;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="任务接收人")
    private String receivername;

    @ApiParam(hidden = true)
    @ApiModelProperty(value="任务接收时间")
    private String receivetime;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="是否做账")
    private Boolean accountstatus;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="做账会计")
    private String accountername;
    
    @ApiParam(hidden = true)
    @ApiModelProperty(value="做账时间")
    private String accounttime;

}
