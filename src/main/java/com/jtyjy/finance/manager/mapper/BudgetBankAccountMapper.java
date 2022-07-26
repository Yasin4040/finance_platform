package com.jtyjy.finance.manager.mapper;


import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetBankAccount;
import com.jtyjy.finance.manager.easyexcel.BankAccountExcelData;
import com.jtyjy.finance.manager.mapper.response.BankInfo;
import com.jtyjy.finance.manager.vo.BankAccountVO;

/**
 * @author shubo
 */
public interface BudgetBankAccountMapper extends BaseMapper<BudgetBankAccount> {
	List<BankAccountVO> getBankAccountPageInfo(Page<BankAccountVO> pageCond, Map<String, Object> conditionMap, String authSql);
	/**
	 * 根据个人银行账号查询银行信息
	 * @param inSql
	 * @return
	 */
	List<BankInfo> getBankInfoByAccounts(@Param("inSql") String inSql);

	List<BankAccountExcelData> getBankAccountExcelInfo(@Param("conditionMap") Map<String, Object> conditionMap);

	List<BankAccountVO> getBankAccountByAccount(String bankaccount, String accountname);
}
