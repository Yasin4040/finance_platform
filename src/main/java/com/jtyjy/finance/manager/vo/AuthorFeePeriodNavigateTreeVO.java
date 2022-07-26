package com.jtyjy.finance.manager.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "稿费导航栏树")
@Data
public class AuthorFeePeriodNavigateTreeVO {
	
	@ApiModelProperty(value="查询条件（选中时传递的参数）")
	private String query;
	
	@ApiModelProperty(hidden = true)
	private String yearCode;
	
	@ApiModelProperty(value="父id")
	private String parentId;
	
	@ApiModelProperty(value="显示文本")
	private String text;
	
	@ApiModelProperty(value="层级")
	private Integer level;
	
	@ApiModelProperty(value="是否当前届别")
	private boolean curYearFlag = false;
	
	@ApiModelProperty(value="子级树")
	private List<AuthorFeePeriodNavigateTreeVO> children = new ArrayList<>();
}
