package com.jtyjy.finance.manager.task;

import com.jtyjy.finance.manager.cache.DefaultCache;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author minzhq
 * 刷新用户 部门等数据的缓存
 */

@Slf4j
@Component
public class RefreshCacheTask {

	@Autowired
	private DefaultCache defaultCache;

	@XxlJob("refreshFinanceCacheTask")
	public ReturnT<String> refreshCacheTask(String param) throws Exception {
		log.info("刷新用户部门等数据的缓存==================START==============================");
		defaultCache.recache();
		log.info("刷新用户部门等数据的缓存==================END==============================");
		return ReturnT.SUCCESS;
	}
}
