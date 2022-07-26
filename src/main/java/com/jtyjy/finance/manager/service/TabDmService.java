package com.jtyjy.finance.manager.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.interceptor.BaseUser;
import com.jtyjy.core.interceptor.LoginThreadLocal;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.log.DefaultChangeLog;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.TabDm;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.mapper.TabDmMapper;

/**
 * <p></p>
 * <p>作者 konglingcheng</p>
 * <p>date 2020年4月28日</p>
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager")
@JdbcSelector(value = "defaultJdbcTemplateService")
@SuppressWarnings("all")
public class TabDmService extends DefaultBaseService<TabDmMapper,TabDm>{

	@Autowired
	private TabChangeLogMapper loggerMapper;
	
	@Autowired
	private TabDmMapper dmMapper;

	@Override
	public BaseMapper getLoggerMapper() {
		return loggerMapper;
	}

	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLog theLog = new DefaultChangeLog();
		BaseUser user = LoginThreadLocal.get();
		//SysDepartment对应的表名
		theLog.setTableName("tab_dm");
		//当前登录人的主键
		theLog.setOperatorId(user.getEmpid().toString());
		//当前登录人的名称
		theLog.setUsername(user.getEmpname());
		//当前登录人的其他信息，如工号
		theLog.setOperatorName(user.getEmpno());
		//日志创建时间
		theLog.setCreateTime(System.currentTimeMillis());
		DefaultChangeLogThreadLocal.set(theLog);
	}

	/**
	 * 查询是否存在相同的记录
	 * @param dmType
	 * @param dm
	 * @return
	 */
	public int validIsTheSame(String dmType, String dm) {
		int count = dmMapper.selectSameRecords(dmType,dm);
		return count;
	}

	public TabDm validIsTheSameByName(String dmType, String dmName) {
		TabDm dm =  dmMapper.selectSameRecordsByName(dmType,dmName);
		return dm;
	}

	/**
	 * 
		@Description 查询所有的父类
		@Author ldw
		@Date：20202020年6月2日
	 */
	public List<Map<String, Object>> selectAllType() {
		List<Map<String, Object>> list =  dmMapper.selectAllType();
		return list;
	}

	/**
	 * 
		@Description 根据属性查选项
		@Author ldw
		@Date：20202020年6月2日
	 */
	public List<TabDm> selectAllSubType(String type) {
		List<TabDm> list =  dmMapper.selectSubAllType(type);
		return list;
	}

	public void start(TabDm bean) {
		this.update("UPDATE tab_dm SET dm_status=1 WHERE dm =? AND dm_type = ?", new Object[]{bean.getDm(),bean.getDmType()});
	}

	public void stop(TabDm bean) {
		this.update("UPDATE tab_dm SET dm_status=0 WHERE dm =? AND dm_type = ?", new Object[]{bean.getDm(),bean.getDmType()});
	}

	public void updateMe(String newDm,TabDm bean) {
		this.update("UPDATE tab_dm SET dm=?, dm_name =? WHERE dm =? AND dm_type = ?", new Object[]{newDm,bean.getDmName(),bean.getDm(),bean.getDmType()});
	}

	/**
	 * 根据代码类型和代码值查询
	 * 作者:konglingcheng
	 * 日期:2020年8月12日
	 * @param ckczDmlx
	 * @param ckczDm
	 * @return
	 */
	public TabDm getByPrimaryKey(String ckczDmlx, String ckczDm) {
		String sql = "select * from tab_dm where dm_type = ? and dm = ? and dm_status = 1";
		List<TabDm> list = this.query(sql, TabDm.class, new Object[] {ckczDmlx,ckczDm});
		if(list == null || list.size() == 0) {
			return null;
		}
		return list.get(0);
	}

	public boolean getFoodFeeEmp(String empno) {
		String sql = "select * from tab_dm where dm_type = 'FOODFEE' and dm = '" + empno + "'";
		List<TabDm> list = this.jdbcTemplateService.query(sql, TabDm.class);
		if(list != null && list.size() > 0) {
			return true;
		}
		return false;
	}
	
}
