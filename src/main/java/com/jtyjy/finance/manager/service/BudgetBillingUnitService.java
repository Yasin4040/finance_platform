package com.jtyjy.finance.manager.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.common.tools.PinyinTools;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.jdbc.JdbcTemplateService;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.local.JdbcSqlThreadLocal;
import com.jtyjy.core.result.KVBean;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.BudgetBillingUnit;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.bean.WbUser;
import com.jtyjy.finance.manager.mapper.BudgetBillingUnitMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.mapper.WbUserMapper;
import com.jtyjy.finance.manager.vo.BillingUnitVO;

import lombok.RequiredArgsConstructor;

/**
 * 开票单位service
 * @author shubo
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@JdbcSelector(value = "defaultJdbcTemplateService")
public class BudgetBillingUnitService extends DefaultBaseService<BudgetBillingUnitMapper, BudgetBillingUnit> {

	private final TabChangeLogMapper loggerMapper;
	private final BudgetBillingUnitMapper budgetBillingUnitMapper;
	private final WbUserMapper wbUserMapper;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_billing_unit"));
	}
	
	public boolean add(BudgetBillingUnit bean) {
	    bean.setFirstSpell(PinyinTools.getFirstspell(bean.getName()));
	    bean.setFullSpell(PinyinTools.getPinYin(bean.getName()));
	    int insertRet = budgetBillingUnitMapper.insert(bean);
	    if (insertRet > 0) {
	        bean.setCode("U00" + bean.getId());
	        insertRet = budgetBillingUnitMapper.updateById(bean);
	    }
	    return insertRet > 0;
	}
	
    public boolean modify(BudgetBillingUnit bean) {
        
        bean.setFirstSpell(PinyinTools.getFirstspell(bean.getName()));
        bean.setFullSpell(PinyinTools.getPinYin(bean.getName()));
        int insertRet = budgetBillingUnitMapper.updateById(bean);
        
        return insertRet > 0;
    }
    
	/**
	 * 条件查询开票单位
	 * @param conditionMap
	 * @param page
	 * @param rows
	 * @return
	 */
	public Page<BillingUnitVO> getBillUnitPageList(Map<String, Object> conditionMap, Integer page, Integer rows){
	    Page<BillingUnitVO> pageCond = new Page<>(page, rows);
        
	    List<BillingUnitVO> retList = budgetBillingUnitMapper.getBillUnitPageList(pageCond, conditionMap, JdbcSqlThreadLocal.get());
	    for(BillingUnitVO bu : retList) {
	        String budgeters = bu.getBudgeters();
	        String accountants = bu.getAccountants();
	        if (StringUtils.isNotBlank(budgeters)) {
	            String[] userNameCodes = getUserNameCodeStr(budgeters);
	            bu.setBudgetersName(userNameCodes[0]);
	            bu.setBudgetersCode(userNameCodes[1]);
	        }else {
	            bu.setBudgetersName("");
                bu.setBudgetersCode("");
	        }
	        if (StringUtils.isNotBlank(accountants)) {
	            String[] userNameCodes = getUserNameCodeStr(accountants);
                bu.setAccountantsName(userNameCodes[0]);
                bu.setAccountantsCode(userNameCodes[1]);
            }else {
                bu.setAccountantsName("");
                bu.setAccountantsCode("");
            }
	        String fullName = bu.getName();
	        if ("0".equals(bu.getBillingUnitType())) {
	            fullName = fullName + "(无票)";
	        }
	        bu.setFullName(fullName);
	    }
	    pageCond.setRecords(retList);
	    return pageCond;
	}
	
	/**
	 * 根据id获取名称，以逗号隔开
	 * @param codes
	 * @return
	 */
	public String[] getUserNameCodeStr(String codes) {
	    String [] userNameCode = new String[2];
	    StringJoiner userNames = new StringJoiner(",");
	    StringJoiner userCodes = new StringJoiner(","); 
	    String[] codeArr = codes.split(",");
	    List<String> codeList = Arrays.asList(codeArr);
	    List<WbUser> userList = wbUserMapper.selectList(new QueryWrapper<WbUser>().in("USER_ID", codeList));
		Map<String, WbUser> userMap = userList.stream().collect(Collectors.toMap(WbUser::getUserId, Function.identity(), (e1, e2) -> e1));
		for (String id : codeArr) {
			WbUser user = userMap.get(id);
			userNames.add(user.getDisplayName());
			userCodes.add(user.getUserName());
		}
	    userNameCode[0] = userNames.toString();
	    userNameCode[1] = userCodes.toString();
	    return userNameCode;
	}
	
    public boolean checkData(BudgetBillingUnit bean, StringBuffer errMsg) {
        if (null == bean) {
            errMsg.append("数据不能为空");
            return false;
        }
        BudgetBillingUnit sameName = this.getOne(new QueryWrapper<BudgetBillingUnit>().eq("name", bean.getName()));
        
        if(null == bean.getId() || 0 == bean.getId().intValue()) {
            if (null != sameName && bean.getName().equals(sameName.getName())) {
                errMsg.append(bean.getName() + "名称已存在！");
                return false;
            }
            
        }else {
            if (null != sameName && !sameName.getId().equals(bean.getId())) {
                errMsg.append(bean.getName() + "名称已存在！");
                return false;
            }
        }
        return true;
    }

    /**
     * 查询开票单位会计的工号和姓名，工号之间用逗号分割，姓名主键也用逗号分割
     * @param bunits
     * @throws Exception 
     */
	public Map<Long, KVBean> getAccountsInfo(List<Long> bunits) throws Exception {
		//查询开票单位下的所有会计
		String inSql = JdbcTemplateService.getInSql(bunits, null);
		String sql = "SELECT id,code,name,billingunittype,corporation,ownflag,stopflag,orderno,budgeters,accountants,remark,firstspell,fullspell,outkey FROM budget_billing_unit WHERE (id IN ("+inSql+") AND stopflag = 0 AND accountants IS NOT NULL)";
		List<BudgetBillingUnit> units = this.jdbcTemplateService.query(sql, BudgetBillingUnit.class);
		if(units == null || units.size() == 0) {
			return null;
		}
		Set<String> accountIdSet = new HashSet<String>();
		String[] accountants = null;
		for (BudgetBillingUnit bean : units) {
			accountants = bean.getAccountants().split(",");
			for (String accountant : accountants) {
				accountIdSet.add(accountant);
			}
		}
		List<String> accountIds = new ArrayList<String>(accountIdSet);
		QueryWrapper<WbUser> userWrapper = new QueryWrapper<WbUser>();
		userWrapper.in("user_id", accountIds);
		userWrapper.in("status", 1);
		List<WbUser> users = this.wbUserMapper.selectList(userWrapper);
		if(users == null || users.size() == 0) {
			return null;
		}
		//将用户和单位组合
		Map<Long, KVBean> result = new HashMap<Long, KVBean>();
		StringJoiner sjk = null;
		StringJoiner sjv = null;
		KVBean kvBean = null;
		for (BudgetBillingUnit unit : units) {
			if(result.get(unit.getId()) == null) {
				sjk = new StringJoiner(",");
				sjv = new StringJoiner(",");
				result.put(unit.getId(), new KVBean(sjk, sjv));
			}
			kvBean = result.get(unit.getId());
			for (WbUser wbUser : users) {
				if(StringUtils.isNotBlank(unit.getAccountants()) && unit.getAccountants().contains(wbUser.getUserId())) {
					sjk = (StringJoiner) kvBean.getK();
					sjk.add(wbUser.getUserName());
					sjv = (StringJoiner) kvBean.getV();
					sjv.add(wbUser.getDisplayName());
				}
			}
		}
		return result;
	}
}
