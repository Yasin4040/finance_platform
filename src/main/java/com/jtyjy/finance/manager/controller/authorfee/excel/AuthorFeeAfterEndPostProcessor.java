package com.jtyjy.finance.manager.controller.authorfee.excel;

import com.alibaba.fastjson.JSONObject;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.service.BudgetAuthorfeesumService;
import com.klcwqy.easyexcel.imported.ExcelImportHelper;
import com.klcwqy.easyexcel.processor.AfterEndPostProcessor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Data
public class AuthorFeeAfterEndPostProcessor extends AfterEndPostProcessor {
	private List<ContributionFeeExcelDetail> excelDetails = new ArrayList<>();
	public final static int handleRecords = 50;
	private ContributionFeeImportCommonData commonData;

	public void add(ContributionFeeExcelDetail ele){
		this.excelDetails.add(ele);

	}

	private void invokeDetail(ContributionFeeExcelHead head, ContributionFeeExcelDetail detail, ExcelImportHelper helper, Row row,Map<String, Object> headMap,List<ContributionFeeExcelDetail> details){
		try {
			String errorInfo = BaseController.validate(detail);
			if(StringUtils.isNotBlank(errorInfo)) throw new RuntimeException(errorInfo);
			/**
			 * 校验填写的数据是否正确
			 */
			validateDataDetailIsTrue(detail,head);
			details.add(detail);
		}catch(Exception e) {
			e.printStackTrace();
			Cell cell = row.createCell(row.getLastCellNum());
			CellStyle cellStyle = helper.getWorkbook().createCellStyle();
			Font font = helper.getWorkbook().createFont();
			font.setColor(Font.COLOR_RED);
			cellStyle.setFont(font);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(StringUtils.isBlank(e.getMessage())?"空指针":e.getMessage());
			helper.setExportError(true);
		}
	}

	@Override
	public void afterEnd(ExcelImportHelper excelImportHelper, Class<?> aClass, final Map<String, Object> map, Sheet sheet) throws Exception {
		if(!excelDetails.isEmpty()){
			ContributionFeeExcelHead head = new ContributionFeeExcelHead();
			head.setYearName(map.get("yearName").toString());
			head.setContributionFeeNo(map.get("contributionFeeNo").toString());
			head.setUnitName(map.get("unitName").toString());
			head.setContributionFeeMonth(map.get("contributionFeeMonth").toString());
			head.setBxEmpno(map.get("bxEmpno").toString());
			head.setYearPeriod((BudgetYearPeriod)map.get("yearPeriod"));
			head.setUnit((BudgetUnit)map.get("unit"));
			head.setMonthPeriod((BudgetMonthPeriod)map.get("monthPeriod"));
			head.setFeeSum((BudgetAuthorfeesum)map.get("feeSum"));
			List<ContributionFeeExcelDetail> details = Collections.synchronizedList(new ArrayList<ContributionFeeExcelDetail>());
			int threadSize = (excelDetails.size()/handleRecords)+1;
			final CountDownLatch latch = new CountDownLatch(threadSize);
			List<ContributionFeeExcelDetail> newlist = null;
			ExecutorService executorService = Executors.newFixedThreadPool(threadSize);
			for(int i=0;i<threadSize;i++){
				if ((i + 1) == threadSize) {
					newlist = excelDetails.subList((i * handleRecords), excelDetails.size());
				}else{
					newlist = excelDetails.subList(i * handleRecords, (i+1) * handleRecords);
				}
				List<ContributionFeeExcelDetail> processList = newlist;
				executorService.execute(new Runnable() {
					@Override
					public void run() {
						processList.forEach(detail->{
							invokeDetail(head,detail,excelImportHelper,detail.getRow(),map,details);
						});
						latch.countDown();
					}
				});
			}
			latch.await();
			executorService.shutdown();
			map.put("feeDetails",details);
		}

	}

	private void validateDataDetailIsTrue(ContributionFeeExcelDetail detail, ContributionFeeExcelHead head) {
		if(!BudgetAuthorfeesumService.TAX_TYPE_YES.equals(detail.getIsDecutionTax()) && !BudgetAuthorfeesumService.TAX_TYPE_NO.equals(detail.getIsDecutionTax()) ) throw new RuntimeException("是否扣税请填写【"+BudgetAuthorfeesumService.TAX_TYPE_YES+"】或【"+BudgetAuthorfeesumService.TAX_TYPE_NO+"】！");
		if(!BudgetAuthorfeesumService.TAX_TYPE_YES.equals(detail.getIsNeedTran()) && !BudgetAuthorfeesumService.TAX_TYPE_NO.equals(detail.getIsNeedTran()) ) throw new RuntimeException("是否转账请填写【"+BudgetAuthorfeesumService.TAX_TYPE_YES+"】或【"+BudgetAuthorfeesumService.TAX_TYPE_NO+"】！");
		/**
		 * 判断报销科目是否在提报部门和划拨部门下
		 */
		List<String> subjectNameList = new ArrayList<>();
		subjectNameList.add(detail.getSubjectName());
		List<Map<String,Object>> list1 = this.commonData.getUnitSubjects().stream().filter(e->head.getUnit().getId().toString().equals(e.get("unitid").toString()) && detail.getSubjectName().equals(e.get("name").toString())).collect(Collectors.toList());
		if(list1 == null || list1.isEmpty()) throw new RuntimeException("提报部门【"+head.getUnit().getName()+"】下无报销科目【"+detail.getSubjectName()+"】");

		/**
		 * 校验产品形态。必须是产品一级分类
		 */
		BudgetProductCategory productCategory = commonData.getProductCategories().stream().filter(e->detail.getProductForm().equals(e.getName()) && "0".equals(e.getPid().toString())).findFirst().orElseThrow(()->new RuntimeException("产品形态【"+detail.getProductForm()+"】不存在"));
		BudgetUnit hbUnit = commonData.getUnitList().stream().filter(e->e.getYearid().equals(head.getYearPeriod().getId()) && e.getName().equals(detail.getContributionFeeUnitName())).findFirst().orElseThrow(()->new RuntimeException("届别【"+head.getYearPeriod().getPeriod()+"】"+"稿费所属部门【"+detail.getContributionFeeUnitName()+"】不存在！"));
		detail.setHbUnit(hbUnit);
		List<Map<String,Object>> list2 = this.commonData.getUnitSubjects().stream().filter(e->hbUnit.getId().toString().equals(e.get("unitid").toString()) && detail.getSubjectName().equals(e.get("name").toString())).collect(Collectors.toList());
		if(list2 == null || list2.isEmpty()) throw new RuntimeException("稿费所属部门【"+hbUnit.getName()+"】下无报销科目【"+detail.getSubjectName()+"】");
		String subjectId = list2.get(0).get("id").toString();
		detail.setSubjectId(subjectId);
		/**
		 * 校验产品预算II类。月度的产品动因
		 */
		//获取当前一级分类下所有的产品。
		List<BudgetProductCategory> categoryList = commonData.getProductCategories().stream().filter(e->e.getPids().startsWith(productCategory.getPids())).collect(Collectors.toList());
		List<Long> categoryIds = categoryList.stream().map(e->e.getId()).collect(Collectors.toList());
		List<BudgetProduct> productList = commonData.getProducts().stream().filter(e->categoryIds.contains(e.getProcategoryid()) && e.getStopflag() == 0).collect(Collectors.toList());

		BudgetProduct product = productList.stream().filter(e->e.getName().equals(detail.getMonthAgentName())).findFirst().orElse(null);
		if(Objects.isNull(product)) throw new RuntimeException("产品形态【"+detail.getProductForm()+"】下不存在产品【"+detail.getMonthAgentName()+"】");
		BudgetMonthAgent monthAgent = commonData.getMonthAgentList().stream()
				.filter(e->e.getUnitid().equals(hbUnit.getId())
						&& e.getProductid()!=null
						&& e.getProductid().equals(product.getId())
						&& e.getName().equals(product.getName())
						&& e.getMonthid().equals(head.getMonthPeriod().getId())
						&& e.getSubjectid().toString().equals(subjectId))
				.findFirst()
				.orElseThrow(()->new RuntimeException(head.getMonthPeriod().getId()+"月度产品动因【"+product.getName()+"】不存在"));
		detail.setMonthAgent(monthAgent);
		if(!BudgetAuthorfeesumService.AUTHOR_TYPE_INNER.equals(detail.getAuthorType()) && !BudgetAuthorfeesumService.AUTHOR_TYPE_OUTER.equals(detail.getAuthorType()) ) throw new RuntimeException("作者类型请填写【"+BudgetAuthorfeesumService.AUTHOR_TYPE_INNER+"】或【"+BudgetAuthorfeesumService.AUTHOR_TYPE_OUTER+"】！");
		Boolean authorType = false;
		if(BudgetAuthorfeesumService.AUTHOR_TYPE_INNER.equals(detail.getAuthorType())) authorType = true;
		Boolean authorTypeTemp = authorType;
		String authorName = detail.getAuthorName().trim();
		String authorIdnumber = detail.getAuthorIdnumber().replace("x", "X").trim();
		BudgetAuthor author = commonData.getAuthors().stream().filter(e->e.getAuthortype().toString().equals(authorTypeTemp.toString()) && e.getAuthor().equals(authorName) &&
				(authorIdnumber.equals(e.getIdnumber()) || authorIdnumber.equals(e.getTaxpayernumber()) ))
				.findFirst().orElseThrow(()->new RuntimeException("找不到稿费作者【"+authorName+"、"+authorIdnumber+"】"));

		detail.setAuthor(author);
		WbUser user = commonData.getUserList().stream().filter(e->e.getUserName().equals(detail.getTeacherEmpno()) && e.getDisplayName().equals(detail.getTeacherEmpname())).findFirst().orElseThrow(()->new RuntimeException("找不到约稿教师【"+detail.getTeacherEmpname()+"("+detail.getTeacherEmpno()+")】"));
		detail.setTeacher(user);
		if(StringUtils.isNotBlank(detail.getAscriptionUnitName())) {
			commonData.getUnitList().stream().filter(e->e.getYearid().equals(head.getYearPeriod().getId()) && e.getName().equals(detail.getAscriptionUnitName())).findFirst().orElseThrow(()->new RuntimeException("届别【"+head.getYearPeriod().getPeriod()+"】"+"归属事业群【"+detail.getAscriptionUnitName()+"】不存在！"));
		}
	}

}
