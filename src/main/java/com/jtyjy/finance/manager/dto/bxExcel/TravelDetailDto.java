package com.jtyjy.finance.manager.dto.bxExcel;

import java.math.BigDecimal;
import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.klcwqy.easyexcel.anno.Location;

import lombok.Data;

@Data
public class TravelDetailDto {

	@Location(column = 0)
    @NotBlank(message = "交通工具不能为空")
	private String gj;
	
	@Location(column = 1)
	@NotBlank(message = "开始日期不能为空")
	private String start;
	
	@Location(column = 2)
	@NotBlank(message = "结束日期不能为空")
	private String end;
	
	@Location(column = 3)
    @NotBlank(message = "出发地不能为空")
	private String location;
	
	@Location(column = 4)
    @NotBlank(message = "目的地不能为空")
	private String mdd;
	
	@Location(column = 5)
    @NotNull(message = "长途交通费不能为空")
	private Double ctf;
	
	@Location(column = 6)
    @NotNull(message = "市内交通费不能为空")
	private Double snf;
	
	@Location(column = 7)
    @NotNull(message = "住宿费不能为空")
	private Double zsf;
	
	@Location(column = 8)
    @NotNull(message = "天数不能为空")
	private Double ts;
	
	@Location(column = 9)
    @NotNull(message = "标准不能为空")
	private Double bz;
	
	@Location(column = 10)
    @NotNull(message = "金额不能为空")
	private Double je;
	
	@Location(column = 11)
    @NotNull(message = "其他不能为空")
	private Double qt;
	
	@Location(column = 12)
    @NotNull(message = "小计不能为空")
	private Double xj;
	
	private Integer vehicleType;
	@Override
	public String toString() {
		return "Travel [gj=" + gj + ", start=" + start + ", end=" + end + ", location=" + location + ", mdd=" + mdd
				+ ", ctf=" + ctf + ", snf=" + snf + ", zsf=" + zsf + ", ts=" + ts + ", bz=" + bz + ", je=" + je
				+ ", qt=" + qt + ", xj=" + xj + "]";
	}
}
