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
     * 查询项目借款（分页）
     */
    public PageResult<BudgetProjectLendSumVO> listProjectLendPage(Integer page, Integer rows, HashMap<String, Object> paramMap) {
        Page<BudgetProjectLendSumVO> pageBean = new Page<>(page, rows);
        List<BudgetProjectLendSumVO> resultList = this.budgetProjectlendsumMapper.listProjectLendPage(pageBean, paramMap);
        resultList.forEach(v -> {
            if (v.getBxStatus() != null && v.getBxDate() != null) {
                // 报销状态为null, 默认为草稿状态
                if (v.getRequestStatus() == null) {
                    v.setRequestStatus(0);
                }
                // 冲账金额
                if (v.getPaymentMoney() == null) {
                    v.setPaymentMoney(BigDecimal.ZERO);
                }
                // 转账金额
                if (v.getTransMoney() == null) {
                    v.setTransMoney(BigDecimal.ZERO);
                }
                // 报销金额
                v.setBxMoney(v.getPaymentMoney().add(v.getTransMoney()));
            }
        });
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    /**
     * 修改转账付款单位
     */
    public void updatePayMoneyUnitId(Long id, Long bUnitId) {
        BudgetProjectlendsum projectLendSum = this.budgetProjectlendsumMapper.selectById(id);
        if (projectLendSum == null) {
            throw new RuntimeException("该项目借款不存在");
        } else if (projectLendSum.getVerifyflag() == 1) {
            throw new RuntimeException("该项目借款已审核，不允许修改转账付款单位");
        }
        // 支付方银行账户信息
        BudgetBillingUnit billingUnit = this.budgetBillingUnitMapper.selectById(bUnitId);
        if (billingUnit == null) {
            throw new RuntimeException("转账付款单位不存在或未启用!");
        } else if (billingUnit.getStopFlag() == 1) {
            throw new RuntimeException("转账付款单位未启用!");
        }
        BudgetProjectlendsum updateProjectLendSum = new BudgetProjectlendsum();
        updateProjectLendSum.setId(id);
        updateProjectLendSum.setPaymoneyunitid(bUnitId);
        this.budgetProjectlendsumMapper.updateById(updateProjectLendSum);
    }

    /**
     * 审核数据
     */
    public void verify(Long id) {
        BudgetProjectlendsum projectLend = this.budgetProjectlendsumMapper.selectById(id);
        if (projectLend == null) {
            throw new RuntimeException("项目借款不存在");
        } else if (projectLend.getVerifyflag() == 1) {
            throw new RuntimeException("该项目已被审核!");
        }

        List<BudgetLendmoney> lendMoneyList = this.budgetLendmoneyMapper.selectList(new QueryWrapper<BudgetLendmoney>()
                .eq("deleteflag", 0)
                .eq("projectlendsumid", id));
        if (lendMoneyList.isEmpty()) {
            throw new RuntimeException("审核失败!无项目借款明细!");
        }

        BigDecimal total = lendMoneyList.stream().map(BudgetLendmoney::getLendmoney).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(projectLend.getTotal()) > 0) {
            throw new RuntimeException("审核失败！明细总金额大于项目总金额！");
        }
        // 验证各种类型的借款的总额是否对应上：现金、转账、礼品
        validateDetailMoney(projectLend, lendMoneyList);

        // 查找是否存在转账类型的记录
        List<BudgetLendmoney> list = lendMoneyList.stream()
                .filter(v -> "2".equals(v.getProjectlendtype()))
                .collect(Collectors.toList());
        if (!list.isEmpty() && projectLend.getPaymoneyunitid() == null) {
            throw new RuntimeException("请先设置转账付款单位!");
        }
        // 支付方银行账户信息
        BudgetBillingUnit billingUnit = this.budgetBillingUnitMapper.selectOne(new QueryWrapper<BudgetBillingUnit>()
                .eq("stopflag", 0)
                .eq("id", projectLend.getPaymoneyunitid()));
        if (billingUnit == null) {
            throw new RuntimeException("转账付款单位不存在或未启用!");
        }
        List<BudgetBillingUnitAccount> accountList = this.budgetBillingUnitAccountMapper.selectList(new QueryWrapper<BudgetBillingUnitAccount>()
                .eq("stopflag", 0)
                .eq("billingunitid", billingUnit.getId()));
        if (accountList.isEmpty()) {
            throw new RuntimeException("开票单位【" + billingUnit.getName() + "】无单位账户!");
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

        // 1.审核完成后不需给业务员发消息确认
        // 2.为转账生成付款单
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

            // 收款方银行账户信息
            BudgetBankAccount bankAccount = this.budgetBankAccountMapper.selectOne(new QueryWrapper<BudgetBankAccount>()
                    .eq("stopflag", 0)
                    .eq("code", lendMoney.getEmpno())
                    .eq("pname", lendMoney.getEmpname())
                    .orderByDesc("wagesflag")
                    .last("limit 1"));
            if (bankAccount == null) {
                throw new RuntimeException("【" + lendMoney.getEmpname() + "(" + lendMoney.getEmpno() + ")】没有银行账户!");
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
        // 验证各种类型的借款的总额是否对应上：现金、转账、礼品
        BigDecimal cashTotal = lendMoneyList.stream().filter(v -> "1".equals(v.getProjectlendtype())).map(BudgetLendmoney::getLendmoney).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal transTotal = lendMoneyList.stream().filter(v -> "2".equals(v.getProjectlendtype())).map(BudgetLendmoney::getLendmoney).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal giftTotal = lendMoneyList.stream().filter(v -> "3".equals(v.getProjectlendtype())).map(BudgetLendmoney::getLendmoney).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (cashTotal.compareTo(projectLend.getCashmoney()) > 0) {
            throw new RuntimeException("审核失败！明细现金总金额大于项目现金总金额！");
        }
        if (transTotal.compareTo(projectLend.getTransfermoney()) > 0) {
            throw new RuntimeException("审核失败！明细转账总金额大于项目转账总金额！");
        }
        if (giftTotal.compareTo(projectLend.getGiftmoney()) > 0) {
            throw new RuntimeException("审核失败！明细礼品总金额大于项目礼品总金额！");
        }
    }

    /**
     * 查询借款明细
     */
    public PageResult<BudgetProjectLendDetailVO> listLendMoneyDetail(Integer page, Integer rows, Long id) {
        Page<BudgetProjectLendDetailVO> pageBean = new Page<>(page, rows);
        List<BudgetProjectLendDetailVO> resultList = this.budgetProjectlendsumMapper.listLendMoneyDetail(pageBean, id);
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    /**
     * 查询还款明细
     */
    public PageResult<BudgetProjectRepayDetailVO> listRepayMoneyDetail(Integer page, Integer rows, Long id) {
        Page<BudgetProjectRepayDetailVO> pageBean = new Page<>(page, rows);
        List<BudgetProjectRepayDetailVO> resultList = this.budgetProjectlendsumMapper.listRepayMoneyDetail(pageBean, id);
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    /**
     * 删除借款
     */
    public void deleteLendMoney(List<Long> ids) {
        List<BudgetLendmoney> lendMoneyList = this.budgetLendmoneyMapper.selectBatchIds(ids);
        if (!lendMoneyList.isEmpty()) {
            List<Long> collect = lendMoneyList.stream().map(BudgetLendmoney::getProjectlendsumid).filter(Objects::nonNull).collect(Collectors.toList());
            if (lendMoneyList.size() != collect.size()) {
                throw new RuntimeException("需要删除借款单存在非同项目导入借款");
            }
            HashSet<Long> hashSet = new HashSet<>(collect);
            if (hashSet.size() != 1) {
                throw new RuntimeException("需要删除借款单不是同项目的借款");
            }
            BudgetProjectlendsum projectLendSum = this.budgetProjectlendsumMapper.selectById(collect.get(0));
            if (projectLendSum == null) {
                throw new RuntimeException("该项目销售政策借款不存在或已删除");
            } else if (projectLendSum.getVerifyflag() == 1) {
                throw new RuntimeException("该项目销售政策借款已审核");
            }
            this.budgetLendmoneyMapper.deleteBatchIds(ids);
        }
    }

    /**
     * 批量"达标/完成"项目借款单
     */
    public void batchReachStandard(List<Long> ids) {
        List<BudgetLendmoney> lendMoneyList = updateReachStandardStatus(ids, true);

        // 利息清空
        clearInterest(lendMoneyList);
    }

    private void clearInterest(List<BudgetLendmoney> lendMoneyList) {
        // 将利息清0
        for (BudgetLendmoney lm : lendMoneyList) {
            if (lm.getInterestmoney().compareTo(BigDecimal.ZERO) >= 0) {
                BigDecimal curInterestMoney = lm.getTempInterestMoney();

                // 把台账里的利息清除
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
                throw new RuntimeException("借款单号【" + lendMoney.getLendmoneycode() + "】未审核!");
            } else if (!lendMoney.getConfirmflag()) {
                throw new RuntimeException("借款单号【" + lendMoney.getLendmoneycode() + "】未确认!");
            } else if (lendMoney.getFlushingflag() != null && !lendMoney.getFlushingflag()) {
                throw new RuntimeException("借款单号【" + lendMoney.getLendmoneycode() + "】已设置不达标!");
            } else if (lendMoney.getFlushingflag() != null && lendMoney.getFlushingflag()) {
                throw new RuntimeException("借款单号【" + lendMoney.getLendmoneycode() + "】已设置达标!");
            }

            // 减去员工台账利息用
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
            // 达标利息清零
            updateLendMoney.setInterestmoney(BigDecimal.ZERO);
        } else {
            // 不达标，自动设置允许还款
            updateLendMoney.setChargebillflag(true);
            updateLendMoney.setChargebillor(user.getUserName());
            updateLendMoney.setChargebillorname(user.getDisplayName());
            updateLendMoney.setChargebilltime(new Date());
        }
        this.budgetLendmoneyMapper.update(updateLendMoney, new QueryWrapper<BudgetLendmoney>().in("id", ids));
        return lendMoneyList;
    }

    /**
     * 批量"不达标/未完成"项目借款单
     */
    public void batchNotReachStandard(List<Long> ids) {
        updateReachStandardStatus(ids, false);
    }

    /**
     * 项目借款单允许还款
     */
    public void allowBuckleMoney(List<Long> ids) {
        List<BudgetLendmoney> lendMoneyList = this.budgetLendmoneyMapper.selectBatchIds(ids);
        for (BudgetLendmoney lendMoney : lendMoneyList) {
            BudgetProjectlendsum budgetProjectlendsum = this.budgetProjectlendsumMapper.selectById(lendMoney.getProjectlendsumid());
            if (budgetProjectlendsum == null || budgetProjectlendsum.getType() != 1) {
                throw new RuntimeException("借款单号【" + lendMoney.getLendmoneycode() + "】非预领项目借款, 不支持该功能!");
            } else if (lendMoney.getConfirmflag() == null) {
                throw new RuntimeException("借款单号【" + lendMoney.getLendmoneycode() + "】未审核!");
            } else if (!lendMoney.getConfirmflag()) {
                throw new RuntimeException("借款单号【" + lendMoney.getLendmoneycode() + "】未确认!");
            } else if (lendMoney.getChargebillflag()) {
                throw new RuntimeException("借款单号【" + lendMoney.getLendmoneycode() + "】已设置允许还款!");
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
     * 项目借款明细导出
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
            rowList.add("1".equals(v.getProjectLendType()) ? "现金" : "2".equals(v.getProjectLendType()) ? "转账" : "礼品");
            dataList.add(rowList);
        });
        return dataList;
    }

    /**
     * 查询利息规则
     */
    public List<BudgetLendInterestRule> listInterestRules(Long id) {
        return this.budgetLendInterestRuleMapper.selectList(new QueryWrapper<BudgetLendInterestRule>().eq("projectlendsumid", id));
    }

    /**
     * 新增利息规则
     */
    public void addInterestRule(BudgetLendInterestRule insertRule) {
        BudgetProjectlendsum projectLend = this.budgetProjectlendsumMapper.selectById(insertRule.getProjectlendsumid());
        if (projectLend == null) {
            throw new RuntimeException("不存在该项目借款");
        }
        insertRule.setId(null);
        insertRule.setLendtype(LendTypeEnum.LEND_TYPE_13.getType());
        insertRule.setInterestrateduringtheperiod(insertRule.getInterestrateduringtheperiod().divide(new BigDecimal("100")));
        insertRule.setInterestrateouttheperiod(insertRule.getInterestrateouttheperiod().divide(new BigDecimal("100")));
        this.budgetLendInterestRuleMapper.insert(insertRule);
    }

    /**
     * 修改利息规则
     */
    public void updateInterestRule(BudgetLendInterestRule updateRule) {
        BudgetLendInterestRule interestRule = this.budgetLendInterestRuleMapper.selectById(updateRule.getId());
        if (interestRule == null) {
            throw new RuntimeException("不存在该利息规则");
        }
        updateRule.setLendtype(interestRule.getLendtype());
        updateRule.setProjectlendsumid(interestRule.getProjectlendsumid());
        updateRule.setInterestrateduringtheperiod(updateRule.getInterestrateduringtheperiod().divide(new BigDecimal("100")));
        updateRule.setInterestrateouttheperiod(updateRule.getInterestrateouttheperiod().divide(new BigDecimal("100")));
        this.budgetLendInterestRuleMapper.updateById(updateRule);
    }

    /**
     * 删除利息规则
     */
    public void deleteInterestRules(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        this.budgetLendInterestRuleMapper.deleteBatchIds(ids);
    }

    /**
     * 项目借款导出
     */
    public Map<String, List<List<String>>> exportValidate(Long id) {
        BudgetProjectlendsum projectLend = this.budgetProjectlendsumMapper.selectById(id);
        if (projectLend == null) {
            throw new RuntimeException("不存在该项目借款");
        } else if (projectLend.getVerifyflag() != 1) {
            throw new RuntimeException("未审核的记录不允许导出");
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
            rowList.add("1".equals(v.getProjectlendtype()) ? "现金" : "2".equals(v.getProjectlendtype()) ? "转账" : "礼品");
            rowList.add(v.getLendmoneycode());
            rowList.add(v.getFlushingflag() == null ? "" : (v.getFlushingflag() ? "是" : "否"));
            dataList.add(rowList);
        });
        Map<String, List<List<String>>> resultMap = new HashMap<>(2);
        resultMap.put(projectLend.getProjectname(), dataList);
        return resultMap;
    }

    /**
     * 利息模板导出
     */
    public Map<String, List<List<String>>> exportInterest(Long id) {
        BudgetProjectlendsum projectLend = this.budgetProjectlendsumMapper.selectById(id);
        if (projectLend == null) {
            throw new RuntimeException("不存在该项目借款.");
        } else if (projectLend.getVerifyflag() != 1) {
            throw new RuntimeException("未审核的记录不允许导出");
        }

        List<List<String>> dataList = new ArrayList<>();
        List<BudgetLendmoney> lendMoneyList = this.budgetLendmoneyMapper.selectList(new QueryWrapper<BudgetLendmoney>()
                .eq("deleteflag", 0)
                .eq("projectlendsumid", id));
        lendMoneyList.forEach(v -> {
            List<String> rowList = new ArrayList<>();
            rowList.add(v.getEmpno());
            rowList.add(v.getEmpname());
            rowList.add("1".equals(v.getProjectlendtype()) ? "现金" : "2".equals(v.getProjectlendtype()) ? "转账" : "礼品");
            rowList.add(v.getLendmoney().toString());
            dataList.add(rowList);
        });
        Map<String, List<List<String>>> resultMap = new HashMap<>(2);
        resultMap.put(projectLend.getProjectname(), dataList);
        return resultMap;
    }

    /**
     * 达标验证导入
     */
    public List<List<String>> importValidateComplete(Long id, List<List<String>> excelDataList) {
        BudgetProjectlendsum projectLend = this.budgetProjectlendsumMapper.selectById(id);
        if (projectLend == null) {
            throw new RuntimeException("不存在该项目借款");
        } else if (projectLend.getVerifyflag() != 1) {
            throw new RuntimeException("该项目借款还未审核");
        }

        BudgetUnit budgetUnit = this.budgetUnitMapper.selectById(projectLend.getUnitid());
        WbUser user = UserThreadLocal.get();
        if (!"admin".equals(user.getUserId())) {
            String managers = budgetUnit.getManagers();
            if (!managers.contains(user.getUserId())) {
                throw new RuntimeException("员工【" + user.getDisplayName() + "(" + user.getUserName() + ")】不是预算单位【" + budgetUnit.getName() + "】的预算员");
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
            // 表格正文从第二行开始
            if (i < 1) {
                continue;
            }
            validateComplete(lendMoneyMap, excelDataList.get(i), projectLend, errorList, successList);
        }
        if (errorList.isEmpty() && !successList.isEmpty()) {
            // 修改借款达标信息
            successList.forEach(this.budgetLendmoneyMapper::updateById);

            // 清空已达标员工利息
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
                throw new RuntimeException("内容填写不完整");
            }
            for (int i = 1; i <= columnSize; i++) {
                String data = row.get(i - 1);
                switch (i) {
                    case 1:
                        isNotBlank(data, "项目编号");
                        if (!projectLend.getProjectno().equals(data)) {
                            throw new RuntimeException("选择的项目编号和表格中的项目编号不一致");
                        }
                        break;
                    case 2:
                        isNotBlank(data, "项目名称");
                        if (!projectLend.getProjectname().equals(data)) {
                            throw new RuntimeException("选择的项目名称和表格中的项目名称不一致");
                        }
                        break;
                    case 3:
                        isNotBlank(data, "工号");
                        empNo = data;
                        break;
                    case 4:
                        isNotBlank(data, "姓名");
                        this.wbUserService.validateUser(empNo, data);
                        break;
                    case 9:
                        projectLendType = getProjectLendType(data, projectLend.getType());
                        projectLendTypeStr = data;
                        break;
                    case 11:
                        isNotBlank(data, "达标/完成状态");
                        if (!"是".equals(data) && !"否".equals(data)) {
                            throw new RuntimeException("达标/完成状态请填'是'或者'否'");
                        }
                        flushingFlag = "是".equals(data);
                        break;
                    default:
                }
            }

            BudgetLendmoney lendMoney = lendMoneyMap.get(empNo + projectLendType);
            if (lendMoney == null) {
                throw new RuntimeException("项目【" + projectLend.getProjectname() + "】下工号【" + empNo + "】类型【" + projectLendTypeStr + "】不存在!");
            } else if (lendMoney.getConfirmflag() == null || !lendMoney.getConfirmflag()) {
                throw new RuntimeException("该记录还未确认!");
            } else if (lendMoney.getFlushingflag() != null) {
                if (lendMoney.getFlushingflag() && !flushingFlag) {
                    throw new RuntimeException("该记录已设置达标!");
                } else if (!lendMoney.getFlushingflag() && flushingFlag) {
                    throw new RuntimeException("该记录已设置不达标!");
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
                // 达标利息清零
                updateLendMoney.setInterestmoney(BigDecimal.ZERO);

                // 减去员工台账利息用
                updateLendMoney.setTempInterestMoney(lendMoney.getInterestmoney());
            } else {
                // 不达标，自动设置允许还款
                updateLendMoney.setChargebillflag(true);
                updateLendMoney.setChargebillor(user.getUserName());
                updateLendMoney.setChargebillorname(user.getDisplayName());
                updateLendMoney.setChargebilltime(new Date());
            }
            successList.add(updateLendMoney);
        } catch (Exception e) {
            // 解决异常: Transaction rolled back because it has been marked as rollback-only
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            errorList.add(ExcelBean.transformBean(row, totalColumn, e.getMessage()));
        }
    }

    private void isNotBlank(String data, String message) {
        if (StringUtils.isBlank(data)) {
            throw new RuntimeException(message + "不能为空");
        }
    }

    private String getProjectLendType(String data, Integer type) {
        if (StringUtils.isBlank(data)) {
            throw new RuntimeException("项目借款类型不能为空");
        }
        if (type == 1 && !"现金".equals(data) && !"礼品".equals(data) && !"转账".equals(data)) {
            throw new RuntimeException("预领项目借款类型只能为【现金、转账、礼品】");
        } else if (type == 2 && !"转账".equals(data)) {
            throw new RuntimeException("借支项目借款类型请填写【转账】");
        }
        return "现金".equals(data) ? "1" : "转账".equals(data) ? "2" : "3";
    }

    /**
     * 利息导入
     */
    public List<List<String>> importInterest(Long id, List<List<String>> excelDataList) {
        BudgetProjectlendsum projectLend = this.budgetProjectlendsumMapper.selectById(id);
        if (projectLend == null) {
            throw new RuntimeException("不存在该项目借款");
        }

        List<ExcelBean> errorList = new ArrayList<>();
        List<BudgetLendmoney> successList = new ArrayList<>();
        int size = excelDataList.size();
        for (int i = 0; i < size; i++) {
            // 表格正文从第二行开始
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
                throw new RuntimeException("内容填写不完整");
            }
            for (int i = 1; i <= columnSize; i++) {
                String data = row.get(i - 1);
                switch (i) {
                    case 1:
                        isNotBlank(data, "工号");
                        empNo = data;
                        break;
                    case 2:
                        isNotBlank(data, "姓名");
                        this.wbUserService.validateUser(empNo, data);
                        break;
                    case 3:
                        projectLendType = getProjectLendType(data, projectLend.getType());
                        projectLendTypeStr = data;
                        break;
                    case 5:
                        isNotBlank(data, "利息");
                        try {
                            interestMoney = new BigDecimal(data);
                        } catch (Exception ignored) {
                            throw new RuntimeException("利息格式错误");
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
                throw new RuntimeException("项目【" + projectLend.getProjectname() + "】下工号【" + empNo + "】类型【" + projectLendTypeStr + "】不存在!");
            } else if (lendMoney.getFlushingflag() != null && lendMoney.getFlushingflag()) {
                throw new RuntimeException("项目【" + projectLend.getProjectname() + "】下工号【" + empNo + "】类型【" + projectLendTypeStr + "】借款已达标!");
            }

            // 新增或修改员工台账利息用
            lendMoney.setTempInterestMoney(interestMoney);
            successList.add(lendMoney);
        } catch (Exception e) {
            // 解决异常: Transaction rolled back because it has been marked as rollback-only
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            errorList.add(ExcelBean.transformBean(row, totalColumn, e.getMessage()));
        }
    }

    /**
     * 项目还款记录明细导出
     */
    public Map<String, List<List<String>>> exportRepayMoneyDetail(Long id) {
        BudgetProjectlendsum projectLend = this.budgetProjectlendsumMapper.selectById(id);
        if (projectLend == null) {
            throw new RuntimeException("不存在该项目借款");
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
     * 项目借款导入
     */
    public List<List<String>> importProjectLend(Long id, List<List<String>> excelDataList) {
        BudgetProjectlendsum projectLend = this.budgetProjectlendsumMapper.selectById(id);
        if (projectLend == null) {
            throw new RuntimeException("不存在该项目借款");
        } else if (projectLend.getVerifyflag() == 1) {
            throw new RuntimeException("该项目借款已被审核!");
        }

        List<BudgetLendmoney> successList = new ArrayList<>();
        List<ExcelBean> errorList = new ArrayList<>();
        int size = excelDataList.size();
        for (int i = 0; i < size; i++) {
            // 表格正文从第二行开始
            if (i < 1) {
                continue;
            }
            validateProjectLend(projectLend, successList, excelDataList.get(i), errorList);
        }

        if (errorList.isEmpty() && !successList.isEmpty()) {
            // 校验同一个人同项目借款类型只能有一条记录
            long count = successList.stream().map(v -> v.getEmpno() + v.getProjectlendtype()).distinct().count();
            if (count != successList.size()) {
                throw new RuntimeException("同一员工存在多条相同项目借款类型记录!");
            }

            // 导入的总金额不能超过所属项目的总金额
            List<BudgetLendmoney> existLendList = this.budgetLendmoneyMapper.selectList(new QueryWrapper<BudgetLendmoney>()
                    .eq("deleteflag", 0)
                    .eq("projectlendsumid", projectLend.getId()));
            BigDecimal totalMoney = existLendList.stream().map(BudgetLendmoney::getLendmoney).reduce(BigDecimal.ZERO, BigDecimal::add);

            Date currentDate = new Date();
            for (BudgetLendmoney lendMoney : successList) {
                // 判断是否已存在借款
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
                throw new RuntimeException("借款总金额超出项目可用金额！");
            }
            // 验证各种类型的借款的总额是否对应上：现金、转账、礼品
            validateDetailMoney(projectLend, successList);

            for (BudgetLendmoney lendMoney : successList) {
                try {
                    if (lendMoney.getId() == null) {
                        // 新增
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
                        throw new RuntimeException("员工【" + lendMoney.getEmpname() + "(" + lendMoney.getEmpno() + ")】存在借款类型、借款金额、借款时间都相同的借款记录");
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
                throw new RuntimeException("内容填写不完整");
            }
            BudgetLendmoney lendMoney = new BudgetLendmoney();
            lendMoney.setProjectlendsumid(projectLend.getId());

            for (int i = 1; i <= columnSize; i++) {
                String data = row.get(i - 1);
                switch (i) {
                    case 1:
                        isNotBlank(data, "工号");
                        lendMoney.setEmpno(data);
                        break;
                    case 2:
                        isNotBlank(data, "姓名");
                        WbUser user = this.wbUserService.validateUser(lendMoney.getEmpno(), data);
                        lendMoney.setEmpid(user.getUserId());
                        lendMoney.setEmpname(user.getDisplayName());
                        break;
                    case 3:
                        isNotBlank(data, "借款金额");
                        try {
                            BigDecimal money = new BigDecimal(data);
                            if (BigDecimal.ZERO.compareTo(money) > 0) {
                                throw new RuntimeException("借款金额不能小于等于零");
                            }
                            lendMoney.setLendmoney(money);
                        } catch (Exception ignored) {
                            throw new RuntimeException("借款金额格式错误");
                        }
                        break;
                    case 4:
                        isNotBlank(data, "借款日期");
                        try {
                            Date lendDate = Constants.FORMAT_10.parse(data);
                            lendMoney.setLenddate(lendDate);
                        } catch (Exception ignored) {
                            throw new RuntimeException("借款日期格式错误");
                        }
                        break;
                    case 5:
                        isNotBlank(data, "计划还款日期");
                        try {
                            Date planPayDate = Constants.FORMAT_10.parse(data);
                            lendMoney.setPlanpaydate(planPayDate);
                        } catch (Exception ignored) {
                            throw new RuntimeException("计划还款日期格式错误");
                        }
                        if (lendMoney.getPlanpaydate().compareTo(lendMoney.getLenddate()) <= 0) {
                            throw new RuntimeException("还款日期应该在借款日期之后!");
                        }
                        break;
                    case 6:
                        isNotBlank(data, "借款事由");
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
            // 解决异常: Transaction rolled back because it has been marked as rollback-only
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            errorList.add(ExcelBean.transformBean(row, totalColumn, e.getMessage()));
        }
    }

    // ----------------------------------------------------------------------------------------------------

    /**
     * 获取报销开票单位
     */
    public List<BudgetBillingUnit> curUserInvoiceUnit() {
        return this.budgetBillingUnitMapper.selectList(new QueryWrapper<BudgetBillingUnit>()
                .eq("stopflag", 0)
                .orderByAsc("orderno"));
    }

    /**
     * 获取付款开票单位账户
     */
    public List<PaymentUnitVO> curUserPaymentUnitAccount(List<String> ids) {
        if (ids != null && !ids.isEmpty()) {
            return this.budgetBillingUnitMapper.curUserPaymentUnitAccount(String.join(",", ids));
        }
        return null;
    }

    /**
     * 获取可报销月度动因
     */
    public List<BxMonthAgentVO> listMonthAgentByBx(Long yearId, Long budgetUnitId, Long monthId) {
        return this.budgetMonthAgentMapper.listMonthAgentByBx(yearId, budgetUnitId, monthId);
    }

    /**
     * 项目借款报销冲账明细
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
     * 项目借款报销转账明细
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
                            trans.setPayeebankaccount("无");
                            BudgetBankAccount bankAccount = this.budgetBankAccountMapper.selectOne(new QueryWrapper<BudgetBankAccount>()
                                    .eq("stopflag", 0)
                                    .eq("code", repayMoney.getEmpno())
                                    .orderByDesc("wagesflag")
                                    .last("limit 1"));
                            if (bankAccount == null) {
                                throw new RuntimeException("收款人" + repayMoney.getEmpname() + "(" + repayMoney.getEmpno() + ")账户不存在");
                            }
                            trans.setPayeebankaccount(bankAccount.getBankaccount());
                            WbBanks bank = this.wbBanksMapper.selectOne(new QueryWrapper<WbBanks>().eq("sub_branch_code", bankAccount.getBranchcode()));
                            if (bank == null) {
                                throw new RuntimeException("收款人" + repayMoney.getEmpname() + "(" + repayMoney.getEmpno() + ")开户行不存在");
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
     * 项目借款报销明细
     */
    public List<BudgetProjectlendbxdetail> projectLendBxDetail(Long id) {
        return this.budgetProjectlendbxdetailMapper.selectList(new QueryWrapper<BudgetProjectlendbxdetail>().eq("projectlendsumid", id));
    }

    /**
     * 项目借款保存报销单
     */
    public void saveReimbursementData(ProjectLendReimbursementDTO bean) throws Exception {
        boolean isUpdate = false;
        BudgetProjectlendsum projectLendSum = this.budgetProjectlendsumMapper.selectById(bean.getProjectLendSumId());
        if (projectLendSum == null) {
            throw new RuntimeException("不存在该项目借款");
        } else if (projectLendSum.getBxorderid() != null) {
            BudgetReimbursementorder order = this.budgetReimbursementorderMapper.selectById(projectLendSum.getBxorderid());
            if (order != null) {
                // 审核状态，-1：退回，0：保存，1：已提交（待审核），2：审核通过
                if (order.getReuqeststatus() == 1) {
                    throw new RuntimeException("该项目已生成报销单, 并且已提交");
                } else if (order.getReuqeststatus() == 2) {
                    throw new RuntimeException("该项目已生成报销单, 并且已审核通过");
                }
                isUpdate = true;
            }
        }

        List<BudgetLendmoney> lendMoneyList = this.budgetLendmoneyMapper.selectList(new QueryWrapper<BudgetLendmoney>()
                .eq("deleteflag", 0)
                .eq("projectlendsumid", projectLendSum.getId()));
        long count = lendMoneyList.stream().filter(v -> v.getFlushingflag() == null).count();
        if (count > 0) {
            throw new RuntimeException("存在未设置达标状态的借款单");
        }

        BudgetMonthEndUnit monthEndUnit = this.budgetMonthEndUnitMapper.selectOne(new QueryWrapper<BudgetMonthEndUnit>()
                .eq("unitid", projectLendSum.getUnitid())
                .eq("monthid", bean.getMonthId()));
        if (monthEndUnit == null) {
            throw new RuntimeException("【" + bean.getMonthId() + "月】月度预算还未启动");
        } else if (monthEndUnit.getRequeststatus() != 2) {
            throw new RuntimeException("【" + bean.getMonthId() + "月】月度预算还未审核通过");
        } else if (monthEndUnit.getMonthendflag()) {
            throw new RuntimeException("【" + bean.getMonthId() + "月】月度预算已月结");
        }

        BigDecimal bxTotal = saveBxData(projectLendSum, bean);
        BigDecimal czTotal = saveCzData(bean.getCzList());
        BigDecimal zzTotal = saveZzData(projectLendSum.getId(), bean.getZzList());
        if (bean.getIsSubmit()) {
            // 校验 报销金额 == 转账金额 + 冲账金额
            if (bxTotal.compareTo(czTotal.add(zzTotal)) != 0) {
                throw new RuntimeException("转账金额 + 冲账金额 不等于 报销金额");
            }

            // 动因被别的报销单占用
            checkBxEnough(projectLendSum.getId());
            // 提交
            generateBxOrder(projectLendSum, isUpdate);
        }
    }

    private void generateBxOrder(BudgetProjectlendsum sum, boolean isUpdate) throws Exception {
        WbUser user = UserThreadLocal.get();
        // 生成报销单
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

            // 通过科目名称分组
            Map<String, List<BudgetProjectlendbxdetail>> bxDetailBySubjectNameMap = bxDetailList.stream().collect(Collectors.groupingBy(BudgetProjectlendbxdetail::getSubjectname));
            bxDetailBySubjectNameMap.forEach((subjectName, detailListByName) -> {
                // 相同科目的总报销金额
                BigDecimal totalReimMoney = detailListByName.stream().map(BudgetProjectlendbxdetail::getReimmoney).reduce(BigDecimal.ZERO, BigDecimal::add);

                BudgetMonthAgent monthAgent = this.budgetMonthAgentMapper.selectById(detailListByName.get(0).getMonthagentid());
                Long unitId = monthAgent.getUnitid();
                Long subjectId = monthAgent.getSubjectid();

                BudgetUnitSubject unitSubject = this.budgetUnitSubjectMapper.selectOne(new QueryWrapper<BudgetUnitSubject>()
                        .eq("unitid", monthAgent.getUnitid())
                        .eq("subjectid", monthAgent.getSubjectid()));

                // 年度控制
                BigDecimal usedYearMoney = BigDecimal.ZERO;
                if (unitSubject.getYearcontrolflag()) {
                    usedYearMoney = getSubjectLockedMoney(unitId, subjectId, null);
                }
                // 月度控制
                BigDecimal usedMonthSubjectLockedMoney = BigDecimal.ZERO;
                if (unitSubject.getMonthcontrolflag()) {
                    //获取科目本月被占用金额
                    usedMonthSubjectLockedMoney = getSubjectLockedMoney(unitId, subjectId, monthAgent.getMonthid());
                }
                // 年度科目控制
                BigDecimal usedYearSubjectLockedMoney = BigDecimal.ZERO;
                if (unitSubject.getYearsubjectcontrolflag()) {
                    usedYearSubjectLockedMoney = reimExecuteList.stream().filter(e -> subjectId.equals(e.getSubjectid()) && unitId.equals(e.getUnitid()))
                            .map(BudgetAgentExecuteView::getExecutemoney)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                }

                // 年度科目可用
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
                    // 年度动因可用
                    BigDecimal yearAgentUnMoney = detail.getYearagentunmoney();
                    // 月度科目可用
                    BigDecimal monthSubjectUnMoney = detail.getMonthagentunmoney();

                    if (bxMoney.add(usedYearMoney).compareTo(yearAgentUnMoney) > 0) {
                        throw new RuntimeException("动因【" + detail.getMonthagentname() + "】年度可用【" + NumberUtil.subZeroAndDot(yearAgentUnMoney)
                                + "】不足以报销金额【" + NumberUtil.subZeroAndDot(bxMoney) + "】!"
                                + "锁定金额:【" + NumberUtil.subZeroAndDot(usedYearMoney) + "】");
                    } else if (totalReimMoney.add(usedMonthSubjectLockedMoney).compareTo(monthSubjectUnMoney) > 0) {
                        throw new RuntimeException("科目【" + detail.getSubjectname() + "】本月可用【" + NumberUtil.subZeroAndDot(monthSubjectUnMoney)
                                + "】不足以报销金额【" + NumberUtil.subZeroAndDot(totalReimMoney) + "】!"
                                + "锁定金额:【" + NumberUtil.subZeroAndDot(usedMonthSubjectLockedMoney) + "】");
                    } else if (totalReimMoney.add(usedYearSubjectLockedMoney).compareTo(yearSubjectUnMoney) > 0) {
                        throw new RuntimeException("科目【" + detail.getSubjectname() + "】年度可用【" + NumberUtil.subZeroAndDot(yearSubjectUnMoney)
                                + "】不足以报销金额【" + NumberUtil.subZeroAndDot(totalReimMoney) + "】!"
                                + "锁定金额:【" + NumberUtil.subZeroAndDot(usedYearSubjectLockedMoney) + "】");
                    }
                }
            });
        }
    }

    private BigDecimal getSubjectLockedMoney(Long unitId, Long subjectId, Long monthId) {
        // 获取月度科目报销占用(monthId = null 时为年度科目报销占用)
        List<BudgetReimbursementorderDetail> details = this.budgetReimbursementorderDetailMapper.listDetailByMonthId(unitId, subjectId, monthId);
        BigDecimal bxUsedMoney = details.stream().map(BudgetReimbursementorderDetail::getReimmoney).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 获取月度科目划拨占用(monthId = null 时为年度科目划拨占用)
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
                throw new RuntimeException("开票单位下不存在有效的付款账户");
            }
            Set<Long> unitAccountIds = paymentUnits.stream().map(PaymentUnitVO::getUnitAccountId).collect(Collectors.toSet());

            for (BudgetProjectlendbxtrans v : zzList) {
                if (!unitAccountIds.contains(v.getDraweeunitaccountid())) {
                    throw new RuntimeException("明细开票单位下不存在付款账户【" + v.getDraweeunitname() + "(" + v.getDraweebankaccount() + ")】");
                }
                if (v.getId() == null) {
                    this.budgetProjectlendbxtransMapper.insert(v);
                } else {
                    this.budgetProjectlendbxtransMapper.updateById(v);
                }
                // 累计转账金额
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
                // 累计冲账金额
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

        // 报销明细
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

        // 刷新数据
        BigDecimal bxTotal = BigDecimal.ZERO;
        List<BudgetProjectlendbxdetail> bxDetailList = this.budgetProjectlendbxdetailMapper.selectList(new QueryWrapper<BudgetProjectlendbxdetail>().eq("projectlendsumid", sum.getId()));
        if (!bxDetailList.isEmpty()) {
            for (BudgetProjectlendbxdetail detail : bxDetailList) {
                // 获取所有的动因
                BudgetMonthAgent monthAgent = this.budgetMonthAgentMapper.selectById(detail.getMonthagentid());
                if (monthAgent == null) {
                    throw new RuntimeException("月度动因不存在");
                } else if (!monthAgent.getYearid().equals(sum.getYearid())) {
                    throw new RuntimeException("月度动因【" + monthAgent.getName() + "】所在届别与项目所在届别不一致");
                } else if (!monthAgent.getUnitid().equals(sum.getUnitid())) {
                    throw new RuntimeException("月度动因【" + monthAgent.getName() + "】所在预算单位与项目所在预算单位不一致");
                }
                // 根据动因获取所有的科目id
                BudgetMonthSubject monthSubject = this.budgetMonthSubjectMapper.selectOne(new QueryWrapper<BudgetMonthSubject>()
                        .eq("yearid", sum.getYearid())
                        .eq("unitid", sum.getUnitid())
                        .eq("monthid", bean.getMonthId())
                        .eq("subjectid", monthAgent.getSubjectid()));
                // 获取科目本月可用
                BigDecimal monthAgentUnMoney = monthSubject.getTotal()
                        .add(monthSubject.getAddmoney())
                        .add(monthSubject.getLendinmoney())
                        .subtract(monthSubject.getLendoutmoney())
                        .subtract(monthSubject.getExecutemoney());
                detail.setMonthagentmoney(monthSubject.getTotal());
                detail.setMonthagentunmoney(monthAgentUnMoney);
                // 获取动因年度可用
                BudgetYearAgent yearAgent = this.budgetYearAgentMapper.selectById(monthAgent.getYearagentid());
                BigDecimal yearAgentUnMoney = yearAgent.getTotal()
                        .add(yearAgent.getAddmoney())
                        .add(yearAgent.getLendinmoney())
                        .subtract(yearAgent.getLendoutmoney())
                        .subtract(yearAgent.getExecutemoney());
                detail.setYearagentmoney(yearAgent.getTotal());
                detail.setYearagentunmoney(yearAgentUnMoney);
                this.budgetProjectlendbxdetailMapper.updateById(detail);

                // 累计报销金额
                bxTotal = bxTotal.add(detail.getReimmoney());
            }
        }
        return bxTotal;
    }

}
