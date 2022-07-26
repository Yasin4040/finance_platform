package com.jtyjy.finance.manager.dto.bxExcel;

import java.util.Date;

import javax.validation.constraints.NotNull;

import com.klcwqy.easyexcel.anno.Location;

import lombok.Data;

@Data
public class EntertainDetailDto {

	@Location(column = 0)
    @NotNull(message = "日期不能为空")
	private String date;
	
	@Location(column = 1)
    @NotNull(message = "餐费人数不能为空")
	private Integer cfrs;
	
	@Location(column = 2)
    @NotNull(message = "餐费标准不能为空")
	private Double cfbz;
	
	@Location(column = 3)
    @NotNull(message = "餐费金额不能为空")
	private Double cfje;
	
	@Location(column = 4)
    @NotNull(message = "住宿费人数不能为空")
	private Integer zsrs;
	
	@Location(column = 5)
    @NotNull(message = "住宿费标准不能为空")
	private Double zsbz;
	
	@Location(column = 6)
    @NotNull(message = "住宿费间数不能为空")
	private Integer zsjs;
	
	@Location(column = 7)
    @NotNull(message = "住宿费金额不能为空")
	private Double zsje;
	
	@Location(column = 8)
    @NotNull(message = "其它不能为空")
	private Double other;
	
	@Location(column = 9)
    @NotNull(message = "宣传品费不能为空")
	private Double xcfje;
	
	@Location(column = 10)
    @NotNull(message = "小计不能为空")
	private Double count;
    @Override
    public String toString() {
        return "EntertainDetailDto [date=" + date + ", cfrs=" + cfrs + ", cfbz=" + cfbz + ", cfje=" + cfje + ", zsrs="
                + zsrs + ", zsbz=" + zsbz + ", zsjs=" + zsjs + ", zsje=" + zsje + ", other=" + other + ", xcfje="
                + xcfje + ", count=" + count + "]";
    }
	
}
