package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.finance.manager.bean.WbBanks;
import com.jtyjy.finance.manager.bean.WbRegion;
import com.jtyjy.finance.manager.mapper.WbBanksMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.local.JdbcSqlThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@JdbcSelector(value = "defaultJdbcTemplateService")
public class WbBanksService extends DefaultBaseService<WbBanksMapper, WbBanks> {

	@Autowired
	private TabChangeLogMapper loggerMapper;
	@Autowired
	private  WbBanksMapper wbMapper;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("wb_banks"));
	}
	
	/**
	 * 分页查询省市区
	 * @param level 区域等级 1：省份 2：城市 3：区县
	 * @param pcode 上级代码
	 * @param page 当前页
	 * @param rows 页数量
	 * @return
	 */
    public Page<WbRegion> getAreaInfo(Integer level, String pcode, String name, Integer page, Integer rows){
        Page<WbRegion> pageCond = new Page<>(page, rows);
        List<WbRegion> resultList = this.wbMapper.getAreaInfo(pageCond, level, pcode, name, JdbcSqlThreadLocal.get());
        pageCond.setRecords(resultList);
        return pageCond;
    }
    
    public List<String> getBankType(){
        return this.wbMapper.getBankType();
    }
    
	public Page<WbBanks> getBankPageInfo(Map<String, Object> conditionMap, Integer page, Integer rows){
	    Page<WbBanks> pageCond = new Page<>(page, rows);
        List<WbBanks> resultList = this.wbMapper.getBankPageInfo(pageCond, conditionMap, JdbcSqlThreadLocal.get());
        pageCond.setRecords(resultList);
        return pageCond;
	}
}
