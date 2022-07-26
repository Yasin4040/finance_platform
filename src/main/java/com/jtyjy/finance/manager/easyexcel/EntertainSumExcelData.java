package com.jtyjy.finance.manager.easyexcel;

import java.math.BigDecimal;

import com.alibaba.excel.annotation.ExcelProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 招待汇总报表
 * @author User
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntertainSumExcelData {
	@ExcelProperty(value="报销单号")
	private String reimcode;
	
	@ExcelProperty(value="预算单位")
	private String unitName; 
	
	@ExcelProperty(value="月份")
	private String bxMonth;
	
	@ExcelProperty(value="报销人")
	private String bxr;
	
	@ExcelProperty(value="招待日期")
	private String zdDate;
	
	@ExcelProperty(value="餐费人数")
	private Integer cfrs;
	
	@ExcelProperty(value="餐费金额")
	private BigDecimal cfje;

	@ExcelProperty(value="餐费标准")
	private BigDecimal cfbz;
	   
    @ExcelProperty(value="住宿人数")
    private Integer zsrs;
    
    @ExcelProperty(value="住宿标准")
    private BigDecimal zsbz;
    
    @ExcelProperty(value="住宿间数")
    private Integer zsjs;
    
    @ExcelProperty(value="住宿金额")
    private BigDecimal zsje;

    @ExcelProperty(value="其他")
    private BigDecimal other;
    
    @ExcelProperty(value="宣传品费")
    private BigDecimal xcpf;
    
    @ExcelProperty(value="报销动因")
    private String agentName;  
    
    @ExcelProperty(value="备注")
    private String remark;
    
}
