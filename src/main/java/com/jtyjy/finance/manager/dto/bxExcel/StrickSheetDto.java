package com.jtyjy.finance.manager.dto.bxExcel;

import com.klcwqy.easyexcel.anno.SheetInfo;

/**
 * 申请报销导入第2页冲账信息
 * @author shubo
 */
@SheetInfo(index = 1,startRow = 1,key = "detail")
public class StrickSheetDto {
	private StrickDetailDto instance;
}
