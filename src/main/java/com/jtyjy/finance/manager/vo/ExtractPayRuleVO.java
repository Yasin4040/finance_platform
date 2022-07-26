package com.jtyjy.finance.manager.vo;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel
@Data
public class ExtractPayRuleVO {
	
	@ApiModelProperty(hidden = false,value = "id")
	private Long id;
	
	/**
     * 是否终止 true 终止 ， 可以提前终止
     */
    @ApiModelProperty(hidden = false,value = "是否终止。true终止 false不终止")
	private Boolean endflag = false;
    
    
    @ApiModelProperty(hidden = false,value = "规则名称")
	private String name;
    
    @ApiModelProperty(hidden = false,value = "工资发放单位id（以,分隔）",dataType = "String")
    @TableField(value = "billunitids")
	private String billunitids;

    /**
     * 生效日期
     */
    @ApiModelProperty(hidden = false,value = "生效日期(年-月-日)")
	private String effectdate;

    /**
     * 临界金额
     */
    @ApiModelProperty(hidden = false,value = "临界金额")
	private BigDecimal je;

    /**
     * 银行账户id
     */
    @ApiModelProperty(hidden = false,value = "避税发放单位账户id")
	private Long personunitid;

    
    @ApiModelProperty(hidden = false,value = "避税发放单位名称")
    private String personunitname;

    /**
     * 创建时间
     */
    @ApiModelProperty(hidden = false,value = "创建时间")
	private String createtime;

    /**
     * 备注
     */ 
    @ApiModelProperty(hidden = false,value = "备注")
	private String remark;

    /**
     * 工资发放单位名称
     */
    @ApiModelProperty(hidden = false,value = "工资发放单位名称")
	private String salarypayunitnames;
}
