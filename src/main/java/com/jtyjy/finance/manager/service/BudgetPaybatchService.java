package com.jtyjy.finance.manager.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.result.KVBean;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.easyexcel.PayVerifyExcelData;
import com.jtyjy.finance.manager.enmus.ExtractPayTemplateEnum;
import com.jtyjy.finance.manager.enmus.PaymoneyStatusEnum;
import com.jtyjy.finance.manager.enmus.PaymoneyTypeEnum;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.BudgetPaybatchLogMapper;
import com.jtyjy.finance.manager.mapper.BudgetPaybatchMapper;
import com.jtyjy.finance.manager.mapper.BudgetPaymoneyMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.utils.DataEncryptionUtil;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.weixin.message.MessageSender;
import com.jtyjy.weixin.message.QywxTextMsg;
import com.jtyjy.weixin.message.TextcardMessage;
import com.jtyjy.weixin.message.component.TextcardDetail;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@JdbcSelector(value = "defaultJdbcTemplateService")
public class BudgetPaybatchService extends DefaultBaseService<BudgetPaybatchMapper, BudgetPaybatch> {

    //付款类型：【0：其它 1：报销 2：提成3：现金 4:项目付款】
    public static final String OTHER = "0";
    public static final String BX = "1";
    public static final String TC = "2";
    public static final String XJ = "3";
    public static final String XM = "4";

    private final TabChangeLogMapper loggerMapper;
    private final BudgetPaymoneyService payMoneyService;
    private final BudgetPaybatchMapper mapper;
    private final BudgetPaybatchLogMapper logMapper;
    private final BudgetPaymoneyMapper payMoneyMapper;
    private final WbUserService userService;
    private final MessageSender messageSender;
    private final BudgetBankAccountService bankAccountService;
    private final BudgetReimbursementorderService orderService;
    private final BudgetExtractpaymentService tcSerive;
    private final BudgetExtractsumService tcSumSerive;
    private final BudgetUnitService unitService;
    private final BudgetLendmoneyService lendmoneyService;
    private final BudgetExtractsumService tcSumService;
    private final BudgetExtractOuterpersonService tcOutPersionService;
    private final BudgetPaymoneyService paymoneyService;    
    private final BudgetReimburmentTimedetailService timeDetailService;
    private final BudgetReimbursementorderDetailService budgetReimbursementorderDetailService;
    private final BudgetReimbursementorderTransService transService;
    private final MessageSender sender;
    @Value("${webfront.url}")
    private String webfront_url;//前端ip地址
    @Override
    public BaseMapper<TabChangeLog> getLoggerMapper() {
        return loggerMapper;
    }

    @Override
    public void setBaseLoggerBean() {
        DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_paybatch"));
    }

    /**
     * 创建付款批次
     *
     * @param payBatchType
     * @param remark
     * @param payids
     * @return
     */
    public String preparePay(String payBatchType, String remark, String payids) {
        //创建付款批次
        List<String> payIds = Arrays.asList(payids.split(","));
        QueryWrapper<BudgetPaymoney> wrapper = new QueryWrapper<BudgetPaymoney>();
        wrapper.eq("paymoneystatus", 1);
        wrapper.in("id", payIds);
        List<BudgetPaymoney> list = this.payMoneyService.list(wrapper);
        if (list == null || list.size() == 0) {
            return "付款失败，未获取到待付款的付款单！";
        }
        if (XJ.equals(payBatchType) && list.size() > 1) {
            return "现金付款每次不能多于一条！";
        }

        /*
          只允许一次性付一种类型的付款单
         */
        int paymoneyTypeCount = list.stream().collect(Collectors.groupingBy(e->{
            if(e.getPaymoneytype() == PaymoneyTypeEnum.EXTRACT_PAY.type){
                return PaymoneyTypeEnum.REIMBURSEMENT_PAY.type;
            }
            return e.getPaymoneytype();
        })).size();
        if(paymoneyTypeCount > 1){
            return "不允许选择多个来源系统数据，请筛选后重新付款！";
        }
        //取出真正的付款单主键，并累加付款金额
        BigDecimal total = BigDecimal.ZERO;
        StringJoiner ids = new StringJoiner(",");
        for (BudgetPaymoney bean : list) {
            total = total.add(bean.getPaymoney());
            ids.add(bean.getId().toString());
        }
        //创建付款批次
        BudgetPaybatch BudgetPaybatch = new BudgetPaybatch();
        BudgetPaybatch.setCreatetime(new Date());
        BudgetPaybatch.setPaybatchtype(Integer.parseInt(payBatchType));
        BudgetPaybatch.setPaymoneyids(ids.toString());
        BudgetPaybatch.setCreator(UserThreadLocal.getEmpNo());
        BudgetPaybatch.setCreatorname(UserThreadLocal.getEmpName());
        BudgetPaybatch.setPaytotalje(total);
        BudgetPaybatch.setPaytotalnum(list.size());
        BudgetPaybatch.setRemark(remark);
        BudgetPaybatch.setPaybatchcode(System.currentTimeMillis() + "");
        BudgetPaybatch.setPayTemplateType(ExtractPayTemplateEnum.OLD.type);
        this.save(BudgetPaybatch);
        Date nowDate = new Date();
        //付款成功添加报销出纳付款时间节点表
        Set<String> bxCodeSet = new TreeSet<>();
        list.forEach(pay -> bxCodeSet.add(null == pay.getPaymoneyobjectcode() ? "" : pay.getPaymoneyobjectcode()));
        for (String bxCode : bxCodeSet) {
            if (bxCode.contains("BX")) {
                this.timeDetailService.createBudgetReimbursentTimeDetail(null, nowDate, bxCode, UserThreadLocal.get().getUserName(), 7);
            }
        }
        //修改付款的状态
        this.jdbcTemplateService.update("update budget_paymoney set paybatchid=?,paymoneystatus=?,paytime=?,verifystatus=0 WHERE id in (" + ids.toString() + ") ", new Object[]{BudgetPaybatch.getId(), PaymoneyStatusEnum.PAYED.getType(), nowDate});
        //借款生效
        this.jdbcTemplateService.update("update budget_lendmoney_new set effectflag=1 WHERE id IN (SELECT paymoneyobjectid FROM budget_paymoney WHERE id in (" + ids.toString() + ")  AND paymoneytype=3) ", null);
        //按照类型发送消息
        this.sendMessageByPayMoneyList(list);
        return null;
    }

    /**
     * 根据付款单主键集，发送付款消息，次方法不影响业务
     *
     * @param list 付款单
     */
    @Transactional(noRollbackFor = RuntimeException.class)
    public void sendMessageByPayMoneyList(List<BudgetPaymoney> list) {
        try {
            if (list == null || list.size() == 0) {
                return;
            }
            //预算单位，员工工号
            Map<String, StringJoiner> unitEmpNoMap = new HashMap<String, StringJoiner>();
            //获取所有银行账户对应的工号
            Set<String> bankAccounts = list.stream().filter(ele -> StringUtils.isNotBlank(ele.getBankaccount())).map(ele -> ele.getBankaccount()).collect(Collectors.toSet());
            List<BudgetBankAccount> bankAccountList = this.bankAccountService.getAccounts(bankAccounts);
            Map<String, BudgetBankAccount> bankAccountMap = new HashMap<String, BudgetBankAccount>();
            bankAccountList.forEach(ele -> bankAccountMap.put(ele.getBankaccount(), ele));
            //付款单主键-报销单缓存
            Map<Long, BudgetReimbursementorder> payIdOrderMap = new HashMap<Long, BudgetReimbursementorder>();
            //付款单主键--借款单工号映射
            Map<Long, BudgetLendmoney> lendMoneyEmpNoMap = new HashMap<Long, BudgetLendmoney>();
            Set<Long> lendMoneyIds = list.stream().filter(ele -> ele.getPaymoneyobjectid() != null).map(ele -> ele.getPaymoneyobjectid()).collect(Collectors.toSet());
            List<BudgetLendmoney> lendMoneyList = this.lendmoneyService.listByIds(lendMoneyIds);
            if (lendMoneyList != null && lendMoneyList.size() > 0) {
                lendMoneyList.forEach(ele -> lendMoneyEmpNoMap.put(ele.getId(), ele));
            }
            //提成详情缓存
            Map<Long, BudgetExtractpaydetail> payIdTcDetailMap = new HashMap<Long, BudgetExtractpaydetail>();
            //提成总额缓存
            Map<String, BudgetExtractsum> tcCodeSumMap = new HashMap<String, BudgetExtractsum>();
            Set<String> codes = list.stream().filter(ele -> ele.getPaymoneytype() == 2).map(ele -> ele.getPaymoneyobjectcode()).collect(Collectors.toSet());
            List<BudgetExtractsum> tcSumList = this.tcSumService.getByCodes(codes);
            if (tcSumList != null && tcSumList.size() > 0) {
                tcSumList.forEach(ele -> tcCodeSumMap.put(ele.getCode(), ele));
            }
            //外部提成接收人缓存
            List<BudgetPaymoney> tcOutterBeanList = new ArrayList<BudgetPaymoney>();
            //通用消息模板
            StringBuilder commonMsgTemplate = new StringBuilder();
            commonMsgTemplate.append("%s 付款单（单据尾号****%s）正在付款，请于24小时后查收。\n")
                    .append("收款账户：%s \n")
                    .append("收款账号：%s \n")
                    .append("收款金额：%s \n");
            String bxMsgTemplate = commonMsgTemplate.toString() +"报销科目：%s \n摘要：%s";
            String documentFormat = "稿费报销单（单据尾号****%s）正在付款\n，付款金额：%s \n";
            String commonBxFormat = "报销单（单号%s）下的付款单（单据尾号****%s）正在付款\n，付款金额：%s \n";
            //报销单--稿费：发送给预算单位的第一个预算管理员，消息：稿费报销单（单据尾号****）正在付款，付款金额：${该报销单每一个划拨单位的划拨总额}
            //报销单-- 非稿费：发送给银行账户对应的工号（外部银行账户发送给预算单位的第一个预算管理员），消息：commonMsgTemplate
            //借款单：发送给借款单对应的工号，消息：commonMsgTemplate
            //项目转账：发送给银行账户对应的工号，消息：commonMsgTemplate
            //提成：内部员工：发送给银行账户对应的工号，消息：commonMsgTemplate
            //提成：外部：如果为内部人员所关联的外部人员，给内部人员发消息 否则不发，消息：commonMsgTemplate
            //确认消息类型
            Set<String> reimcodeSet = new TreeSet<>();
            int messageType = -1;
            for (BudgetPaymoney bean : list) {
                BudgetBankAccount budgetBankAccount = bankAccountMap.get(bean.getBankaccount());
                if(budgetBankAccount == null || budgetBankAccount.getStopflag()){
                    throw new RuntimeException("账号【"+bean.getBankaccount()+"】不存在或者已被停用。");
                }
                messageType = this.ensureMessageType(bean, payIdOrderMap, bankAccountMap, payIdTcDetailMap);
                switch (messageType) {
                    //报销单-稿费
                    case 1:
                        reimcodeSet.add(bean.getPaymoneyobjectcode());
                        break;
                    //报销单-非稿费，内部
                    case 2:
                        List<BudgetReimbursementorderDetail> details = budgetReimbursementorderDetailService.getByBxNum(bean.getPaymoneyobjectcode());
                        String subjects = "没有科目";
                        String remarks = "没有摘要";
                        if(details!=null && details.size()>0){
                            subjects = details.stream().map(BudgetReimbursementorderDetail::getSubjectname).collect(Collectors.joining(","));
                            remarks = details.stream().map(BudgetReimbursementorderDetail::getRemark).collect(Collectors.joining(","));
                        }
                        this.sendMessageByBankAccountInner(bean, bankAccountMap, bxMsgTemplate,
                                "您于" + Constants.FORMAT_10.format(payIdOrderMap.get(bean.getId()).getReimdate()) + "申请的报销单下的",
                                bean.getPaymoneycode().substring(bean.getPaymoneycode().length() - 4),
                                bean.getBankaccountname(),
                                bean.getBankaccount(),
                                bean.getPaymoney().setScale(2, BigDecimal.ROUND_HALF_UP).toString(),
                                subjects,remarks);
                        break;
                    //报销单-非稿费，外部
                    case 3:
                        this.sendCommonBxMessage(bean, unitEmpNoMap, payIdOrderMap.get(bean.getId()), commonBxFormat);
                        break;
                    //借款单：发送给借款单对应的工号
                    case 4:
                        this.sendMessage(commonMsgTemplate.toString(),
                                lendMoneyEmpNoMap.get(Long.parseLong("" + bean.getPaymoneyobjectid())).getEmpno(),
                                "您于" + Constants.FORMAT_10.format(lendMoneyEmpNoMap.get(Long.parseLong("" + bean.getPaymoneyobjectid())).getLenddate()) + "申请的借款单下的",
                                bean.getPaymoneycode().substring(bean.getPaymoneycode().length() - 4),
                                bankAccountMap.get(bean.getBankaccount()).getAccountname(),
                                DataEncryptionUtil.encryptBankAcct(bean.getBankaccount()),
                                bean.getPaymoney().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                        break;
                    //项目转账
                    case 5:
                        this.sendMessageByBankAccountInner(bean, bankAccountMap, commonMsgTemplate.toString(),
                                "您的项目借款",
                                bean.getPaymoneycode().substring(bean.getPaymoneycode().length() - 4),
                                bankAccountMap.get(bean.getBankaccount()).getAccountname(),
                                DataEncryptionUtil.encryptBankAcct(bean.getBankaccount()),
                                bean.getPaymoney().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                        break;
                    //提成--内部员工
                    case 6:
                        this.sendExtractInnerMessage(bean, payIdTcDetailMap, bankAccountMap, tcCodeSumMap, commonMsgTemplate.toString());
                        break;
                    //提成--外部，缓存
                    case 7:
                        tcOutterBeanList.add(bean);
                        break;
                    default:
                        break;
                }
            }

            this.sendMessageByDocument(reimcodeSet, unitEmpNoMap, documentFormat);
            //发送外部提成消息
            this.sendExtractOutterMessage(tcOutterBeanList, payIdTcDetailMap, tcCodeSumMap, commonBxFormat, bankAccountMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public int importVerify(InputStream inputStream, Long batchId, List<PayVerifyExcelData> errorList) {
        List<PayVerifyExcelData> excelList = EasyExcelUtil.getExcelContent(inputStream, PayVerifyExcelData.class);
        List<BudgetPaymoney> paymoneys = this.payMoneyMapper.selectList(new QueryWrapper<BudgetPaymoney>().eq("paybatchid", batchId));
        List<BudgetPaymoney> successList = new ArrayList<>();
        Map<String,BudgetPaymoney> listmap = paymoneys.stream().collect(Collectors.toMap(BudgetPaymoney::getPaymoneycode, a -> a,(k1,k2)->k1));
        for(PayVerifyExcelData excelData : excelList) {
            StringBuilder errMsg = new StringBuilder();
            if (BaseController.validate(PayVerifyExcelData.class, errMsg) > 0) {
                excelData.setErrMsg(errMsg.toString());
                errorList.add(excelData);
                continue;
            }
            BudgetPaymoney budgetPayMoney = listmap.get(excelData.getPaymoneycode());
            if (null == budgetPayMoney) {
                errMsg.append("付款单【" + excelData.getPaymoneycode() + "】未找到；");
                excelData.setErrMsg(errMsg.toString());
                errorList.add(excelData);
                continue;
            }
            if(0!=budgetPayMoney.getVerifystatus()) {
                errMsg.append("付款单号【" + excelData.getPaymoneycode() + "】已被验证；");
            }
            //收款收款人账户
            if(!budgetPayMoney.getBankaccount().equals(excelData.getBankaccount())) {
                errMsg.append("收款人账户【" + excelData.getBankaccount() + "】有误；");
            }
            //收款人名称
            if(!budgetPayMoney.getBankaccountname().equals(excelData.getBankaccountname())) {
                errMsg.append("收款人名称【" + excelData.getBankaccountname() + "】有误；");
            }
            //收方开户支行
            if(!budgetPayMoney.getOpenbank().equals(excelData.getOpenbank())) {
                errMsg.append("收方开户支行【" + excelData.getBankaccountbranchname() + "】有误；");
            }
            try {
                BigDecimal payMoney = new BigDecimal(excelData.getPaymoney());
                if(budgetPayMoney.getPaymoney().compareTo(payMoney) != 0) {
                    throw new RuntimeException("付款金额与原金额不一致");
                }
            }catch(Exception e) {
                errMsg.append("付款金额格式错误或有误；");
            }
            if(!budgetPayMoney.getBankaccountbranchcode().equals(excelData.getBankaccountbranchcode())) {
                errMsg.append("收方电子联行号【").append(excelData.getBankaccountbranchcode()).append("】有误；");
            }
            if(!budgetPayMoney.getBankaccountbranchname().equals(excelData.getBankaccountbranchname())) {
                errMsg.append("收方开户银行类型【").append(excelData.getBankaccountbranchname()).append("】有误；");
            }
            budgetPayMoney.setVerifystatus(-1);
            budgetPayMoney.setVerifyremark(excelData.getVerifyremark());
            if (0 == errMsg.length()) {
                successList.add(budgetPayMoney);
            }else {
                excelData.setErrMsg(errMsg.toString());
                errorList.add(excelData);
            }
        }
        if (!successList.isEmpty()) {
            this.verifyPayMoney(batchId, paymoneys);
        }
        return successList.size();
    }
    
    private void verifyPayMoney(Long batchId,List<BudgetPaymoney> paymoneys) {
        //报销单
        //提成单
        //付款单
        //发消息
        BudgetPaybatch batch = this.getById(batchId);
        String payfailids = batch.getPayfailids();
        if(StringUtils.isEmpty(payfailids)) {
            payfailids = "";
        }
        BudgetPaybatchLog log = JSON.parseObject(JSON.toJSONString(batch), BudgetPaybatchLog.class);
        log.setBatchid(batchId);
        log.setId(null);
        log.setCreatetime(new Date());
        log.setCreator(null == paymoneys.get(0).getVerifyer() ? "" : paymoneys.get(0).getVerifyer());
        log.setCreatorname(null == paymoneys.get(0).getVerifyername() ? "" : paymoneys.get(0).getVerifyername());
        logMapper.insert(log);
        
        //报销单
        Map<String,BudgetReimbursementorder> ordermap = new HashMap<String,BudgetReimbursementorder>();
        //借款
        Map<String,BudgetLendmoney> lendmoneymap = new HashMap<String,BudgetLendmoney>();
        //提成
        Map<String,BudgetExtractsum> tcmap = new HashMap<String,BudgetExtractsum>();
        //预算单位
        Map<Long,BudgetUnit> unitmap = new HashMap<Long,BudgetUnit>();
        //消息模板
        StringBuffer commonMsgTemplate = new StringBuffer();
        commonMsgTemplate.append("%s已经付款，具体到账时间以银行短信通知为准！\n")
                .append("付款单号：%s \n")
                .append("收款人账户：%s \n")
                .append("付款金额：%s \n");
        DecimalFormat df = new DecimalFormat("#0.00");
        
        for(BudgetPaymoney paymoney:paymoneys) {
            this.payMoneyMapper.updateById(paymoney);
            if(!(","+payfailids+",").contains(","+paymoney.getId()+",")) {
                if(StringUtils.isEmpty(payfailids)) {
                    payfailids = paymoney.getId() +"";
                }else {
                    payfailids += ","+paymoney.getId();
                }
            }
            //付款单类型
            Integer paymoneytype = paymoney.getPaymoneytype();
            String paymoneytypeStr = "";
            String toEmpNo = "";
            if((paymoneytype==1 || paymoneytype ==0)) {//报销
                paymoneytypeStr = "报销付款";
                BudgetReimbursementorder order = ordermap.get(paymoney.getPaymoneyobjectcode());
                if(null==order) {
                    order = this.orderService.getOne(new QueryWrapper<BudgetReimbursementorder>().eq("reimcode", paymoney.getPaymoneyobjectcode()));
                    if(null!=order) {
                        ordermap.put(order.getReimcode(), order);
                        BudgetUnit unit = unitmap.get(order.getUnitid());
                        if(null==unit) {
                            unit = this.unitService.getById(order.getUnitid());
                            if(null!=unit) {
                                unitmap.put(order.getUnitid(),unit);
                            }
                        }
                        if(null!=unit && StringUtils.isNotEmpty(unit.getManagers())) {
                            String ss = unit.getManagers().split(",")[0];
                            WbUser wbUser = this.userService.getById(ss);
                            if(null!=wbUser) {
                                toEmpNo = wbUser.getUserName(); 
                            }
                        }
                    }
                }
            }else if(paymoneytype==3 && 1==paymoney.getPaytype()){//借款
                paymoneytypeStr = "借款付款";
                BudgetLendmoney lendmoney = this.lendmoneyService.getById(paymoney.getPaymoneyobjectid());
                lendmoney.setEffectflag(true);
                lendmoneymap.put(lendmoney.getLendmoneycode(), lendmoney);
                toEmpNo = lendmoney.getEmpno();
            }else if(paymoneytype ==2){//提成
                paymoneytypeStr = "提成付款";
                BudgetExtractsum budgetExtractSum = tcmap.get(paymoney.getPaymoneyobjectcode());
                if(null==budgetExtractSum) {
                    budgetExtractSum = this.tcSumSerive.getOne(new QueryWrapper<BudgetExtractsum>().eq("code", paymoney.getPaymoneyobjectcode()));
                    if(null!=budgetExtractSum) {
                        tcmap.put(budgetExtractSum.getCode(), budgetExtractSum);
                        BudgetUnit unit = unitmap.get(Long.valueOf(budgetExtractSum.getDeptid()));
                        if(null==unit) {
                            unit = this.unitService.getById(Long.valueOf(budgetExtractSum.getDeptid()));
                            if(null!=unit) {
                                unitmap.put(Long.valueOf(budgetExtractSum.getDeptid()),unit);
                            }
                        }
                        if(null!=unit && StringUtils.isNotEmpty(unit.getManagers())) {
                            String ss = unit.getManagers().split(",")[0];
                            WbUser wbUser = this.userService.getById(ss);
                            if(null!=wbUser) {
                                toEmpNo = wbUser.getUserName();  
//                                this.sender.sendQywxMsgSyn(new TextcardMessage("20333", 
//                                        null, "付款失败通知", webfront_url+"/main/budgetImplementation/errorPay?token=no&paymoneycode=" + paymoney.getPaymoneycode(), "详情", 
//                                            TextcardDetail.apply(TextcardDetail.GRAY, Constants.FULL_FORMAT.format(new Date())+"<br>", true),
//                                            TextcardDetail.apply(TextcardDetail.GRAY,"付款单号："+paymoney.getPaymoneycode(),false),
//                                            TextcardDetail.apply(TextcardDetail.GRAY,"单据号："+paymoney.getPaymoneyobjectcode(),false),
//                                            TextcardDetail.apply(TextcardDetail.GRAY,"付款单类型："+paymoneytypeStr,false),
//                                            TextcardDetail.apply(TextcardDetail.RED,"失败原因："+paymoney.getVerifyremark(),false)
//                                        ));
                            }
                        }
                    }
                }
            }
            
            if(StringUtils.isNotBlank(toEmpNo)) {
                try {
                    //token=no必传，前端写死了可通过这个判断无需要拦截地址
                    this.sender.sendQywxMsgSyn(new TextcardMessage(toEmpNo, 
                            null, "付款失败通知", webfront_url+"/main/budgetImplementation/errorPay?token=no&paymoneycode=" + paymoney.getPaymoneycode(), "详情", 
                                TextcardDetail.apply(TextcardDetail.GRAY, Constants.FULL_FORMAT.format(new Date())+"<br>", true),
                                TextcardDetail.apply(TextcardDetail.GRAY,"付款单号："+paymoney.getPaymoneycode(),false),
                                TextcardDetail.apply(TextcardDetail.GRAY,"单据号："+paymoney.getPaymoneyobjectcode(),false),
                                TextcardDetail.apply(TextcardDetail.GRAY,"付款单类型："+paymoneytypeStr,false),
                                TextcardDetail.apply(TextcardDetail.RED,"失败原因："+paymoney.getVerifyremark(),false)
                            ));
                }catch(Exception e) {
                    System.out.println("付款失败通知发送失败");
                }
                
            }
        }

        batch.setPayfailids(payfailids);
        batch.setPayfailnum(payfailids.split(",").length);
        this.updateById(batch);
       
    }

    /**
     * 确认消息类型
     * 1：报销单--稿费
     * 2：报销单--非稿费，内部
     * 3：报销单--非稿费，外部
     * 4：借款单
     * 5：项目转账
     * 6：提成--内部
     * 7：提成--外部
     *
     * @param bean
     * @param payIdOrderMap
     * @param bankAccountMap
     * @param payIdTcDetailMap
     * @return
     */
    private int ensureMessageType(BudgetPaymoney bean, Map<Long, BudgetReimbursementorder> payIdOrderMap, Map<String, BudgetBankAccount> bankAccountMap, Map<Long, BudgetExtractpaydetail> payIdTcDetailMap) {
        //付款单类型：1：报销转账付款 2：提成发放付款 3：(日常)借款付款 4：资金调拨付款 5:项目现金付款 6:项目转账付款（借款）
        //是否是报销单
        Integer type = bean.getPaymoneytype();
        if (1 == type) {
            BudgetReimbursementorder order = payIdOrderMap.get(bean.getId());
            if (order == null) {
                order = this.orderService.getByCode(bean.getPaymoneyobjectcode());
                if (order != null) {
                    payIdOrderMap.put(bean.getId(), order);
                    //稿费
                    if (1 == order.getOrderscrtype()) {
                        return 1;
                    }
                    BudgetBankAccount bankAccount = bankAccountMap.get(bean.getBankaccount());
                    if (bankAccount != null) {
                        //非稿费，外部
                        if (2 == bankAccount.getAccounttype()) {
                            return 3;
                        }
                        //非稿费，内部
                        return 2;
                    }else{
                        //银行账号可能会被删除（hr同步）
                        return 2;
                    }
                }
            }
        }
        if (2 == type) {
            BudgetExtractpaydetail detail = payIdTcDetailMap.get(bean.getId());
            if (detail == null) {
                detail = this.tcSerive.getDetail(bean.getPaymoneyobjectid());
            }
            if (detail != null) {
                payIdTcDetailMap.put(bean.getId(), detail);
                //提成--内部
                if (detail.getIscompanyemp()) {
                    return 6;
                }
                //提成--外部
                return 7;
            }
        }
        //借款
        if (3 == type && Constants.PAY_TYPE.TRANSFER.equals(bean.getPaytype())) {
            return 4;
        }
        if (6 == type) {
            return 5;
        }
        return -1;
    }

    /**
     * 发送提成消息：内部员工
     *
     * @param bean
     * @param payIdTcDetailMap
     * @param bankAccountMap
     * @param tcCodeSumMap
     * @param messageFormat
     */
    private void sendExtractInnerMessage(BudgetPaymoney bean, Map<Long, BudgetExtractpaydetail> payIdTcDetailMap, Map<String, BudgetBankAccount> bankAccountMap, Map<String, BudgetExtractsum> tcCodeSumMap, String messageFormat) {
        BudgetExtractpaydetail detail = payIdTcDetailMap.get(bean.getId());
        if (detail != null) {
            BudgetExtractsum extractsum = tcCodeSumMap.get(bean.getPaymoneyobjectcode());
            if (extractsum != null) {
                DecimalFormat df = new DecimalFormat("#0.00");
                this.sendMessage(
                        messageFormat,
                        detail.getEmpno(),
                        "您" + extractsum.getExtractmonth().substring(0, 6) + "的提成",
                        bean.getPaymoneycode().substring(bean.getPaymoneycode().length() - 4),
                        bankAccountMap.get(bean.getBankaccount()).getAccountname(),
                        DataEncryptionUtil.encryptBankAcct(bean.getBankaccount()),
                        bean.getPaymoney().setScale(2, BigDecimal.ROUND_HALF_UP).toString()
                );

            }
        }
    }

    /**
     * 发送提成消息：外部
     *
     * @param list
     * @param payIdTcDetailMap
     * @param bankAccountMap
     * @param tcCodeSumMap
     * @param messageFormat
     * @param bankAccountMap
     */
    private void sendExtractOutterMessage(List<BudgetPaymoney> list, Map<Long, BudgetExtractpaydetail> payIdTcDetailMap, Map<String, BudgetExtractsum> tcCodeSumMap, String messageFormat, Map<String, BudgetBankAccount> bankAccountMap) {
        if (payIdTcDetailMap != null && payIdTcDetailMap.size() > 0) {
            Set<String> idNums = payIdTcDetailMap.values().stream().map(ele -> ele.getIdnumber()).collect(Collectors.toSet());
            List<BudgetExtractOuterperson> outPersionList = this.tcOutPersionService.getByIdNums(idNums);
            Map<String, String> empNoMap = new HashMap<String, String>();
            if (outPersionList != null && outPersionList.size() > 0) {
                outPersionList.forEach(ele -> empNoMap.put(ele.getIdnumber(), ele.getEmpno()));
            }
            BudgetExtractpaydetail detail = null;
            String to = null;
            BudgetExtractsum extractsum = null;
            DecimalFormat df = new DecimalFormat("#0.00");
            for (BudgetPaymoney bean : list) {
                detail = payIdTcDetailMap.get(bean.getId());
                if (detail != null) {
                    to = empNoMap.get(detail.getIdnumber());
                    if (StringUtils.isNotBlank(to)) {
                        extractsum = tcCodeSumMap.get(bean.getPaymoneyobjectcode());
                        if (extractsum != null) {
                            this.sendMessage(messageFormat, to,
                                    "您" + extractsum.getExtractmonth().substring(0, 6) + "的提成",
                                    bean.getPaymoneycode().substring(bean.getPaymoneycode().length() - 4),
                                    bankAccountMap.get(bean.getBankaccount()).getAccountname(),
                                    DataEncryptionUtil.encryptBankAcct(bean.getBankaccount()),
                                    bean.getPaymoney().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                        }
                    }
                }
            }
        }
    }

    /**
     * 普通报销单
     *
     * @param bean
     * @param unitEmpNoMap
     * @param budgetReimbursementorder
     * @param commonBxFormat
     */
    private void sendCommonBxMessage(BudgetPaymoney bean, Map<String, StringJoiner> unitEmpNoMap, BudgetReimbursementorder order, String commonBxFormat) {
        StringJoiner users = unitEmpNoMap.get(order.getUnitid().toString());
        if (users == null) {
            //查询预算单位下的预算管理员
            BudgetUnit unit = this.unitService.getById(order.getUnitid());

            String empNo = "";
            if(unit!=null && StringUtils.isNotBlank(unit.getManagers())){
                WbUser user = userService.getById(unit.getManagers().split(",")[0]);
                empNo = user.getUserName();
            }

            //if (unit != null) {
            //    List<KVBean> unitUserIds = new ArrayList<KVBean>();
            //    unitUserIds.add(new KVBean(unit.getId().toString(), unit.getManagers()));
                //将预算单位主键和预算管理员工号映射
            //    this.mappingUnitAndEmpNo(unitUserIds, unitEmpNoMap);
           // }
            //发送消息
            if(StringUtils.isNotBlank(empNo)){
                DecimalFormat df = new DecimalFormat("#0.00");
                //StringJoiner to = unitEmpNoMap.get(unit.getId().toString());
                this.sendMessage(commonBxFormat, empNo, order.getReimcode(), bean.getPaymoneycode().substring(bean.getPaymoneycode().length() - 4), bean.getPaymoney().setScale(2, BigDecimal.ROUND_HALF_UP).toString());

            }
        }
    }


    /**
     * 报销单：稿费消息
     * 发送给预算单位的第一个预算管理员，消息：稿费报销单（单据尾号****）正在付款，付款金额：${该报销单每一个划拨单位的划拨总额}
     */
    public void sendMessageByDocument(Set<String> reimcodeSet, Map<String, StringJoiner> unitEmpNoMap, String messageFormat) {

        DecimalFormat df = new DecimalFormat("#0.00");
        for (String reimcode : reimcodeSet) {
          //根据报销单号查询预算单位下预算管理员：k:预算单位主键 v:预算管理员主键
            List<KVBean> unitUserIds = this.mapper.getManagerOfBudgetUnitByBxCode(reimcode);
            //将预算单位主键和预算管理员工号映射
            this.mappingUnitAndEmpNo(unitUserIds, unitEmpNoMap);
            //查询每一个预算单位划拨单总额
            List<KVBean> unitMoney = this.mapper.getUnitAllocateMoney(reimcode);
            if (unitMoney != null && unitMoney.size() > 0) {
                //发消息
                String unitId = null;
                StringJoiner to = null;
                for (KVBean kvBean : unitMoney) {
                    unitId = kvBean.getK().toString();
                    to = unitEmpNoMap.get(unitId);
                    if (StringUtils.isNotEmpty(to.toString())) {
                        Double totalAmt = new Double(kvBean.getV().toString());
                        this.sendMessage(messageFormat, to.toString().split(",")[0], reimcode.substring(reimcode.length() - 4), df.format(totalAmt));
                    }
                }
            }
        }
        
    }

    /**
     * 按照银行账户发送消息：内部员工
     *
     * @param pay
     * @param bankAccountMap
     * @param messageFormat
     */
    public void sendMessageByBankAccountInner(BudgetPaymoney pay, Map<String, BudgetBankAccount> bankAccountMap, String messageFormat, String... params) {
        BudgetBankAccount bankAccount = bankAccountMap.get(pay.getBankaccount());
        if (bankAccount != null) {
            this.sendMessage(messageFormat, bankAccount.getCode(), params);
        }else{
            List<BudgetReimbursementorderTrans> list = transService.list(new LambdaQueryWrapper<BudgetReimbursementorderTrans>().eq(BudgetReimbursementorderTrans::getPaymoneyid, pay.getId()));
            if(!list.isEmpty()){
                this.sendMessage(messageFormat, list.get(0).getPayeecode(), params);
            }
        }
    }

    public void sendMessageByBankAccountOutter(BudgetPaymoney pay, Map<String, BudgetBankAccount> bankAccountMap, String messageFormat, String... params) {
        BudgetBankAccount bankAccount = bankAccountMap.get(pay.getBankaccount());
        if (bankAccount != null) {
            this.sendMessage(messageFormat, bankAccount.getCode(), params);
        }
    }

    /**
     * 发送消息
     *
     * @param messageTemplate
     * @param to              消息接收人，多个以"|"分割
     * @param params
     */
    private void sendMessage(String messageFormat, String to, String... params) {
        try {
            if (StringUtils.isNotEmpty(to)) {
                String message = String.format(messageFormat, params);
                this.messageSender.sendQywxMsgSyn(new QywxTextMsg(to, null, null, 0, message, 0));
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
       
    }

    /**
     * 将预算单位主键和预算管理员工号映射：ex 1 = 19283,17474
     *
     * @param unitUserIds
     * @param unitEmpMap
     */
    private void mappingUnitAndEmpNo(List<KVBean> unitUserIds, Map<String, StringJoiner> unitEmpMap) {
        //取出所有主键
        StringJoiner sj = new StringJoiner(",");
        if (unitUserIds != null && unitUserIds.size() > 0) {
            for (KVBean kvBean : unitUserIds) {
                sj.add(kvBean.getV().toString());
            }
            String ids = sj.toString();
            if (StringUtils.isNotEmpty(ids)) {
                List<String> idList = Arrays.asList(ids.split(","));
                List<WbUser> userList = this.userService.selectByIds(idList);
                if (userList != null && userList.size() > 0) {
                    
                    for (KVBean kv : unitUserIds) {
                        if (unitEmpMap.get(kv.getK().toString()) == null) {
                            unitEmpMap.put(kv.getK().toString(), new StringJoiner(","));
                        }
                        for (WbUser wbUser : userList) {
                            if (kv.getV().toString().contains(wbUser.getUserId())) {
                                unitEmpMap.get(kv.getK().toString()).add(wbUser.getUserName());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 付款历史查询
     * @param page
     * @param rows
     * @param paybatchcode
     * @param remark
     * @return
     */
    public Page<BudgetPaybatch> getPayHis(Integer page, Integer rows, String paybatchcode, String remark) {
        Page<BudgetPaybatch> pageCond = new Page<>(page,rows);       
        QueryWrapper<BudgetPaybatch> wrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(paybatchcode)) {
            wrapper.like("paybatchcode", paybatchcode);
        }
        if(StringUtils.isNotBlank(remark)) {
            wrapper.like("remark", remark);
        }
        wrapper.orderByDesc("createtime");
        return this.mapper.selectPage(pageCond, wrapper);
    }
    
    /**
     * 按付款批次号查询付款明细
     * @param page
     * @param rows
     * @param batchId
     * @return
     */
    public Page<BudgetPaymoney> getPayMoneyByBatchId(Integer page, Integer rows, Long batchId) {
        Page<BudgetPaymoney> pageCond = new Page<>(page,rows);       
        QueryWrapper<BudgetPaymoney> wrapper = new QueryWrapper<>();
        wrapper.eq("paybatchid", batchId);
        Page<BudgetPaymoney> resultPage = this.payMoneyMapper.selectPage(pageCond, wrapper);
        for(BudgetPaymoney pm : resultPage.getRecords()) {
            if (null != pm.getPaytime()) {
                pm.setPaytimeStr(Constants.FULL_FORMAT.format(pm.getPaytime()));  
            }
            if (null != pm.getReceivetime()) {
                pm.setReceivetimeStr(Constants.FULL_FORMAT.format(pm.getReceivetime()));
            }
        }
        return resultPage;
    }
}
