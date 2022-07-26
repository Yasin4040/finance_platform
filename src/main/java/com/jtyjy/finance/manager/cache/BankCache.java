package com.jtyjy.finance.manager.cache;

import com.jtyjy.finance.manager.bean.WbBanks;
import com.jtyjy.finance.manager.service.WbBanksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 银行缓存
 * @author minzhq
 */
@Component
public class BankCache extends BaseCache{
	
	@Autowired
	private WbBanksService banksService;
	

	public static Map<String, WbBanks> BANK_MAP = new HashMap<String, WbBanks>();


	public static WbBanks getBankByBranchCode(String key) {
		return BANK_MAP.get(key);
	}


	@Override
	public void cache() throws Exception {
		this.doJob();
	}

	@Override
	public void recache() throws Exception {
		this.doJob();
	}

	private void doJob() throws Exception {
		String sql = "SELECT * FROM wb_banks";
		List<WbBanks> banks = banksService.query(sql, WbBanks.class);
		setMapByList(banks, BANK_MAP, "subBranchCode", WbBanks.class);
	}
	
}
