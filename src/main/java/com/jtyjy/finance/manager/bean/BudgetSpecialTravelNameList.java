package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 出差特殊人员
 */
@TableName(value = "budget_special_travel_name_list")
@Data
public class BudgetSpecialTravelNameList {

	@ApiModelProperty(value = "主键Id")
	@TableId(type = IdType.AUTO)
	private Long id;

	@ApiModelProperty(value = "编号")
	@TableField(value = "emp_no")
	private String empNo;

	@ApiModelProperty(value = "名称")
	@TableField(value = "emp_name")
	@NotEmpty(message = "名称不能为空")
	private String empName;

	@ApiModelProperty(value = "部门id")
	@TableField(value = "dept_id")
	private String deptId;

	@ApiModelProperty(value = "是否停用")
	@NotNull(message = "是否停用不能为空")
	@TableField(value = "stop_flag")
	private Boolean stopFlag;

	@ApiModelProperty(value = "创建时间")
	@TableField(value = "create_time")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date createTime;

	@ApiModelProperty(value = "更新时间")
	@TableField(value = "update_time")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date updateTime;

	@ApiModelProperty(value = "部门名称")
	@TableField(exist = false)
	private String deptName;

	@ApiModelProperty(value = "部门全名称")
	@TableField(exist = false)
	private String deptFullName;

}
