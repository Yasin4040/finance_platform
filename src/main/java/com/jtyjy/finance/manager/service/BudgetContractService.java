package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.ecology.EcologyClient;
import com.jtyjy.ecology.EcologyParams;
import com.jtyjy.ecology.EcologyWorkFlowValue;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.enmus.LendTypeEnum;
import com.jtyjy.finance.manager.enmus.PaymoneyStatusEnum;
import com.jtyjy.finance.manager.enmus.PaymoneyTypeEnum;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.trade.DistributedNumber;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.utils.ResponseUtil;
import com.jtyjy.finance.manager.vo.*;
import com.xxl.job.core.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetContractService extends DefaultBaseService<BudgetContractMapper, BudgetContract> {

    private final TabChangeLogMapper loggerMapper;
    private final BudgetContractMapper budgetContractMapper;
    private final BudgetLendmoneyMapper budgetLendmoneyMapper;
    private final BudgetPaymoneyMapper budgetPaymoneyMapper;
    private final BudgetBillingUnitMapper budgetBillingUnitMapper;
    private final WbBanksMapper wbBanksMapper;
    private final WbUserMapper wbUserMapper;
    private final DistributedNumber distributedNumber;
    private final RedisClient redisClient;

    @Override
    public BaseMapper<TabChangeLog> getLoggerMapper() {
        return loggerMapper;
    }

    @Override
    public void setBaseLoggerBean() {
        DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_contract"));
    }

    /**
     * ??????????????????????????????
     */
    public PageResult<BudgetContractVO> listContractPage(Integer page, Integer rows, HashMap<String, Object> paramMap) {
        Page<BudgetContractVO> pageBean = new Page<>(page, rows);
        List<BudgetContractVO> resultList = this.budgetContractMapper.listContractPage(pageBean, paramMap);
        resultList.forEach(v -> {
            // ???????????????????????????, ?????????????????????????????????
            if (v.getUnRepaidMoney() == null) {
                v.setUnRepaidMoney(v.getContractMoney());
            }
            // ???????????????????????????, ?????????????????????????????????
            if (v.getPaidMoney() == null) {
                v.setUnPaidMoney(v.getContractMoney());
            } else {
                v.setUnPaidMoney(v.getContractMoney().subtract(v.getPaidMoney()));
            }
        });
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    /**
     * ????????????
     */
    public void stopContract(Long id) {
        BudgetContract budgetContract = this.budgetContractMapper.selectById(id);
        if (budgetContract == null) {
            throw new RuntimeException("???????????????");
        }
        if (budgetContract.getTerminationflag() == 1) {
            throw new RuntimeException("?????? ???" + budgetContract.getContractname() + "???????????????!");
        }
        BudgetContract updateContract = new BudgetContract();
        updateContract.setId(budgetContract.getId());
        updateContract.setTerminationflag(1);
        updateContract.setTerminationdate(new Date());
        this.budgetContractMapper.updateById(updateContract);
    }

    /**
     * ??????????????????
     */
    public PageResult<BudgetPayMoneyDetailVO> getPayMoneyDetail(Integer page, Integer rows, Long id) {
        Page<BudgetPayMoneyDetailVO> pageBean = new Page<>(page, rows);
        HashMap<String, Object> paramMap = new HashMap<>(2);
        paramMap.put("contractId", id);
        List<BudgetPayMoneyDetailVO> payMoneyDetail = this.budgetLendmoneyMapper.getPayMoneyDetail(pageBean, paramMap);
        payMoneyDetail.forEach(v -> v.setLendTypeDesc(LendTypeEnum.getValue(v.getLendType())));
        return PageResult.apply(pageBean.getTotal(), payMoneyDetail);
    }

    /**
     * ??????????????????
     */
    public PageResult<BudgetStrikeMoneyDetailVO> getStrikeMoneyDetail(Integer page, Integer rows, Long id) {
        Page<BudgetStrikeMoneyDetailVO> pageBean = new Page<>(page, rows);
        List<BudgetStrikeMoneyDetailVO> resultList = this.budgetContractMapper.getStrikeMoneyDetail(pageBean, id);
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    // ----------------------------------------------------------------------------------------------------

    /**
     * ????????????????????????????????????
     */
    public PageResult<BudgetContractLendVO> listContractLendPage(Integer page, Integer rows, HashMap<String, Object> paramMap) {
        Page<BudgetContractLendVO> pageBean = new Page<>(page, rows);
        List<BudgetContractLendVO> resultList = this.budgetContractMapper.listContractLendPage(pageBean, paramMap);
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    /**
     * ????????????
     */
    public void contractSigning(EcologyParams params) {
        String requestId = params.getRequestid();

        if (redisClient.exist("contractSigning:" + requestId)) {
            throw new RuntimeException("??????????????????????????????????????????!");
        }
        redisClient.set("contractSigning:" + requestId, "contractSigning",60);

        try{
            Integer count = this.budgetLendmoneyMapper.selectCount(new QueryWrapper<BudgetLendmoney>().eq("requestid", requestId));
            if (count > 0) {
                throw new RuntimeException("????????????????????????!");
            }

            EcologyWorkFlowValue value = EcologyClient.getWorkflowValue(params);
            Map<String, String> mainTableValue = value.getMaintablevalue();

            // ?????????
            String applyPerson = mainTableValue.get("sqr");
            // ????????????
            String contractName = mainTableValue.get("htmc");
            // ????????????
            String contractCode = mainTableValue.get("htbh");
            // ????????????
            String contractType = mainTableValue.get("htlx_new").split("_")[1];
            // ??????/????????????
            String contractAmtStr = mainTableValue.get("htje");
            // ????????????
            String signDateStr = mainTableValue.get("qdsj");
            // ??????????????????
            String oppoCompany = mainTableValue.get("dfdwmc");
            // ????????????
            String signCopies = mainTableValue.get("qjfs");
            // ????????????
            String contractDistribute = mainTableValue.get("htfp");
            // ???????????????
            String addrTel = mainTableValue.get("txdzjdh");
            // ??????????????????
            String agreeSumType = mainTableValue.get("htydjsfs");
            // ??????????????????
            String contextDigest = mainTableValue.get(" htnrzy");

            if (StringUtils.isNotBlank(agreeSumType)) {
                agreeSumType = agreeSumType.replace("&nbsp;", " ");
            }

            BudgetContract budgetContract = new BudgetContract();
            budgetContract.setContractname(contractName);
            budgetContract.setContractcode(contractCode);
            budgetContract.setContracttype(contractType);
            budgetContract.setContractmoney(Float.parseFloat(contractAmtStr));
            try {
                budgetContract.setSigndate(Constants.FORMAT_10.parse(signDateStr));
            } catch (ParseException e) {
                e.printStackTrace();
                throw new RuntimeException("???????????????????????????????????????");
            }
            budgetContract.setContractcopies(signCopies);
            budgetContract.setCreatetime(new Date());
            budgetContract.setTerminationflag(0);
            budgetContract.setAgreesumtype(agreeSumType);
            budgetContract.setContextdigest(contextDigest);
            budgetContract.setOtherpartyunit(oppoCompany);
            budgetContract.setRequestid(requestId);

            // ????????????????????????
            budgetContract.setOtherinfo("????????????" + applyPerson + ", ????????????????????????" + addrTel + ", ???????????????" + contractDistribute);
            this.budgetContractMapper.insert(budgetContract);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("????????????????????????!");
        }finally {
            redisClient.delete("contractSigning:" + requestId);
        }
    }

    /**
     * ????????????
     */
    public void contractLendMoney(EcologyParams params) {
        String requestId = params.getRequestid();

        if (redisClient.exist("contractLendMoney:" + requestId)) {
            throw new RuntimeException("??????????????????????????????????????????!");
        }
        redisClient.set("contractLendMoney:" + requestId, "contractLendMoney",60);

        try{
            Integer count = this.budgetLendmoneyMapper.selectCount(new QueryWrapper<BudgetLendmoney>().eq("requestid", requestId));
            if (count > 0) {
                throw new RuntimeException("????????????????????????!");
            }

            EcologyWorkFlowValue value = EcologyClient.getWorkflowValue(params);
            Map<String, String> mainTableValue = value.getMaintablevalue();
            Map<String, List<Map<String, String>>> detailTableValues = value.getDetailtablevalues();

            // ??????
            String empNo = mainTableValue.get("gh");
            if (StringUtils.isNotBlank(empNo) && empNo.length() > 5) {
                empNo = empNo.substring(0, 5);
            }
            // ????????????
            String lendDateStr = mainTableValue.get("jbrq");
            // ??????????????????
            String planDateStr = mainTableValue.get("yjbxrq");
            // ????????????Id
            String yearId = mainTableValue.get("bxjb");
            // ??????Id
            String contractNameId = mainTableValue.get("htmc");
            // ????????????
            String nonContractAmtStr = mainTableValue.get("xmzje");
            // ????????????
            String payRemark = mainTableValue.get("fksy");
            // ????????????  0 ?????????1 ??????
            String payType = mainTableValue.get("yqzffs");
            // ????????????
            String requestCode = mainTableValue.get("lcbh");

            // ??????????????????  0 ??? ???1 ???
            String isContract = mainTableValue.get("sfqdht");
            // ???????????????
            String nonContractName = mainTableValue.get("xmmc");

            if (StringUtils.isNotBlank(payRemark)) {
                payRemark = payRemark.replace("&nbsp;", " ");
            }

            int lendType;
            BudgetContract budgetContract;
            if ("1".equals(isContract)) {
                // ????????????????????????????????????, ?????????????????????????????????
                // update minzhq 2021-09-15  ??????????????????
                //Integer exist = this.budgetContractMapper.selectCount(new QueryWrapper<BudgetContract>().eq("contractname", nonContractName));
                //if (exist > 0) {
                //    throw new RuntimeException("??????????????????" + nonContractName + "?????????????????????????????????");
                //}
                budgetContract = new BudgetContract();
                budgetContract.setContractname(nonContractName);
                budgetContract.setContractmoney(Float.parseFloat(nonContractAmtStr));
                budgetContract.setCreatetime(new Date());
                budgetContract.setTerminationflag(0);
                budgetContract.setRequestid(requestId);
                budgetContract.setOtherinfo(payRemark);
                this.budgetContractMapper.insert(budgetContract);

                lendType = LendTypeEnum.LEND_TYPE_16.getType();
            } else {
                budgetContract = this.budgetContractMapper.selectById(Long.parseLong(contractNameId));
                if (budgetContract == null) {
                    throw new RuntimeException("??????????????????");
                }
                lendType = LendTypeEnum.LEND_TYPE_15.getType();
            }

            WbUser user = this.wbUserMapper.selectOne(new QueryWrapper<WbUser>().eq("user_name", empNo));
            if (user == null) {
                throw new RuntimeException("?????????" + empNo + "??????????????????");
            }

            if (detailTableValues != null && !detailTableValues.isEmpty()) {
                // ?????????????????????(????????????????????????????????????????????????????????????)
                String finalPayRemark = payRemark;
                detailTableValues.forEach((str, values) -> {
                    values.forEach(detail -> {
                        // ?????????????????????(????????????)
                        String gatherId = detail.get("wbdw");
                        String gatherAccountId = detail.get("wbdwyhzh");
                        String openBank = detail.get("khh");
                        String je = detail.get("je");
                        WbBanks bank = this.wbBanksMapper.selectByAccountId(gatherAccountId);
                        if (bank == null) {
                            throw new RuntimeException("?????????????????????" + gatherAccountId + "????????????????????????");
                        }

                        // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                        String payUnitId = detail.get("kpdw");
                        String payAccount = detail.get("kpdwyhzh");
                        String payOpenBank = detail.get("kpdwkhh");
                        WbBanks unitBank;
                        if (StringUtils.isBlank(payAccount)) {
                            List<WbBanks> unitBanks = this.wbBanksMapper.selectByBillingUnitId(payUnitId);
                            if(CollectionUtils.isEmpty(unitBanks)){
                                throw new RuntimeException("???????????????" + payUnitId + "????????????????????????");
                            }
                            unitBank = unitBanks.get(0);
                        } else {
                            unitBank = this.wbBanksMapper.selectByUnitAccount(payAccount);
                        }
                        if (unitBank == null) {
                            throw new RuntimeException("?????????????????????" + payAccount + "????????????????????????");
                        }

                        // ?????????(?????????????????????????????????????????????????????????????????????????????????????????????)
                        BudgetLendmoney lendMoney = new BudgetLendmoney();
                        lendMoney.setEmpno(bank.getBankCode());
                        lendMoney.setEmpname(bank.getAccountName());
                        lendMoney.setOperatorEmpId(user.getUserId());
                        lendMoney.setOperatorEmpNo(user.getUserName());
                        lendMoney.setOperatorEmpName(user.getDisplayName());
                        lendMoney.setLendmoneycode(this.distributedNumber.getLendNum());
                        lendMoney.setLendmoney(new BigDecimal(je));
                        lendMoney.setContractid(budgetContract.getId());
                        lendMoney.setLendtype(lendType);
                        lendMoney.setRepaidmoney(BigDecimal.ZERO);
                        try {
                            lendMoney.setLenddate(Constants.FORMAT_10.parse(lendDateStr));
                            lendMoney.setPlanpaydate(Constants.FORMAT_10.parse(planDateStr));
                        } catch (ParseException e) {
                            e.printStackTrace();
                            throw new RuntimeException("???????????????????????????");
                        }
                        lendMoney.setCreatetime(new Date());
                        lendMoney.setRemark(finalPayRemark);
                        lendMoney.setRequestid(requestId);
                        lendMoney.setDeleteflag(false);
                        lendMoney.setYearid(Long.valueOf(yearId));
                        lendMoney.setInterestmoney(BigDecimal.ZERO);
                        lendMoney.setFlushingflag(false);
                        lendMoney.setEffectflag(true);
                        lendMoney.setChargebillflag(false);
                        lendMoney.setMakeaccountflag(false);
                        lendMoney.setRequestcode(requestCode);
                        this.budgetLendmoneyMapper.insert(lendMoney);

                        // ???????????????
                        BudgetPaymoney payMoney = new BudgetPaymoney();
                        payMoney.setId(null);
                        payMoney.setPaymoneytype(PaymoneyTypeEnum.LEND_PAY.type);
                        payMoney.setPaymoneystatus(PaymoneyStatusEnum.RECEIVE_PAY.type);
                        payMoney.setReceivetime(new Date());
                        payMoney.setPaymoney(new BigDecimal(je));
                        payMoney.setLendtype(lendMoney.getLendtype());
                        payMoney.setPaytype(Integer.parseInt(payType));
                        payMoney.setMonth(Constants.FORMAT_6.format(new Date()));
                        payMoney.setCreatetime(new Date());
                        payMoney.setBunitname(unitBank.getBillingUnitName());
                        payMoney.setBunitbankaccount(unitBank.getBankAccount());
                        payMoney.setBunitaccountbranchcode(unitBank.getSubBranchCode());
                        payMoney.setBunitaccountbranchname(unitBank.getBankName());

                        BudgetBillingUnit budgetBillingUnit = this.budgetBillingUnitMapper.selectById(payUnitId);
                        if (budgetBillingUnit != null) {
                            payMoney.setBunitname(budgetBillingUnit.getName());
                        }

                        payMoney.setBankaccount(bank.getBankAccount());
                        payMoney.setBankaccountbranchcode(bank.getSubBranchCode());
                        payMoney.setBankaccountbranchname(bank.getBankName());
                        payMoney.setBankaccountname(bank.getAccountName());
                        payMoney.setOpenbank(openBank);
                        payMoney.setPaymoneycode(this.distributedNumber.getPaymoneyNum());
                        payMoney.setPaymoneyobjectid(lendMoney.getId());
                        payMoney.setPaymoneyobjectcode(lendMoney.getLendmoneycode());
                        this.budgetPaymoneyMapper.insert(payMoney);
                    });
                });
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("????????????????????????!");
        }finally {
            redisClient.delete("contractLendMoney:" + requestId);
        }
    }

    public List<BudgetContractExcelVO> listContract(HashMap<String, Object> paramMap) {
        List<BudgetContractVO> resultList = this.budgetContractMapper.listContractPage(null, paramMap);
        List<BudgetContractExcelVO> vos = new ArrayList<>();
        resultList.forEach(v -> {
            // ???????????????????????????, ?????????????????????????????????
            if (v.getUnRepaidMoney() == null) {
                v.setUnRepaidMoney(v.getContractMoney());
            }
            // ???????????????????????????, ?????????????????????????????????
            if (v.getPaidMoney() == null) {
                v.setUnPaidMoney(v.getContractMoney());
            } else {
                v.setUnPaidMoney(v.getContractMoney().subtract(v.getPaidMoney()));
            }
            BudgetContractExcelVO vo = new BudgetContractExcelVO();
            BeanUtils.copyProperties(v,vo);
            if(0==v.getTerminationFlag()){
                vo.setTerminationFlagName("?????????");
            }else if(1==v.getTerminationFlag()){
                vo.setTerminationFlagName("?????????");
            }
            vos.add(vo);
        });
        return vos;
    }

    public void exportContractLend(HashMap<String, Object> paramMap, HttpServletResponse response) throws Exception {
        List<BudgetContractLendVO> resultList = this.budgetContractMapper.listContractLendPage(null, paramMap);
        List<List<String>> dataList = new ArrayList<>();
        for (BudgetContractLendVO vo : resultList) {
            List<String> colList = new ArrayList<>();
            colList.add(vo.getRepaymentStatus()?"?????????":"?????????");
            colList.add(vo.getLendMoneyCode());
            String payMoneyStatusName = "";
            if(0==vo.getPayMoneyStatus()){
                payMoneyStatusName = "????????????";
            }else if(1==vo.getPayMoneyStatus()){
                payMoneyStatusName = "????????????";
            }else if(2==vo.getPayMoneyStatus()){
                payMoneyStatusName = "????????????";
            }else if(3==vo.getPayMoneyStatus()){
                payMoneyStatusName = "????????????";
            }
            colList.add(payMoneyStatusName);
            colList.add(vo.getEmpNo());
            colList.add(vo.getEmpName());
            colList.add(vo.getLendMoney().toString());
            colList.add(vo.getRepaidMoney().toString());
            colList.add(vo.getUnRepaidMoney().toString());
            colList.add(DateUtil.format(vo.getLendDate(),"yyyy-MM-dd"));
            if(Objects.isNull(vo.getPayPlanDate())){
                colList.add("");
            }else{
                colList.add(DateUtil.format(vo.getPayPlanDate(),"yyyy-MM-dd"));
            }
            colList.add(vo.getContractName());
            colList.add(vo.getAgreeSumType());
            colList.add(vo.getRemark());
            dataList.add(colList);
        }
        ResponseUtil.exportContractLend(dataList,  EasyExcelUtil.getOutputStream("??????????????????", response));

    }
}
