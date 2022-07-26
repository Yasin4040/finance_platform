package com.jtyjy.finance.manager.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 预算科目表
 * @author shubo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetSubjectExcelData {
	
	@ExcelProperty(value="科目名称")
	private String name;
	
	@ExcelProperty(value="科目代码")
	private String code;
    
    @ExcelProperty(value="金蝶科目代码")
    private String jindiecode;
    
	@ExcelProperty(value="届别")
	private String yearname;
	
	@ExcelProperty(value="停用")
	private String stopflagstr;
	
	@ExcelProperty(value="辅助指标")
	private String assistflagstr;
	
	@ExcelProperty(value="向上汇总")
	private String upsumflagstr;
	
	@ExcelProperty(value="费用分解")
	private String costsplitflagstr;
	
	@ExcelProperty(value="费用拆借")
	private String costlendflagstr;
	
	@ExcelProperty(value="费用追加")
	private String costaddflagstr;
	
	@ExcelProperty(value="关联产品")
	private String jointproductflagstr;
	
	@ExcelProperty(value="产品分类")
	private String procategoryname;
	
	@ExcelProperty(value="公式科目")
	private String formulaflagstr;
	
	@ExcelProperty(value="计算公式")
	private String formula;
	
	
}
