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

    //??????????????????0????????? 1????????? 2?????????3????????? 4:???????????????
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
    private String webfront_url;//??????ip??????
    @Override
    public BaseMapper<TabChangeLog> getLoggerMapper() {
        return loggerMapper;
    }

    @Override
    public void setBaseLoggerBean() {
        DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_paybatch"));
    }

    /**
     * ??????????????????
     *
     * @param payBatchType
     * @param remark
     * @param payids
     * @return
     */
    public String preparePay(String payBatchType, String remark, String payids) {
        //??????????????????
        List<String> payIds = Arrays.asList(payids.split(","));
        QueryWrapper<BudgetPaymoney> wrapper = new QueryWrapper<BudgetPaymoney>();
        wrapper.eq("paymoneystatus", 1);
        wrapper.in("id", payIds);
        List<BudgetPaymoney> list = this.payMoneyService.list(wrapper);
        if (list == null || list.size() == 0) {
            return "???????????????????????????????????????????????????";
        }
        if (XJ.equals(payBatchType) && list.size() > 1) {
            return "???????????????????????????????????????";
        }

        /*
          ?????????????????????????????????????????????
         */
        int paymoneyTypeCount = list.stream().collect(Collectors.groupingBy(e->{
            if(e.getPaymoneytype() == PaymoneyTypeEnum.EXTRACT_PAY.type){
                return PaymoneyTypeEnum.REIMBURSEMENT_PAY.type;
            }
            return e.getPaymoneytype();
        })).size();
        if(paymoneyTypeCount > 1){
            return "?????????????????????????????????????????????????????????????????????";
        }
        //??????????????????????????????????????????????????????
        BigDecimal total = BigDecimal.ZERO;
        StringJoiner ids = new StringJoiner(",");
        for (BudgetPaymoney bean : list) {
            total = total.add(bean.getPaymoney());
            ids.add(bean.getId().toString());
        }
        //??????????????????
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
        //???????????????????????????????????????????????????
        Set<String> bxCodeSet = new TreeSet<>();
        list.forEach(pay -> bxCodeSet.add(null == pay.getPaymoneyobjectcode() ? "" : pay.getPaymoneyobjectcode()));
        for (String bxCode : bxCodeSet) {
            if (bxCode.contains("BX")) {
                this.timeDetailService.createBudgetReimbursentTimeDetail(null, nowDate, bxCode, UserThreadLocal.get().getUserName(), 7);
            }
        }
        //?????????????????????
        this.jdbcTemplateService.update("update budget_paymoney set paybatchid=?,paymoneystatus=?,paytime=?,verifystatus=0 WHERE id in (" + ids.toString() + ") ", new Object[]{BudgetPaybatch.getId(), PaymoneyStatusEnum.PAYED.getType(), nowDate});
        //????????????
        this.jdbcTemplateService.update("update budget_lendmoney_new set effectflag=1 WHERE id IN (SELECT paymoneyobjectid FROM budget_paymoney WHERE id in (" + ids.toString() + ")  AND paymoneytype=3) ", null);
        //????????????????????????
        this.sendMessageByPayMoneyList(list);
        return null;
    }

    /**
     * ????????????????????????????????????????????????????????????????????????
     *
     * @param list ?????????
     */
    @Transactional(noRollbackFor = RuntimeException.class)
    public void sendMessageByPayMoneyList(List<BudgetPaymoney> list) {
        try {
            if (list == null || list.size() == 0) {
                return;
            }
            //???????????????????????????
            Map<String, StringJoiner> unitEmpNoMap = new HashMap<String, StringJoiner>();
            //???????????????????????????????????????
            Set<String> bankAccounts = list.stream().filter(ele -> StringUtils.isNotBlank(ele.getBankaccount())).map(ele -> ele.getBankaccount()).collect(Collectors.toSet());
            List<BudgetBankAccount> bankAccountList = this.bankAccountService.getAccounts(bankAccounts);
            Map<String, BudgetBankAccount> bankAccountMap = new HashMap<String, BudgetBankAccount>();
            bankAccountList.forEach(ele -> bankAccountMap.put(ele.getBankaccount(), ele));
            //???????????????-???????????????
            Map<Long, BudgetReimbursementorder> payIdOrderMap = new HashMap<Long, BudgetReimbursementorder>();
            //???????????????--?????????????????????
            Map<Long, BudgetLendmoney> lendMoneyEmpNoMap = new HashMap<Long, BudgetLendmoney>();
            Set<Long> lendMoneyIds = list.stream().filter(ele -> ele.getPaymoneyobjectid() != null).map(ele -> ele.getPaymoneyobjectid()).collect(Collectors.toSet());
            List<BudgetLendmoney> lendMoneyList = this.lendmoneyService.listByIds(lendMoneyIds);
            if (lendMoneyList != null && lendMoneyList.size() > 0) {
                lendMoneyList.forEach(ele -> lendMoneyEmpNoMap.put(ele.getId(), ele));
            }
            //??????????????????
            Map<Long, BudgetExtractpaydetail> payIdTcDetailMap = new HashMap<Long, BudgetExtractpaydetail>();
            //??????????????????
            Map<String, BudgetExtractsum> tcCodeSumMap = new HashMap<String, BudgetExtractsum>();
            Set<String> codes = list.stream().filter(ele -> ele.getPaymoneytype() == 2).map(ele -> ele.getPaymoneyobjectcode()).collect(Collectors.toSet());
            List<BudgetExtractsum> tcSumList = this.tcSumService.getByCodes(codes);
            if (tcSumList != null && tcSumList.size() > 0) {
                tcSumList.forEach(ele -> tcCodeSumMap.put(ele.getCode(), ele));
            }
            //???????????????????????????
            List<BudgetPaymoney> tcOutterBeanList = new ArrayList<BudgetPaymoney>();
            //??????????????????
            StringBuilder commonMsgTemplate = new StringBuilder();
            commonMsgTemplate.append("%s ????????????????????????****%s????????????????????????24??????????????????\n")
                    .append("???????????????%s \n")
                    .append("???????????????%s \n")
                    .append("???????????????%s \n");
            String bxMsgTemplate = commonMsgTemplate.toString() +"???????????????%s \n?????????%s";
            String documentFormat = "??????????????????????????????****%s???????????????\n??????????????????%s \n";
            String commonBxFormat = "??????????????????%s?????????????????????????????????****%s???????????????\n??????????????????%s \n";
            //?????????--???????????????????????????????????????????????????????????????????????????????????????????????????****?????????????????????????????????${????????????????????????????????????????????????}
            //?????????-- ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????commonMsgTemplate
            //?????????????????????????????????????????????????????????commonMsgTemplate
            //???????????????????????????????????????????????????????????????commonMsgTemplate
            //????????????????????????????????????????????????????????????????????????commonMsgTemplate
            //?????????????????????????????????????????????????????????????????????????????????????????? ????????????????????????commonMsgTemplate
            //??????????????????
            Set<String> reimcodeSet = new TreeSet<>();
            int messageType = -1;
            for (BudgetPaymoney bean : list) {
                BudgetBankAccount budgetBankAccount = bankAccountMap.get(bean.getBankaccount());
                if(budgetBankAccount == null || budgetBankAccount.getStopflag()){
                    throw new RuntimeException("?????????"+bean.getBankaccount()+"?????????????????????????????????");
                }
                messageType = this.ensureMessageType(bean, payIdOrderMap, bankAccountMap, payIdTcDetailMap);
                switch (messageType) {
                    //?????????-??????
                    case 1:
                        reimcodeSet.add(bean.getPaymoneyobjectcode());
                        break;
                    //?????????-??????????????????
                    case 2:
                        List<BudgetReimbursementorderDetail> details = budgetReimbursementorderDetailService.getByBxNum(bean.getPaymoneyobjectcode());
                        String subjects = "????????????";
                        String remarks = "????????????";
                        if(details!=null && details.size()>0){
                            subjects = details.stream().map(BudgetReimbursementorderDetail::getSubjectname).collect(Collectors.joining(","));
                            remarks = details.stream().map(BudgetReimbursementorderDetail::getRemark).collect(Collectors.joining(","));
                        }
                        this.sendMessageByBankAccountInner(bean, bankAccountMap, bxMsgTemplate,
                                "??????" + Constants.FORMAT_10.format(payIdOrderMap.get(bean.getId()).getReimdate()) + "????????????????????????",
                                bean.getPaymoneycode().substring(bean.getPaymoneycode().length() - 4),
                                bean.getBankaccountname(),
                                bean.getBankaccount(),
                                bean.getPaymoney().setScale(2, BigDecimal.ROUND_HALF_UP).toString(),
                                subjects,remarks);
                        break;
                    //?????????-??????????????????
                    case 3:
                        this.sendCommonBxMessage(bean, unitEmpNoMap, payIdOrderMap.get(bean.getId()), commonBxFormat);
                        break;
                    //?????????????????????????????????????????????
                    case 4:
                        this.sendMessage(commonMsgTemplate.toString(),
                                lendMoneyEmpNoMap.get(Long.parseLong("" + bean.getPaymoneyobjectid())).getEmpno(),
                                "??????" + Constants.FORMAT_10.format(lendMoneyEmpNoMap.get(Long.parseLong("" + bean.getPaymoneyobjectid())).getLenddate()) + "????????????????????????",
                                bean.getPaymoneycode().substring(bean.getPaymoneycode().length() - 4),
                                bankAccountMap.get(bean.getBankaccount()).getAccountname(),
                                DataEncryptionUtil.encryptBankAcct(bean.getBankaccount()),
                                bean.getPaymoney().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                        break;
                    //????????????
                    case 5:
                        this.sendMessageByBankAccountInner(bean, bankAccountMap, commonMsgTemplate.toString(),
                                "??????????????????",
                                bean.getPaymoneycode().substring(bean.getPaymoneycode().length() - 4),
                                bankAccountMap.get(bean.getBankaccount()).getAccountname(),
                                DataEncryptionUtil.encryptBankAcct(bean.getBankaccount()),
                                bean.getPaymoney().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                        break;
                    //??????--????????????
                    case 6:
                        this.sendExtractInnerMessage(bean, payIdTcDetailMap, bankAccountMap, tcCodeSumMap, commonMsgTemplate.toString());
                        break;
                    //??????--???????????????
                    case 7:
                        tcOutterBeanList.add(bean);
                        break;
                    default:
                        break;
                }
            }

            this.sendMessageByDocument(reimcodeSet, unitEmpNoMap, documentFormat);
            //????????????????????????
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
                errMsg.append("????????????" + excelData.getPaymoneycode() + "???????????????");
                excelData.setErrMsg(errMsg.toString());
                errorList.add(excelData);
                continue;
            }
            if(0!=budgetPayMoney.getVerifystatus()) {
                errMsg.append("???????????????" + excelData.getPaymoneycode() + "??????????????????");
            }
            //?????????????????????
            if(!budgetPayMoney.getBankaccount().equals(excelData.getBankaccount())) {
                errMsg.append("??????????????????" + excelData.getBankaccount() + "????????????");
            }
            //???????????????
            if(!budgetPayMoney.getBankaccountname().equals(excelData.getBankaccountname())) {
                errMsg.append("??????????????????" + excelData.getBankaccountname() + "????????????");
            }
            //??????????????????
            if(!budgetPayMoney.getOpenbank().equals(excelData.getOpenbank())) {
                errMsg.append("?????????????????????" + excelData.getBankaccountbranchname() + "????????????");
            }
            try {
                BigDecimal payMoney = new BigDecimal(excelData.getPaymoney());
                if(budgetPayMoney.getPaymoney().compareTo(payMoney) != 0) {
                    throw new RuntimeException("?????????????????????????????????");
                }
            }catch(Exception e) {
                errMsg.append("????????????????????????????????????");
            }
            if(!budgetPayMoney.getBankaccountbranchcode().equals(excelData.getBankaccountbranchcode())) {
                errMsg.append("????????????????????????").append(excelData.getBankaccountbranchcode()).append("????????????");
            }
            if(!budgetPayMoney.getBankaccountbranchname().equals(excelData.getBankaccountbranchname())) {
                errMsg.append("???????????????????????????").append(excelData.getBankaccountbranchname()).append("????????????");
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
        //?????????
        //?????????
        //?????????
        //?????????
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
        
        //?????????
        Map<String,BudgetReimbursementorder> ordermap = new HashMap<String,BudgetReimbursementorder>();
        //??????
        Map<String,BudgetLendmoney> lendmoneymap = new HashMap<String,BudgetLendmoney>();
        //??????
        Map<String,BudgetExtractsum> tcmap = new HashMap<String,BudgetExtractsum>();
        //????????????
        Map<Long,BudgetUnit> unitmap = new HashMap<Long,BudgetUnit>();
        //????????????
        StringBuffer commonMsgTemplate = new StringBuffer();
        commonMsgTemplate.append("%s???????????????????????????????????????????????????????????????\n")
                .append("???????????????%s \n")
                .append("??????????????????%s \n")
                .append("???????????????%s \n");
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
            //???????????????
            Integer paymoneytype = paymoney.getPaymoneytype();
            String paymoneytypeStr = "";
            String toEmpNo = "";
            if((paymoneytype==1 || paymoneytype ==0)) {//??????
                paymoneytypeStr = "????????????";
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
            }else if(paymoneytype==3 && 1==paymoney.getPaytype()){//??????
                paymoneytypeStr = "????????????";
                BudgetLendmoney lendmoney = this.lendmoneyService.getById(paymoney.getPaymoneyobjectid());
                lendmoney.setEffectflag(true);
                lendmoneymap.put(lendmoney.getLendmoneycode(), lendmoney);
                toEmpNo = lendmoney.getEmpno();
            }else if(paymoneytype ==2){//??????
                paymoneytypeStr = "????????????";
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
//                                        null, "??????????????????", webfront_url+"/main/budgetImplementation/errorPay?token=no&paymoneycode=" + paymoney.getPaymoneycode(), "??????", 
//                                            TextcardDetail.apply(TextcardDetail.GRAY, Constants.FULL_FORMAT.format(new Date())+"<br>", true),
//                                            TextcardDetail.apply(TextcardDetail.GRAY,"???????????????"+paymoney.getPaymoneycode(),false),
//                                            TextcardDetail.apply(TextcardDetail.GRAY,"????????????"+paymoney.getPaymoneyobjectcode(),false),
//                                            TextcardDetail.apply(TextcardDetail.GRAY,"??????????????????"+paymoneytypeStr,false),
//                                            TextcardDetail.apply(TextcardDetail.RED,"???????????????"+paymoney.getVerifyremark(),false)
//                                        ));
                            }
                        }
                    }
                }
            }
            
            if(StringUtils.isNotBlank(toEmpNo)) {
                try {
                    //token=no??????????????????????????????????????????????????????????????????
                    this.sender.sendQywxMsgSyn(new TextcardMessage(toEmpNo, 
                            null, "??????????????????", webfront_url+"/main/budgetImplementation/errorPay?token=no&paymoneycode=" + paymoney.getPaymoneycode(), "??????", 
                                TextcardDetail.apply(TextcardDetail.GRAY, Constants.FULL_FORMAT.format(new Date())+"<br>", true),
                                TextcardDetail.apply(TextcardDetail.GRAY,"???????????????"+paymoney.getPaymoneycode(),false),
                                TextcardDetail.apply(TextcardDetail.GRAY,"????????????"+paymoney.getPaymoneyobjectcode(),false),
                                TextcardDetail.apply(TextcardDetail.GRAY,"??????????????????"+paymoneytypeStr,false),
                                TextcardDetail.apply(TextcardDetail.RED,"???????????????"+paymoney.getVerifyremark(),false)
                            ));
                }catch(Exception e) {
                    System.out.println("??????????????????????????????");
                }
                
            }
        }

        batch.setPayfailids(payfailids);
        batch.setPayfailnum(payfailids.split(",").length);
        this.updateById(batch);
       
    }

    /**
     * ??????????????????
     * 1????????????--??????
     * 2????????????--??????????????????
     * 3????????????--??????????????????
     * 4????????????
     * 5???????????????
     * 6?????????--??????
     * 7?????????--??????
     *
     * @param bean
     * @param payIdOrderMap
     * @param bankAccountMap
     * @param payIdTcDetailMap
     * @return
     */
    private int ensureMessageType(BudgetPaymoney bean, Map<Long, BudgetReimbursementorder> payIdOrderMap, Map<String, BudgetBankAccount> bankAccountMap, Map<Long, BudgetExtractpaydetail> payIdTcDetailMap) {
        //??????????????????1????????????????????? 2????????????????????? 3???(??????)???????????? 4????????????????????? 5:?????????????????? 6:??????????????????????????????
        //??????????????????
        Integer type = bean.getPaymoneytype();
        if (1 == type) {
            BudgetReimbursementorder order = payIdOrderMap.get(bean.getId());
            if (order == null) {
                order = this.orderService.getByCode(bean.getPaymoneyobjectcode());
                if (order != null) {
                    payIdOrderMap.put(bean.getId(), order);
                    //??????
                    if (1 == order.getOrderscrtype()) {
                        return 1;
                    }
                    BudgetBankAccount bankAccount = bankAccountMap.get(bean.getBankaccount());
                    if (bankAccount != null) {
                        //??????????????????
                        if (2 == bankAccount.getAccounttype()) {
                            return 3;
                        }
                        //??????????????????
                        return 2;
                    }else{
                        //?????????????????????????????????hr?????????
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
                //??????--??????
                if (detail.getIscompanyemp()) {
                    return 6;
                }
                //??????--??????
                return 7;
            }
        }
        //??????
        if (3 == type && Constants.PAY_TYPE.TRANSFER.equals(bean.getPaytype())) {
            return 4;
        }
        if (6 == type) {
            return 5;
        }
        return -1;
    }

    /**
     * ?????????????????????????????????
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
                        "???" + extractsum.getExtractmonth().substring(0, 6) + "?????????",
                        bean.getPaymoneycode().substring(bean.getPaymoneycode().length() - 4),
                        bankAccountMap.get(bean.getBankaccount()).getAccountname(),
                        DataEncryptionUtil.encryptBankAcct(bean.getBankaccount()),
                        bean.getPaymoney().setScale(2, BigDecimal.ROUND_HALF_UP).toString()
                );

            }
        }
    }

    /**
     * ???????????????????????????
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
                                    "???" + extractsum.getExtractmonth().substring(0, 6) + "?????????",
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
     * ???????????????
     *
     * @param bean
     * @param unitEmpNoMap
     * @param budgetReimbursementorder
     * @param commonBxFormat
     */
    private void sendCommonBxMessage(BudgetPaymoney bean, Map<String, StringJoiner> unitEmpNoMap, BudgetReimbursementorder order, String commonBxFormat) {
        StringJoiner users = unitEmpNoMap.get(order.getUnitid().toString());
        if (users == null) {
            //???????????????????????????????????????
            BudgetUnit unit = this.unitService.getById(order.getUnitid());

            String empNo = "";
            if(unit!=null && StringUtils.isNotBlank(unit.getManagers())){
                WbUser user = userService.getById(unit.getManagers().split(",")[0]);
                empNo = user.getUserName();
            }

            //if (unit != null) {
            //    List<KVBean> unitUserIds = new ArrayList<KVBean>();
            //    unitUserIds.add(new KVBean(unit.getId().toString(), unit.getManagers()));
                //???????????????????????????????????????????????????
            //    this.mappingUnitAndEmpNo(unitUserIds, unitEmpNoMap);
           // }
            //????????????
            if(StringUtils.isNotBlank(empNo)){
                DecimalFormat df = new DecimalFormat("#0.00");
                //StringJoiner to = unitEmpNoMap.get(unit.getId().toString());
                this.sendMessage(commonBxFormat, empNo, order.getReimcode(), bean.getPaymoneycode().substring(bean.getPaymoneycode().length() - 4), bean.getPaymoney().setScale(2, BigDecimal.ROUND_HALF_UP).toString());

            }
        }
    }


    /**
     * ????????????????????????
     * ??????????????????????????????????????????????????????????????????????????????????????????****?????????????????????????????????${????????????????????????????????????????????????}
     */
    public void sendMessageByDocument(Set<String> reimcodeSet, Map<String, StringJoiner> unitEmpNoMap, String messageFormat) {

        DecimalFormat df = new DecimalFormat("#0.00");
        for (String reimcode : reimcodeSet) {
          //?????????????????????????????????????????????????????????k:?????????????????? v:?????????????????????
            List<KVBean> unitUserIds = this.mapper.getManagerOfBudgetUnitByBxCode(reimcode);
            //???????????????????????????????????????????????????
            this.mappingUnitAndEmpNo(unitUserIds, unitEmpNoMap);
            //??????????????????????????????????????????
            List<KVBean> unitMoney = this.mapper.getUnitAllocateMoney(reimcode);
            if (unitMoney != null && unitMoney.size() > 0) {
                //?????????
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
     * ?????????????????????????????????????????????
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
     * ????????????
     *
     * @param messageTemplate
     * @param to              ???????????????????????????"|"??????
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
     * ??????????????????????????????????????????????????????ex 1 = 19283,17474
     *
     * @param unitUserIds
     * @param unitEmpMap
     */
    private void mappingUnitAndEmpNo(List<KVBean> unitUserIds, Map<String, StringJoiner> unitEmpMap) {
        //??????????????????
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
     * ??????????????????
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
     * ????????????????????????????????????
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
