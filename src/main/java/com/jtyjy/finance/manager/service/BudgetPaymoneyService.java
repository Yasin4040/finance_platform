package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.local.JdbcSqlThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.controller.reimbursement.ReimbursementController;
import com.jtyjy.finance.manager.easyexcel.PayErrorImportExcelData;
import com.jtyjy.finance.manager.enmus.PaymoneyTypeEnum;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.mapper.response.BankInfo;
import com.jtyjy.finance.manager.trade.DistributedNumber;
import com.jtyjy.weixin.message.MessageSender;
import com.jtyjy.weixin.message.TextcardMessage;
import com.jtyjy.weixin.message.component.TextcardDetail;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("all")
public class BudgetPaymoneyService extends DefaultBaseService<BudgetPaymoneyMapper, BudgetPaymoney> implements ImportBaseInterface{

    private final TabChangeLogMapper loggerMapper;

    @Autowired
    private BudgetPaymoneyMapper mapper;
    
    @Autowired
    private BudgetPaybatchMapper batchMapper;
    
    @Autowired
    private BudgetReimbursementorderMapper orderMapper;
    
    @Autowired
    private BudgetLendmoneyMapper lendMoneyMapper;
    
    @Autowired
    private BudgetExtractsumMapper  extractSumMapper;
    
    @Autowired
    private BudgetUnitMapper unitMapper;
    
    @Autowired
    private WbPersonMapper personMapper;
    
    @Autowired
    private BudgetReimbursementorderCashService cashService;

    @Autowired
    private BudgetReimbursementorderTransService transService;

    @Autowired
    private BudgetBillingUnitAccountService billingUnitAccountService;

    @Autowired
    private BudgetBankAccountService bankAccountService;

    @Autowired
    private BudgetAuthorService authorService;
    
    @Autowired
    private WbBanksService banksService;
    
    @Autowired
    private DistributedNumber distributedNumber;
    
    @Autowired
    private MessageSender sender;
    
    @Value("${webfront.url}")
    private String webfront_url;//前端ip地址
    @Override
    public BaseMapper<TabChangeLog> getLoggerMapper() {
        return loggerMapper;
    }

    @Override
    public void setBaseLoggerBean() {
        DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_paymoney"));
    }

    /**
     * 按照报销单主键查询
     *
     * @param id 报销单主键
     * @return
     */
    public List<BudgetPaymoney> getByReimbursementOrderId(String reimcode) {
        QueryWrapper<BudgetPaymoney> wrapper = new QueryWrapper<BudgetPaymoney>();
        wrapper.eq("paymoneytype", 1);
        wrapper.eq("paymoneyobjectcode", reimcode);
        return this.list(wrapper);
    }

    /**
     * 根据报销单保存付款单
     * 1 每条转账信息
     * 1.1.设置付款单类型为报销单 paymoneytype  1：报销转账付款
     * 1.2.设置付款单付款状态 paymoneystatus   0:等待付款
     * 1.3.设置付款单支付类型为转账 paytype   true:转账
     *
     * @param order
     * @throws Exception
     */
    public void savePayMoneyByReimbursementOrder(BudgetReimbursementorder order) throws Exception {
        if (order == null) {
            return;
        }
        //根据转账信息生成付款单
        this.getPaymoneyListByTrans(order);
        //根据现金信息生成付款单
        this.getPaymoneyListByCash(order);
    }

    /**
     * 根据现金信息生成付款单
     *
     * @param order
     * @return
     */
    private void getPaymoneyListByCash(BudgetReimbursementorder order) {
        List<BudgetReimbursementorderCash> cashList = this.cashService.getByOrderId(order.getId());
        if (cashList != null && cashList.size() > 0) {
            BudgetPaymoney paymoney = null;
            for (BudgetReimbursementorderCash cash : cashList) {
                paymoney = new BudgetPaymoney();
                paymoney.setPaymoneytype(1);
                paymoney.setCreatetime(new Date());
                paymoney.setPaymoneystatus(0);
                paymoney.setPaytype(Constants.PAY_TYPE.CASH);
                paymoney.setPaymoneyobjectid(cash.getId());
                paymoney.setPaymoneyobjectcode(order.getReimcode());
                paymoney.setPaymoney(cash.getCashmoney());
                //付款方
                paymoney.setBunitname(cash.getDraweeunitname());
                //收款方
                paymoney.setBankaccountname(cash.getPayeename() + "(" + cash.getPayeecode() + ")");
                paymoney.setPaymoneycode(this.distributedNumber.getPaymoneyNum());
                this.save(paymoney);
                cash.setPaymoneyid(paymoney.getId());
            }
            this.cashService.updateBatchById(cashList);
        }
    }

    /**
     * 根据转账信息生成付款单
     *
     * @param order
     * @param unitBankInfoMap
     * @param empBankInfoMap
     * @return
     * @throws Exception
     */
    private void getPaymoneyListByTrans(BudgetReimbursementorder order) throws Exception {
        //单位银行信息
        Map<String, BankInfo> unitBankInfoMap = new HashMap<String, BankInfo>();
        Map<String, BankInfo> empBankInfoMap = new HashMap<String, BankInfo>();
        //根据报销单查询转账信息
        List<BudgetReimbursementorderTrans> transList = this.transService.list(new QueryWrapper<BudgetReimbursementorderTrans>().eq("reimbursementid",order.getId()));
        //转账信息
        if (transList != null && transList.size() > 0) {
            //查询单位银行信息
            List<String> bankAccountList = transList.stream().map(BudgetReimbursementorderTrans::getDraweebankaccount).collect(Collectors.toList());
            List<BankInfo> bankInfoList = this.billingUnitAccountService.getBankInfoByAccounts(bankAccountList);
            if (bankInfoList != null && bankInfoList.size() > 0) {
                //账户-银行信息映射
                bankInfoList.forEach(ele -> unitBankInfoMap.put(ele.getBankAccount(), ele));
            }
            List<BankInfo> empBankInfoList = null;
            //稿费报销单
            if (order.getOrderscrtype() == 1) {
                //检查稿费作者的银行账号是否改变：如若改变则使用新的
                List<String> authorCodeList = transList.stream().map(BudgetReimbursementorderTrans::getPayeecode).collect(Collectors.toList());
                empBankInfoList = this.authorService.getBankInfoByAuthorCode(authorCodeList);
                Map<String, BankInfo> codeMap = new HashMap<String, BankInfo>();
                empBankInfoList.forEach(ele -> codeMap.put(ele.getAuthorCode(), ele));
                BankInfo info = null;
                for (BudgetReimbursementorderTrans bean : transList) {
                    info = codeMap.get(bean.getPayeecode());
                    if (info == null) {
                        throw new Exception("稿费作者【" + bean.getPayeename() + "】的收款账户不存在！");
                    }
                    bean.setPayeebankaccount(info.getBankAccount());
                    bean.setPayeebankname(info.getBankName());
                }
            } else {
                //其他报销单
                //查询个人银行信息
                List<String> empBankAccountList = transList.stream().map(BudgetReimbursementorderTrans::getPayeebankaccount).collect(Collectors.toList());
                empBankInfoList = this.bankAccountService.getBankInfoByAccounts(empBankAccountList);
            }
            if (empBankInfoList != null && empBankInfoList.size() > 0) {
                //账户-银行信息映射
                empBankInfoList.forEach(ele -> empBankInfoMap.put(ele.getBankAccount(), ele));
            }
            BankInfo unitBankInfo = null;
            BankInfo empBankInfo = null;
            BudgetPaymoney paymoney = null;
            for (BudgetReimbursementorderTrans tran : transList) {
                //创建付款单
                unitBankInfo = unitBankInfoMap.get(tran.getDraweebankaccount());
                empBankInfo = empBankInfoMap.get(tran.getPayeebankaccount());
                if (unitBankInfo == null) {
                    throw new Exception("付款单位账户【" + tran.getDraweebankaccount() + "】不存在或已停用！");
                }
                if (empBankInfo == null) {
                    throw new Exception("收款账户【" + tran.getPayeebankaccount() + "】不存在或已停用！");
                }
                paymoney = this.createDefaultPaymoney(
                        1, order.getReimcode(), tran.getId(), tran.getTransmoney(), true,
                        unitBankInfo.getAccountName(), unitBankInfo.getBankAccount(), unitBankInfo.getBankCode(), unitBankInfo.getBankName(),
                        empBankInfo.getAccountName(), empBankInfo.getBankAccount(), empBankInfo.getBankCode(), empBankInfo.getBankName(), empBankInfo.getOpenBank(), "报销转账信息自动创建付款单！");
                this.save(paymoney);
                tran.setPaymoneyid(paymoney.getId());
            }
            //更新转账信息的付款单
            this.transService.updateBatchById(transList);
        }
    }

    /**
     * 创建默认的付款单
     *
     * @param paymoneyType           付款单类型 1：报销转账付款
     * @param paymoneyObjectCode     付款对象编号
     * @param paymoneyObjectId       付款对象id
     * @param paymoney               付款金额
     * @param payType                支付类型：false:现金；true:转账
     * @param bunitname              开票单位名称(户名) - 支付方
     * @param bunitbankaccount       开票单位账户（银行账号）-支付方
     * @param bunitaccountbranchcode 开票单位账户 - 银行编号-支付方
     * @param bunitaccountbranchname 开票单位账户 - 银行名称-支付方
     * @param bankaccountname        银行账户 名称(户名)-收款方
     * @param bankaccount            银行账户 -收款方
     * @param bankaccountbranchcode  银行账户 - 银行编号 - 收款方
     * @param bankaccountbranchname  银行账户 - 银行名称-收款方
     * @param openbank               开户行
     * @param remark                 备注
     * @return
     */
    public BudgetPaymoney createDefaultPaymoney(Integer paymoneyType, String paymoneyObjectCode, Long paymoneyObjectId,
                                                BigDecimal paymoney, boolean payType, String bunitname, String bunitbankaccount, String bunitaccountbranchcode,
                                                String bunitaccountbranchname, String bankaccountname, String bankaccount, String bankaccountbranchcode,
                                                String bankaccountbranchname, String openbank, String remark) {
        BudgetPaymoney bean = new BudgetPaymoney();
        //paymoneycode 付款单号
        bean.setPaymoneycode(this.distributedNumber.getPaymoneyNum());
        //paymoneytype 付款单类型：1：报销转账付款
        bean.setPaymoneytype(paymoneyType);
        //paymoneyobjectcode 付款对象编号
        bean.setPaymoneyobjectcode(paymoneyObjectCode);
        //paymoneyobjectid  付款对象id
        bean.setPaymoneyobjectid(paymoneyObjectId);
        //paymoney 付款金额
        bean.setPaymoney(paymoney);
        //paytype 支付类型：false:现金；true:转账
        bean.setPaytype(payType ? Constants.PAY_TYPE.TRANSFER : Constants.PAY_TYPE.CASH);
        //paymoneystatus 付款状态 0:等待付款
        bean.setPaymoneystatus(0);
        //createtime 创建时间
        bean.setCreatetime(new Date());
        //bunitname 开票单位名称(户名) - 支付方
        bean.setBunitname(bunitname);
        //bunitbankaccount 开票单位账户（银行账号）-支付方
        bean.setBunitbankaccount(bunitbankaccount);
        //bunitaccountbranchcode   开票单位账户 - 银行编号-支付方
        bean.setBunitaccountbranchcode(bunitaccountbranchcode);
        //bunitaccountbranchname 开票单位账户 - 银行名称-支付方
        bean.setBunitaccountbranchname(bunitaccountbranchname);
        //bankaccountname 银行账户 名称(户名)-收款方
        bean.setBankaccountname(bankaccountname);
        //bankaccount 银行账户 -收款方
        bean.setBankaccount(bankaccount);
        //bankaccountbranchcode  银行账户 - 银行编号 - 收款方
        bean.setBankaccountbranchcode(bankaccountbranchcode);
        //bankaccountbranchname 银行账户 - 银行名称-收款方
        bean.setBankaccountbranchname(bankaccountbranchname);
        //openbank  开户行(收款)
        bean.setOpenbank(openbank);
        //remark 备注
        bean.setRemark(remark);
        return bean;
    }

    /**
     * 根据保险单号查询付款单
     *
     * @param reimcode
     * @return
     */
    public List<BudgetPaymoney> getByOrderCode(String reimcode) {
        QueryWrapper<BudgetPaymoney> wrapper = new QueryWrapper<BudgetPaymoney>();
        wrapper.eq("paymoneyobjectcode", reimcode);
        return this.list(wrapper);
    }    

    /**
     * 分页查询报销付款单
     * @param id
     * @param page
     * @param rows
     * @param conditionMap 
     * @return 
     * @throws Exception 
     */
    public Page<BudgetPaymoney> payPage(Integer page, Integer rows, Map<String, Object> conditionMap) throws Exception {
        Page<BudgetPaymoney> pageCond = new Page<>(page, rows);
        List<BudgetPaymoney> retList = this.mapper.getBxPaymoneyPageInfo(pageCond, conditionMap, JdbcSqlThreadLocal.get());
        pageCond.setRecords(retList);
        return pageCond;
    }
    
    /**
     * 出纳付款分页查询
     * @param page
     * @param rows
     * @param conditionMap
     * @return
     */
    public Page<BudgetPaymoney> cashPageInfo(Integer page, Integer rows, Map<String, Object> conditionMap){
        Page<BudgetPaymoney> pageCond = new Page<>(page, rows);
        WbUser user = UserThreadLocal.get();
        List<BudgetPaymoney> retList = this.mapper.getCashPayMoneyPageInfo(pageCond, conditionMap, JdbcSqlThreadLocal.get());
        retList.stream().forEach(budgetPaymoney -> {
            if(budgetPaymoney.getPaymoneytype() == 1 || budgetPaymoney.getPaymoneytype() == 2){
                budgetPaymoney.setSourceTypeName("预算系统");
            }else if(Arrays.asList(3).contains(budgetPaymoney.getPaymoneytype())){
                budgetPaymoney.setSourceTypeName("OA系统");
            }
        });
        pageCond.setRecords(retList);
        return pageCond;
    }
    
    /**
     * 查询未付款的报销单号
     * @param page
     * @param rows
     * @param condition
     * @return
     */
    public Page<Map<String, String>> getReimcodePage(Integer page, Integer rows, String condition) {
        Page<Map<String, String>> pageCond = new Page<>(page, rows);
        List<Map<String, String>> retList = this.mapper.getReimcodePage(pageCond, condition, JdbcSqlThreadLocal.get());
        pageCond.setRecords(retList);
        return pageCond;
    }
    
    /**
     * 查询未付款的提成编号
     * @param page
     * @param rows
     * @param condition
     * @return
     */
    public Page<String> getTccodePage(Integer page, Integer rows, String condition) {
        Page<String> pageCond = new Page<>(page, rows);
        List<String> retList = this.mapper.getTccodePage(pageCond, condition, JdbcSqlThreadLocal.get());
        pageCond.setRecords(retList);
        return pageCond;
    }
    
    /**
     * 查询未付款的项目转账付款
     * @param page
     * @param rows
     * @param condition
     * @return
     */
    public Page<Map<String, String>> getXmcodePage(Integer page, Integer rows, String condition) {
        Page<Map<String, String>> pageCond = new Page<>(page, rows);
        List<Map<String, String>> retList = this.mapper.getXmcodePage(pageCond, condition, JdbcSqlThreadLocal.get());
        pageCond.setRecords(retList);
        return pageCond;
    }
    
    /**
     * 根据付款单类型查询未付款信息（可准备付款）
     * @param paymoneytype
     * @param objectcode
     * @param paytype 
     * @return
     */
    public List<BudgetPaymoney> getCanPayMoneyByPmtype(Integer paymoneytype, String objectcode, Integer paytype){
        List<BudgetPaymoney> retList = this.mapper.getCanPayMoneyByPmtype(paymoneytype, objectcode,paytype);
        retList.stream().forEach(budgetPaymoney -> {
            if(budgetPaymoney.getPaymoneytype() == 1 || budgetPaymoney.getPaymoneytype() == 2){
                budgetPaymoney.setSourceTypeName("预算系统");
            }else if(Arrays.asList(3).contains(budgetPaymoney.getPaymoneytype())){
                budgetPaymoney.setSourceTypeName("OA系统");
            }
        });
        return retList;
    }
       
    /**
     * 根据付款方式查询未付款信息（可准备付款）
     * @param paytype 0：现金 1：其他
     * @param objectcode 单据号
     * @param bankaccountname 收款人名称
     * @param ids 
     * @return
     */
    public List<BudgetPaymoney> getCanPayMoneyByFkType(Integer paytype, String objectcode, String bankaccountname, String ids){
        String theIds = "";
        if (Constants.PAY_TYPE.TRANSFER.equals(paytype.intValue())) {//其他方式
            if (StringUtils.isBlank(ids)) {
                theIds = "0";
            }else {
                theIds = ids;
            }
        }
        List<BudgetPaymoney> retList = this.mapper.getCanPayMoneyByFkType(paytype, objectcode, bankaccountname, theIds);
        retList.stream().forEach(budgetPaymoney -> {
            if(budgetPaymoney.getPaymoneytype() == 1 || budgetPaymoney.getPaymoneytype() == 2){
                budgetPaymoney.setSourceTypeName("预算系统");
            }else if(Arrays.asList(3).contains(budgetPaymoney.getPaymoneytype())){
                budgetPaymoney.setSourceTypeName("OA系统");
            }
        });
        return retList;
    }
    
    
  /**
   * 待添加付款列表查询
   * @param page
   * @param rows
   * @param conditionMap
   * @return
   */
  public Page<BudgetPaymoney> otherAddQuery(Integer page, Integer rows, Map<String, Object> conditionMap){
      Page<BudgetPaymoney> pageCond = new Page<>(page, rows);
      List<BudgetPaymoney> retList = this.mapper.otherAddQuery(pageCond, conditionMap, JdbcSqlThreadLocal.get());
      retList.stream().forEach(budgetPaymoney -> {
          if(budgetPaymoney.getPaymoneytype().equals(PaymoneyTypeEnum.REIMBURSEMENT_PAY.type) || budgetPaymoney.getPaymoneytype().equals(PaymoneyTypeEnum.EXTRACT_PAY.type)){
              budgetPaymoney.setSourceTypeName("预算系统");
          }else if(Arrays.asList(3).contains(budgetPaymoney.getPaymoneytype())){
              budgetPaymoney.setSourceTypeName("OA系统");
          }
      });
      pageCond.setRecords(retList);
      return pageCond;
  }
    
    /**
     * 查询正常的付款列表（可设置失败付款）
     * @param page
     * @param rows
     * @param conditionMap
     * @return
     */
    public Page<BudgetPaymoney> getNaturalPayPage(Integer page, Integer rows, Map<String, Object> conditionMap){
        Page<BudgetPaymoney> pageCond = new Page<>(page, rows);
        List<BudgetPaymoney> retList = this.mapper.getNaturalPayPageInfo(pageCond, conditionMap, JdbcSqlThreadLocal.get());
        pageCond.setRecords(retList);
        return pageCond;
    }
    
    /**
     * 设置付款失败
     * @param payid
     * @param verifyremark
     * @param host
     * @return
     */
    public String exceptionPay(Long payid,String verifyremark,String host) {
        BudgetPaymoney paymoney = this.getById(payid);
        Integer paymoneystatus = paymoney.getPaymoneystatus();
        if (3 != paymoneystatus.intValue()) {
            return "该付款单未付过款，不能设置为异常付款。";
        }
        Integer paytype = paymoney.getPaytype();
        if (1 != paytype) {
            return "该付款单为现金付款，不能处理。";
        }
        Integer verify = paymoney.getVerifystatus();
        if (-1 == verify) {
            return "该付款单已经设置为异常付款。";
        }
        paymoney.setVerifytime(new Date());
        paymoney.setVerifyer(UserThreadLocal.getEmpNo());
        paymoney.setVerifyername(UserThreadLocal.get().getDisplayName());
        paymoney.setVerifystatus(-1);
        paymoney.setVerifyremark(verifyremark);
        this.mapper.updateById(paymoney);
        BudgetPaybatch paybatch = this.batchMapper.selectById(paymoney.getPaybatchid());
        String payfailids = paybatch.getPayfailids();
        if(StringUtils.isEmpty(payfailids)) {
            payfailids = "";
        }
        if(!(","+payfailids+",").contains(","+payid+",")) {
            if(StringUtils.isEmpty(payfailids)) {
                payfailids = payid+"";
            }else {
                payfailids += "," + payid;
            }
        }
        paybatch.setPayfailids(payfailids);
        paybatch.setPayfailnum(payfailids.split(",").length);
        this.batchMapper.updateById(paybatch);
        try {
            String toEmpNo = "";
            Integer paymoneytype = paymoney.getPaymoneytype();
            Map<String, Map<String, Object>> userMap = this.unitMapper.queryAllUserName();
            if((paymoneytype.intValue() == 1 || paymoneytype.intValue() == 0)) {//报销
                BudgetReimbursementorder order = this.orderMapper.selectOne(new QueryWrapper<BudgetReimbursementorder>().eq("reimcode", paymoney.getPaymoneyobjectcode()));
                BudgetUnit unit = this.unitMapper.selectById(order.getUnitid());
                if(null != unit && StringUtils.isNotEmpty(unit.getManagers())) {//报销预算员
                    for(String ss:unit.getManagers().split(",")) {
                        Map<String, Object> wbUser = userMap.get(ss);
                        if(null!=wbUser) {
                            if(StringUtils.isEmpty(toEmpNo)) {
                                toEmpNo = wbUser.get("USER_NAME").toString();
                            }else {
                                toEmpNo += "|" + wbUser.get("USER_NAME").toString();
                            }
                        }
                    }
                }
                Map<String, Object> bxUser = userMap.get(order.getReimperonsid());
                toEmpNo += "|" + bxUser.get("USER_NAME").toString();
            }else if(paymoneytype.intValue() == 2){//提成
                BudgetExtractsum budgetExtractSum = this.extractSumMapper.selectOne(new QueryWrapper<BudgetExtractsum>().eq("code", paymoney.getPaymoneyobjectcode()));
                toEmpNo = budgetExtractSum.getCreator();//提成导入人
//                unit = this.unitMapper.selectById(Long.valueOf(budgetExtractSum.getDeptid()));
            }else if(paymoneytype.intValue() == 3 && 1 == paymoney.getPaytype()) {//借款
                BudgetLendmoney lendmoney = this.lendMoneyMapper.selectById(paymoney.getPaymoneyobjectcode());
                toEmpNo = lendmoney.getEmpid();
               
            }
            if(StringUtils.isNotBlank(toEmpNo)) {
                
                String paymoneytypeStr = "";
                if (1 == paymoneytype) {
                    paymoneytypeStr = "报销付款";
                }else if (2 == paymoneytype) {
                    paymoneytypeStr = "提出付款";
                }else if (3 == paymoneytype) {
                    paymoneytypeStr = "借款付款";
                }else if (4 == paymoneytype) {
                    paymoneytypeStr = "资金调拨付款";
                }else if (5 == paymoneytype) {
                    paymoneytypeStr = "项目现金付款";
                }else if (6 == paymoneytype) {
                    paymoneytypeStr = "项目转账付款";
                }
                try {
                    //token=no必传，前端写死了可通过这个判断无需要拦截地址
                    this.sender.sendQywxMsgSyn(new TextcardMessage(toEmpNo, 
                            null, "付款失败通知", webfront_url+"/main/budgetImplementation/errorPay?token=no&paymoneycode=" + paymoney.getPaymoneycode(), "详情", 
                                TextcardDetail.apply(TextcardDetail.GRAY, Constants.FULL_FORMAT.format(new Date())+"<br>", true),
                                TextcardDetail.apply(TextcardDetail.GRAY,"付款单号："+paymoney.getPaymoneycode(),false),
                                TextcardDetail.apply(TextcardDetail.GRAY,"单据号："+paymoney.getPaymoneyobjectcode(),false),
                                TextcardDetail.apply(TextcardDetail.GRAY,"付款单类型："+paymoneytypeStr,false),
                                TextcardDetail.apply(TextcardDetail.RED,"失败原因："+verifyremark,false)
                            ));
                }catch(Exception e) {
                    System.out.println("付款失败通知发送失败");
                }
                
                //sendTextcard("付款失败通知", "有失败付款，请注意注意查看。", host+"/m?xwl=qb/servicemodule/budget/budgetenforcement/reimbursement/reimbursementapplication/exceptionpay", "详情", tt);
            }
            return "";
        }catch(Exception e) {
            return e.toString();
        }
    }
    
    /**
     * 还原异常付款
     * @param payid
     */
    public String reexceptionPay(Long payid) {
        BudgetPaymoney paymoney = this.getById(payid);
        if (null == paymoney) {
            return "未找到此付款单";
        }
        Integer paymoneystatus = paymoney.getPaymoneystatus();
        if (3 !=paymoneystatus.intValue()) {
            return "该付款单未付过款。";
        }
        Integer paytype = paymoney.getPaytype();
        if (1 != paytype) {
            return "该付款单为现金付款，不能处理。";
        }
        Integer verify = paymoney.getVerifystatus();
        if (-1 != verify) {
            return "该付款单未设置为异常付款。";
        }
        paymoney.setVerifystatus(0);
        BudgetPaybatch paybatch = this.batchMapper.selectById(paymoney.getPaybatchid());
        String payfailids = paybatch.getPayfailids();
        if (StringUtils.isBlank(payfailids)) {
            payfailids = "";
        }
        Set<String> ids = new HashSet<String>();
        for(String payfailid:payfailids.split(",")) {
            ids.add(payfailid);
        }
        ids.remove(payid+"");
        payfailids = "";
        paybatch.setPayfailnum(ids.size());
        for(String id:ids) {
            if (StringUtils.isEmpty(payfailids)) {
                payfailids = id;
            }else {
                payfailids += "," + id;
            }
        }
        paybatch.setPayfailids(payfailids);
        this.batchMapper.updateById(paybatch);
        this.mapper.updateById(paymoney);
        return "";
    }
    
    public void updateExceptionpay(PayErrorImportExcelData excelData) {
        
        BudgetPaymoney payMoney = new BudgetPaymoney(excelData);
        //通过付款单号 获取付款单
        BudgetPaymoney oldpayMoney = this.getOne(new QueryWrapper<BudgetPaymoney>().eq("paymoneycode", payMoney.getPaymoneycode()));       
        Integer paymoneytype = oldpayMoney.getPaymoneytype();
        //银行账户 名称(户名)
        String oldbankaccountname = oldpayMoney.getBankaccountname();
        //银行账户 
        String oldbankaccount = oldpayMoney.getBankaccount();
        
        //银行账户 名称(户名)
        String newbankaccountname = payMoney.getBankaccountname();
        //银行账户 
        String newbankaccount = payMoney.getBankaccount();
        //银行账户 - 银行编号
        String newbankaccountbranchcode =  payMoney.getBankaccountbranchcode();
        //银行账户 - 银行名称
        String newbankaccountbranchname = payMoney.getBankaccountbranchname();
        //开户行
        String newopenbank = payMoney.getOpenbank();


        //wb_banks
        //通过支行号获取支行
        WbBanks bank = this.banksService.getOne(new QueryWrapper<WbBanks>().eq("sub_branch_code", newbankaccountbranchcode));
        Boolean accountflag = true;
        if(2==paymoneytype) {//提成
            accountflag = true;
            
        }else if(3==paymoneytype) {//付款
            accountflag = true;
        }else if(0==paymoneytype || 1==paymoneytype) {//报销
            //报销单号
            String paymoneyobjectcode = oldpayMoney.getPaymoneyobjectcode();
            //通过报销单号获取报销单
            //budget_reimbursementorder
            BudgetReimbursementorder _order = this.orderMapper.selectOne(new QueryWrapper<BudgetReimbursementorder>().eq("reimcode", paymoneyobjectcode));
            //稿费
            if(1==_order.getOrderscrtype()) {
                accountflag = false;
            }else {
                accountflag = true;
            }
        }
        //银行账户
        if(true == accountflag) {
            BudgetBankAccount newBudgetBankAccount = this.bankAccountService.getOne(new QueryWrapper<BudgetBankAccount>().eq("bankaccount", newbankaccount));
            Optional.ofNullable(newBudgetBankAccount).orElseThrow(()->new RuntimeException("请先在财务系统平台添加账号【"+newbankaccount+"】"));
            //BudgetBankAccount oldBudgetBankAccount = this.bankAccountService.getOne(new QueryWrapper<BudgetBankAccount>().eq("bankaccount", oldbankaccount));
            //修改原来的银行账户
           // if(null!=oldBudgetBankAccount && null==newBudgetBankAccount) {
           //     oldBudgetBankAccount.setAccountname(newbankaccountname);
            //    oldBudgetBankAccount.setBankaccount(newbankaccount);
            //    oldBudgetBankAccount.setBranchcode(newbankaccountbranchcode);
            //    this.bankAccountService.updateById(oldBudgetBankAccount);
            //}
        }
        //稿费作者
        else {
            BudgetAuthor newBudgetAuthor = this.authorService.getOne(new QueryWrapper<BudgetAuthor>().eq("author", newbankaccountname).eq("bankaccount", newbankaccount));

            BudgetAuthor oldBudgetAuthor = this.authorService.getOne(new QueryWrapper<BudgetAuthor>().eq("author", oldbankaccountname).eq("bankaccount", oldbankaccount));
            //修改原来的稿费作者
            if(null!=oldBudgetAuthor && null==newBudgetAuthor) {
                oldBudgetAuthor.setAuthor(newbankaccountname);
                oldBudgetAuthor.setBankaccount(newbankaccount);
                oldBudgetAuthor.setBranchcode(newbankaccountbranchcode);
                this.authorService.updateById(oldBudgetAuthor);
            }
        }

        oldpayMoney.setBankaccountname(newbankaccountname);
        oldpayMoney.setBankaccount(newbankaccount);
        oldpayMoney.setBunitaccountbranchcode(newbankaccountbranchcode);
        oldpayMoney.setBunitaccountbranchname(newbankaccountbranchname);
        oldpayMoney.setOpenbank(newopenbank);
        oldpayMoney.setVerifystatus(1);
        oldpayMoney.setPaymoneystatus(1);
        this.updateById(oldpayMoney);
    }

    private void validatePayMoney(Map<Integer, String> data, StringBuilder errMsg){
        PayErrorImportExcelData excelData = new PayErrorImportExcelData(data);
        int errNum = BaseController.validate(excelData, errMsg);
        if (errNum > 0) {
            throw new RuntimeException(errMsg.toString());
        }
        //通过付款单号 获取付款单
        BudgetPaymoney oldpayMoney = this.getOne(new QueryWrapper<BudgetPaymoney>().eq("paymoneycode", excelData.getPayMoneyCode()));
        if(null==oldpayMoney) {
            throw new RuntimeException("未找到付款单【"+excelData.getPayMoneyCode()+"】；");
        }
        if(3!=oldpayMoney.getPaymoneystatus() && -1!=oldpayMoney.getVerifystatus()) {
            errMsg.append("付款单【"+excelData.getPayMoneyCode()+"】不是失败付款单；。");
        }
        //paymoneyobjectcode
        if(!oldpayMoney.getPaymoneyobjectcode().equals(excelData.getPayObjectCode())) {
            errMsg.append("付款单号【"+excelData.getPayMoneyCode()+"】与单据号【"+excelData.getPayObjectCode()+"】不匹配；");
        }
        try {
            BigDecimal payMoney = new BigDecimal(excelData.getPayMoney());
            if(oldpayMoney.getPaymoney().compareTo(payMoney) != 0) {
                throw new RuntimeException("付款金额与原金额不一致；");
            }
        }catch (Exception e){
            errMsg.append("付款金额格式错误或与原金额不一致；");
        }
        Integer paymoneytype = oldpayMoney.getPaymoneytype();
        //户名
        String oldbankaccountname = oldpayMoney.getBankaccountname();
        //账户 
        String oldbankaccount = oldpayMoney.getBankaccount();
        //账户电子联行号
        String oldbankaccountbranchcode =  oldpayMoney.getBankaccountbranchcode();
        //银行名称
        String oldbankaccountbranchname = oldpayMoney.getBankaccountbranchname();
        //开户支行
        String oldopenbank = oldpayMoney.getOpenbank();
        
        //户名
        String newbankaccountname = excelData.getBankAccountName();
        //账户 
        String newbankaccount = excelData.getBankAccount();
        //账户电子联行号
        String newbankaccountbranchcode =  excelData.getBankBranchCode();
        //银行名称
        String newbankaccountbranchname = excelData.getOpenBankType();
        //开户支行
        String newopenbank = excelData.getBankBranchName();
        
        if(newbankaccountname.equals(oldbankaccountname)
                && newbankaccount.equals(oldbankaccount)
                && newbankaccountbranchcode.equals(oldbankaccountbranchcode)
                && newbankaccountbranchname.equals(oldbankaccountbranchname)
                && newopenbank.equals(oldopenbank)
                ) {
            errMsg.append("内容无变化，请修改相应的信息；");
        }
        /**
         * 户名 和 账户只能修改其中一个（防止 恶意 修改 导致钱付给别人）
         **/
        if(!newbankaccountname.equals(oldbankaccountname) && !newbankaccount.equals(newbankaccount)) {
            errMsg.append("户名 和 账户只能修改其中一个信息；");
        }
        //wb_banks
        //通过支行号获取支行
        WbBanks bank = this.banksService.getOne(new QueryWrapper<WbBanks>().eq("sub_branch_code", newbankaccountbranchcode));
        if(null==bank) {
            //电子联行号
            errMsg.append("未找到电子联行号【"+newbankaccountbranchcode+"】对应的开户行；");
        }else {
            String sub_branch_name = bank.getSubBranchName();
            String bank_name = bank.getBankName();
            if(!sub_branch_name.equals(newopenbank)
                    && !("中国"+sub_branch_name).equals(newopenbank)
                        && !(sub_branch_name).equals("中国"+newopenbank)
                    ) {//兼容带或不带“中国”字样的银行名称
                errMsg.append("收方开户支行【"+newopenbank+"】与电子联行号【"+newbankaccountbranchcode+"】不匹配；");
            }
            if (!bank_name.equals(newbankaccountbranchname)
                    && !("中国"+bank_name).equals(newbankaccountbranchname)
                        && !(sub_branch_name).equals("中国"+newbankaccountbranchname)) {
                errMsg.append("收方开户银行类型【"+newopenbank+"】与电子联行号【"+newbankaccountbranchcode+"】不匹配；");
            }
        }
        
        Boolean accountflag = true;
        if(2==paymoneytype) {//提成
            accountflag = true;
            
        }else if(3==paymoneytype) {//付款
            accountflag = true;
        }else if(0==paymoneytype || 1==paymoneytype) {//报销
            //报销单号
            String paymoneyobjectcode = oldpayMoney.getPaymoneyobjectcode();
            //通过报销单号获取报销单
            //budget_reimbursementorder
            BudgetReimbursementorder _order = this.orderMapper.selectOne(new QueryWrapper<BudgetReimbursementorder>().eq("reimcode", paymoneyobjectcode));
            //稿费
            if(1==_order.getOrderscrtype()) {
                accountflag = false;
            }else {
                accountflag = true;
            }
        }
        //银行账户
        if(true == accountflag) {
            BudgetBankAccount newBudgetBankAccount = this.bankAccountService.getOne(new QueryWrapper<BudgetBankAccount>().eq("accountname", newbankaccountname).eq("bankaccount", newbankaccount));
            if(null!=newBudgetBankAccount && !newBudgetBankAccount.getBranchcode().equals(newbankaccountbranchcode)) {
                errMsg.append("银行账户 【"+newbankaccount+"】，户名【"+newbankaccountname+"】与系统信息不匹配；");
            }
        }
        //稿费作者
        else {
            BudgetAuthor newBudgetAuthor = this.authorService.getOne(new QueryWrapper<BudgetAuthor>().eq("author", newbankaccountname).eq("bankaccount", newbankaccount));
            if(null!=newBudgetAuthor && !newBudgetAuthor.getBranchcode().equals(newbankaccountbranchcode)) {
                errMsg.append("稿费作者 【"+newbankaccount+"】，户名【"+newbankaccountname+"】与系统信息不匹配；");
            }
        }
        if (errMsg.length() != 0) {
            throw new RuntimeException(errMsg.toString());
        }
    }
    
    @Override
    public Object validate(Integer row, Map<Integer, String> data, String importType,Object head, Object... params) {
        
        if(ReimbursementController.PEIMPORT.equals(importType)) {
            if(row>=2) {
                //校验明细数据
                StringBuilder errMsg = new StringBuilder();
                validatePayMoney(data, errMsg);
            }
        }
        return head;
    }

    @Override
    public void saveData(Map<Integer, Map<Integer, String>> successMap, String importType,
            Map<Integer, Map<Integer, String>> errorMap, List<String> headErrorMsg,Object head,List<Object> details, Object... params) {
        // TODO Auto-generated method stub
        List<Map<Integer, Map<Integer, String>>> errorDatas = new ArrayList<>();
        BudgetExtractsum extractsum = null;
        
        List<Integer> errorKeyList = new ArrayList<>();
        for (Entry<Integer, Map<Integer, String>> entry : successMap.entrySet()) {
            if (1 != entry.getKey()) {
                //排除表头
                Map<Integer, String> detailMap = entry.getValue();
                if (null != detailMap) {
                    PayErrorImportExcelData excelData = new PayErrorImportExcelData(detailMap);
                    try {
                        //修改付款失败
                        updateExceptionpay(excelData);
                    }catch(Exception e) {
                        e.printStackTrace();            
                        detailMap.put(detailMap.size(), e.getMessage());
                        errorMap.put(entry.getKey(), detailMap);
                        errorKeyList.add(entry.getKey());                    
                    }
                }
            }
        }
    }
}
