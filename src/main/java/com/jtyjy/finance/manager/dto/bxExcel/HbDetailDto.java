package com.jtyjy.finance.manager.dto.bxExcel;

import java.math.BigDecimal;
import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.jtyjy.finance.manager.mapper.response.MonthAgentMoneyInfo;
import com.klcwqy.easyexcel.anno.Location;

import lombok.Data;

@Data
public class HbDetailDto {

	@Location(column = 0)
    @NotBlank(message = "预算单位不能为空")
	private String unitName;
	
	@Location(column = 1)
    @NotBlank(message = "科目不能为空")
	private String subName;
	
	@Location(column = 2)
    @NotBlank(message = "划拨动因名称不能为空")
	private String agentName;
	
	@Location(column = 3)
    @NotNull(message = "划拨金额不能为空")
	private Double hbMoney;
	
	@Location(column = 4)
	private String remark;
	
	private MonthAgentMoneyInfo agentMoneyInfo;
    @Override
    public String toString() {
        return "HbDetailDto [unitName=" + unitName + ", subName=" + subName + ", agentName=" + agentName + ", hbMoney="
                + hbMoney + ", remark=" + remark + "]";
    }	
}
