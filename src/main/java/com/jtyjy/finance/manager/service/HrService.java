package com.jtyjy.finance.manager.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.local.JdbcSqlThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.hrbean.HrSalaryYearTaxUser;
import com.jtyjy.finance.manager.hrmapper.HrSalaryYearTaxUserMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;

import lombok.RequiredArgsConstructor;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "hrTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HrService extends DefaultBaseService<HrSalaryYearTaxUserMapper, HrSalaryYearTaxUser> {

    private final TabChangeLogMapper loggerMapper;

    @Autowired
    private HrSalaryYearTaxUserMapper mapper;
    
    
    @Override
    public BaseMapper<TabChangeLog> getLoggerMapper() {
        return loggerMapper;
    }

    @Override
    public void setBaseLoggerBean() {
        DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("hr_salary_year_tax_user"));
    }

	public Map<String,Object> getSalary(String salaryMonth) {
		String year = salaryMonth.substring(0, 4);
		List<Map<String, Object>> datas = mapper.getSalary(year,salaryMonth);
		List<Map<String,Object>> taxratelist = mapper.getTaxratelist();
		
		Map<Integer,Map<String,Object>> taxratemap = new HashMap<Integer,Map<String,Object>>();
		for(Map<String,Object> taxrate:taxratelist) {
			String companyids = (String) taxrate.get("companyids");
			Integer id = Integer.valueOf(taxrate.get("id").toString());
			if(StringUtils.isEmpty(companyids)) {
				continue;
			}
			for(String companyid:companyids.split(",")) {
				if(StringUtils.isNotEmpty(companyid)) {
					taxratemap.put(Integer.valueOf(companyid), taxrate);
				}
			}
		}
		//hr_taxrate_detail
		List<Map<String,Object>> taxratedetaillist = mapper.gettaxratedetaillist();
			
		Map<Integer,List<Map<String,BigDecimal>>> taxratedetailmap = new HashMap<Integer,List<Map<String,BigDecimal>>>();
		for(Map<String,Object> taxratedetail:taxratedetaillist) {
			Integer taxrateid = Integer.valueOf(taxratedetail.get("taxrateid").toString());
			//lower up taxrate  quickcal
			BigDecimal lower = new BigDecimal(taxratedetail.get("lower").toString());
			BigDecimal up = new BigDecimal(taxratedetail.get("up").toString());
			BigDecimal taxrate = new BigDecimal(taxratedetail.get("taxrate").toString());
			BigDecimal quickcal = new BigDecimal(taxratedetail.get("val").toString());
			Map<String,BigDecimal> data = new HashMap<String,BigDecimal>();
			data.put("lower", lower);
			data.put("up", up);
			data.put("taxrate", taxrate);
			data.put("quickcal", quickcal);
			
			List<Map<String,BigDecimal>> mydatas = taxratedetailmap.get(taxrateid);
			if(null==mydatas) {
				mydatas = new ArrayList<Map<String,BigDecimal>>();
			}
			mydatas.add(data);
			taxratedetailmap.put(taxrateid, mydatas);
		}
		Map<String,Object> result = new HashMap<String,Object>();
		/**
		 * update by minzhq
		 * 解决1月有工资，3月不发工资累计工资为0的情况
		 * 自己求累计
		 */
		Map<String, List<Map<String, Object>>> dataMap = datas.stream().collect(Collectors.groupingBy(e->e.get("empno").toString()));
		for(Map.Entry<String, List<Map<String, Object>>> entry : dataMap.entrySet()){
			String empno = entry.getKey();
			Map<String,Object> resultMap = new HashMap<>();
			List<Map<String, Object>> values = entry.getValue();
			
			resultMap.put("empno", empno);
			resultMap.put("curmonth", salaryMonth);						
			List<Map<String, Object>> currentMonth = values.stream().filter(e->e.get("curmonth").toString().equals(salaryMonth)).collect(Collectors.toList());
			List<Map<String, Object>> lastMonth = values.stream().filter(e->!e.get("curmonth").toString().equals(salaryMonth)).collect(Collectors.toList());
			if(!currentMonth.isEmpty()){
				Map<String, Object> data = currentMonth.get(0);
				Integer salarycompanyid = Integer.valueOf(data.get("salarycompanyid").toString());				
				Map<String,Object> tax = taxratemap.get(salarycompanyid);
				if(null==tax) {
					continue;
				}
				Integer taxrateid = Integer.valueOf(tax.get("id").toString());
				List<Map<String,BigDecimal>> mytaxratelist = taxratedetailmap.get(taxrateid);
				if(null==mytaxratelist || mytaxratelist.size() <= 0) {
					continue;
				}
				resultMap.put("taxlist", JSON.parse(JSON.toJSONString(mytaxratelist)));
				resultMap.put("empname", data.get("empname").toString());
				resultMap.put("realpersonalincometax", data.get("realpersonalincometax").toString());
				resultMap.put("salary", data.get("salary").toString());
				resultMap.put("sociinsurcharge", data.get("sociinsurcharge").toString());
				resultMap.put("salarycompanyid", salarycompanyid);
				resultMap.put("specialtax", data.get("specialtax").toString());
				resultMap.put("specialtaxs", data.get("specialtaxs").toString());
				resultMap.put("startpoint", data.get("startpoint").toString());
				resultMap.put("startpoints", data.get("startpoints").toString());
				resultMap.put("freetax", data.get("freetax").toString());
				resultMap.put("freetaxs", data.get("freetaxs").toString());
				if(!lastMonth.isEmpty()){
					//过滤非同公司
					lastMonth = lastMonth.stream().filter(e->e.get("salarycompanyid").toString().equals(salarycompanyid.toString())).collect(Collectors.toList());
					if(!lastMonth.isEmpty()){
						BigDecimal realpersonalincometaxs = lastMonth.stream().map(e->new BigDecimal(e.get("realpersonalincometax").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
						BigDecimal salarys = lastMonth.stream().map(e->new BigDecimal(e.get("salary").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
						BigDecimal sociinsurcharges = lastMonth.stream().map(e->new BigDecimal(e.get("sociinsurcharge").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
						BigDecimal specialtaxs = lastMonth.stream().map(e->new BigDecimal(e.get("specialtax").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
						BigDecimal startpoints = lastMonth.stream().map(e->new BigDecimal(e.get("startpoint").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
						resultMap.put("realpersonalincometaxs", realpersonalincometaxs.toString());
						resultMap.put("salarys", salarys.toString());
						resultMap.put("sociinsurcharges", sociinsurcharges.toString());
						Map<String,Object> m = lastMonth.stream().sorted((e1,e2)->Integer.compare(Integer.valueOf(e1.get("curmonth").toString()), Integer.valueOf(e2.get("curmonth").toString()))).findFirst().get();
						resultMap.put("startmonth", m.get("curmonth").toString());
					}else{
						resultMap.put("realpersonalincometaxs", "0.00");
						resultMap.put("salarys", "0.00");
						resultMap.put("sociinsurcharges", "0.00");
						resultMap.put("startmonth", salaryMonth);
					}					
				}else{
					resultMap.put("realpersonalincometaxs", "0.00");
					resultMap.put("salarys", "0.00");
					resultMap.put("sociinsurcharges", "0.00");
					resultMap.put("startmonth", salaryMonth);
				}
				result.put(empno, resultMap);
			}
			
		}
		return result;
	}

	public String getSalaryUnitByEmpno(String empNo){
    	return this.mapper.getSalaryUnitByEmpno(empNo);
	}
	
	
	/**
	 * 获取工资发放单位
	 * @param month
	 * @return
	 */
	public Map<String, Object> getSalaryCompanyByMonth(String month) {
		Map<String,Object> result = new HashMap<String,Object>(); 
		List<Map<String,Object>> datas = this.mapper.getSalaryCompanyByMonth(month);
		for(Map<String,Object> data:datas) {
			String empname = data.get("empname").toString();
			String cardno = data.get("cardno").toString();
			String salarycompanyid = data.get("salarycompanyid").toString();
			String salarycompanyname = data.get("salarycompanyname").toString();
			result.put(empname+"_"+cardno, salarycompanyid+"|"+salarycompanyname);
		}
		return result;
	}
	
	public Page<Map<String, Object>> getTravelEmpPage(Integer page, Integer rows, String date, String queryText){
	    Page<Map<String, Object>> pageCond = new Page(page, rows);
	    List<Map<String, Object>> retList = this.mapper.getTravelEmpPage(pageCond, date, queryText, JdbcSqlThreadLocal.get());
	    pageCond.setRecords(retList);
	    return pageCond;
	}
	public List<Map<String,Object>> getSyncUserList(){
		return this.mapper.getSyncUserList();
	}

	public List<Map<String,Object>> getSyncBankAccountList(){
		return this.mapper.getSyncBankAccountList();
	}

	public List<Map<String,Object>> getSyncDeptList(){
		return this.mapper.getSyncDeptList();
	}

	public List<Map<String, Object>> getHrUserList() {
		return this.mapper.getHrUserList();
	}
}
