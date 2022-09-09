package com.jtyjy.finance.manager.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 描述：<p></p>
 *
 * @author minzhq
 * @since 2022/9/1
 */
@Data
public class ExtractPersonalityPayDetailVO implements Cloneable{

	@ApiModelProperty("提成批次")
	@NotBlank(message = "提成批次不能为空")
	private String query;

	@ApiModelProperty(hidden = false)
	private String extractBatch;

	@ApiModelProperty("id")
	private Long id;

	@ApiModelProperty("批次")
	private String batch;

	@ApiModelProperty("部门")
	private String firstDept;

	@ApiModelProperty("省区/大区")
	private String secondDept;

	@ApiModelProperty("工号")
	private Integer empNo;

	@ApiModelProperty("姓名")
	private String empName;

	@ApiModelProperty("当期待发提成金额")
	private BigDecimal extract;

	@ApiModelProperty(value = "个体户id")
	@NotNull(message = "员工个体户不能为空")
	private Long personalityId;

	@ApiModelProperty("个体户名称/户名")
	private String personalityName;

	@ApiModelProperty("账户类型")
	private String userType;

	@ApiModelProperty("累计交票")
	private BigDecimal receiptSum;

	@ApiModelProperty("累计已发提成")
	private BigDecimal extractSum;

	@ApiModelProperty("当期提成发放金额")
	@NotNull(message = "当期提成发放金额不能为空")
	private BigDecimal curExtract;

	@ApiModelProperty("累计已发工资")
	private BigDecimal salarySum;

	@ApiModelProperty("当期工资发放金额")
	@NotNull(message = "当期提成发放金额不能为空")
	private BigDecimal curSalary;

	@ApiModelProperty("累计已发福利")
	private BigDecimal welfareSum;

	@ApiModelProperty("当期福利费发放金额")
	@NotNull(message = "当期提成发放金额不能为空")
	private BigDecimal curWelfare;

	@ApiModelProperty("累计已发")
	private BigDecimal moneySum;
	
	@ApiModelProperty("当期发放总额")
	private BigDecimal curPaySum;

	@ApiModelProperty("剩余票额")
	private BigDecimal remainingInvoices;

	@ApiModelProperty("剩余发放限额")
	private BigDecimal remainingPayLimitMoney;

	@ApiModelProperty("发放单位")
	private String billingUnitName;

	@ApiModelProperty("发放单位id")
	@NotNull(message = "发放单位不能为空")
	private Long billingUnitId;

	@ApiModelProperty("发放状态")
	@NotNull(message = "发放状态不能为空")
	private Integer payStatus;

	@ApiModelProperty("离职日期 yyyy-MM-dd")
	private String separateDate;

	@ApiModelProperty("发放状态名称")
	private String payStatusName;

	@ApiModelProperty("更新时间")
	private String updateTime;

	@ApiModelProperty("确认完成/确认发放时间")
	private String operateTime;

	@ApiModelProperty("是否发放")
	private Boolean isSend = false;

	@ApiModelProperty(hidden = true)
	private Boolean isSelf = true;

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
