package com.jtyjy.finance.manager.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.finance.manager.bean.BudgetMonthPeriod;
import com.jtyjy.finance.manager.bean.BudgetMonthStartup;
import com.jtyjy.finance.manager.bean.BudgetYearPeriod;
import com.jtyjy.finance.manager.bean.BudgetYearStartup;
import com.jtyjy.finance.manager.bean.BudgetYearPeriod;
import com.jtyjy.finance.manager.mapper.BudgetMonthPeriodMapper;
import com.jtyjy.finance.manager.mapper.BudgetMonthStartupMapper;
import com.jtyjy.finance.manager.mapper.BudgetYearPeriodMapper;
import com.jtyjy.finance.manager.mapper.BudgetYearStartupMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.vo.YearPeriodVO;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.bean.TabProcedure;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import lombok.RequiredArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetYearPeriodService extends DefaultBaseService<BudgetYearPeriodMapper, BudgetYearPeriod> {

	private final TabChangeLogMapper loggerMapper;
	
	private final BudgetYearPeriodMapper bypMapper;

    private final BudgetYearStartupMapper bysMapper;
    
    private final BudgetMonthPeriodMapper bmpMapper;
    
    private final BudgetMonthStartupMapper bmsMapper;
	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_year_period"));
	}
	
	public Boolean addPeriod(BudgetYearPeriod bperiod, StringBuffer errMsg) {
        bperiod.setCreatetime(new Date());
        //判断期间名称是否存在
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT * FROM budget_year_period WHERE period=? ");
        BudgetYearPeriod yearperiod = this.bypMapper.selectOne(new QueryWrapper<BudgetYearPeriod>().eq("period", bperiod.getPeriod()));
        
        if (null!=yearperiod) {
            errMsg.append("期间【"+bperiod.getPeriod()+"】已经存在。");
            return false;
        }
        if (bperiod.getCurrentflag()) {
            UpdateWrapper<BudgetYearPeriod> wrapper = new UpdateWrapper<BudgetYearPeriod>();
            wrapper.set("currentflag", false);
            wrapper.eq("currentflag", true);
            this.update(wrapper);
        }
        this.bypMapper.insert(bperiod);
        
        List<BudgetMonthPeriod> monthperiods = this.bmpMapper.selectList(null);
        //BudgetMonthStartup
        //添加月度启动
        for(BudgetMonthPeriod monthperiod:monthperiods) {
            BudgetMonthStartup monthst = new BudgetMonthStartup();
            monthst.setCreateTime(new Date());
            monthst.setYearid(bperiod.getId());
            monthst.setEndbudgeteditflag(false);
            monthst.setStartbudgetflag(false);
            monthst.setMonthid(monthperiod.getId());
            //monthst.setMonth(Integer.valueOf(monthperiod.getCode()));
            this.bmsMapper.insert(monthst);
        }
        //添加年度启动
        BudgetYearStartup yearstartup = new BudgetYearStartup();
        yearstartup.setYearid(bperiod.getId());
        yearstartup.setCreateTime(new Date());
        yearstartup.setEndbudgeteditflag(false);
        yearstartup.setEndsubjecteditflag(false);
        yearstartup.setEnduniteditflag(false);
        yearstartup.setStartbudgetflag(false);
        this.bysMapper.insert(yearstartup);
        //当前期间
        if (bperiod.getCurrentflag()) {
            //update budget_year_period set currentflag=0 WHERE id != ?
            UpdateWrapper<BudgetMonthPeriod> wrapper = new UpdateWrapper<BudgetMonthPeriod>();
            wrapper.eq(false, "id", 6);
            BudgetMonthPeriod updateBean = new BudgetMonthPeriod();
            updateBean.setCurrentflag(false);//非6月更新为非当前
            this.bmpMapper.update(updateBean, wrapper);
            wrapper = new UpdateWrapper<BudgetMonthPeriod>();
            wrapper.eq("id", 6);
            updateBean.setCurrentflag(true);
            this.bmpMapper.update(updateBean, wrapper);//6月更新为当前
        }
        return true;
	}
	
	public Boolean updatePeriod(BudgetYearPeriod bperiod, StringBuffer errMsg) {

        if (bperiod.getPeriod().contains("月")) {
            errMsg.append("月度期间不可修改。");
            return false;
        }
        BudgetYearPeriod oldperiod = this.getById(bperiod.getId());
        if (null == oldperiod) {
            errMsg.append("此期间未找到");
            return false;
        }
        if(bperiod.getCurrentflag()) {
            UpdateWrapper<BudgetYearPeriod> wrapper = new UpdateWrapper<BudgetYearPeriod>();
            wrapper.set("currentflag", false);
            wrapper.eq("currentflag", true);
            this.update(wrapper);
        }
        
        List<Map<String, Object>> budgetMonthStartups = this.bypMapper.getMonthPeriod(oldperiod.getId());
        if(null==budgetMonthStartups || budgetMonthStartups.isEmpty()) {
            List<BudgetMonthPeriod> monthperiods = this.bmpMapper.selectList(null);
            //添加月度启动
            for(BudgetMonthPeriod monthperiod:monthperiods) {
                BudgetMonthStartup monthst = new BudgetMonthStartup();
                monthst.setCreateTime(new Date());
                monthst.setYearid(oldperiod.getId());
                monthst.setEndbudgeteditflag(false);
                monthst.setStartbudgetflag(false);
                monthst.setMonthid(monthperiod.getId());
                //monthst.setMonth(Integer.valueOf(monthperiod.getCode()));
                this.bmsMapper.insert(monthst);
            }
        }
        
        BudgetYearStartup budgetYearStartup = this.bysMapper.selectOne(new QueryWrapper<BudgetYearStartup>().eq("yearid", oldperiod.getId()));
        if(null==budgetYearStartup) {
            budgetYearStartup = new BudgetYearStartup();
            budgetYearStartup.setCreateTime(new Date());
            budgetYearStartup.setYearid(oldperiod.getId());
            budgetYearStartup.setEndbudgeteditflag(false);
            budgetYearStartup.setEndsubjecteditflag(false);
            budgetYearStartup.setEnduniteditflag(false);
            budgetYearStartup.setStartbudgetflag(false);
            this.bysMapper.insert(budgetYearStartup);
        }
        this.bypMapper.updateById(bperiod);
        return true;
	}
	
	public List<YearPeriodVO> getYearMonthPeriod() {
	    List<YearPeriodVO> list = new ArrayList<YearPeriodVO>();
        
	    List<Map<String, Object>> yearList = bypMapper.getYearPeriod();
	    for (Map<String, Object> year : yearList) {
	        YearPeriodVO yearVo = new YearPeriodVO(); 
	        yearVo.setPeriodId(String.valueOf(year.get("yid")));
            yearVo.setId((Long) year.get("yid"));
            yearVo.setPeriod((String)year.get("yname"));
            yearVo.setYearmonthname(yearVo.getPeriod());
            yearVo.setCode((String)year.get("ycode"));
            yearVo.setStartdate((String)year.get("startdate"));
            yearVo.setEnddate((String)year.get("enddate"));
            yearVo.setCurrentflag((String)year.get("ycurrent"));
            yearVo.setStartbudgetflag((String)year.get("startbudgetflag"));
            List<Map<String, Object>> monthList = this.bypMapper.getMonthPeriod(yearVo.getId());
            List<YearPeriodVO> monthVoList = new ArrayList<YearPeriodVO>();
            
            for (Map<String, Object> month : monthList) {
                YearPeriodVO monthVo = new YearPeriodVO();
                monthVo.setId((Long) month.get("mid"));
                monthVo.setPeriod((String)month.get("mname"));
                monthVo.setCode((String)month.get("mcode"));
                monthVo.setCurrentflag((String)month.get("mcurrent"));
                monthVo.setPeriodId(String.valueOf(year.get("yid")) + "-" + (String)month.get("mcode"));
                monthVo.setYearmonthname(yearVo.getPeriod() + "-" + (String)month.get("mcode"));
                monthVo.setStartbudgetflag((String)month.get("startbudgetflag"));
                if ("1".equals(month.get("mcurrent")) && "1".equals(yearVo.getCurrentflag())) {
                    monthVo.setCurrentflag("1");
                }else {
                    monthVo.setCurrentflag("0");
                }
                monthVo.setEndbudgeteditflag((String)month.get("endbudgeteditflag"));
                monthVoList.add(monthVo);
            }
            yearVo.setChildren(monthVoList);
            list.add(yearVo);
	    }
	    return list;
	}
	
	public List<BudgetMonthPeriod> getMonthPeriod() {
        List<BudgetMonthPeriod> list = new ArrayList<BudgetMonthPeriod>();
        list = this.bmpMapper.selectList(null);
        return list;
    }
	
	//获取当前届别
	public BudgetYearPeriod getNowYearPeriod() {
	    List<BudgetYearPeriod> yearPeriod = this.bypMapper.getCurrentPeriod();
	    if (null == yearPeriod || yearPeriod.isEmpty()) {//无当前届别默认获取最新的届别
	        yearPeriod = this.bypMapper.getNewestPeriod();
	    }
	    return yearPeriod.get(0);
	}
	
    
    public boolean checkData(BudgetYearPeriod bean, StringBuffer errMsg) {

        if (null == bean) {
            errMsg.append("数据不能为空");
            return false;
        }
        BudgetYearPeriod sameName = this.getOne(new QueryWrapper<BudgetYearPeriod>().eq("period", bean.getPeriod()));
        BudgetYearPeriod sameCode = this.getOne(new QueryWrapper<BudgetYearPeriod>().eq("code", bean.getCode()));
        
        if(null == bean.getId() || 0 == bean.getId().intValue()) {
            if (null != sameName && bean.getPeriod().equals(sameName.getPeriod())) {
                errMsg.append(bean.getPeriod() + "名称已存在！");
                return false;
            }
            if (null != sameCode && bean.getCode().equals(sameCode.getCode())) {
                errMsg.append(bean.getCode() + "代码已存在！");
                return false;
            }
            
        }else {
            if (null != sameName && !sameName.getId().equals(bean.getId())) {
                errMsg.append(bean.getPeriod() + "名称已存在！");
                return false;
            }
            if (null != sameCode && !sameCode.getId().equals(bean.getId())) {
                errMsg.append(bean.getCode() + "代码已存在！");
                return false;
            }
        }

        return true;
    }
}
