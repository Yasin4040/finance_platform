package com.jtyjy.finance.manager.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetBillingUnitAccount;
import com.jtyjy.finance.manager.bean.WbBanks;
import com.jtyjy.finance.manager.mapper.response.BankInfo;
import com.jtyjy.finance.manager.vo.BillingUnitAccountVO;
/**
 * @author Admin
 */
public interface BudgetBillingUnitAccountMapper extends BaseMapper<BudgetBillingUnitAccount> {
	
    // 查询单位账户信息
    List<BillingUnitAccountVO> getUnitAccountPageList(Page pageCond, Integer stopFlag, String billingUnitId, String unitName, String authSql);
    
    // 查询开票单位信息
    List<WbBanks> getBankInfoPageList(Page pageCond,
                                                 String bankName,
                                                 String authSql);

    /**
     * 根据银行账户查询银行信息
     * @param inSql
     * @return
     */
	List<BankInfo> getBankInfoByAccounts(@Param("inSql")String inSql);
}
