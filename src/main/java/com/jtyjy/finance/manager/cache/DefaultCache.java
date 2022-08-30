package com.jtyjy.finance.manager.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class DefaultCache {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCache.class);
	
	@Autowired
	private DeptCache deptCache;
	
	@Autowired
	private UserCache userCache;

	@Autowired
	private BankCache bankCache;

	@Autowired
	private PersonCache personCache;
	@Autowired
	private UnitCache unitCache;
	@PostConstruct
	public void create() throws Exception {
		deptCache.cache();
		LOGGER.info("缓存部门成功......");
		this.userCache.cache();
		LOGGER.info("缓存用户成功......");
		bankCache.cache();
		LOGGER.info("缓存银行成功......");
		personCache.cache();
		LOGGER.info("缓存人员成功......");
		unitCache.cache();
		LOGGER.info("缓存开票单位成功......");
	}
	
	/**
	 * 重新缓存
	 * 作者:konglingcheng
	 * 日期:2020年7月9日
	 * @throws Exception
	 */
	public void recache() throws Exception {
		deptCache.recache();
		LOGGER.info("重新缓存部门成功......");
		userCache.recache();
		LOGGER.info("重新缓存用户成功......");
		bankCache.recache();
		LOGGER.info("重新缓存银行成功......");
		personCache.cache();
		LOGGER.info("缓存人员成功......");
	}
}
