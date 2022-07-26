package com.jtyjy.finance.manager.dto.bxExcel;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.jtyjy.finance.manager.mapper.response.MonthAgentMoneyInfo;
import com.klcwqy.easyexcel.anno.Location;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 申请报销导入第1页明细信息
 * @author shubo
 */
@Data
public class BxDetailDto {

	@Location(column = 0)
    @NotBlank(message = "开票单位不能为空")
	private String unitName;
	@Location(column = 1)
    @NotBlank(message = "科目名称不能为空")
	private String subjectName;
	@Location(column = 2)
    @NotBlank(message = "动因名称不能为空")
	private String agentName;
	@Location(column = 3)
    @NotBlank(message = "报销金额不能为空")
	private String bxAmount;
	@Location(column = 4)
	private String remark;
	@Location(column = 5)
    @NotBlank(message = "计入执行不能为空")
	private String include;
    @Override
    public String toString() {
        return "BxDetailDto [unitName=" + unitName + ", subjectName=" + subjectName + ", agentName=" + agentName
                + ", bxAmount=" + bxAmount + ", remark=" + remark + ", include=" + include + "]";
    }

    private Long unitId;
    private MonthAgentMoneyInfo agentMoneyInfo;
}
