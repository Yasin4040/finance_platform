package com.jtyjy.finance.manager.dto.bxExcel;

import com.klcwqy.easyexcel.anno.Location;
import com.klcwqy.easyexcel.anno.SheetInfo;

import lombok.Data;

/**
 * 申请报销导入报表第4页差旅信息
 * @author shubo
 */
@Data
@SheetInfo(index = 3, startRow = 3, key = "detail")
public class TravelSheetDto {

    @Location(soleRow = 0, column = 2)
    private String name;
    @Location(soleRow = 0, column = 7)
    private String reason;
    private TravelDetailDto instance;
}
