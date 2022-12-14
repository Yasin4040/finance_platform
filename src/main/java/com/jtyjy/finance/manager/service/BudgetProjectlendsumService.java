package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.controller.reimbursement.ReimbursementWorker;
import com.jtyjy.finance.manager.dto.ProjectLendReimbursementDTO;
import com.jtyjy.finance.manager.dto.ReimbursementRequest;
import com.jtyjy.finance.manager.enmus.LendTypeEnum;
import com.jtyjy.finance.manager.enmus.PaymoneyStatusEnum;
import com.jtyjy.finance.manager.enmus.ReimbursementFromEnmu;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.trade.DistributedNumber;
import com.jtyjy.finance.manager.utils.NumberUtil;
import com.jtyjy.finance.manager.vo.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetProjectlendsumService extends DefaultBaseService<BudgetProjectlendsumMapper, BudgetProjectlendsum> {

    private final TabChangeLogMapper loggerMapper;
    private final WbBanksMapper wbBanksMapper;
    private final WbDeptMapper wbDeptMapper;
    private final WbPersonMapper wbPersonMapper;
    private final BudgetUnitMapper budgetUnitMapper;
    private final BudgetArrearsMapper budgetArrearsMapper;
    private final BudgetPaymoneyMapper budgetPaymoneyMapper;
    private final BudgetMonthAgentMapper budgetMonthAgentMapper;
    private final BudgetMonthSubjectMapper budgetMonthSubjectMapper;
    private final BudgetYearSubjectMapper budgetYearSubjectMapper;
    private final BudgetYearAgentMapper budgetYearAgentMapper;
    private final BudgetUnitSubjectMapper budgetUnitSubjectMapper;
    private final BudgetLendmoneyMapper budgetLendmoneyMapper;
    private final BudgetBankAccountMapper budgetBankAccountMapper;
    private final BudgetBillingUnitMapper budgetBillingUnitMapper;
    private final BudgetProjectlendsumMapper budgetProjectlendsumMapper;
    private final BudgetLendInterestRuleMapper budgetLendInterestRuleMapper;
    private final BudgetLendmoneyInterestMapper budgetLendmoneyInterestMapper;
    private final BudgetBillingUnitAccountMapper budgetBillingUnitAccountMapper;
    private final BudgetProjectlendbxpaymentMapper budgetProjectlendbxpaymentMapper;
    private final BudgetProjectlendbxtransMapper budgetProjectlendbxtransMapper;
    private final BudgetProjectlendbxdetailMapper budgetProjectlendbxdetailMapper;
    private final BudgetRepaymoneyDetailMapper budgetRepaymoneyDetailMapper;
    private final BudgetRepaymoneyMapper budgetRepaymoneyMapper;
    private final BudgetMonthEndUnitMapper budgetMonthEndUnitMapper;
    private final BudgetAgentExecuteViewMapper budgetAgentExecuteViewMapper;
    private final BudgetReimbursementorderDetailMapper budgetReimbursementorderDetailMapper;
    private final BudgetReimbursementorderAllocatedMapper budgetReimbursementorderAllocatedMapper;
    private final BudgetReimbursementorderMapper budgetReimbursementorderMapper;

    private final DistributedNumber distributedNumber;
    private final WbUserService wbUserService;
    private final ReimbursementWorker reimbursementWorker;

    @Override
    public BaseMapper<TabChangeLog> getLoggerMapper() {
        return loggerMapper;
    }

    @Override
    public void setBaseLoggerBean() {
        DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_projectlendsum_new"));
    }

    /**
     * ??????????????????????????????
     */
    public PageResult<BudgetProjectLendSumVO> listProjectLendPage(Integer page, Integer rows, HashMap<String, Object> paramMap) {
        Page<BudgetProjectLendSumVO> pageBean = new Page<>(page, rows);
        List<BudgetProjectLendSumVO> resultList = this.budgetProjectlendsumMapper.listProjectLendPage(pageBean, paramMap);
        resultList.forEach(v -> {
            if (v.getBxStatus() != null && v.getBxDate() != null) {
                // ???????????????null, ?????????????????????
                if (v.getRequestStatus() == null) {
                    v.setRequestStatus(0);
                }
                // ????????????
                if (v.getPaymentMoney() == null) {
                    v.setPaymentMoney(BigDecimal.ZERO);
                }
                // ????????????
                if (v.getTransMoney() == null) {
                    v.setTransMoney(BigDecimal.ZERO);
                }
                // ????????????
                v.setBxMoney(v.getPaymentMoney().add(v.getTransMoney()));
            }
        });
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    /**
     * ????????????????????????
     */
    public void updatePayMoneyUnitId(Long id, Long bUnitId) {
        BudgetProjectlendsum projectLendSum = this.budgetProjectlendsumMapper.selectById(id);
        if (projectLendSum == null) {
            throw new RuntimeException("????????????????????????");
        } else if (projectLendSum.getVerifyflag() == 1) {
            throw new RuntimeException("????????????????????????????????????????????????????????????");
        }
        // ???????????????????????????
        BudgetBillingUnit billingUnit = this.budgetBillingUnitMapper.selectById(bUnitId);
        if (billingUnit == null) {
            throw new RuntimeException("???????????????????????????????????????!");
        } else if (billingUnit.getStopFlag() == 1) {
            throw new RuntimeException("???????????????????????????!");
        }
        BudgetProjectlendsum updateProjectLendSum = new BudgetProjectlendsum();
        updateProjectLendSum.setId(id);
        updateProjectLendSum.setPaymoneyunitid(bUnitId);
        this.budgetProjectlendsumMapper.updateById(updateProjectLendSum);
    }

    /**
     * ????????????
     */
    public void verify(Long id) {
        BudgetProjectlendsum projectLend = this.budgetProjectlendsumMapper.selectById(id);
        if (projectLend == null) {
            throw new RuntimeException("?????????????????????");
        } else if (projectLend.getVerifyflag() == 1) {
            throw new RuntimeException("?????????????????????!");
        }

        List<BudgetLendmoney> lendMoneyList = this.budgetLendmoneyMapper.selectList(new QueryWrapper<BudgetLendmoney>()
                .eq("deleteflag", 0)
                .eq("projectlendsumid", id));
        if (lendMoneyList.isEmpty()) {
            throw new RuntimeException("????????????!?????????????????????!");
        }

        BigDecimal total = lendMoneyList.stream().map(BudgetLendmoney::getLendmoney).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(projectLend.getTotal()) > 0) {
            throw new RuntimeException("??????????????????????????????????????????????????????");
        }
        // ??????????????????????????????????????????????????????????????????????????????
        validateDetailMoney(projectLend, lendMoneyList);

        // ???????????????????????????????????????
        List<BudgetLendmoney> list = lendMoneyList.stream()
                .filter(v -> "2".equals(v.getProjectlendtype()))
                .collect(Collectors.toList());
        if (!list.isEmpty() && projectLend.getPaymoneyunitid() == null) {
            throw new RuntimeException("??????????????????????????????!");
        }
        // ???????????????????????????
        BudgetBillingUnit billingUnit = this.budgetBillingUnitMapper.selectOne(new QueryWrapper<BudgetBillingUnit>()
                .eq("stopflag", 0)
                .eq("id", projectLend.getPaymoneyunitid()));
        if (billingUnit == null) {
            throw new RuntimeException("???????????????????????????????????????!");
        }
        List<BudgetBillingUnitAccount> accountList = this.budgetBillingUnitAccountMapper.selectList(new QueryWrapper<BudgetBillingUnitAccount>()
                .eq("stopflag", 0)
                .eq("billingunitid", billingUnit.getId()));
        if (accountList.isEmpty()) {
            throw new RuntimeException("???????????????" + billingUnit.getName() + "??????????????????!");
        }
        BudgetBillingUnitAccount unitAccount = accountList.stream().filter(BudgetBillingUnitAccount::getDefaultflag).findFirst().orElse(null);
        if (unitAccount == null) {
            unitAccount = accountList.stream().min(Comparator.comparing(BudgetBillingUnitAccount::getOrderno)).orElse(accountList.get(0));
        }

        Date currentDate = new Date();
        WbUser user = UserThreadLocal.get();

        BudgetProjectlendsum updateProjectLend = new BudgetProjectlendsum();
        updateProjectLend.setId(projectLend.getId());
        updateProjectLend.setVerifyflag(1);
        updateProjectLend.setVerifytime(currentDate);
        updateProjectLend.setVerifyorid(user.getUserName());
        updateProjectLend.setVerifyname(user.getDisplayName());
        this.budgetProjectlendsumMapper.updateById(updateProjectLend);

        // 1.????????????????????????????????????????????????
        // 2.????????????????????????
        BudgetBillingUnitAccount finalUnitAccount = unitAccount;
        lendMoneyList.forEach(lendMoney -> {
            BudgetPaymoney payMoney = new BudgetPaymoney();
            payMoney.setPaymoneytype(6);
            payMoney.setPaymoneyobjectid(lendMoney.getId());
            payMoney.setPaymoneyobjectcode(projectLend.getProjectno());
            payMoney.setPaymoney(lendMoney.getLendmoney());
            payMoney.setPaytype(Constants.PAY_TYPE.TRANSFER);
            payMoney.setPaymoneystatus(PaymoneyStatusEnum.RECEIVE_PAY.getType());
            payMoney.setLendtype(lendMoney.getLendtype());
            payMoney.setCreatetime(currentDate);
            payMoney.setReceivetime(currentDate);

            WbBanks unitBank = this.wbBanksMapper.selectOne(new QueryWrapper<WbBanks>().eq("sub_branch_code", finalUnitAccount.getBranchcode()));
            payMoney.setBunitname(billingUnit.getName());
            payMoney.setBunitbankaccount(finalUnitAccount.getBankaccount());
            payMoney.setBunitaccountbranchcode(finalUnitAccount.getBranchcode());
            payMoney.setBunitaccountbranchname(unitBank.getBankName());

            // ???????????????????????????
            BudgetBankAccount bankAccount = this.budgetBankAccountMapper.selectOne(new QueryWrapper<BudgetBankAccount>()
                    .eq("stopflag", 0)
                    .eq("code", lendMoney.getEmpno())
                    .eq("pname", lendMoney.getEmpname())
                    .orderByDesc("wagesflag")
                    .last("limit 1"));
            if (bankAccount == null) {
                throw new RuntimeException("???" + lendMoney.getEmpname() + "(" + lendMoney.getEmpno() + ")?????????????????????!");
            }
            WbBanks bank = this.wbBanksMapper.selectOne(new QueryWrapper<WbBanks>().eq("sub_branch_code", bankAccount.getBranchcode()));
            payMoney.setBankaccountname(bankAccount.getAccountname());
            payMoney.setBankaccount(bankAccount.getBankaccount());
            payMoney.setBankaccountbranchcode(bankAccount.getBranchcode());
            payMoney.setBankaccountbranchname(bank.getBankName());
            payMoney.setOpenbank(bank.getSubBranchName());
            payMoney.setPaymoneycode(this.distributedNumber.getPaymoneyNum());
            this.budgetPaymoneyMapper.insert(payMoney);

            BudgetLendmoney updateLendMoney = new BudgetLendmoney();
            updateLendMoney.setId(lendMoney.getId());
            updateLendMoney.setConfirmflag(true);
            updateLendMoney.setEffectflag(true);
            updateLendMoney.setConfirmtime(currentDate);
            this.budgetLendmoneyMapper.updateById(updateLendMoney);
        });
    }

    private void validateDetailMoney(BudgetProjectlendsum projectLend, List<BudgetLendmoney> lendMoneyList) {
        // ??????????????????????????????????????????????????????????????????????????????
        BigDecimal cashTotal = lendMoneyList.stream().filter(v -> "1".equals(v.getProjectlendtype())).map(BudgetLendmoney::getLendmoney).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal transTotal = lendMoneyList.stream().filter(v -> "2".equals(v.getProjectlendtype())).map(BudgetLendmoney::getLendmoney).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal giftTotal = lendMoneyList.stream().filter(v -> "3".equals(v.getProjectlendtype())).map(BudgetLendmoney::getLendmoney).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (cashTotal.compareTo(projectLend.getCashmoney()) > 0) {
            throw new RuntimeException("??????????????????????????????????????????????????????????????????");
        }
        if (transTotal.compareTo(projectLend.getTransfermoney()) > 0) {
            throw new RuntimeException("??????????????????????????????????????????????????????????????????");
        }
        if (giftTotal.compareTo(projectLend.getGiftmoney()) > 0) {
            throw new RuntimeException("??????????????????????????????????????????????????????????????????");
        }
    }

    /**
     * ??????????????????
     */
    public PageResult<BudgetProjectLendDetailVO> listLendMoneyDetail(Integer page, Integer rows, Long id) {
        Page<BudgetProjectLendDetailVO> pageBean = new Page<>(page, rows);
        List<BudgetProjectLendDetailVO> resultList = this.budgetProjectlendsumMapper.listLendMoneyDetail(pageBean, id);
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    /**
     * ??????????????????
     */
    public PageResult<BudgetProjectRepayDetailVO> listRepayMoneyDetail(Integer page, Integer rows, Long id) {
        Page<BudgetProjectRepayDetailVO> pageBean = new Page<>(page, rows);
        List<BudgetProjectRepayDetailVO> resultList = this.budgetProjectlendsumMapper.listRepayMoneyDetail(pageBean, id);
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    /**
     * ????????????
     */
    public void deleteLendMoney(List<Long> ids) {
        List<BudgetLendmoney> lendMoneyList = this.budgetLendmoneyMapper.selectBatchIds(ids);
        if (!lendMoneyList.isEmpty()) {
            List<Long> collect = lendMoneyList.stream().map(BudgetLendmoney::getProjectlendsumid).filter(Objects::nonNull).collect(Collectors.toList());
            if (lendMoneyList.size() != collect.size()) {
                throw new RuntimeException("???????????????????????????????????????????????????");
            }
            HashSet<Long> hashSet = new HashSet<>(collect);
            if (hashSet.size() != 1) {
                throw new RuntimeException("?????????????????????????????????????????????");
            }
            BudgetProjectlendsum projectLendSum = this.budgetProjectlendsumMapper.selectById(collect.get(0));
            if (projectLendSum == null) {
                throw new RuntimeException("????????????????????????????????????????????????");
            } else if (projectLendSum.getVerifyflag() == 1) {
                throw new RuntimeException("????????????????????????????????????");
            }
            this.budgetLendmoneyMapper.deleteBatchIds(ids);
        }
    }

    /**
     * ??????"??????/??????"???????????????
     */
    public void batchReachStandard(List<Long> ids) {
        List<BudgetLendmoney> lendMoneyList = updateReachStandardStatus(ids, true);

        // ????????????
        clearInterest(lendMoneyList);
    }

    private void clearInterest(List<BudgetLendmoney> lendMoneyList) {
        // ????????????0
        for (BudgetLendmoney lm : lendMoneyList) {
            if (lm.getInterestmoney().compareTo(BigDecimal.ZERO) >= 0) {
                BigDecimal curInterestMoney = lm.getTempInterestMoney();

                // ???????????????????????????
                BudgetArrears budgetArrears = this.budgetArrearsMapper.selectOne(new QueryWrapper<BudgetArrears>().eq("empNo", lm.getEmpno()));
                if (budgetArrears != null) {
                    BudgetArrears updateArrears = new BudgetArrears();
                    updateArrears.setId(budgetArrears.getId());
                    updateArrears.setInterestmoney(budgetArrears.getInterestmoney().subtract(curInterestMoney));
                    updateArrears.setArrearsmoeny(budgetArrears.getArrearsmoeny().subtract(curInterestMoney));
                    this.budgetArrearsMapper.updateById(updateArrears);
                }
            }
        }
    }

    private List<BudgetLendmoney> updateReachStandardStatus(List<Long> ids, boolean isReach) {
        List<BudgetLendmoney> lendMoneyList = this.budgetLendmoneyMapper.selectBatchIds(ids);
        for (BudgetLendmoney lendMoney : lendMoneyList) {
            if (lendMoney.getConfirmflag() == null) {
                throw new RuntimeException("???????????????" + lendMoney.getLendmoneycode() + "????????????!");
            } else if (!lendMoney.getConfirmflag()) {
                throw new RuntimeException("???????????????" + lendMoney.getLendmoneycode() + "????????????!");
            } else if (lendMoney.getFlushingflag() != null && !lendMoney.getFlushingflag()) {
                throw new RuntimeException("???????????????" + lendMoney.getLendmoneycode() + "?????????????????????!");
            } else if (lendMoney.getFlushingflag() != null && lendMoney.getFlushingflag()) {
                throw new RuntimeException("???????????????" + lendMoney.getLendmoneycode() + "??????????????????!");
            }

            // ???????????????????????????
            lendMoney.setTempInterestMoney(lendMoney.getInterestmoney());
        }

        WbUser user = UserThreadLocal.get();
        BudgetLendmoney updateLendMoney = new BudgetLendmoney();
        updateLendMoney.setFlushingflag(isReach);
        updateLendMoney.setEffectflag(true);
        updateLendMoney.setFlushingor(user.getUserName());
        updateLendMoney.setFlushingorname(user.getDisplayName());
        updateLendMoney.setFlushtime(new Date());
        if (isReach) {
            // ??????????????????
            updateLendMoney.setInterestmoney(BigDecimal.ZERO);
        } else {
            // ????????????????????????????????????
            updateLendMoney.setChargebillflag(true);
            updateLendMoney.setChargebillor(user.getUserName());
            updateLendMoney.setChargebillorname(user.getDisplayName());
            updateLendMoney.setChargebilltime(new Date());
        }
        this.budgetLendmoneyMapper.update(updateLendMoney, new QueryWrapper<BudgetLendmoney>().in("id", ids));
        return lendMoneyList;
    }

    /**
     * ??????"?????????/?????????"???????????????
     */
    public void batchNotReachStandard(List<Long> ids) {
        updateReachStandardStatus(ids, false);
    }

    /**
     * ???????????????????????????
     */
    public void allowBuckleMoney(List<Long> ids) {
        List<BudgetLendmoney> lendMoneyList = this.budgetLendmoneyMapper.selectBatchIds(ids);
        for (BudgetLendmoney lendMoney : lendMoneyList) {
            BudgetProjectlendsum budgetProjectlendsum = this.budgetProjectlendsumMapper.selectById(lendMoney.getProjectlendsumid());
            if (budgetProjectlendsum == null || budgetProjectlendsum.getType() != 1) {
                throw new RuntimeException("???????????????" + lendMoney.getLendmoneycode() + "????????????????????????, ??????????????????!");
            } else if (lendMoney.getConfirmflag() == null) {
                throw new RuntimeException("???????????????" + lendMoney.getLendmoneycode() + "????????????!");
            } else if (!lendMoney.getConfirmflag()) {
                throw new RuntimeException("???????????????" + lendMoney.getLendmoneycode() + "????????????!");
            } else if (lendMoney.getChargebillflag()) {
                throw new RuntimeException("???????????????" + lendMoney.getLendmoneycode() + "????????????????????????!");
            }
        }

        WbUser user = UserThreadLocal.get();
        BudgetLendmoney updateLendMoney = new BudgetLendmoney();
        updateLendMoney.setChargebillflag(true);
        updateLendMoney.setEffectflag(true);
        updateLendMoney.setChargebillor(user.getUserName());
        updateLendMoney.setChargebillorname(user.getDisplayName());
        updateLendMoney.setChargebilltime(new Date());
        this.budgetLendmoneyMapper.update(updateLendMoney, new QueryWrapper<BudgetLendmoney>().in("id", ids));
    }

    /**
     * ????????????????????????
     */
    public List<List<String>> exportProjectLend(Long id) {
        List<List<String>> dataList = new ArrayList<>();
        List<BudgetProjectLendDetailVO> detailList = this.budgetProjectlendsumMapper.listLendMoneyDetail(null, id);
        detailList.forEach(v -> {
            List<String> rowList = new ArrayList<>();
            rowList.add(v.getLendMoneyCode());
            rowList.add(v.getEmpNo());
            rowList.add(v.getEmpName());
            rowList.add(v.getProjectName());
            rowList.add(v.getLendMoney().toString());
            rowList.add(v.getRepaidMoney().toString());
            rowList.add(v.getUnpaidMoney().toString());
            rowList.add(v.getInterestMoney().toString());
            rowList.add(Constants.FORMAT_10.format(v.getLendDate()));
            rowList.add(Constants.FORMAT_10.format(v.getPlanPayDate()));
            rowList.add("1".equals(v.getProjectLendType()) ? "??????" : "2".equals(v.getProjectLendType()) ? "??????" : "??????");
            dataList.add(rowList);
        });
        return dataList;
    }

    /**
     * ??????????????????
     */
    public List<BudgetLendInterestRule> listInterestRules(Long id) {
        return this.budgetLendInterestRuleMapper.selectList(new QueryWrapper<BudgetLendInterestRule>().eq("projectlendsumid", id));
    }

    /**
     * ??????????????????
     */
    public void addInterestRule(BudgetLendInterestRule insertRule) {
        BudgetProjectlendsum projectLend = this.budgetProjectlendsumMapper.selectById(insertRule.getProjectlendsumid());
        if (projectLend == null) {
            throw new RuntimeException("????????????????????????");
        }
        insertRule.setId(null);
        insertRule.setLendtype(LendTypeEnum.LEND_TYPE_13.getType());
        insertRule.setInterestrateduringtheperiod(insertRule.getInterestrateduringtheperiod().divide(new BigDecimal("100")));
        insertRule.setInterestrateouttheperiod(insertRule.getInterestrateouttheperiod().divide(new BigDecimal("100")));
        this.budgetLendInterestRuleMapper.insert(insertRule);
    }

    /**
     * ??????????????????
     */
    public void updateInterestRule(BudgetLendInterestRule updateRule) {
        BudgetLendInterestRule interestRule = this.budgetLendInterestRuleMapper.selectById(updateRule.getId());
        if (interestRule == null) {
            throw new RuntimeException("????????????????????????");
        }
        updateRule.setLendtype(interestRule.getLendtype());
        updateRule.setProjectlendsumid(interestRule.getProjectlendsumid());
        updateRule.setInterestrateduringtheperiod(updateRule.getInterestrateduringtheperiod().divide(new BigDecimal("100")));
        updateRule.setInterestrateouttheperiod(updateRule.getInterestrateouttheperiod().divide(new BigDecimal("100")));
        this.budgetLendInterestRuleMapper.updateById(updateRule);
    }

    /**
     * ??????????????????
     */
    public void deleteInterestRules(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        this.budgetLendInterestRuleMapper.deleteBatchIds(ids);
    }

    /**
     * ??????????????????
     */
    public Map<String, List<List<String>>> exportValidate(Long id) {
        BudgetProjectlendsum projectLend = this.budgetProjectlendsumMapper.selectById(id);
        if (projectLend == null) {
            throw new RuntimeException("????????????????????????");
        } else if (projectLend.getVerifyflag() != 1) {
            throw new RuntimeException("?????????????????????????????????");
        }

        List<List<String>> dataList = new ArrayList<>();
        List<BudgetLendmoney> lendMoneyList = this.budgetLendmoneyMapper.selectList(new QueryWrapper<BudgetLendmoney>()
                .eq("deleteflag", 0)
                .eq("projectlendsumid", id));
        lendMoneyList.forEach(v -> {
            List<String> rowList = new ArrayList<>();
            rowList.add(projectLend.getProjectno());
            rowList.add(projectLend.getProjectname());
            rowList.add(v.getEmpno());
            rowList.add(v.getEmpname());
            rowList.add(v.getLendmoney().toString());
            rowList.add(Constants.FORMAT_10.format(v.getLenddate()));
            rowList.add(Constants.FORMAT_10.format(v.getPlanpaydate()));
            rowList.add(v.getRemark());
            rowList.add("1".equals(v.getProjectlendtype()) ? "??????" : "2".equals(v.getProjectlendtype()) ? "??????" : "??????");
            rowList.add(v.getLendmoneycode());
            rowList.add(v.getFlushingflag() == null ? "" : (v.getFlushingflag() ? "???" : "???"));
            dataList.add(rowList);
        });
        Map<String, List<List<String>>> resultMap = new HashMap<>(2);
        resultMap.put(projectLend.getProjectname(), dataList);
        return resultMap;
    }

    /**
     * ??????????????????
     */
    public Map<String, List<List<String>>> exportInterest(Long id) {
        BudgetProjectlendsum projectLend = this.budgetProjectlendsumMapper.selectById(id);
        if (projectLend == null) {
            throw new RuntimeException("????????????????????????.");
        } else if (projectLend.getVerifyflag() != 1) {
            throw new RuntimeException("?????????????????????????????????");
        }

        List<List<String>> dataList = new ArrayList<>();
        List<BudgetLendmoney> lendMoneyList = this.budgetLendmoneyMapper.selectList(new QueryWrapper<BudgetLendmoney>()
                .eq("deleteflag", 0)
                .eq("projectlendsumid", id));
        lendMoneyList.forEach(v -> {
            List<String> rowList = new ArrayList<>();
            rowList.add(v.getEmpno());
            rowList.add(v.getEmpname());
            rowList.add("1".equals(v.getProjectlendtype()) ? "??????" : "2".equals(v.getProjectlendtype()) ? "??????" : "??????");
            rowList.add(v.getLendmoney().toString());
            dataList.add(rowList);
        });
        Map<String, List<List<String>>> resultMap = new HashMap<>(2);
        resultMap.put(projectLend.getProjectname(), dataList);
        return resultMap;
    }

    /**
     * ??????????????????
     */
    public List<List<String>> importValidateComplete(Long id, List<List<String>> excelDataList) {
        BudgetProjectlendsum projectLend = this.budgetProjectlendsumMapper.selectById(id);
        if (projectLend == null) {
            throw new RuntimeException("????????????????????????");
        } else if (projectLend.getVerifyflag() != 1) {
            throw new RuntimeException("???????????????????????????");
        }

        BudgetUnit budgetUnit = this.budgetUnitMapper.selectById(projectLend.getUnitid());
        WbUser user = UserThreadLocal.get();
        if (!"admin".equals(user.getUserId())) {
            String managers = budgetUnit.getManagers();
            if (!managers.contains(user.getUserId())) {
                throw new RuntimeException("?????????" + user.getDisplayName() + "(" + user.getUserName() + ")????????????????????????" + budgetUnit.getName() + "???????????????");
            }
        }

        HashMap<String, BudgetLendmoney> lendMoneyMap = new HashMap<>(5);
        List<BudgetLendmoney> projectLendList = this.budgetLendmoneyMapper.selectList(new QueryWrapper<BudgetLendmoney>()
                .eq("deleteflag", 0)
                .eq("projectlendsumid", id));
        projectLendList.forEach(v -> lendMoneyMap.put(v.getEmpno() + v.getProjectlendtype(), v));

        List<ExcelBean> errorList = new ArrayList<>();
        List<BudgetLendmoney> successList = new ArrayList<>();
        int size = excelDataList.size();
        for (int i = 0; i < size; i++) {
            // ??????????????????????????????
            if (i < 1) {
                continue;
            }
            validateComplete(lendMoneyMap, excelDataList.get(i), projectLend, errorList, successList);
        }
        if (errorList.isEmpty() && !successList.isEmpty()) {
            // ????????????????????????
            successList.forEach(this.budgetLendmoneyMapper::updateById);

            // ???????????????????????????
            List<BudgetLendmoney> flushingList = successList.stream().filter(BudgetLendmoney::getFlushingflag).collect(Collectors.toList());
            clearInterest(flushingList);
        }
        return ExcelBean.transformList(errorList);
    }

    private void validateComplete(HashMap<String, BudgetLendmoney> lendMoneyMap, List<String> row, BudgetProjectlendsum projectLend, List<ExcelBean> errorList, List<BudgetLendmoney> successList) {
        int totalColumn = 11;
        try {
            String empNo = null;
            String projectLendType = null;
            String projectLendTypeStr = null;
            Boolean flushingFlag = null;
            int columnSize = row.size();
            if (columnSize < totalColumn) {
                throw new RuntimeException("?????????????????????");
            }
            for (int i = 1; i <= columnSize; i++) {
                String data = row.get(i - 1);
                switch (i) {
                    case 1:
                        isNotBlank(data, "????????????");
                        if (!projectLend.getProjectno().equals(data)) {
                            throw new RuntimeException("?????????????????????????????????????????????????????????");
                        }
                        break;
                    case 2:
                        isNotBlank(data, "????????????");
                        if (!projectLend.getProjectname().equals(data)) {
                            throw new RuntimeException("?????????????????????????????????????????????????????????");
                        }
                        break;
                    case 3:
                        isNotBlank(data, "??????");
                        empNo = data;
                        break;
                    case 4:
                        isNotBlank(data, "??????");
                        this.wbUserService.validateUser(empNo, data);
                        break;
                    case 9:
                        projectLendType = getProjectLendType(data, projectLend.getType());
                        projectLendTypeStr = data;
                        break;
                    case 11:
                        isNotBlank(data, "??????/????????????");
                        if (!"???".equals(data) && !"???".equals(data)) {
                            throw new RuntimeException("??????/??????????????????'???'??????'???'");
                        }
                        flushingFlag = "???".equals(data);
                        break;
                    default:
                }
            }

            BudgetLendmoney lendMoney = lendMoneyMap.get(empNo + projectLendType);
            if (lendMoney == null) {
                throw new RuntimeException("?????????" + projectLend.getProjectname() + "???????????????" + empNo + "????????????" + projectLendTypeStr + "????????????!");
            } else if (lendMoney.getConfirmflag() == null || !lendMoney.getConfirmflag()) {
                throw new RuntimeException("?????????????????????!");
            } else if (lendMoney.getFlushingflag() != null) {
                if (lendMoney.getFlushingflag() && !flushingFlag) {
                    throw new RuntimeException("????????????????????????!");
                } else if (!lendMoney.getFlushingflag() && flushingFlag) {
                    throw new RuntimeException("???????????????????????????!");
                }
            }

            WbUser user = UserThreadLocal.get();
            BudgetLendmoney updateLendMoney = new BudgetLendmoney();
            updateLendMoney.setId(lendMoney.getId());
            updateLendMoney.setFlushingflag(flushingFlag);
            updateLendMoney.setFlushingor(user.getUserName());
            updateLendMoney.setFlushingorname(user.getDisplayName());
            updateLendMoney.setFlushtime(new Date());
            updateLendMoney.setEffectflag(true);
            if (flushingFlag) {
                // ??????????????????
                updateLendMoney.setInterestmoney(BigDecimal.ZERO);

                // ???????????????????????????
                updateLendMoney.setTempInterestMoney(lendMoney.getInterestmoney());
            } else {
                // ????????????????????????????????????
                updateLendMoney.setChargebillflag(true);
                updateLendMoney.setChargebillor(user.getUserName());
                updateLendMoney.setChargebillorname(user.getDisplayName());
                updateLendMoney.setChargebilltime(new Date());
            }
            successList.add(updateLendMoney);
        } catch (Exception e) {
            // ????????????: Transaction rolled back because it has been marked as rollback-only
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            errorList.add(ExcelBean.transformBean(row, totalColumn, e.getMessage()));
        }
    }

    private void isNotBlank(String data, String message) {
        if (StringUtils.isBlank(data)) {
            throw new RuntimeException(message + "????????????");
        }
    }

    private String getProjectLendType(String data, Integer type) {
        if (StringUtils.isBlank(data)) {
            throw new RuntimeException("??????????????????????????????");
        }
        if (type == 1 && !"??????".equals(data) && !"??????".equals(data) && !"??????".equals(data)) {
            throw new RuntimeException("???????????????????????????????????????????????????????????????");
        } else if (type == 2 && !"??????".equals(data)) {
            throw new RuntimeException("?????????????????????????????????????????????");
        }
        return "??????".equals(data) ? "1" : "??????".equals(data) ? "2" : "3";
    }

    /**
     * ????????????
     */
    public List<List<String>> importInterest(Long id, List<List<String>> excelDataList) {
        BudgetProjectlendsum projectLend = this.budgetProjectlendsumMapper.selectById(id);
        if (projectLend == null) {
            throw new RuntimeException("????????????????????????");
        }

        List<ExcelBean> errorList = new ArrayList<>();
        List<BudgetLendmoney> successList = new ArrayList<>();
        int size = excelDataList.size();
        for (int i = 0; i < size; i++) {
            // ??????????????????????????????
            if (i < 1) {
                continue;
            }
            validateInterest(projectLend, excelDataList.get(i), errorList, successList);
        }
        if (errorList.isEmpty()) {
            for (BudgetLendmoney lendMoney : successList) {
                BigDecimal oldInterestMoney = lendMoney.getInterestmoney();
                BigDecimal newInterestMoney = lendMoney.getTempInterestMoney();

                BudgetLendmoney updateLendMoney = new BudgetLendmoney();
                updateLendMoney.setId(lendMoney.getId());
                updateLendMoney.setInterestmoney(newInterestMoney);
                this.budgetLendmoneyMapper.updateById(updateLendMoney);

                BudgetArrears budgetArrears = this.budgetArrearsMapper.selectOne(new QueryWrapper<BudgetArrears>().eq("empno", lendMoney.getEmpno()));
                if (budgetArrears != null && newInterestMoney.compareTo(BigDecimal.ZERO) != 0) {
                    BudgetArrears updateArrears = new BudgetArrears();
                    updateArrears.setId(budgetArrears.getId());
                    updateArrears.setInterestmoney(budgetArrears.getInterestmoney().subtract(oldInterestMoney).add(newInterestMoney));
                    updateArrears.setArrearsmoeny(budgetArrears.getArrearsmoeny().subtract(oldInterestMoney).add(newInterestMoney));
                    this.budgetArrearsMapper.updateById(updateArrears);
                }
            }
        }
        return ExcelBean.transformList(errorList);
    }

    private void validateInterest(BudgetProjectlendsum projectLend, List<String> row, List<ExcelBean> errorList, List<BudgetLendmoney> successList) {
        int totalColumn = 5;
        try {
            String empNo = null;
            String projectLendType = null;
            String projectLendTypeStr = null;
            BigDecimal interestMoney = BigDecimal.ZERO;
            int columnSize = row.size();
            if (columnSize < totalColumn) {
                throw new RuntimeException("?????????????????????");
            }
            for (int i = 1; i <= columnSize; i++) {
                String data = row.get(i - 1);
                switch (i) {
                    case 1:
                        isNotBlank(data, "??????");
                        empNo = data;
                        break;
                    case 2:
                        isNotBlank(data, "??????");
                        this.wbUserService.validateUser(empNo, data);
                        break;
                    case 3:
                        projectLendType = getProjectLendType(data, projectLend.getType());
                        projectLendTypeStr = data;
                        break;
                    case 5:
                        isNotBlank(data, "??????");
                        try {
                            interestMoney = new BigDecimal(data);
                        } catch (Exception ignored) {
                            throw new RuntimeException("??????????????????");
                        }
                        break;
                    default:
                }
            }

            BudgetLendmoney lendMoney = this.budgetLendmoneyMapper.selectOne(new QueryWrapper<BudgetLendmoney>()
                    .eq("projectlendsumid", projectLend.getId())
                    .eq("empno", empNo)
                    .eq("projectlendtype", projectLendType));
            if (lendMoney == null) {
                throw new RuntimeException("?????????" + projectLend.getProjectname() + "???????????????" + empNo + "????????????" + projectLendTypeStr + "????????????!");
            } else if (lendMoney.getFlushingflag() != null && lendMoney.getFlushingflag()) {
                throw new RuntimeException("?????????" + projectLend.getProjectname() + "???????????????" + empNo + "????????????" + projectLendTypeStr + "??????????????????!");
            }

            // ????????????????????????????????????
            lendMoney.setTempInterestMoney(interestMoney);
            successList.add(lendMoney);
        } catch (Exception e) {
            // ????????????: Transaction rolled back because it has been marked as rollback-only
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            errorList.add(ExcelBean.transformBean(row, totalColumn, e.getMessage()));
        }
    }

    /**
     * ??????????????????????????????
     */
    public Map<String, List<List<String>>> exportRepayMoneyDetail(Long id) {
        BudgetProjectlendsum projectLend = this.budgetProjectlendsumMapper.selectById(id);
        if (projectLend == null) {
            throw new RuntimeException("????????????????????????");
        }

        List<List<String>> dataList = new ArrayList<>();
        List<BudgetProjectRepayDetailVO> list = this.budgetProjectlendsumMapper.listRepayMoneyDetail(null, id);
        list.forEach(v -> {
            List<String> row = new ArrayList<>();
            row.add(projectLend.getProjectname());
            row.add(v.getLendMoneyCode());
            row.add(v.getEmpNo());
            row.add(v.getEmpName());
            row.add(v.getLendMoney().toString());
            row.add(v.getRepaidMoney().toString());
            row.add(Constants.FORMAT_10.format(v.getCreateDate()));
            dataList.add(row);
        });
        Map<String, List<List<String>>> resultMap = new HashMap<>(2);
        resultMap.put(projectLend.getProjectname(), dataList);
        return resultMap;
    }

    /**
     * ??????????????????
     */
    public List<List<String>> importProjectLend(Long id, List<List<String>> excelDataList) {
        BudgetProjectlendsum projectLend = this.budgetProjectlendsumMapper.selectById(id);
        if (projectLend == null) {
            throw new RuntimeException("????????????????????????");
        } else if (projectLend.getVerifyflag() == 1) {
            throw new RuntimeException("???????????????????????????!");
        }

        List<BudgetLendmoney> successList = new ArrayList<>();
        List<ExcelBean> errorList = new ArrayList<>();
        int size = excelDataList.size();
        for (int i = 0; i < size; i++) {
            // ??????????????????????????????
            if (i < 1) {
                continue;
            }
            validateProjectLend(projectLend, successList, excelDataList.get(i), errorList);
        }

        if (errorList.isEmpty() && !successList.isEmpty()) {
            // ????????????????????????????????????????????????????????????
            long count = successList.stream().map(v -> v.getEmpno() + v.getProjectlendtype()).distinct().count();
            if (count != successList.size()) {
                throw new RuntimeException("??????????????????????????????????????????????????????!");
            }

            // ??????????????????????????????????????????????????????
            List<BudgetLendmoney> existLendList = this.budgetLendmoneyMapper.selectList(new QueryWrapper<BudgetLendmoney>()
                    .eq("deleteflag", 0)
                    .eq("projectlendsumid", projectLend.getId()));
            BigDecimal totalMoney = existLendList.stream().map(BudgetLendmoney::getLendmoney).reduce(BigDecimal.ZERO, BigDecimal::add);

            Date currentDate = new Date();
            for (BudgetLendmoney lendMoney : successList) {
                // ???????????????????????????
                BudgetLendmoney existLend = this.budgetLendmoneyMapper.selectOne(new QueryWrapper<BudgetLendmoney>()
                        .eq("projectlendsumid", lendMoney.getProjectlendsumid())
                        .eq("projectlendtype", lendMoney.getProjectlendtype())
                        .eq("empno", lendMoney.getEmpno()));
                if (existLend == null) {
                    totalMoney = totalMoney.add(lendMoney.getLendmoney());
                } else {
                    lendMoney.setId(existLend.getId());
                    lendMoney.setDeleteflag(false);

                    if (!existLend.getDeleteflag()) {
                        totalMoney = totalMoney.subtract(existLend.getLendmoney()).add(lendMoney.getLendmoney());
                    }
                }
            }
            if (projectLend.getTotal().compareTo(totalMoney) < 0) {
                throw new RuntimeException("??????????????????????????????????????????");
            }
            // ??????????????????????????????????????????????????????????????????????????????
            validateDetailMoney(projectLend, successList);

            for (BudgetLendmoney lendMoney : successList) {
                try {
                    if (lendMoney.getId() == null) {
                        // ??????
                        lendMoney.setYearid(projectLend.getYearid());
                        lendMoney.setLendmoneycode(this.distributedNumber.getLendNum());
                        lendMoney.setLendtype(LendTypeEnum.LEND_TYPE_13.getType());
                        lendMoney.setDeleteflag(false);
                        lendMoney.setCreatetime(currentDate);
                        lendMoney.setChargebillflag(false);
                        lendMoney.setConfirmflag(null);
                        lendMoney.setInterestmoney(BigDecimal.ZERO);
                        lendMoney.setRepaidmoney(BigDecimal.ZERO);

                        WbPerson wbPerson = this.wbPersonMapper.selectOne(new QueryWrapper<WbPerson>().eq("user_id", lendMoney.getEmpid()));
                        WbDept dept = this.wbDeptMapper.selectById(wbPerson.getDeptId());
                        lendMoney.setDeptid(dept.getDeptId());
                        lendMoney.setDeptname(dept.getDeptName());

                        WbUser operator = this.wbUserService.getByEmpNo(projectLend.getCreator());
                        lendMoney.setOperatorEmpId(operator.getUserId());
                        lendMoney.setOperatorEmpNo(operator.getUserName());
                        lendMoney.setOperatorEmpName(operator.getDisplayName());

                        this.budgetLendmoneyMapper.insert(lendMoney);
                    } else {
                        this.budgetLendmoneyMapper.updateById(lendMoney);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e.getMessage().contains("Duplicate entry")) {
                        throw new RuntimeException("?????????" + lendMoney.getEmpname() + "(" + lendMoney.getEmpno() + ")???????????????????????????????????????????????????????????????????????????");
                    }
                    throw e;
                }
            }
        }
        return ExcelBean.transformList(errorList);
    }

    private void validateProjectLend(BudgetProjectlendsum projectLend, List<BudgetLendmoney> successList, List<String> row, List<ExcelBean> errorList) {
        int totalColumn = 7;
        try {
            int columnSize = row.size();
            if (columnSize < totalColumn) {
                throw new RuntimeException("?????????????????????");
            }
            BudgetLendmoney lendMoney = new BudgetLendmoney();
            lendMoney.setProjectlendsumid(projectLend.getId());

            for (int i = 1; i <= columnSize; i++) {
                String data = row.get(i - 1);
                switch (i) {
                    case 1:
                        isNotBlank(data, "??????");
                        lendMoney.setEmpno(data);
                        break;
                    case 2:
                        isNotBlank(data, "??????");
                        WbUser user = this.wbUserService.validateUser(lendMoney.getEmpno(), data);
                        lendMoney.setEmpid(user.getUserId());
                        lendMoney.setEmpname(user.getDisplayName());
                        break;
                    case 3:
                        isNotBlank(data, "????????????");
                        try {
                            BigDecimal money = new BigDecimal(data);
                            if (BigDecimal.ZERO.compareTo(money) > 0) {
                                throw new RuntimeException("?????????????????????????????????");
                            }
                            lendMoney.setLendmoney(money);
                        } catch (Exception ignored) {
                            throw new RuntimeException("????????????????????????");
                        }
                        break;
                    case 4:
                        isNotBlank(data, "????????????");
                        try {
                            Date lendDate = Constants.FORMAT_10.parse(data);
                            lendMoney.setLenddate(lendDate);
                        } catch (Exception ignored) {
                            throw new RuntimeException("????????????????????????");
                        }
                        break;
                    case 5:
                        isNotBlank(data, "??????????????????");
                        try {
                            Date planPayDate = Constants.FORMAT_10.parse(data);
                            lendMoney.setPlanpaydate(planPayDate);
                        } catch (Exception ignored) {
                            throw new RuntimeException("??????????????????????????????");
                        }
                        if (lendMoney.getPlanpaydate().compareTo(lendMoney.getLenddate()) <= 0) {
                            throw new RuntimeException("???????????????????????????????????????!");
                        }
                        break;
                    case 6:
                        isNotBlank(data, "????????????");
                        lendMoney.setRemark(data);
                        break;
                    case 7:
                        String projectLendType = getProjectLendType(data, projectLend.getType());
                        lendMoney.setProjectlendtype(projectLendType);
                        break;
                    default:
                }
            }
            successList.add(lendMoney);
        } catch (Exception e) {
            // ????????????: Transaction rolled back because it has been marked as rollback-only
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            errorList.add(ExcelBean.transformBean(row, totalColumn, e.getMessage()));
        }
    }

    // ----------------------------------------------------------------------------------------------------

    /**
     * ????????????????????????
     */
    public List<BudgetBillingUnit> curUserInvoiceUnit() {
        return this.budgetBillingUnitMapper.selectList(new QueryWrapper<BudgetBillingUnit>()
                .eq("stopflag", 0)
                .orderByAsc("orderno"));
    }

    /**
     * ??????????????????????????????
     */
    public List<PaymentUnitVO> curUserPaymentUnitAccount(List<String> ids) {
        if (ids != null && !ids.isEmpty()) {
            return this.budgetBillingUnitMapper.curUserPaymentUnitAccount(String.join(",", ids));
        }
        return null;
    }

    /**
     * ???????????????????????????
     */
    public List<BxMonthAgentVO> listMonthAgentByBx(Long yearId, Long budgetUnitId, Long monthId) {
        return this.budgetMonthAgentMapper.listMonthAgentByBx(yearId, budgetUnitId, monthId);
    }

    /**
     * ??????????????????????????????
     */
    public List<BudgetProjectlendbxpayment> projectLendBxPaymentDetail(Long id) {
        List<BudgetProjectlendbxpayment> bxPaymentList = this.budgetProjectlendbxpaymentMapper.selectList(new QueryWrapper<BudgetProjectlendbxpayment>().eq("projectlendsumid", id));
        if (bxPaymentList.isEmpty()) {
            List<BudgetLendmoney> lendMoneyList = this.budgetLendmoneyMapper.selectList(new QueryWrapper<BudgetLendmoney>()
                    .eq("deleteflag", 0)
                    .eq("flushingflag", 1)
                    .eq("projectlendsumid", id)
                    .gt("lendmoney-repaidmoney", 0));

            lendMoneyList.forEach(lm -> {
                BudgetProjectlendbxpayment payment = new BudgetProjectlendbxpayment();
                payment.setLendmoneyid(lm.getId());
                payment.setProjectlendsumid(id);
                payment.setEmpno(lm.getEmpno());
                payment.setLendmoneyname(lm.getEmpname() + "(" + lm.getEmpno() + ")");
                payment.setLendcode(lm.getLendmoneycode());
                payment.setLendmoney(lm.getLendmoney());
                payment.setUnrepaidmoney(lm.getLendmoney().subtract(lm.getRepaidmoney()));
                payment.setLendmoneyremark(lm.getRemark());
                payment.setPaymentmoney(payment.getUnrepaidmoney());
                bxPaymentList.add(payment);
            });
        }
        return bxPaymentList;
    }

    /**
     * ??????????????????????????????
     */
    public List<BudgetProjectlendbxtrans> projectLendBxTransDetail(Long id) {
        List<BudgetProjectlendbxtrans> bxTransList = this.budgetProjectlendbxtransMapper.selectList(new QueryWrapper<BudgetProjectlendbxtrans>().eq("projectlendsumid", id));
        if (bxTransList.isEmpty()) {
            List<BudgetLendmoney> lendMoneyList = this.budgetLendmoneyMapper.selectList(new QueryWrapper<BudgetLendmoney>()
                    .eq("deleteflag", 0)
                    .eq("flushingflag", 1)
                    .eq("chargebillflag", 1)
                    .eq("projectlendsumid", id));

            if (!lendMoneyList.isEmpty()) {
                List<String> ids = lendMoneyList.stream().map(e -> e.getId().toString()).collect(Collectors.toList());
                List<BudgetRepaymoneyDetail> repayList = this.budgetRepaymoneyDetailMapper.selectList(new QueryWrapper<BudgetRepaymoneyDetail>().in("lendmoneyid", ids));
                if (!repayList.isEmpty()) {
                    repayList.forEach(e -> {
                        BudgetRepaymoney repayMoney = this.budgetRepaymoneyMapper.selectById(e.getRepaymoneyid());
                        if (repayMoney != null) {
                            BudgetProjectlendbxtrans trans = new BudgetProjectlendbxtrans();
                            trans.setProjectlendsumid(id);
                            trans.setPayeecode(repayMoney.getEmpno());
                            trans.setPayeename(repayMoney.getEmpname());
                            trans.setTransmoney(repayMoney.getRepaymoney());
                            trans.setPayeebankaccount("???");
                            BudgetBankAccount bankAccount = this.budgetBankAccountMapper.selectOne(new QueryWrapper<BudgetBankAccount>()
                                    .eq("stopflag", 0)
                                    .eq("code", repayMoney.getEmpno())
                                    .orderByDesc("wagesflag")
                                    .last("limit 1"));
                            if (bankAccount == null) {
                                throw new RuntimeException("?????????" + repayMoney.getEmpname() + "(" + repayMoney.getEmpno() + ")???????????????");
                            }
                            trans.setPayeebankaccount(bankAccount.getBankaccount());
                            WbBanks bank = this.wbBanksMapper.selectOne(new QueryWrapper<WbBanks>().eq("sub_branch_code", bankAccount.getBranchcode()));
                            if (bank == null) {
                                throw new RuntimeException("?????????" + repayMoney.getEmpname() + "(" + repayMoney.getEmpno() + ")??????????????????");
                            }
                            trans.setPayeebankname(bank.getSubBranchName());
                            bxTransList.add(trans);
                        }
                    });
                }
            }
        }
        return bxTransList;
    }

    /**
     * ????????????????????????
     */
    public List<BudgetProjectlendbxdetail> projectLendBxDetail(Long id) {
        return this.budgetProjectlendbxdetailMapper.selectList(new QueryWrapper<BudgetProjectlendbxdetail>().eq("projectlendsumid", id));
    }

    /**
     * ???????????????????????????
     */
    public void saveReimbursementData(ProjectLendReimbursementDTO bean) throws Exception {
        boolean isUpdate = false;
        BudgetProjectlendsum projectLendSum = this.budgetProjectlendsumMapper.selectById(bean.getProjectLendSumId());
        if (projectLendSum == null) {
            throw new RuntimeException("????????????????????????");
        } else if (projectLendSum.getBxorderid() != null) {
            BudgetReimbursementorder order = this.budgetReimbursementorderMapper.selectById(projectLendSum.getBxorderid());
            if (order != null) {
                // ???????????????-1????????????0????????????1??????????????????????????????2???????????????
                if (order.getReuqeststatus() == 1) {
                    throw new RuntimeException("???????????????????????????, ???????????????");
                } else if (order.getReuqeststatus() == 2) {
                    throw new RuntimeException("???????????????????????????, ?????????????????????");
                }
                isUpdate = true;
            }
        }

        List<BudgetLendmoney> lendMoneyList = this.budgetLendmoneyMapper.selectList(new QueryWrapper<BudgetLendmoney>()
                .eq("deleteflag", 0)
                .eq("projectlendsumid", projectLendSum.getId()));
        long count = lendMoneyList.stream().filter(v -> v.getFlushingflag() == null).count();
        if (count > 0) {
            throw new RuntimeException("???????????????????????????????????????");
        }

        BudgetMonthEndUnit monthEndUnit = this.budgetMonthEndUnitMapper.selectOne(new QueryWrapper<BudgetMonthEndUnit>()
                .eq("unitid", projectLendSum.getUnitid())
                .eq("monthid", bean.getMonthId()));
        if (monthEndUnit == null) {
            throw new RuntimeException("???" + bean.getMonthId() + "??????????????????????????????");
        } else if (monthEndUnit.getRequeststatus() != 2) {
            throw new RuntimeException("???" + bean.getMonthId() + "????????????????????????????????????");
        } else if (monthEndUnit.getMonthendflag()) {
            throw new RuntimeException("???" + bean.getMonthId() + "???????????????????????????");
        }

        BigDecimal bxTotal = saveBxData(projectLendSum, bean);
        BigDecimal czTotal = saveCzData(bean.getCzList());
        BigDecimal zzTotal = saveZzData(projectLendSum.getId(), bean.getZzList());
        if (bean.getIsSubmit()) {
            // ?????? ???????????? == ???????????? + ????????????
            if (bxTotal.compareTo(czTotal.add(zzTotal)) != 0) {
                throw new RuntimeException("???????????? + ???????????? ????????? ????????????");
            }

            // ??????????????????????????????
            checkBxEnough(projectLendSum.getId());
            // ??????
            generateBxOrder(projectLendSum, isUpdate);
        }
    }

    private void generateBxOrder(BudgetProjectlendsum sum, boolean isUpdate) throws Exception {
        WbUser user = UserThreadLocal.get();
        // ???????????????
        BudgetReimbursementorder bxOrder = createBudgetReimbursementOrder(user.getUserId(), user.getDisplayName(), sum.getId());

        ReimbursementRequest request = new ReimbursementRequest();
        request.setOrderDetail(createBxDetail(sum.getId(), bxOrder));
        request.setOrderPayment(createBxPayment(sum.getId(), bxOrder));
        request.setOrderTrans(createBxTrans(sum.getId(), bxOrder));
        request.setOrderAllocated(new ArrayList<>());
        request.setOrder(bxOrder);
        request.setIsProjectBx(true);
        String result;
        if (isUpdate) {
            result = this.reimbursementWorker.update(request, true);
        } else {
            result = this.reimbursementWorker.save(request, true);
        }
        if (StringUtils.isNoneBlank(result)) {
            throw new RuntimeException(result);
        }

        sum.setSubmitbxstatus(1);
        sum.setSubmitorid(user.getUserName());
        sum.setSubmitorname(user.getDisplayName());
        sum.setBxorderid(bxOrder.getId());
        this.budgetProjectlendsumMapper.updateById(sum);
    }

    private List<BudgetReimbursementorderTrans> createBxTrans(Long sumId, BudgetReimbursementorder bxOrder) {
        List<BudgetReimbursementorderTrans> list = new ArrayList<>();
        List<BudgetProjectlendbxtrans> transList = this.budgetProjectlendbxtransMapper.selectList(new QueryWrapper<BudgetProjectlendbxtrans>().eq("projectlendsumid", sumId));
        if (!transList.isEmpty()) {
            for (BudgetProjectlendbxtrans trans : transList) {
                BudgetReimbursementorderTrans bxTrans = new BudgetReimbursementorderTrans();
                bxTrans.setReimbursementid(bxOrder.getId());
                bxTrans.setPayeecode(trans.getPayeecode());
                bxTrans.setPayeename(trans.getPayeename());
                bxTrans.setPayeebankaccount(trans.getPayeebankaccount());
                bxTrans.setPayeebankname(trans.getPayeebankname());
                bxTrans.setTransmoney(trans.getTransmoney());
                bxTrans.setOlddraweeunitaccountid(trans.getDraweeunitaccountid());
                bxTrans.setDraweeunitaccountid(trans.getDraweeunitaccountid());
                bxTrans.setDraweeunitname(trans.getDraweeunitname());
                bxTrans.setDraweebankaccount(trans.getDraweebankaccount());
                bxTrans.setDraweebankname(trans.getDraweebankname());
                list.add(bxTrans);
            }
            bxOrder.setTransmoney(transList.stream().map(BudgetProjectlendbxtrans::getTransmoney).reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        return list;
    }

    private List<BudgetReimbursementorderPayment> createBxPayment(Long sumId, BudgetReimbursementorder bxOrder) {
        List<BudgetReimbursementorderPayment> list = new ArrayList<>();
        List<BudgetProjectlendbxpayment> paymentList = this.budgetProjectlendbxpaymentMapper.selectList(new QueryWrapper<BudgetProjectlendbxpayment>().eq("projectlendsumid", sumId));
        if (!paymentList.isEmpty()) {
            for (BudgetProjectlendbxpayment payment : paymentList) {
                BudgetReimbursementorderPayment bxPayment = new BudgetReimbursementorderPayment();
                bxPayment.setReimbursementid(bxOrder.getId());
                bxPayment.setLendmoneyid(payment.getLendmoneyid());
                bxPayment.setLendmoneycode(payment.getEmpno());
                bxPayment.setLendmoneyname(payment.getLendmoneyname());
                bxPayment.setLendcode(payment.getLendcode());
                bxPayment.setLendmoney(payment.getLendmoney());
                bxPayment.setUnrepaidmoney(payment.getUnrepaidmoney());
                bxPayment.setLendmoneyremark(payment.getLendmoneyremark());
                bxPayment.setPaymentmoney(payment.getPaymentmoney());
                list.add(bxPayment);
            }
            bxOrder.setPaymentmoney(paymentList.stream().map(BudgetProjectlendbxpayment::getPaymentmoney).reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        return list;
    }

    private List<BudgetReimbursementorderDetail> createBxDetail(Long sumId, BudgetReimbursementorder bxOrder) {
        List<BudgetReimbursementorderDetail> list = new ArrayList<>();
        List<BudgetProjectlendbxdetail> detailList = this.budgetProjectlendbxdetailMapper.selectList(new QueryWrapper<BudgetProjectlendbxdetail>().eq("projectlendsumid", sumId));
        if (!detailList.isEmpty()) {
            for (BudgetProjectlendbxdetail detail : detailList) {
                BudgetReimbursementorderDetail bxDetail = new BudgetReimbursementorderDetail();
                bxDetail.setReimbursementid(bxOrder.getId());
                bxDetail.setMonthagentid(detail.getMonthagentid());
                bxDetail.setReimmoney(detail.getReimmoney());
                bxDetail.setMonthagentname(detail.getMonthagentname());
                bxDetail.setRemark(detail.getRemark());
                bxDetail.setSubjectname(detail.getSubjectname());
                bxDetail.setBunitid(detail.getBunitid());
                bxDetail.setBunitname(detail.getBunitname());
                bxDetail.setMonthagentmoney(detail.getMonthagentmoney());
                bxDetail.setReimflag(true);
                bxDetail.setMonthagentunmoney(detail.getMonthagentunmoney());
                bxDetail.setYearagentmoney(detail.getYearagentmoney());
                bxDetail.setYearagentunmoney(detail.getYearagentunmoney());
                list.add(bxDetail);
            }
            bxOrder.setReimmoney(detailList.stream().map(BudgetProjectlendbxdetail::getReimmoney).reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        return list;
    }

    private BudgetReimbursementorder createBudgetReimbursementOrder(String userid, String username, Long lendSumId) {
        Date currentDate = new Date();
        BudgetReimbursementorder reimbursementOrder = new BudgetReimbursementorder();

        BudgetProjectlendsum sum = this.budgetProjectlendsumMapper.selectById(lendSumId);
        if (sum.getBxorderid() != null) {
            BudgetReimbursementorder order = this.budgetReimbursementorderMapper.selectById(sum.getBxorderid());
            if (order != null) {
                reimbursementOrder.setId(order.getId());
            }
        }
        reimbursementOrder.setInterimbatch(null);
        reimbursementOrder.setOrderscrtype(ReimbursementFromEnmu.PROJECT.getCode());
        reimbursementOrder.setYearid(sum.getYearid());
        reimbursementOrder.setUnitid(sum.getUnitid());
        reimbursementOrder.setMonthid(sum.getMonthid());
        reimbursementOrder.setReimperonsid(sum.getBxuserid());
        reimbursementOrder.setReimperonsname(sum.getBxusername());
        reimbursementOrder.setReimdate(sum.getBxdate());
        reimbursementOrder.setReimmoney(BigDecimal.ZERO);
        reimbursementOrder.setNonreimmoney(BigDecimal.ZERO);
        reimbursementOrder.setPaymentmoney(BigDecimal.ZERO);
        reimbursementOrder.setTransmoney(BigDecimal.ZERO);
        reimbursementOrder.setCashmoney(BigDecimal.ZERO);
        reimbursementOrder.setAllocatedmoney(BigDecimal.ZERO);
        reimbursementOrder.setOthermoney(BigDecimal.ZERO);
        reimbursementOrder.setAttachcount(0);
        reimbursementOrder.setVersion("0");
        reimbursementOrder.setHandleflag(false);
        reimbursementOrder.setSubmittime(currentDate);
        reimbursementOrder.setCreatetime(currentDate);
        reimbursementOrder.setUpdatetime(currentDate);
        reimbursementOrder.setApplicantid(userid);
        reimbursementOrder.setApplicantame(username);
        reimbursementOrder.setApplicanttime(currentDate);
        reimbursementOrder.setBxtype(1);
        reimbursementOrder.setReceivestatus(0);
        reimbursementOrder.setReuqeststatus(1);
        return reimbursementOrder;
    }

    private void checkBxEnough(Long lendSumId) {
        List<BudgetProjectlendbxdetail> bxDetailList = this.budgetProjectlendbxdetailMapper.selectList(new QueryWrapper<BudgetProjectlendbxdetail>().eq("projectlendsumid", lendSumId));
        if (!bxDetailList.isEmpty()) {
            List<BudgetAgentExecuteView> reimExecuteList = this.budgetAgentExecuteViewMapper.selectList(new QueryWrapper<BudgetAgentExecuteView>()
                    .eq("reimflag", 1)
                    .eq("reuqeststatus", 1));

            // ????????????????????????
            Map<String, List<BudgetProjectlendbxdetail>> bxDetailBySubjectNameMap = bxDetailList.stream().collect(Collectors.groupingBy(BudgetProjectlendbxdetail::getSubjectname));
            bxDetailBySubjectNameMap.forEach((subjectName, detailListByName) -> {
                // ??????????????????????????????
                BigDecimal totalReimMoney = detailListByName.stream().map(BudgetProjectlendbxdetail::getReimmoney).reduce(BigDecimal.ZERO, BigDecimal::add);

                BudgetMonthAgent monthAgent = this.budgetMonthAgentMapper.selectById(detailListByName.get(0).getMonthagentid());
                Long unitId = monthAgent.getUnitid();
                Long subjectId = monthAgent.getSubjectid();

                BudgetUnitSubject unitSubject = this.budgetUnitSubjectMapper.selectOne(new QueryWrapper<BudgetUnitSubject>()
                        .eq("unitid", monthAgent.getUnitid())
                        .eq("subjectid", monthAgent.getSubjectid()));

                // ????????????
                BigDecimal usedYearMoney = BigDecimal.ZERO;
                if (unitSubject.getYearcontrolflag()) {
                    usedYearMoney = getSubjectLockedMoney(unitId, subjectId, null);
                }
                // ????????????
                BigDecimal usedMonthSubjectLockedMoney = BigDecimal.ZERO;
                if (unitSubject.getMonthcontrolflag()) {
                    //?????????????????????????????????
                    usedMonthSubjectLockedMoney = getSubjectLockedMoney(unitId, subjectId, monthAgent.getMonthid());
                }
                // ??????????????????
                BigDecimal usedYearSubjectLockedMoney = BigDecimal.ZERO;
                if (unitSubject.getYearsubjectcontrolflag()) {
                    usedYearSubjectLockedMoney = reimExecuteList.stream().filter(e -> subjectId.equals(e.getSubjectid()) && unitId.equals(e.getUnitid()))
                            .map(BudgetAgentExecuteView::getExecutemoney)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                }

                // ??????????????????
                BudgetYearSubject yearSubject = this.budgetYearSubjectMapper.selectOne(new QueryWrapper<BudgetYearSubject>()
                        .eq("yearid", monthAgent.getYearid())
                        .eq("unitid", monthAgent.getUnitid())
                        .eq("subjectid", monthAgent.getSubjectid()));
                BigDecimal yearSubjectUnMoney = yearSubject.getTotal()
                        .add(yearSubject.getAddmoney())
                        .add(yearSubject.getLendinmoney())
                        .subtract(yearSubject.getLendoutmoney())
                        .subtract(yearSubject.getExecutemoney());

                for (BudgetProjectlendbxdetail detail : detailListByName) {
                    BigDecimal bxMoney = detail.getReimmoney();
                    // ??????????????????
                    BigDecimal yearAgentUnMoney = detail.getYearagentunmoney();
                    // ??????????????????
                    BigDecimal monthSubjectUnMoney = detail.getMonthagentunmoney();

                    if (bxMoney.add(usedYearMoney).compareTo(yearAgentUnMoney) > 0) {
                        throw new RuntimeException("?????????" + detail.getMonthagentname() + "??????????????????" + NumberUtil.subZeroAndDot(yearAgentUnMoney)
                                + "???????????????????????????" + NumberUtil.subZeroAndDot(bxMoney) + "???!"
                                + "????????????:???" + NumberUtil.subZeroAndDot(usedYearMoney) + "???");
                    } else if (totalReimMoney.add(usedMonthSubjectLockedMoney).compareTo(monthSubjectUnMoney) > 0) {
                        throw new RuntimeException("?????????" + detail.getSubjectname() + "??????????????????" + NumberUtil.subZeroAndDot(monthSubjectUnMoney)
                                + "???????????????????????????" + NumberUtil.subZeroAndDot(totalReimMoney) + "???!"
                                + "????????????:???" + NumberUtil.subZeroAndDot(usedMonthSubjectLockedMoney) + "???");
                    } else if (totalReimMoney.add(usedYearSubjectLockedMoney).compareTo(yearSubjectUnMoney) > 0) {
                        throw new RuntimeException("?????????" + detail.getSubjectname() + "??????????????????" + NumberUtil.subZeroAndDot(yearSubjectUnMoney)
                                + "???????????????????????????" + NumberUtil.subZeroAndDot(totalReimMoney) + "???!"
                                + "????????????:???" + NumberUtil.subZeroAndDot(usedYearSubjectLockedMoney) + "???");
                    }
                }
            });
        }
    }

    private BigDecimal getSubjectLockedMoney(Long unitId, Long subjectId, Long monthId) {
        // ??????????????????????????????(monthId = null ??????????????????????????????)
        List<BudgetReimbursementorderDetail> details = this.budgetReimbursementorderDetailMapper.listDetailByMonthId(unitId, subjectId, monthId);
        BigDecimal bxUsedMoney = details.stream().map(BudgetReimbursementorderDetail::getReimmoney).reduce(BigDecimal.ZERO, BigDecimal::add);

        // ??????????????????????????????(monthId = null ??????????????????????????????)
        List<BudgetReimbursementorderAllocated> allocatedList = this.budgetReimbursementorderAllocatedMapper.listDetailByMonthId(unitId, subjectId, monthId);
        BigDecimal hbUsedMoney = allocatedList.stream().map(BudgetReimbursementorderAllocated::getAllocatedmoney).reduce(BigDecimal.ZERO, BigDecimal::add);

        return bxUsedMoney.add(hbUsedMoney);
    }

    private BigDecimal saveZzData(Long projectLendSumId, List<BudgetProjectlendbxtrans> zzList) {
        BigDecimal zzTotal = BigDecimal.ZERO;
        if (zzList != null && !zzList.isEmpty()) {
            List<BudgetProjectlendbxdetail> bxDetailList = this.budgetProjectlendbxdetailMapper.selectList(new QueryWrapper<BudgetProjectlendbxdetail>()
                    .eq("projectlendsumid", projectLendSumId));
            List<String> bUnitIds = bxDetailList.stream().filter(v -> v.getBunitid() != null).map(v -> v.getBunitid().toString()).distinct().collect(Collectors.toList());
            List<PaymentUnitVO> paymentUnits = this.curUserPaymentUnitAccount(bUnitIds);
            if (paymentUnits == null || paymentUnits.isEmpty()) {
                throw new RuntimeException("?????????????????????????????????????????????");
            }
            Set<Long> unitAccountIds = paymentUnits.stream().map(PaymentUnitVO::getUnitAccountId).collect(Collectors.toSet());

            for (BudgetProjectlendbxtrans v : zzList) {
                if (!unitAccountIds.contains(v.getDraweeunitaccountid())) {
                    throw new RuntimeException("?????????????????????????????????????????????" + v.getDraweeunitname() + "(" + v.getDraweebankaccount() + ")???");
                }
                if (v.getId() == null) {
                    this.budgetProjectlendbxtransMapper.insert(v);
                } else {
                    this.budgetProjectlendbxtransMapper.updateById(v);
                }
                // ??????????????????
                zzTotal = zzTotal.add(v.getTransmoney());
            }
        }
        return zzTotal;
    }

    private BigDecimal saveCzData(List<BudgetProjectlendbxpayment> czList) {
        BigDecimal czTotal = BigDecimal.ZERO;
        if (czList != null && !czList.isEmpty()) {
            for (BudgetProjectlendbxpayment v : czList) {
                BudgetProjectlendbxpayment bxPayment = this.budgetProjectlendbxpaymentMapper.selectById(v.getId());
                if (bxPayment == null) {
                    this.budgetProjectlendbxpaymentMapper.insert(v);
                }
                // ??????????????????
                czTotal = czTotal.add(v.getPaymentmoney());
            }
        }
        return czTotal;
    }

    private BigDecimal saveBxData(BudgetProjectlendsum sum, ProjectLendReimbursementDTO bean) {
        BudgetProjectlendsum updateProjectLendSum = new BudgetProjectlendsum();
        updateProjectLendSum.setId(sum.getId());
        updateProjectLendSum.setSubmitbxstatus(0);
        updateProjectLendSum.setBxuserid(bean.getBxUserId());
        updateProjectLendSum.setBxusername(bean.getBxUserName());
        updateProjectLendSum.setBxdate(bean.getBxDate());
        updateProjectLendSum.setMonthid(bean.getMonthId());
        this.budgetProjectlendsumMapper.updateById(updateProjectLendSum);

        // ????????????
        if (bean.getUpdateList() != null && !bean.getUpdateList().isEmpty()) {
            bean.getUpdateList().forEach(detail -> {
                detail.setProjectlendsumid(sum.getId());
                if (detail.getId() == null) {
                    this.budgetProjectlendbxdetailMapper.insert(detail);
                } else {
                    this.budgetProjectlendbxdetailMapper.updateById(detail);
                }
            });
        }
        if (bean.getDeleteList() != null && !bean.getDeleteList().isEmpty()) {
            this.budgetProjectlendbxdetailMapper.deleteBatchIds(bean.getDeleteList());
        }

        // ????????????
        BigDecimal bxTotal = BigDecimal.ZERO;
        List<BudgetProjectlendbxdetail> bxDetailList = this.budgetProjectlendbxdetailMapper.selectList(new QueryWrapper<BudgetProjectlendbxdetail>().eq("projectlendsumid", sum.getId()));
        if (!bxDetailList.isEmpty()) {
            for (BudgetProjectlendbxdetail detail : bxDetailList) {
                // ?????????????????????
                BudgetMonthAgent monthAgent = this.budgetMonthAgentMapper.selectById(detail.getMonthagentid());
                if (monthAgent == null) {
                    throw new RuntimeException("?????????????????????");
                } else if (!monthAgent.getYearid().equals(sum.getYearid())) {
                    throw new RuntimeException("???????????????" + monthAgent.getName() + "?????????????????????????????????????????????");
                } else if (!monthAgent.getUnitid().equals(sum.getUnitid())) {
                    throw new RuntimeException("???????????????" + monthAgent.getName() + "?????????????????????????????????????????????????????????");
                }
                // ?????????????????????????????????id
                BudgetMonthSubject monthSubject = this.budgetMonthSubjectMapper.selectOne(new QueryWrapper<BudgetMonthSubject>()
                        .eq("yearid", sum.getYearid())
                        .eq("unitid", sum.getUnitid())
                        .eq("monthid", bean.getMonthId())
                        .eq("subjectid", monthAgent.getSubjectid()));
                // ????????????????????????
                BigDecimal monthAgentUnMoney = monthSubject.getTotal()
                        .add(monthSubject.getAddmoney())
                        .add(monthSubject.getLendinmoney())
                        .subtract(monthSubject.getLendoutmoney())
                        .subtract(monthSubject.getExecutemoney());
                detail.setMonthagentmoney(monthSubject.getTotal());
                detail.setMonthagentunmoney(monthAgentUnMoney);
                // ????????????????????????
                BudgetYearAgent yearAgent = this.budgetYearAgentMapper.selectById(monthAgent.getYearagentid());
                BigDecimal yearAgentUnMoney = yearAgent.getTotal()
                        .add(yearAgent.getAddmoney())
                        .add(yearAgent.getLendinmoney())
                        .subtract(yearAgent.getLendoutmoney())
                        .subtract(yearAgent.getExecutemoney());
                detail.setYearagentmoney(yearAgent.getTotal());
                detail.setYearagentunmoney(yearAgentUnMoney);
                this.budgetProjectlendbxdetailMapper.updateById(detail);

                // ??????????????????
                bxTotal = bxTotal.add(detail.getReimmoney());
            }
        }
        return bxTotal;
    }

}
