package com.jtyjy.finance.manager.mapper;

import com.jtyjy.finance.manager.bean.WbBanks;
import com.jtyjy.finance.manager.bean.WbRegion;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.MapKey;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @author shubo
 */
public interface WbBanksMapper extends BaseMapper<WbBanks> {

    List<WbRegion> getAreaInfo(Page<WbRegion> pageCond, Integer level, String pcode, String name, String authSql);
    
	List<String> getBankType();
	
	List<WbBanks> getBankPageInfo(Page<WbBanks> pageCond, Map<String, Object> conditionMap, String authSql);

    @MapKey("subBranchCode")
    Map<String, WbBanks> queryAllBanks();

    WbBanks selectByAccount(String account);

    WbBanks selectByAccountId(String accountId);

    WbBanks selectByUnitAccount(String bankAccount);

	List<WbBanks> selectByBillingUnitId(String billingUnitId);
}
