package com.jtyjy.finance.manager.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.jdbc.JdbcTemplateService;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.local.JdbcSqlThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.BudgetBillingUnitAccount;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.bean.WbBanks;
import com.jtyjy.finance.manager.mapper.BudgetBillingUnitAccountMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.mapper.response.BankInfo;
import com.jtyjy.finance.manager.vo.BillingUnitAccountVO;

import lombok.RequiredArgsConstructor;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetBillingUnitAccountService extends DefaultBaseService<BudgetBillingUnitAccountMapper, BudgetBillingUnitAccount> {

	private final TabChangeLogMapper loggerMapper;
	
	private final BudgetBillingUnitAccountMapper buaMapper;
	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_billing_unit_account"));
	}

	/**
	 * 按照单位查询账户
	 * @param kpdw
	 * @return
	 */
	public Set<BudgetBillingUnitAccount> getByUnitIds(Collection<Long> kpdw) {
		QueryWrapper<BudgetBillingUnitAccount> wrapper = new QueryWrapper<BudgetBillingUnitAccount>();
		wrapper.in("billingunitid", kpdw);
		wrapper.eq("stopflag", 0);
		List<BudgetBillingUnitAccount> list = this.list(wrapper);
		return new HashSet<BudgetBillingUnitAccount>(list);
	}
	
	/**
	 * 分页查询单位账户信息
	 * @param billingUnitId 开票单位id
	 * @param stopFlag 
	 * @param page
	 * @param rows
	 * @return
	 */
	public Page<BillingUnitAccountVO> getUnitAccountPageList(String billingUnitId, Integer stopFlag, String unitName, Integer page, Integer rows){
	    Page<BillingUnitAccountVO> pageCond = new Page<>(page, rows);
	    List<BillingUnitAccountVO> resultList = this.buaMapper.getUnitAccountPageList(pageCond, stopFlag, billingUnitId, unitName, JdbcSqlThreadLocal.get());
	    pageCond.setRecords(resultList);
	    return pageCond;
	}   
    
	/**
	 * 分页查询开户行信息
	 * @param bankName 行名模糊查询
	 * @param page
	 * @param rows
	 * @return
	 */
    public Page<WbBanks> getBankInfoPageList(String bankName, Integer page, Integer rows){
        Page<WbBanks> pageCond = new Page<>(page, rows);
        List<WbBanks> retList = this.buaMapper.getBankInfoPageList(pageCond, bankName, JdbcSqlThreadLocal.get());
        pageCond.setRecords(retList);
        return pageCond;
    }
    
    
    public boolean checkData(BudgetBillingUnitAccount bean, StringBuffer errMsg) {

        if (null == bean) {
            errMsg.append("数据不能为空");
            return false;
        }
        BudgetBillingUnitAccount sameAccount = this.getOne(new QueryWrapper<BudgetBillingUnitAccount>().eq("bankaccount", bean.getBankaccount()));
        BudgetBillingUnitAccount sameUnitBranch = this.getOne(new QueryWrapper<BudgetBillingUnitAccount>().eq("billingunitid", bean.getBillingunitid()).eq("branchcode", bean.getBranchcode()));
        
        if(null == bean.getId() || 0 == bean.getId().intValue()) {
            if (null != sameAccount && bean.getBankaccount().equals(sameAccount.getBankaccount())) {
                errMsg.append(bean.getBankaccount() + "账户已存在！");
                return false;
            }
            if (null != sameUnitBranch) {
                errMsg.append("单位下已存在该银行！");
                return false;
            }
            
        }else {
            if (null != sameAccount && !sameAccount.getId().equals(bean.getId())) {
                errMsg.append(bean.getBankaccount() + "账户已存在！");
                return false;
            }
            if (null != sameUnitBranch && !sameUnitBranch.getId().equals(bean.getId())) {
                errMsg.append("单位下已存在该银行！");
                return false;
            }
        }
        if (bean.getDefaultflag()) {//勾选了默认账户
            List<BudgetBillingUnitAccount> sameUnit = this.buaMapper.selectList(new QueryWrapper<BudgetBillingUnitAccount>().eq("billingunitid", bean.getBillingunitid()));
            for(BudgetBillingUnitAccount bua : sameUnit) {
                if (bua.getDefaultflag() && !bua.getId().equals(bean.getId())) {
                    errMsg.append("已经存在默认账户，一个单位只能有一个默认账户！");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 根据单位银行账号查询银行信息
     * @param bankAccountList
     * @return
     * @throws Exception 
     */
	public List<BankInfo> getBankInfoByAccounts(List<String> bankAccountList) throws Exception {
		String inSql = JdbcTemplateService.getInSql(bankAccountList, "'");
		return this.buaMapper.getBankInfoByAccounts(inSql);
	}
}
