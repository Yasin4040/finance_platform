package com.jtyjy.finance.manager.service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jtyjy.finance.manager.bean.*;
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
import com.jtyjy.finance.manager.easyexcel.TravelSumExcelData;
import com.jtyjy.finance.manager.mapper.BudgetReimbursementorderTravelMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;

import lombok.RequiredArgsConstructor;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetReimbursementorderTravelService extends DefaultBaseService<BudgetReimbursementorderTravelMapper, BudgetReimbursementorderTravel> {

	private final TabChangeLogMapper loggerMapper;

	private final BudgetReimbursementorderTravelMapper mapper; 
	
    private final BudgetYearPeriodService yearService;

    private final BudgetReimbursementorderAllocatedMapper allocatedMapper;
    
    private final WbUserService userService;
	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_reimbursementorder_travel"));
	}

	public int saveByOrder(List<BudgetReimbursementorderTravel> list, BudgetReimbursementorder order) {
        //按照订单主键删除
        QueryWrapper<BudgetReimbursementorderTravel> wrapper = new QueryWrapper<BudgetReimbursementorderTravel>();
        wrapper.eq("reimbursementid", order.getId());
        this.remove(wrapper);
	    if(list == null || list.size() == 0) {
			return 0;
		}
		BudgetReimbursementorderTravel.setBase(list,order);
		this.saveBatch(list);
		return list.size();
	}

	public List<BudgetReimbursementorderTravel> getByOrderId(long id) {
		QueryWrapper<BudgetReimbursementorderTravel> wrapper = new QueryWrapper<BudgetReimbursementorderTravel>();
		wrapper.eq("reimbursementid", id);
		return this.list(wrapper);
	}
	
	   
    public void exportTravelSum(Integer bxType, Long yearId, Long monthId, HttpServletResponse response) throws Exception {
        if (null == bxType || (2 != bxType && 4 != bxType)) throw new Exception("无效的报销类型");
        BudgetYearPeriod year = this.yearService.getById(yearId);
        if (null == year) throw new Exception("无效的届别");
        List<TravelSumExcelData> list = this.mapper.travelSummaryByYear(bxType, yearId, monthId);
        if (null == list || list.isEmpty()) {
            throw new Exception("无差旅数据");
        }
        List<TravelSumExcelData> resultList = new ArrayList<>();
        for (TravelSumExcelData data : list) {
            StringJoiner sj = new StringJoiner(",");
            for (String empNo : data.getTraveler().split(",")) {
                WbUser user = this.userService.getByEmpNo(empNo);
                if (null != user) {
                    sj.add(user.getDisplayName());
                }else {
                    sj.add(empNo);
                }
            }
            data.setTraveler(sj.toString());
            if (data.getAllocatedmoney().compareTo(BigDecimal.ZERO) > 0) {
                //存在划拨金额
                resultList.add(data);
                List<BudgetReimbursementorderAllocated> allocatedList = allocatedMapper.selectList(Wrappers.<BudgetReimbursementorderAllocated>lambdaQuery().eq(BudgetReimbursementorderAllocated::getReimbursementid, data.getReimbursementid()));
                allocatedList.forEach(allocated -> {
                    TravelSumExcelData allocateData = new TravelSumExcelData();
                    BeanUtils.copyProperties(data, allocateData);
                    allocateData.setTraveler("");
                    allocateData.setTravelerNum(null);
                    allocateData.setTravelPeriod("");
                    allocateData.setTravelDay(null);
                    allocateData.setTotalAmt(null);
                    allocateData.setLongAmt(null);
                    allocateData.setCityAmt(null);
                    allocateData.setHotelAmt(null);
                    allocateData.setSubsidyDay(null);
                    allocateData.setSubsidyBz(null);
                    allocateData.setSubsidyAmt(null);
                    allocateData.setOtherAmt(null);
                    allocateData.setTravelReason("");
                    allocateData.setUnitName(allocated.getUnitname());
                    allocateData.setAgentName(allocated.getMonthagentname());
                    allocateData.setSubject(allocated.getSubjectname());
                    allocateData.setAllocatedmoney(allocated.getAllocatedmoney());
                    resultList.add(allocateData);
                });
            } else {
                resultList.add(data);
            }
        }
        String excelName = 2 == bxType ? "差旅报销" : "差旅补贴";
        Map<String, String> heads = new HashMap<>();
        heads.put("yearName", year.getPeriod());
        heads.put("exportDate", Constants.FORMAT_10.format(new Date()));
        InputStream templatePathName = this.getClass().getClassLoader().getResourceAsStream("template/travelSumTemplate.xlsx");
        ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream(year.getPeriod() + "导出" + excelName + "汇总表", response)).withTemplate(templatePathName).build();
        WriteSheet sheet = EasyExcel.writerSheet(0).build();
        sheet.setSheetName(excelName + "汇总");
        workBook.fill(heads, sheet);
        workBook.fill(resultList, sheet);
        workBook.finish();
    }
}
