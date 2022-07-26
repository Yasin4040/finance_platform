package com.jtyjy.finance.manager.dto.bxExcel;

import com.klcwqy.easyexcel.anno.SheetInfo;

/**
 * 申请报销导入报表第6页划拨信息
 * @author shubo
 */
@SheetInfo(index = 5, startRow = 1, key = "detail")
public class HbSheetDto {

    private HbDetailDto instance;
}
