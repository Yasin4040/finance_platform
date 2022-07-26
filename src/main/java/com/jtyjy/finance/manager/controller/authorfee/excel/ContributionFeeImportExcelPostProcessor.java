package com.jtyjy.finance.manager.controller.authorfee.excel;

import com.alibaba.fastjson.JSONObject;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.enmus.AuthorFeeStatusEnum;
import com.jtyjy.finance.manager.service.BudgetAuthorfeesumService;
import com.klcwqy.easyexcel.imported.ExcelImportHelper;
import com.klcwqy.easyexcel.processor.ImportPostProcessor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 稿费导入的后置处理器
 * @author User
 *
 */
@Data
public class ContributionFeeImportExcelPostProcessor implements ImportPostProcessor{

	private ContributionFeeImportCommonData commonData;
	private BudgetAuthorfeesumService feeSumService;
	private AuthorFeeAfterEndPostProcessor afterEndPostProcessor;

	@Override
	public void instanceProcess(ExcelImportHelper helper, Class<?> arg1, Row row, Map<String, Object> headMap, Object obj)
			throws Exception {
		if(headMap.get("headError")!=null && (Boolean)headMap.get("headError")) return;
		ContributionFeeExcelDetail detail = (ContributionFeeExcelDetail)obj;
		detail.setRow(row);
		this.afterEndPostProcessor.add(detail);
	}





	@Override
	public void process(ExcelImportHelper helper, Class<?> arg1, Map<String, Object> headMap, Sheet sheet, int row)
			throws Exception {
		afterEndPostProcessor.setCommonData(commonData);
		ContributionFeeExcelHead head = JSONObject.parseObject(JSONObject.toJSONString(headMap),ContributionFeeExcelHead.class);
		/**
		 * 校验表头
		 */
		try {
			String errorInfo = BaseController.validate(head);
			if(StringUtils.isNotBlank(errorInfo)) throw new RuntimeException(errorInfo);
			BudgetYearPeriod yearPeriod = commonData.getYearPeriods().stream().filter(e->head.getYearName().equals(e.getPeriod())).findFirst().orElseThrow(()->new RuntimeException("届别【"+head.getYearName()+"】不存在！"));
			head.setYearPeriod(yearPeriod);
			headMap.put("yearPeriod", yearPeriod);
			BudgetUnit unit = commonData.getUnitList().stream().filter(e->e.getYearid().equals(yearPeriod.getId()) && head.getUnitName().equals(e.getName())).findFirst().orElseThrow(()->new RuntimeException("【"+head.getYearName()+"】下不存在预算单位【"+head.getUnitName()+"】！"));
			head.setUnit(unit);
			headMap.put("unit", unit);
			/**
			 * 校验稿费月份（格式为202105）
			 */
			String contributionFeeMonth = head.getContributionFeeMonth();
			if(contributionFeeMonth.length() != 6) throw new RuntimeException("稿费月份格式错误！例【202105】");
			String date = contributionFeeMonth.substring(0, 4) + "-" + contributionFeeMonth.substring(4, 6);
			try {
				Constants.FORMAT_10.parse(date+"-01");
			}catch(Exception e) {
				throw new RuntimeException("稿费月份格式错误！例【202105】");
			}
			/**
			 * 校验稿费月份要填在届别中
			 */
			Integer code = Integer.valueOf(yearPeriod.getCode());
			List<String> periodMonthList = getPeriodMonthList(code);
			if(!periodMonthList.contains(contributionFeeMonth)) throw new RuntimeException("请根据届别【"+yearPeriod.getPeriod()+"】填写正确的月份。当前届别年为【"+code+"】");
			
			Integer month = Integer.valueOf(head.getContributionFeeMonth().substring(4, 6));
			BudgetMonthPeriod monthPeriod = commonData.getMonthPeriods().stream().filter(e->e.getCode().equals(month.toString())).findFirst().orElseThrow(()->new RuntimeException("不存在月【"+month+"】"));;
			head.setMonthPeriod(monthPeriod);
			headMap.put("monthPeriod",monthPeriod);
			WbUser user = commonData.getUserList().stream().filter(e->e.getUserName().equals(head.getBxEmpno())).findFirst().orElseThrow(()->new RuntimeException("找不到报销人【"+head.getBxEmpno()+"】"));
			head.setBxUser(user);
			/**
			 * 校验报销人是否在此预算单位下
			 */
			validateReimbursementEmp(unit,head.getBxEmpno());
			/**
			 * 校验当前稿费月份是否能够导入
			 * 只要当前稿费月份存在计税了就不能导入。
			 */
			validateContributionFeeMonthIsCanImport(head.getContributionFeeMonth());
			
			//创建稿费主表
			createAuthorFeeSum(head);
			headMap.put("feeSum", head.getFeeSum());
			
		}catch(Exception e) {
			e.printStackTrace();
			Row curRow = sheet.getRow(row);
			Cell cell = curRow.createCell(curRow.getLastCellNum());
			CellStyle cellStyle = helper.getWorkbook().createCellStyle();
			Font font = helper.getWorkbook().createFont();
			font.setColor(Font.COLOR_RED);
			cellStyle.setFont(font);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(StringUtils.isBlank(e.getMessage())?"空指针":e.getMessage());
			helper.setExportError(true);
			//throw e;
			headMap.put("headError", true);
		}		
	}
	
	private List<String> getPeriodMonthList(Integer code) {
		List<String> monthList = new ArrayList<>();
		monthList.add((code-1)+"06");
		monthList.add((code-1)+"07");
		monthList.add((code-1)+"08");
		monthList.add((code-1)+"09");
		monthList.add((code-1)+"10");
		monthList.add((code-1)+"11");
		monthList.add((code-1)+"12");
		monthList.add(code+"01");
		monthList.add((code)+"02");
		monthList.add((code)+"03");
		monthList.add((code)+"04");
		monthList.add((code)+"05");
		return monthList;
	}

	private void validateContributionFeeMonthIsCanImport(String contributionFeeMonth) {
		long count = commonData.getAuthorfeesums().stream().filter(e -> e.getFeemonth().equals(contributionFeeMonth) && e.getStatus() > AuthorFeeStatusEnum.STATUS_AUDITED.getType()).count();
		if(count > 1) throw new RuntimeException("稿费月份【"+contributionFeeMonth+"】中已存在计税后的数据！");
	}

	/**
	 * 创建稿费主表
	 * @param head
	 */
	private void createAuthorFeeSum(ContributionFeeExcelHead head) {
		BudgetAuthorfeesum budgetAuthorFeeSum = feeSumService.saveAuthorFeeSum(head);
		head.setFeeSum(budgetAuthorFeeSum);
	}

	/**
	 * 校验报销人是否在此预算单位下
	 * @param unit
	 * @param empNo
	 */
	private void validateReimbursementEmp(BudgetUnit unit, String empNo) {
		String budgetdepts = unit.getBudgetdepts();
		String budgetusers = unit.getBudgetusers();
		boolean isExistUser = false;
		if(StringUtils.isNotBlank(budgetusers)) {
			List<String> budgetUserIdList = Arrays.asList(budgetusers.split(","));
			isExistUser = commonData.getUserList().stream().filter(e->budgetUserIdList.contains(e.getUserId()) && e.getUserName().equals(empNo)).findFirst().isPresent();
		}
		if(isExistUser) return; //人员中匹配上的话直接返回。不用校验部门。提升效率。
		boolean isExistDept = false;
		if(StringUtils.isNotBlank(budgetdepts)) {
			Map<String, WbDept> deptMap = commonData.getDeptList().stream().collect(Collectors.toMap(WbDept::getDeptId, Function.identity()));
			//所有的子部门
			List<WbDept> allChildDeptList = new ArrayList<>();
			for(String deptid : budgetdepts.split(",")) {
				WbDept dept = deptMap.get(deptid);
				List<WbDept> childrenDepts = commonData.getDeptList().stream().filter(e->e.getParentIds().startsWith(dept.getParentIds())).collect(Collectors.toList());
				allChildDeptList.addAll(childrenDepts);
			}
			//子部门的id
			List<String> allChildDeptIdList = allChildDeptList.stream().map(e->e.getDeptId()).distinct().collect(Collectors.toList());
			isExistDept = commonData.getPersonList().stream().filter(e->allChildDeptIdList.contains(e.getDeptId()) && e.getPersonCode().equals(empNo)).findFirst().isPresent();
		}
		if(!isExistUser && !isExistDept) throw new RuntimeException("预算单位【"+unit.getName()+"】下不存在报销人【"+empNo+"】");
	}


}
