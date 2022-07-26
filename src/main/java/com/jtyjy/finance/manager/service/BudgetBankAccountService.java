package com.jtyjy.finance.manager.service;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.easyexcel.ImportBankAccountExcelData;
import com.jtyjy.finance.manager.event.InvokeRecordEvent;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.jdbc.JdbcTemplateService;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.local.JdbcSqlThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.easyexcel.BankAccountExcelData;
import com.jtyjy.finance.manager.mapper.response.BankInfo;
import com.jtyjy.finance.manager.vo.BankAccountVO;

import lombok.RequiredArgsConstructor;

import javax.annotation.Resource;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetBankAccountService extends DefaultBaseService<BudgetBankAccountMapper, BudgetBankAccount> {
    public static final Integer IN_ACCOUNT_TYPE =1;  //对内账户
    public static final Integer OUT_ACCOUNT_TYPE =2;  //对外账户
    public static final String OPERATE_TYPE_ADD = "new bankaccount";
    public static final String OPERATE_TYPE_MODIFY= "modify bankaccount";
	private final TabChangeLogMapper loggerMapper;

	private final BudgetBankAccountMapper bbaMapper;
	
	private final WbDeptMapper wdMapper;
	
	private final WbUserMapper wuMapper;

    private final BudgetUnitMapper unitMapper;

    private final WbBanksMapper wbBanksMapper;

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_bank_account"));
	}
	
	public String addUserAccount(BudgetBankAccount bankAcc, WbUser wbUser) {
        String errMsg = "";
	    bankAcc.setId(null);
        
        //编号只能有字母和数字（对内账户：全数字；对外账户：字母、数字）
         String regex = "^[a-z0-9A-Z]+$";
         if (!bankAcc.getCode().matches(regex)) {
             return "【编号】请填写数字、字母或者字母数字的组合";
        }
        //验证“账户类型”、“编号”、“户名”
        errMsg = validateType_Code_Name(bankAcc,OPERATE_TYPE_ADD);
        if(!"".equals(errMsg)) {
            return errMsg;
        }
        if(null == bankAcc.getOrderno()) {
            bankAcc.setOrderno(0);
        }
        //验证银行卡号是否填写正确
        /*boolean isTrueNum = BudgetSysUtils.validateBankCardNum(bankAcc.getBankaccount());
        if (!isTrueNum) {
              throw new RuntimeException("【"+bankAcc.getBankaccount()+"】"+"该银行卡号填写有误，请检查！");
        }*/
        String newCardNO = trimCardNO(bankAcc.getBankaccount());//卡号去空格
        BudgetBankAccount accountList = this.bbaMapper.selectOne(new QueryWrapper<BudgetBankAccount>().eq("bankaccount", bankAcc.getBankaccount()));
        if (null != accountList) {
            return "【"+bankAcc.getBankaccount()+"】该银行账号已重复，请重新输入！";
        }
        if (bankAcc.getWagesflag()) {
            List<BudgetBankAccount> wagesAccount =this.bbaMapper.selectList(new QueryWrapper<BudgetBankAccount>().eq("accounttype", 1).eq("wagesflag", 1).eq("pname", bankAcc.getPname()).eq("code", bankAcc.getCode()));
            if (null != wagesAccount) {
                return "该人员已经存在一个工资账户，不允许再次添加！";

            }
        }
        //"户名"没填就默认为“名称”对应的名字
        if (StringUtils.isBlank(bankAcc.getAccountname())) {
            bankAcc.setAccountname(bankAcc.getPname());
        }
        bankAcc.setBankaccount(newCardNO);
        applicationEventPublisher.publishEvent(new InvokeRecordEvent(bankAcc,wbUser));
        this.save(bankAcc);
        return errMsg;
    }
	
    public String editUserAccount(BudgetBankAccount bbk, WbUser wbUser) {
        String errMsg = "";
        if (StringUtils.isBlank(bbk.getRemark())) {
            bbk.setRemark(" ");
        }
        
        if (StringUtils.isNotBlank(bbk.getBankaccount())) {
            //验证银行卡号是否填写正确
            /*if (null !=bbk.get("bankaccount").toString()) {
                if (!BudgetSysUtils.validateBankCardNum(bbk.get("bankaccount").toString())) {
                      throw new RuntimeException("【"+bbk.get("bankaccount").toString()+"】"+"该银行卡号填写有误，请检查！");
                }
            }*/
            String newCardNO = trimCardNO(bbk.getBankaccount());//卡号去空格
            bbk.setBankaccount(newCardNO);
            BudgetBankAccount accountList = this.bbaMapper.selectOne(new QueryWrapper<BudgetBankAccount>().eq("bankaccount", bbk.getBankaccount()));
            if (null != accountList && !accountList.getId().equals(bbk.getId())) {
                return "【"+bbk.getBankaccount()+"】该银行账号已重复，请重新输入！";
            }
        }
        
        errMsg = validateType_Code_Name(bbk,OPERATE_TYPE_MODIFY);
        if (!"".equals(errMsg)) {
            return errMsg;
        }
        if (bbk.getWagesflag()) {
            BudgetBankAccount wagesAccount =this.bbaMapper.selectOne(new QueryWrapper<BudgetBankAccount>().eq("accounttype", 1).eq("wagesflag", 1).eq("pname", bbk.getPname()).eq("code", bbk.getCode()));
            if (null != wagesAccount && !wagesAccount.getId().equals(bbk.getId())) {
                return "该人员已经存在一个工资账户，不允许再次添加！";
                
            }
        }
        if(null == bbk.getOrderno()){
            bbk.setOrderno(0);
        }
        applicationEventPublisher.publishEvent(new InvokeRecordEvent(bbk,wbUser));
        this.updateById(bbk);
        return errMsg;
    }
	
	public String validateType_Code_Name(BudgetBankAccount bankAcc,String operateType){
        String errMsg = "";
        //对内账户(类型：1)，编号应该为员工工号；对外账户(类型：2)，编号可以自己填写，但不能占用工号
/*      if (operateType .equals(OPERATE_TYPE_ADD)) {//新增时需要验证是否已存在该编号
            isDuplicateCode(bankAcc.getCode());
        }*/
        List<BudgetBankAccount> bankAccounts = this.bbaMapper.selectList(new QueryWrapper<BudgetBankAccount>().eq("accounttype", 2));
        if (IN_ACCOUNT_TYPE.equals(bankAcc.getAccounttype()) && null != bankAcc.getCode()) {
            WbUser inUser = this.wuMapper.selectOne(new QueryWrapper<WbUser>().eq("USER_NAME", bankAcc.getCode().trim()));
            if (null != inUser && !inUser.getDisplayName().equals(bankAcc.getPname().trim())) {
                return "工号与姓名不一致！";
            }
        }
        if (OUT_ACCOUNT_TYPE.equals(bankAcc.getAccounttype()) && null != bankAcc.getCode()) {
            if (bankAcc.getWagesflag()) {
                return "选择“对外账户”时，不能勾选‘工资卡’！";
            }
            WbUser outUser = this.wuMapper.selectOne(new QueryWrapper<WbUser>().eq("USER_NAME", bankAcc.getCode().trim()));
            if (null != outUser) {
                return "选择“对外账户”时，编号不能与内部员工工号重复！";
            }
            if (null != bankAccounts && OPERATE_TYPE_ADD.equals(operateType)) { //对外人员存在
                Map<String, BudgetBankAccount> outMap = bankAccounts.stream().collect(Collectors.toMap(BudgetBankAccount :: getCode, a -> a,(k1,k2)->k1));
                String lowCase = bankAcc.getCode().toLowerCase();
                String upCase = bankAcc.getCode().toUpperCase();
                BudgetBankAccount ba = outMap.get(bankAcc.getCode());
                BudgetBankAccount baLow = outMap.get(lowCase);
                BudgetBankAccount baUp = outMap.get(upCase);
                
                if (null != ba || null != baLow || null != baUp) {
                    return "该编号人员已存在！请更换编号重新录入！";
                }
                /*
                if (null != ba) {
                    if (!ba.getPname().equals(bankAcc.getPname())) {
                        throw new RuntimeException("同一编号的人员名称必须一致！");
                    }
                }   */
            /*  for (BudgetBankAccount budgetBankAccount : bankAccounts) {
                    if ((budgetBankAccount.getAccountname()+budgetBankAccount.getCode()).equals(bankAcc.getAccountname()+bankAcc.getCode())) {
                        throw new RuntimeException("【"+bankAcc.getAccountname()+","+bankAcc.getCode()+"】，该对外账户已存在！");
                    }
                }*/
            }
        }
        return errMsg;
    }
	   
	public Page<BankAccountVO> getBankInfo(Map<String, Object> conditionMap, Integer page, Integer rows){
	    Page<BankAccountVO> pageCond = new Page<>(page, rows);
        List<BankAccountVO> retList = this.bbaMapper.getBankAccountPageInfo(pageCond, conditionMap, JdbcSqlThreadLocal.get());
        for(BankAccountVO vo : retList) {
            WbDept deptInfo = this.wdMapper.selectById(vo.getDeptId());
            if (null != deptInfo) {
                vo.setDeptName(deptInfo.getDeptFullname());
            }
        }
        pageCond.setRecords(retList);
        return pageCond;
	}
	
	public List<BankAccountExcelData> getExcelInfo(Map<String, Object> conditionMap){
	    List<BankAccountExcelData> retList = this.bbaMapper.getBankAccountExcelInfo(conditionMap);
	    Map<String, Map<String, Object>> depts = this.unitMapper.queryAllDeptName();
	    for(BankAccountExcelData excel : retList) {
	        Map<String, Object> deptInfo = depts.get(excel.getDeptId());
	        if (null != deptInfo) {
                excel.setDeptName((String)deptInfo.get("DEPT_FULLNAME"));
            }
	    }
	    return retList;
	}
	
	private String trimCardNO(String cardNO){
        String newCardNO = cardNO;
        if (null != cardNO) {
            if (cardNO.indexOf(" ") > 0) {
                newCardNO = cardNO.replaceAll(" ", "");
            }
        }
        return newCardNO.trim();
    }

	/**
	 * 根据个人银行账号查询银行信息
	 * @param empBankAccountList
	 * @return
	 * @throws Exception 
	 */
	public List<BankInfo> getBankInfoByAccounts(List<String> empBankAccountList) throws Exception {
		String inSql = JdbcTemplateService.getInSql(empBankAccountList, "'");
		return this.bbaMapper.getBankInfoByAccounts(inSql);
	}

	public List<BudgetBankAccount> getByAccounts(Set<String> bankAccounts) {
		if (null == bankAccounts || bankAccounts.isEmpty()) {
		    return new ArrayList<>();
		}
	    QueryWrapper<BudgetBankAccount> wrapper = new QueryWrapper<BudgetBankAccount>();
		wrapper.in("bankaccount", bankAccounts);
		wrapper.eq("stopflag", 0);
		return this.list(wrapper);
	}
	
	public BankAccountVO getBankAccountByAccount(String bankaccount, String accountname) {
	    List<BankAccountVO> voList = this.bbaMapper.getBankAccountByAccount(bankaccount, accountname);
	    if (null == voList || voList.size() == 0) {
	        return null;
	    }else {
	        return voList.get(0);
	    }
	}

    /**
     * 新增导入
     * @param inputStream
     * @param errorList
     * @return
     * @throws Exception
     */
	@Transactional(rollbackFor = Exception.class)
    public int importAdd(InputStream inputStream, List<ImportBankAccountExcelData> errorList) throws Exception {
	    List<ImportBankAccountExcelData> excelList = EasyExcelUtil.getExcelContent(inputStream, ImportBankAccountExcelData.class);
        if (null == excelList || excelList.isEmpty()) {
            ImportBankAccountExcelData excelData = new ImportBankAccountExcelData();
            excelData.setErrMsg("表格解析失败或无有效数据");
            errorList.add(excelData);
            return 0;
        }
        WbUser wbUser = UserThreadLocal.get();
        Set<String> branchCodeSet = excelList.stream().map(ImportBankAccountExcelData::getBranchCode).collect(Collectors.toSet());
        Set<String> allBranchCodeSet = wbBanksMapper.selectList(Wrappers.<WbBanks>lambdaQuery().select(WbBanks::getSubBranchCode).in(WbBanks::getSubBranchCode, branchCodeSet)).stream().map(WbBanks::getSubBranchCode).collect(Collectors.toSet());
        for (ImportBankAccountExcelData excelData : excelList) {
            if (allBranchCodeSet.contains(excelData.getBranchCode())) {
                Integer accountType = "对内账户".equals(excelData.getAccountType()) ? IN_ACCOUNT_TYPE : OUT_ACCOUNT_TYPE;
                boolean wagesflag = "是".equals(excelData.getWagesFlag());
                BudgetBankAccount bankAccount = new BudgetBankAccount();
                bankAccount.setCode(excelData.getCode());
                bankAccount.setPname(excelData.getPname());
                bankAccount.setAccountname(excelData.getAccountName());
                bankAccount.setAccounttype(accountType);
                bankAccount.setBranchcode(excelData.getBranchCode());
                bankAccount.setBankaccount(excelData.getBankAccount());
                bankAccount.setRemark(excelData.getRemark());
                bankAccount.setUpdateTime(new Date());
                bankAccount.setUpdateBy(wbUser.getDisplayName() + "(" + wbUser.getUserName() + ")");
                bankAccount.setWagesflag(wagesflag);
                String errMsg = addUserAccount(bankAccount, wbUser);
                if (StringUtils.isNotBlank(errMsg)) {
                    excelData.setErrMsg(errMsg);
                    errorList.add(excelData);
                }
            } else {
                excelData.setErrMsg("开户行联行号不存在");
                errorList.add(excelData);
            }

        }
        return excelList.size() - errorList.size();
    }

    /**
     * 批量停用
     * @param accountList
     */
    public void batchStop(List<String> accountList) {
        LambdaUpdateWrapper<BudgetBankAccount> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(BudgetBankAccount::getBankaccount, accountList);
        updateWrapper.set(BudgetBankAccount::getStopflag, true);
        super.update(updateWrapper);
    }
}
