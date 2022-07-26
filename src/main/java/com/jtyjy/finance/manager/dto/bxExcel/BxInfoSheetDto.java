package com.jtyjy.finance.manager.dto.bxExcel;

import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.klcwqy.easyexcel.anno.Location;
import com.klcwqy.easyexcel.anno.SheetInfo;

import lombok.Data;

/**
 * 申请报销导入第1页总体信息
 * @author shubo
 */
@Data
@SheetInfo(index = 0,startRow = 5,key = "detail")
public class BxInfoSheetDto {
	@Location(soleRow = 1, column = 1)
    @NotBlank(message = "届别不能为空")
	private String b2;
	@Location(soleRow = 1, column = 3)
    @NotBlank(message = "月份不能为空")
	private String d2;
	@Location(soleRow = 1, column = 5)
    @NotBlank(message = "预算单位不能为空")
	private String f2;
	@Location(soleRow = 2, column = 1)
    @NotBlank(message = "报销人工号不能为空")
	private String b3;
	@Location(soleRow = 2, column = 3)
    @NotNull(message = "报销日期不能为空")
	private String d3;
	@Location(soleRow = 2, column = 5)
	private Double f3;
	@Location(soleRow = 3, column = 1)
    @NotNull(message = "附件张数不能为空")
	private Integer b4;
	@Location(soleRow = 3, column = 3)
    @NotBlank(message = "报销类型不能为空")
	private String d4;
	@Location(soleRow = 3, column = 5)
	private String f4;

	private BxDetailDto instance;
}
