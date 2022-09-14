package com.jtyjy.finance.manager.cache;

import com.jtyjy.finance.manager.bean.BudgetBillingUnit;
import com.jtyjy.finance.manager.bean.WbBanks;
import com.jtyjy.finance.manager.service.BudgetBillingUnitService;
import com.jtyjy.finance.manager.service.BudgetUnitService;
import com.jtyjy.finance.manager.service.WbBanksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * 开票单位缓存
 * @author minzhq
 */
@Component
public class UnitCache extends BaseCache{
	
	@Autowired
	private BudgetBillingUnitService billingUnitService;
	

	public static Map<String, BudgetBillingUnit> UNIT_MAP = new HashMap<>();
	public static Map<String, BudgetBillingUnit> UNIT_NAME_MAP = new HashMap<>();
	public static Map<String, BudgetBillingUnit> UNIT_OUT_KEY_MAP = new HashMap<>();

	public static BudgetBillingUnit get(String key) {
		return UNIT_MAP.get(key);
	}
	public static BudgetBillingUnit getByName(String key) {
		return UNIT_NAME_MAP.get(key);
	}
	public static BudgetBillingUnit getByOutKey(String key) {
		return UNIT_OUT_KEY_MAP.get(key);
	}
	public static List<String> getByFuzzyName(String key) {
		List<String> ids = UNIT_MAP.entrySet().stream().filter(x -> x.getValue().getName().contains(key)).map(x -> x.getKey()).collect(Collectors.toList());
		return ids;
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
		List<BudgetBillingUnit> banks = billingUnitService.list();
		banks.forEach(x->{
			UNIT_MAP.put(String.valueOf(x.getId()),x);
			UNIT_NAME_MAP.put(String.valueOf(x.getName()),x);
			UNIT_OUT_KEY_MAP.put(String.valueOf(x.getOutKey()),x);
		});
	}
	
}
