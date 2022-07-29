package com.jtyjy.finance.manager.easyexcel;

import java.math.BigDecimal;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 总报表
 * @author User
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TravelSumExcelData {
	@ExcelProperty(value="报销单号")
	private String reimcode;
	
	@ExcelProperty(value="预算单位")
	private String unitName; 
	
	@ExcelProperty(value="月份")
	private String bxMonth;
	
	@ExcelProperty(value="报销人")
	private String bxr;
	   
    @ExcelProperty(value="出差人员")
    private String traveler; 
    
    @ExcelProperty(value="出差人数")
    private Integer travelerNum;
    
	@ExcelProperty(value="出差期间")
	private String travelPeriod;
	
	@ExcelProperty(value="出差天数")
	private BigDecimal travelDay;
	
	@ExcelProperty(value="小计")
	private BigDecimal totalAmt;

	@ExcelProperty(value="长途交通费")
	private BigDecimal longAmt;
	   
    @ExcelProperty(value="市内交通费")
    private BigDecimal cityAmt;
    
    @ExcelProperty(value="住宿费")
    private BigDecimal hotelAmt;
    
    @ExcelProperty(value="出差天数")
    private BigDecimal subsidyDay;
    
    @ExcelProperty(value="出差标准")
    private BigDecimal subsidyBz;

    @ExcelProperty(value="出差金额")
    private BigDecimal subsidyAmt;
    
    @ExcelProperty(value="其他")
    private BigDecimal otherAmt;
    
    @ExcelProperty(value="出差事由")
    private String travelReason;
    
    @ExcelProperty(value="报销动因")
    private String agentName;

    @ExcelProperty(value="科目")
    private String subject;

    @ExcelProperty(value="划拨金额")
    private BigDecimal allocatedmoney;

    @ExcelProperty(value="备注")
    private String remark;

    @ExcelIgnore
    private Long reimbursementid;
}
