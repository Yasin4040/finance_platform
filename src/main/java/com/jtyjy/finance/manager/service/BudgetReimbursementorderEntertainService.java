package com.jtyjy.finance.manager.service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.dto.ReimBursementDTO;
import com.jtyjy.finance.manager.mapper.BudgetReimbursementorderAllocatedMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.easyexcel.EntertainSumExcelData;
import com.jtyjy.finance.manager.mapper.BudgetReimbursementorderEntertainMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;

import lombok.RequiredArgsConstructor;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetReimbursementorderEntertainService extends DefaultBaseService<BudgetReimbursementorderEntertainMapper, BudgetReimbursementorderEntertain> {

	private final TabChangeLogMapper loggerMapper;

	private final BudgetReimbursementorderEntertainMapper mapper;

	private final BudgetReimbursementorderAllocatedMapper allocatedMapper;
	
	private final BudgetYearPeriodService yearService;
	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_reimbursementorder_entertain"));
	}

	public int saveByOrder(List<BudgetReimbursementorderEntertain> list, BudgetReimbursementorder order) {
        //按照订单主键删除
        QueryWrapper<BudgetReimbursementorderEntertain> wrapper = new QueryWrapper<BudgetReimbursementorderEntertain>();
        wrapper.eq("reimbursementid", order.getId());
        this.remove(wrapper);
	    if(list == null || list.size() == 0) {
			return 0;
		}
		BudgetReimbursementorderEntertain.setBase(list,order);
		this.saveBatch(list);
		return list.size();
	}

	public List<BudgetReimbursementorderEntertain> getByOrderId(long id) {
		QueryWrapper<BudgetReimbursementorderEntertain> wrapper = new QueryWrapper<BudgetReimbursementorderEntertain>();
		wrapper.eq("reimbursementid", id);
		return this.list(wrapper);
	}
	
	public void exportEntertainSum(Integer bxType, Long yearId, Long monthId, HttpServletResponse response) throws Exception {
	    if (null == bxType || (3 != bxType && 5 != bxType)) throw new Exception("无效的报销类型");
	    BudgetYearPeriod year = this.yearService.getById(yearId);
	    if (null == year) throw new Exception("无效的届别");
	    List<EntertainSumExcelData> list = this.mapper.entertainSummaryByYear(bxType, yearId, monthId);
	    if (null == list || list.isEmpty()) {
	        throw new Exception("无招待数据");
	    }
	    List<EntertainSumExcelData> resultList = new ArrayList<>();
	    for (EntertainSumExcelData excelData : list) {
			BigDecimal totalSum = excelData.getCfje().add(excelData.getZsje()).add(excelData.getOther()).add(excelData.getXcpf());
			excelData.setTotalSum(totalSum);
			if (excelData.getAllocatedmoney().compareTo(BigDecimal.ZERO) > 0) {
				//存在划拨金额
				resultList.add(excelData);
				List<BudgetReimbursementorderAllocated> allocatedList = allocatedMapper.selectList(Wrappers.<BudgetReimbursementorderAllocated>lambdaQuery().eq(BudgetReimbursementorderAllocated::getReimbursementid, excelData.getReimbursementid()));
				allocatedList.forEach(allocated -> {
					EntertainSumExcelData allocateData = new EntertainSumExcelData();
					BeanUtils.copyProperties(excelData, allocateData);
					allocateData.setCfrs(null);
					allocateData.setCfje(null);
					allocateData.setCfbz(null);
					allocateData.setZsrs(null);
					allocateData.setZsrs(null);
					allocateData.setZsbz(null);
					allocateData.setZsjs(null);
					allocateData.setZsje(null);
					allocateData.setOther(null);
					allocateData.setXcpf(null);
					allocateData.setTotalSum(null);
					allocateData.setUnitName(allocated.getUnitname());
					allocateData.setAgentName(allocated.getMonthagentname());
					allocateData.setSubject(allocated.getSubjectname());
					allocateData.setAllocatedmoney(allocated.getAllocatedmoney());
					resultList.add(allocateData);
				});
			} else {
				resultList.add(excelData);
			}
		}
        String excelName = 3 == bxType ? "招待报销" : "推广招待";
	    Map<String, String> heads = new HashMap<>();
	    heads.put("yearName", year.getPeriod());
	    heads.put("exportDate", Constants.FORMAT_10.format(new Date()));
	    InputStream templatePathName = this.getClass().getClassLoader().getResourceAsStream("template/entertainSumTemplate.xlsx");
        ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream(year.getPeriod() + "导出" + excelName + "汇总表", response)).withTemplate(templatePathName).build();
        WriteSheet sheet = EasyExcel.writerSheet(0).build();
        sheet.setSheetName(excelName + "汇总");
        workBook.fill(heads, sheet);
        workBook.fill(resultList, sheet);
        workBook.finish();
	}


}
