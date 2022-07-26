package com.jtyjy.finance.manager.easyexcel;

import java.math.BigDecimal;


import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 稿费导出中心报表的汇总数据
 * @author minzhq
 */
@Data
@NoArgsConstructor
public class AuthorFeeDeptSumExcelData {
	
	private String deptName;
	
	private String subjectName;
	
	private BigDecimal yearMoney;
	
	private BigDecimal yearExecuteMoney;
}
