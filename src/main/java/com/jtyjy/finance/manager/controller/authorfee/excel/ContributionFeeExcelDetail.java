package com.jtyjy.finance.manager.controller.authorfee.excel;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.jtyjy.finance.manager.bean.BudgetAuthor;
import com.jtyjy.finance.manager.bean.BudgetMonthAgent;
import com.jtyjy.finance.manager.bean.BudgetUnit;
import com.jtyjy.finance.manager.bean.WbUser;
import com.klcwqy.easyexcel.anno.Location;

import lombok.Data;
import org.apache.poi.ss.usermodel.Row;

@Data
public class ContributionFeeExcelDetail {
	
	@Location(column = 0)
	@NotBlank(message = "报销科目不能为空！")
	private String subjectName; //报销科目
	
	@Location(column = 1)
	@NotBlank(message = "是否扣税不能为空！")
	private String isDecutionTax; //是否扣税
	
	@Location(column = 2)
	@NotBlank(message = "产品形态不能为空！")
	private String productForm;  //产品形态
	
	@Location(column = 3) 
	@NotBlank(message = "产品预算II类不能为空！")
	private String monthAgentName;  //产品动因名称
	
	@Location(column = 4)
	@NotBlank(message = "学科不能为空！")
	private String subject;  //学科
	
	@Location(column = 5)
	private String remark;  //邀稿内容及去向
	
	@Location(column = 6)
	@NotBlank(message = "作者类型不能为空！")
	private String authorType; //作者类型
	
	@Location(column = 7)
	@NotBlank(message = "作者类型不能为空！")
	private String authorName; //作者名称
	
	@Location(column = 8)
	@NotBlank(message = "作者身份证号不能为空！")
	private String authorIdnumber; //作者身份证号
	
	@Location(column = 9)
	private String manuscriptQuality; //稿件质量
	
	@Location(column = 10)
	private String pageNumber; //页码或份数
	
	@Location(column = 11)
	@NotBlank(message = "稿酬标准不能为空！")
	@Pattern(regexp="^(([1-9][0-9]*)|(([0]\\.\\d{0,2}|[1-9][0-9]*\\.\\d{0,2})))$",message="稿酬标准请填写正确的格式。")  
	private String contributionFeeStandard; //稿酬标准
	
	@Location(column = 12)
	@NotBlank(message = "应发稿酬不能为空！")
	@Pattern(regexp="^(([1-9][0-9]*)|(([0]\\.\\d{0,2}|[1-9][0-9]*\\.\\d{0,2})))$",message="应发稿酬请填写正确的格式。")  
	private String contributionFee;  //应发稿酬
	
	@Location(column = 13)
	@NotBlank(message = "约稿教师工号不能为空！")
	private String teacherEmpno; //约稿教师工号
	
	@Location(column = 14)
	@NotBlank(message = "约稿教师姓名不能为空！")
	private String teacherEmpname;  //约稿教师姓名
	
	@Location(column = 15)
	@NotBlank(message = "稿费所属部门不能为空！")
	private String contributionFeeUnitName;   //稿费所属部门
	
	@Location(column = 16)
	@ExcelProperty(value="归属事业群")
	private String ascriptionUnitName;   //归属事业群（预算单位）
	
	@Location(column = 17)
	@ExcelProperty(value="是否转账")
	@NotBlank(message = "是否转账不能为空！")
	private String isNeedTran;

	@ExcelProperty(value="错误明细")
	@ColumnWidth(30)
	private String errMsg;
	

	public ContributionFeeExcelDetail(){}

	public ContributionFeeExcelDetail(@NotBlank(message = "报销科目不能为空！") String subjectName, @NotBlank(message = "是否扣税不能为空！") String isDecutionTax, @NotBlank(message = "产品形态不能为空！") String productForm, @NotBlank(message = "产品预算II类不能为空！") String monthAgentName, @NotBlank(message = "学科不能为空！") String subject, String remark, @NotBlank(message = "作者类型不能为空！") String authorType, @NotBlank(message = "作者类型不能为空！") String authorName, @NotBlank(message = "作者身份证号不能为空！") String authorIdnumber, String manuscriptQuality, String pageNumber, @NotBlank(message = "稿酬标准不能为空！") @Pattern(regexp = "^(([1-9][0-9]*)|(([0]\\.\\d{0,2}|[1-9][0-9]*\\.\\d{0,2})))$", message = "稿酬标准请填写正确的格式。") String contributionFeeStandard, @NotBlank(message = "应发稿酬不能为空！") @Pattern(regexp = "^(([1-9][0-9]*)|(([0]\\.\\d{0,2}|[1-9][0-9]*\\.\\d{0,2})))$", message = "应发稿酬请填写正确的格式。") String contributionFee, @NotBlank(message = "约稿教师工号不能为空！") String teacherEmpno, @NotBlank(message = "约稿教师姓名不能为空！") String teacherEmpname, @NotBlank(message = "稿费所属部门不能为空！") String contributionFeeUnitName, String ascriptionUnitName, @NotBlank(message = "是否转账不能为空！") String isNeedTran) {
		this.subjectName = subjectName;
		this.isDecutionTax = isDecutionTax;
		this.productForm = productForm;
		this.monthAgentName = monthAgentName;
		this.subject = subject;
		this.remark = remark;
		this.authorType = authorType;
		this.authorName = authorName;
		this.authorIdnumber = authorIdnumber;
		this.manuscriptQuality = manuscriptQuality;
		this.pageNumber = pageNumber;
		this.contributionFeeStandard = contributionFeeStandard;
		this.contributionFee = contributionFee;
		this.teacherEmpno = teacherEmpno;
		this.teacherEmpname = teacherEmpname;
		this.contributionFeeUnitName = contributionFeeUnitName;
		this.ascriptionUnitName = ascriptionUnitName;
		this.isNeedTran = isNeedTran;
	}

	/*************************以下是其它字段********************************************/
	private String subjectId;
	
	private BudgetMonthAgent monthAgent;
	
	private BudgetAuthor author;
	
	private WbUser teacher;
	
	private BudgetUnit hbUnit;

	private Row row;

}
