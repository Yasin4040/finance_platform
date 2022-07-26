package com.jtyjy.finance.manager.dto.bxExcel;

import com.klcwqy.easyexcel.anno.SheetInfo;

/**
 * 申请报销导入第3页转账信息
 * @author shubo
 */
@SheetInfo(index = 2,startRow = 1,key = "detail")
public class TransferSheetDto {
	private TransferDetailDto instance;
}
