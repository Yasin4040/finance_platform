package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.interceptor.BaseUser;
import com.jtyjy.core.interceptor.LoginThreadLocal;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.log.DefaultChangeLog;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.oadao.OAMapper;
import com.jtyjy.finance.manager.oapojo.OaUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 获取oa信息
 */
@Service
@Transactional(transactionManager = "oracleTransactionManager")
@JdbcSelector(value = "oracleJdbcTemplateService")
public class FineOAService extends DefaultBaseService<OAMapper, OaUser> {
	@Autowired
	private TabChangeLogMapper loggerMapper;

	@Autowired
	private OAMapper oaMapper;

	@Override
	public BaseMapper getLoggerMapper() {
		return loggerMapper;
	}

	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLog theLog = new DefaultChangeLog();
		BaseUser user = LoginThreadLocal.get();
		//SysDepartment对应的表名
		theLog.setTableName("hrmresource");
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
	 * 获取外派文秘的小号
	 * @return
	 */
	public List<Map<String, Object>> getSpecialPerson() {
		return oaMapper.getSpecialPerson();
	}
}
