package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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
import com.jtyjy.finance.manager.event.lendmoney.FkCodeRequest;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.mapper.response.LendmoneyUseBean;
import com.jtyjy.finance.manager.trade.DistributedNumber;
import com.jtyjy.finance.manager.utils.NumberUtil;
import com.jtyjy.finance.manager.vo.BudgetLendMoneyVO;
import com.jtyjy.finance.manager.vo.BudgetPayMoneyDetailVO;
import com.jtyjy.finance.manager.vo.BudgetRepayMoneyDetailVO;
import com.jtyjy.finance.manager.vo.ExcelBean;
import com.klcwqy.easy.lock.impl.ZookeeperShareLock;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetLendmoneyService extends DefaultBaseService<BudgetLendmoneyMapper, BudgetLendmoney> {

    private final TabChangeLogMapper loggerMapper;
    private final BudgetLendmoneyMapper budgetLendmoneyMapper;
    private final BudgetRepaymoneyMapper budgetRepaymoneyMapper;
    private final BudgetArrearsMapper budgetArrearsMapper;
    private final BudgetRepaymoneyDetailMapper budgetRepaymoneyDetailMapper;
    private final BudgetLendmoneyUselogMapper budgetLendmoneyUselogMapper;
    private final BudgetLendandrepaymoneyMapper budgetLendandrepaymoneyMapper;
    private final BudgetPaymoneyMapper budgetPaymoneyMapper;
    private final BudgetUnitMapper budgetUnitMapper;
    private final BudgetProjectMapper budgetProjectMapper;
    private final BudgetProjectlendsumMapper budgetProjectlendsumMapper;
    private final WbUserMapper wbUserMapper;
    private final WbBanksMapper wbBanksMapper;

    private final DistributedNumber distributedNumber;
    private final BudgetSysService budgetSysService;
    private final CuratorFramework client;
    private final RedisClient redisClient;

    @Override
    public BaseMapper<TabChangeLog> getLoggerMapper() {
        return loggerMapper;
    }

    @Override
    public void setBaseLoggerBean() {
        DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_lendmoney_new"));
    }

    /**
     * ???????????????????????????
     */
    public List<LendmoneyUseBean> getUseInfo(String ids) {
        return this.budgetLendmoneyMapper.getUseInfo(ids);
    }

    /**
     * ??????????????????
     */
    public List<BudgetLendmoney> getByCodes(Set<String> codes) {
        QueryWrapper<BudgetLendmoney> wrapper = new QueryWrapper<>();
        wrapper.in("lendmoneycode", codes);
        return this.list(wrapper);
    }

    /**
     * ??????????????????????????????
     */
    public PageResult<BudgetLendMoneyVO> listLendMoneyPage(Integer page, Integer rows, HashMap<String, Object> paramMap) {
        Page<BudgetLendMoneyVO> pageBean = new Page<>(page, rows);
        List<BudgetLendMoneyVO> resultList = this.budgetLendmoneyMapper.listLendMoneyPage(pageBean, paramMap);
        resultList.forEach(v -> v.setLendTypeDesc(LendTypeEnum.getValue(v.getLendType())));
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    /**
     * ????????????????????????
     */
    public PageResult<BudgetRepayMoneyDetailVO> getRepayMoneyDetail(Integer page, Integer rows, Long id) {
        Page<BudgetRepayMoneyDetailVO> pageBean = new Page<>(page, rows);
        List<BudgetRepayMoneyDetailVO> resultList = this.budgetLendmoneyMapper.getRepayMoneyDetail(pageBean, id);
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    /**
     * ????????????????????????
     */
    public PageResult<BudgetPayMoneyDetailVO> getPayMoneyDetail(Integer page, Integer rows, Long id) {
        Page<BudgetPayMoneyDetailVO> pageBean = new Page<>(page, rows);
        HashMap<String, Object> paramMap = new HashMap<>(2);
        paramMap.put("lendMoneyId", id);
        List<BudgetPayMoneyDetailVO> payMoneyDetail = this.budgetLendmoneyMapper.getPayMoneyDetail(pageBean, paramMap);
        payMoneyDetail.forEach(v -> v.setLendTypeDesc(LendTypeEnum.getValue(v.getLendType())));
        return PageResult.apply(pageBean.getTotal(), payMoneyDetail);
    }

    /**
     * ????????????
     */
    public PageResult<BudgetLendmoneyUselog> lendByBxLocked(Integer page, Integer rows, Long id) {
        Page<BudgetLendmoneyUselog> pageBean = new Page<>(page, rows);
        List<BudgetLendmoneyUselog> resultList = this.budgetLendmoneyUselogMapper.lendByBxLocked(pageBean, id);
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    /**
     * ????????????
     */
    public void cashRepayMoney(Long id, BigDecimal money, Date planPayTime) throws Exception {
        BudgetLendmoney lendMoney = this.budgetLendmoneyMapper.selectById(id);
        if (lendMoney == null) {
            throw new RuntimeException("?????????????????????");
        } else if (lendMoney.getEffectflag() == null || !lendMoney.getEffectflag()) {
            throw new RuntimeException("????????????????????????!");
        }

        ZookeeperShareLock lock = new ZookeeperShareLock(this.client, "/finance-platform/repayMoney/" + lendMoney.getEmpno(), o -> {
            throw new RuntimeException("??????????????????,??????????????????");
        });
        try {
            lock.tryLock();
            BudgetLendmoney updateLendMoney = new BudgetLendmoney();
            updateLendMoney.setId(lendMoney.getId());

            // ????????????????????????
            if (planPayTime != null) {
                if (lendMoney.getLenddate().compareTo(planPayTime) > 0) {
                    throw new RuntimeException("??????????????????????????????????????????");
                }
                updateLendMoney.setPlanpaydate(planPayTime);
            }

            if (money.compareTo(BigDecimal.ZERO) != 0) {
                // ????????????, ??????????????????
                if (lendMoney.getLendtype() == LendTypeEnum.LEND_TYPE_13.getType()) {
                    if (lendMoney.getFlushingflag() != null && lendMoney.getFlushingflag()) {
                        throw new RuntimeException("???????????????" + lendMoney.getLendmoneycode() + "???????????????????????????????????????????????????");
                    } else if (lendMoney.getChargebillflag() == null || !lendMoney.getChargebillflag()) {
                        throw new RuntimeException("???????????????" + lendMoney.getLendmoneycode() + "??????????????????????????????????????????????????????");
                    }
                }
                BigDecimal remain = lendMoney.getLendmoney()
                        .subtract(lendMoney.getRepaidmoney())
                        .add(lendMoney.getInterestmoney())
                        .subtract(lendMoney.getRepaidinterestmoney());
                if (BigDecimal.ZERO.compareTo(remain) == 0) {
                    throw new RuntimeException("????????????????????????,????????????!");
                } else if (money.compareTo(remain) > 0) {
                    throw new RuntimeException("???????????????" + NumberUtil.subZeroAndDot(money).setScale(2, BigDecimal.ROUND_HALF_UP)
                            + "??????????????????????????????" + NumberUtil.subZeroAndDot(remain).setScale(2, BigDecimal.ROUND_HALF_UP) + "???!");
                } else if (money.compareTo(BigDecimal.ZERO) < 0) {
                    BigDecimal repaidMoney = lendMoney.getRepaidmoney().add(lendMoney.getRepaidinterestmoney());
                    // ???????????????????????????, ????????????????????????????????????0
                    if ((money.add(repaidMoney)).compareTo(BigDecimal.ZERO) < 0) {
                        throw new RuntimeException("???????????????" + NumberUtil.subZeroAndDot(money).setScale(2, BigDecimal.ROUND_HALF_UP)
                                + "??????????????????????????????" + NumberUtil.subZeroAndDot(repaidMoney).setScale(2, BigDecimal.ROUND_HALF_UP) + "???!");
                    }
                }

                List<BudgetLendmoneyUselog> lockList = this.budgetLendmoneyUselogMapper.lendByBxLocked(null, id);
                if (!lockList.isEmpty()) {
                    // ?????????????????????????????????
                    BigDecimal lockMoney = lockList.stream().filter(BudgetLendmoneyUselog::getUseflag).map(BudgetLendmoneyUselog::getLockedmoney).reduce(BigDecimal.ZERO, BigDecimal::add);
                    if (money.compareTo(remain.subtract(lockMoney)) > 0) {
                        throw new RuntimeException("?????????????????????????????????" + NumberUtil.subZeroAndDot(lockMoney).setScale(2, BigDecimal.ROUND_HALF_UP)
                                + "???, ???????????????" + NumberUtil.subZeroAndDot(money).setScale(2, BigDecimal.ROUND_HALF_UP)
                                + "??????????????????" + NumberUtil.subZeroAndDot(remain.subtract(lockMoney)).setScale(2, BigDecimal.ROUND_HALF_UP) + "???(???????????? - ????????????)!");
                    }
                }

                Date currentDate = new Date();
                WbUser user = UserThreadLocal.get();
                if (lendMoney.getLendtype() == LendTypeEnum.LEND_TYPE_13.getType() && user != null) {
                    updateLendMoney.setChargebillflag(true);
                    updateLendMoney.setChargebillor(user.getUserName());
                    updateLendMoney.setChargebillorname(user.getDisplayName());
                    updateLendMoney.setChargebilltime(currentDate);
                }

                // ??????????????????
                BudgetRepaymoney insertRepayMoney = new BudgetRepaymoney();
                insertRepayMoney.setEmpid(lendMoney.getEmpid() == null ? "" : lendMoney.getEmpid());
                insertRepayMoney.setEmpno(lendMoney.getEmpno());
                insertRepayMoney.setEmpname(lendMoney.getEmpname());
                insertRepayMoney.setRepaydate(currentDate);
                insertRepayMoney.setRepaytype(1);
                insertRepayMoney.setRepaytypeid("");
                insertRepayMoney.setCreatetime(currentDate);
                insertRepayMoney.setRepaymoney(money);
                insertRepayMoney.setEffectflag(false);
                insertRepayMoney.setRepaymoneycode(this.distributedNumber.getRepayNum());
                this.budgetRepaymoneyMapper.insert(insertRepayMoney);

                // ????????????
                BudgetRepaymoneyDetail insertRepayDetail = new BudgetRepaymoneyDetail();
                insertRepayDetail.setRepaymoneyid(insertRepayMoney.getId());
                insertRepayDetail.setLendmoneyid(lendMoney.getId());
                insertRepayDetail.setCurlendmoney(remain);
                insertRepayDetail.setCreatetime(currentDate);
                if (lendMoney.getLendmoney().subtract(lendMoney.getRepaidmoney()).compareTo(money) >= 0) {
                    updateLendMoney.setRepaidmoney(lendMoney.getRepaidmoney().add(money));
                    insertRepayDetail.setRepaymoney(money);
                    insertRepayDetail.setInterestmoney(BigDecimal.ZERO);
                } else {
                    // ????????????
                    BigDecimal repaidInterestMoney = money.subtract(lendMoney.getLendmoney()).add(lendMoney.getRepaidmoney());
                    insertRepayDetail.setRepaymoney(lendMoney.getLendmoney().subtract(lendMoney.getRepaidmoney()));
                    insertRepayDetail.setInterestmoney(repaidInterestMoney);
                    updateLendMoney.setRepaidmoney(lendMoney.getLendmoney().subtract(lendMoney.getRepaidmoney()).add(lendMoney.getRepaidmoney()));
                    updateLendMoney.setRepaidinterestmoney(repaidInterestMoney.add(lendMoney.getRepaidinterestmoney()));
                }
                insertRepayDetail.setNowlendmoney(insertRepayDetail.getCurlendmoney().subtract(insertRepayDetail.getRepaymoney()).subtract(insertRepayDetail.getInterestmoney()));
                this.budgetRepaymoneyDetailMapper.insert(insertRepayDetail);

                repayMoney(insertRepayMoney);
            }
            this.budgetLendmoneyMapper.updateById(updateLendMoney);
        } finally {
            lock.unLock();
        }
    }

    /**
     * ??????
     */
    public void repayMoney(BudgetRepaymoney repayMoney) throws Exception {
        BudgetArrears arrears = this.budgetArrearsMapper.selectOne(new QueryWrapper<BudgetArrears>().eq("empno", repayMoney.getEmpno()));
        if (arrears != null) {
            BudgetLendandrepaymoney lrMoney = new BudgetLendandrepaymoney();
            lrMoney.setEmpid(repayMoney.getEmpid());
            lrMoney.setEmpno(repayMoney.getEmpno());
            lrMoney.setEmpname(repayMoney.getEmpname());
            lrMoney.setRepaymoneyid(repayMoney.getId());
            lrMoney.setCurmoney(arrears.getArrearsmoeny());
            lrMoney.setMoney(repayMoney.getRepaymoney());
            lrMoney.setMoneytype(-1);
            lrMoney.setNowmoney(arrears.getArrearsmoeny().subtract(repayMoney.getRepaymoney()));
            lrMoney.setCreatetime(new Date());
            this.budgetLendandrepaymoneyMapper.insert(lrMoney);

            BudgetArrears updateArrears = new BudgetArrears();
            updateArrears.setId(arrears.getId());
            updateArrears.setArrearsmoeny(lrMoney.getNowmoney());
            updateArrears.setRepaymoney(arrears.getRepaymoney().add(lrMoney.getMoney()));
            this.budgetArrearsMapper.updateById(updateArrears);

            repayMoney.setEffectflag(true);
        }
        // ??????????????????????????????(????????????)
        List<BudgetRepaymoney> repayMoneyList = this.budgetRepaymoneyMapper.selectList(new QueryWrapper<BudgetRepaymoney>()
                .eq("empid", repayMoney.getEmpid()));
        BigDecimal totalRepayMoney = BigDecimal.ZERO;
        for (BudgetRepaymoney repay : repayMoneyList) {
            totalRepayMoney = totalRepayMoney.add(repay.getRepaymoney());
        }
        repayMoney.setNowrepaymoney(totalRepayMoney);
        this.budgetRepaymoneyMapper.updateById(repayMoney);
    }

    /**
     * ????????????
     */
    public List<List<String>> importRepayMoney(List<List<String>> excelDataList) throws Exception {
        List<ExcelBean> errorList = new ArrayList<>();
        HashMap<Long, BigDecimal> hashMap = new HashMap<>(5);
        int size = excelDataList.size();
        for (int i = 0; i < size; i++) {
            // ??????????????????????????????
            if (i < 1) {
                continue;
            }
            repayMoneyValidate(excelDataList.get(i), errorList, hashMap);
        }

        if (errorList.isEmpty()) {
            for (Map.Entry<Long, BigDecimal> entry : hashMap.entrySet()) {
                this.cashRepayMoney(entry.getKey(), entry.getValue(), null);
            }
        }
        return ExcelBean.transformList(errorList);
    }

    private void repayMoneyValidate(List<String> row, List<ExcelBean> errorList, HashMap<Long, BigDecimal> hashMap) {
        int totalColumn = 2;
        try {
            BudgetLendmoney lendMoney = null;
            BigDecimal repayMoney = null;
            int columnSize = row.size();
            if (columnSize < totalColumn) {
                throw new RuntimeException("?????????????????????");
            }
            for (int i = 1; i <= columnSize; i++) {
                String data = row.get(i - 1);
                switch (i) {
                    case 1:
                        isNotBlank(data, "????????????");
                        lendMoney = this.budgetLendmoneyMapper.selectOne(new QueryWrapper<BudgetLendmoney>()
                                .eq("lendmoneycode", data));
                        if (lendMoney == null) {
                            throw new RuntimeException("???????????????" + data + "????????????");
                        } else if (lendMoney.getEffectflag() == null || !lendMoney.getEffectflag()) {
                            throw new RuntimeException("????????????????????????!");
                        }
                        break;
                    case 2:
                        isNotBlank(data, "????????????");
                        try {
                            repayMoney = new BigDecimal(data);
                            if (repayMoney.compareTo(BigDecimal.ZERO) > 0) {
                                BigDecimal remain = lendMoney.getLendmoney()
                                        .subtract(lendMoney.getRepaidmoney())
                                        .add(lendMoney.getInterestmoney())
                                        .subtract(lendMoney.getRepaidinterestmoney());
                                if (repayMoney.compareTo(remain) > 0) {
                                    throw new RuntimeException("????????????????????????????????????!");
                                }
                            } else {
                                repayMoney = null;
                            }
                        } catch (Exception e) {
                            throw new RuntimeException("????????????????????????");
                        }
                        break;
                    default:
                }
            }
            if (repayMoney != null) {
                hashMap.put(lendMoney.getId(), repayMoney);
            }
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

    /**
     * ????????????????????????
     */
    public List<List<String>> exportLendMoney(HashMap<String, Object> paramMap) {
        List<List<String>> resultList = new ArrayList<>();

        List<BudgetLendMoneyVO> list = this.budgetLendmoneyMapper.listLendMoneyPage(null, paramMap);
        list.forEach(v -> {
            List<String> row = new ArrayList<>();
            row.add(v.getLendMoneyCode());
            row.add(v.getEmpNo());
            row.add(v.getEmpName());
            row.add(NumberUtil.subZeroAndDot(v.getLendMoney()).toString());
            row.add(NumberUtil.subZeroAndDot(v.getRepaidMoney()).toString());
            row.add(NumberUtil.subZeroAndDot(v.getInterestMoney()).toString());
            row.add(NumberUtil.subZeroAndDot(v.getUnpaidMoney()).toString());
            row.add(Constants.FORMAT_10.format(v.getLendDate()));
            row.add(v.getPlanPayDate() != null ? Constants.FORMAT_10.format(v.getPlanPayDate()) : "");
            row.add(LendTypeEnum.getValue(v.getLendType()));

            resultList.add(row);
        });
        return resultList;
    }

    /**
     * ?????????????????????????????????
     */
    public PageResult<BudgetLendMoneyVO> getUserLendMoneyByBxr(String name, Integer page, Integer rows) {
        if (StringUtils.isBlank(name)) {
            return PageResult.apply(0, new ArrayList<>());
        }
        Page<BudgetLendMoneyVO> pageBean = new Page<>(page, rows);
        List<BudgetLendMoneyVO> resultList = this.budgetLendmoneyMapper.getUserLendMoneyByBxr(pageBean, name);
        resultList.forEach(v -> v.setLendTypeDesc(LendTypeEnum.getValue(v.getLendType())));
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    public int updateLendPayStatus(FkCodeRequest codeRequest, BudgetLendmoney bean) {
        int success = 0;
        bean = this.getOne(new QueryWrapper<BudgetLendmoney>().eq("requestid", codeRequest.getRequestId()));
        if (null != bean) {
            BudgetPaymoney budgetPaymoney = new BudgetPaymoney();
            budgetPaymoney.setPaymoneystatus(PaymoneyStatusEnum.RECEIVE_PAY.getType());
            budgetPaymoney.setReceiver(codeRequest.getEmpNo());
            WbUser user = this.wbUserMapper.selectUserByEmpNo(Long.valueOf(codeRequest.getEmpNo()));
            budgetPaymoney.setReceivername(null == user ? "" : user.getDisplayName());
            budgetPaymoney.setReceivetime(new Date());
            UpdateWrapper<BudgetPaymoney> wrapper = new UpdateWrapper<>();
            wrapper.eq("paymoneyobjectcode", bean.getLendmoneycode());
            wrapper.eq("paymoneyobjectid", bean.getId());
            success = this.budgetPaymoneyMapper.update(budgetPaymoney, wrapper);

        }
        return success;
    }

    // ----------------------------------------------------------------------------------------------------

    /**
     * ????????????
     */
    public void personalLendMoney(EcologyParams params) throws Exception {
        String requestId = params.getRequestid();

        if (redisClient.exist("personalLendMoney:" + requestId)) {
            throw new RuntimeException("??????????????????????????????????????????!");
        }
        redisClient.set("personalLendMoney:" + requestId, "personalLendMoney",60);

        try{
            Integer count = this.budgetLendmoneyMapper.selectCount(new QueryWrapper<BudgetLendmoney>().eq("requestid", requestId));
            if (count > 0) {
                throw new RuntimeException("????????????????????????!");
            }

            EcologyWorkFlowValue value = EcologyClient.getWorkflowValue(params);
            Map<String, String> mainTableValue = value.getMaintablevalue();
            Map<String, List<Map<String, String>>> detailTableValues = value.getDetailtablevalues();
            if (detailTableValues == null || detailTableValues.isEmpty()) {
                throw new RuntimeException("????????????????????????");
            }

            // ???????????????
            BudgetLendmoney lendMoney = createLendMoney(requestId, mainTableValue, LendTypeEnum.LEND_TYPE_11);

            // ????????????????????????????????????
            createPayMoney(lendMoney, mainTableValue, detailTableValues, "1".equals(mainTableValue.get("flag")));

            // ??????
            this.budgetSysService.lendMoney(lendMoney);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("????????????????????????!");
        }finally {
            redisClient.delete("personalLendMoney:" + requestId);
        }
    }

    /**
     * ????????????/???????????????
     */
    public void costLendMoney(EcologyParams params) throws Exception {
        String requestId = params.getRequestid();

        if (redisClient.exist("costLendMoney:" + requestId)) {
            throw new RuntimeException("??????????????????????????????????????????!");
        }
        redisClient.set("costLendMoney:" + requestId, "costLendMoney",60);

        try{
            Integer count = this.budgetLendmoneyMapper.selectCount(new QueryWrapper<BudgetLendmoney>().eq("requestid", requestId));
            if (count > 0) {
                throw new RuntimeException("????????????????????????!");
            }

            EcologyWorkFlowValue value = EcologyClient.getWorkflowValue(params);
            Map<String, String> mainTableValue = value.getMaintablevalue();
            Map<String, List<Map<String, String>>> detailTableValues = value.getDetailtablevalues();
            if (detailTableValues == null || detailTableValues.isEmpty()) {
                throw new RuntimeException("????????????????????????");
            }

            // ???????????????
            String lendType = mainTableValue.get("jklx");
            BudgetLendmoney lendMoney = createLendMoney(requestId, mainTableValue, "1".equals(lendType) ? LendTypeEnum.LEND_TYPE_14 : LendTypeEnum.LEND_TYPE_12);

            // ????????????????????????????????????
            createPayMoney(lendMoney, mainTableValue, detailTableValues, true);

            // ??????
            this.budgetSysService.lendMoney(lendMoney);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("????????????????????????!");
        }finally {
            redisClient.delete("costLendMoney:" + requestId);
        }
    }

    private void createPayMoney(BudgetLendmoney lendMoney, Map<String, String> mainTableValue, Map<String, List<Map<String, String>>> detailTableValues, boolean isMultiple) {
        // ????????????
        int payType = Integer.parseInt(mainTableValue.get("zffs"));

        // ????????????????????????????????????????????????
        WbBanks unitBank = null;
        if (!isMultiple) {
            // ??????????????????????????????????????????
            String unitBankAccount = mainTableValue.get("kpdwbankaccount");
            unitBank = this.wbBanksMapper.selectByUnitAccount(unitBankAccount);
            if (unitBank == null) {
                throw new RuntimeException("???????????????" + unitBankAccount + "????????????????????????");
            }
        }

        Date currentDate = new Date();
        for (List<Map<String, String>> list : detailTableValues.values()) {
            for (Map<String, String> detail : list) {
                // ????????? (????????????)
                String account = detail.get("jkdwbankaccount");
                String openBank = detail.get("jkdwbankname");
                String je = detail.get("je");
                WbBanks bank = this.wbBanksMapper.selectByAccount(account);
                if (bank == null) {
                    throw new RuntimeException("???????????????" + account + "????????????????????????");
                }

                // ????????? (????????????)
                if (isMultiple) {
                    String billingUnitId = detail.get("kpdw");
                    String unitBankAccount = detail.get("kpdwbankaccount");
                    if (StringUtils.isBlank(unitBankAccount)) {
                        List<WbBanks> unitBanks = this.wbBanksMapper.selectByBillingUnitId(billingUnitId);
                        unitBank = unitBanks.get(0);
                    } else {
                        unitBank = this.wbBanksMapper.selectByUnitAccount(unitBankAccount);
                    }
                    if (unitBank == null) {
                        throw new RuntimeException("???????????????" + unitBankAccount + "????????????????????????");
                    }
                }

                // ???????????????Id,????????????????????????
                BudgetPaymoney payMoney = new BudgetPaymoney();
                payMoney.setPaymoneytype(PaymoneyTypeEnum.LEND_PAY.type);
                //??????????????????????????????
                payMoney.setPaymoneystatus(PaymoneyStatusEnum.RECEIVE_PAY.type);
                payMoney.setReceivetime(new Date());
                payMoney.setPaymoney(new BigDecimal(je));
                payMoney.setPaytype(payType);
                payMoney.setCreatetime(currentDate);
                payMoney.setBunitname(unitBank.getBillingUnitName());
                payMoney.setBunitbankaccount(unitBank.getBankAccount());
                payMoney.setBunitaccountbranchcode(unitBank.getSubBranchCode());
                payMoney.setBunitaccountbranchname(unitBank.getBankName());
                payMoney.setBankaccount(bank.getBankAccount());
                payMoney.setBankaccountbranchcode(bank.getSubBranchCode());
                payMoney.setBankaccountbranchname(bank.getBankName());
                payMoney.setBankaccountname(bank.getAccountName());
                payMoney.setOpenbank(openBank);
                payMoney.setPaymoneycode(this.distributedNumber.getPaymoneyNum());
                payMoney.setLendtype(lendMoney.getLendtype());
                payMoney.setPaymoneyobjectid(lendMoney.getId());
                payMoney.setPaymoneyobjectcode(lendMoney.getLendmoneycode());
                this.budgetPaymoneyMapper.insert(payMoney);
            }
        }
    }

    private BudgetLendmoney createLendMoney(String requestId, Map<String, String> mainTableValue, LendTypeEnum lendTypeEnum) {
        String empNo = mainTableValue.get("gh");
        if (!StringUtils.isEmpty(empNo) && empNo.length() > 5) {
            empNo = empNo.substring(0, 5);
        }
        WbUser user = this.wbUserMapper.selectUserByEmpNo(Long.parseLong(empNo));
        if (user == null) {
            throw new RuntimeException("?????????" + empNo + "??????????????????");
        }

        // ????????????Id
        String yearId = mainTableValue.get("jkjb");
        // ????????????
        String lendDateStr = mainTableValue.get("jkrq");
        // ????????????
        String repayDateStr = mainTableValue.get("yjhkrq");
        // ????????????
        String remark = mainTableValue.get("jksy");
        // ????????????
        String moneyStr = mainTableValue.get("jkje");
        // ????????????
        String requestCode = mainTableValue.get("lcbh");

        if (StringUtils.isNotBlank(remark)) {
            remark = remark.replace("&nbsp;", " ");
        }

        BudgetLendmoney lendMoney = new BudgetLendmoney();
        lendMoney.setYearid(Long.valueOf(yearId));
        lendMoney.setEmpid(user.getUserId());
        lendMoney.setEmpno(empNo);
        lendMoney.setEmpname(user.getDisplayName());
        lendMoney.setOperatorEmpId(user.getUserId());
        lendMoney.setOperatorEmpNo(empNo);
        lendMoney.setOperatorEmpName(user.getDisplayName());
        lendMoney.setDeptid(user.getDeptId());
        lendMoney.setDeptname(user.getDeptName());
        lendMoney.setLendtype(lendTypeEnum.getType());
        lendMoney.setLendmoney(new BigDecimal(moneyStr));
        lendMoney.setRepaidmoney(BigDecimal.ZERO);
        try {
            lendMoney.setLenddate(new Date());
            lendMoney.setPlanpaydate(Constants.FORMAT_10.parse(repayDateStr));
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException("???????????????????????????");
        }
        lendMoney.setCreatetime(new Date());
        lendMoney.setRemark(remark);
        lendMoney.setRequestid(requestId);
        lendMoney.setDeleteflag(false);
        lendMoney.setInterestmoney(BigDecimal.ZERO);
        lendMoney.setFlushingflag(false);
        lendMoney.setEffectflag(true);
        lendMoney.setChargebillflag(false);
        lendMoney.setMakeaccountflag(false);
        lendMoney.setLendmoneycode(distributedNumber.getLendNum());
        lendMoney.setRequestcode(requestCode);

        if (LendTypeEnum.LEND_TYPE_11.getType() == lendTypeEnum.getType()) {
            // ???????????? 0 ???????????????2 ?????????????????????3 ??????????????????4 ???????????????
            String moneyType = mainTableValue.get("jklx");
            int personalType = 0;
            switch (moneyType) {
                case "0":
                    personalType = 1;
                    break;
                case "2":
                    personalType = 2;
                    break;
                case "3":
                    personalType = 3;
                    break;
                case "4":
                    personalType = 4;
                    break;
                default:
            }
            lendMoney.setPersonaltype(personalType);
        } else {
            String lendType = mainTableValue.get("jklx");
            lendMoney.setIsbyj("1".equals(lendType));
        }

        this.budgetLendmoneyMapper.insert(lendMoney);

        return lendMoney;
    }

    /**
     * ????????????????????????
     */
    public void projectLendMoney(EcologyParams params) {
        EcologyWorkFlowValue value = EcologyClient.getWorkflowValue(params);
        Map<String, String> mainTableValue = value.getMaintablevalue();

        // ????????????id
        long unitId = Long.parseLong(mainTableValue.get("xmssysdw"));
        // ????????????
        int lendType = Integer.parseInt(mainTableValue.get("jklx"));
        // ??????????????????
        String bankAccount = mainTableValue.get("kpdwbankaccount");
        // ????????????
        String projectName = mainTableValue.get("jkxm");
        // ????????????
        String cashStr = mainTableValue.get("xjje");
        // ????????????
        String transferStr = mainTableValue.get("zzje");
        // ????????????
        String giftStr = mainTableValue.get("swje");
        // ????????????
        String sumStr = mainTableValue.get("hjje");
        // ???????????????
        String empNo = mainTableValue.get("gh");
        if (!StringUtils.isEmpty(empNo) && empNo.length() > 5) {
            empNo = empNo.substring(0, 5);
        }

        BudgetUnit budgetUnit = this.budgetUnitMapper.selectById(unitId);
        if (budgetUnit == null) {
            throw new RuntimeException("????????????????????????");
        }

        WbUser user = this.wbUserMapper.selectOne(new QueryWrapper<WbUser>().eq("user_name", empNo));
        if (user == null) {
            throw new RuntimeException("?????????" + empNo + "??????????????????");
        }

        // ??????????????????
        WbBanks unitBank = this.wbBanksMapper.selectByUnitAccount(bankAccount);
        if (unitBank == null) {
            throw new RuntimeException("?????????????????????" + bankAccount + "????????????????????????");
        }

        // 1 ???????????? 2???????????? 3????????????
        int type = 1;

        // ??????????????????
        BudgetProject project = this.budgetProjectMapper.selectOne(new QueryWrapper<BudgetProject>()
                .eq("yearid", budgetUnit.getYearid())
                .eq("unitids", budgetUnit.getId())
                .eq("type", type)
                .eq("name", projectName));
        if (project == null) {
            project = new BudgetProject();
            project.setId(null);
            project.setName(projectName);
            project.setType(type);
            project.setStopflag(false);
            project.setYearid(budgetUnit.getYearid());
            project.setUnitids(unitId + "");
            project.setUnitnames(budgetUnit.getName());
            project.setCreater(user.getUserName());
            project.setCreatername(user.getDisplayName());
            project.setCreatetime(new Date());
            project.setLendtype(lendType);
            project.setProjectno(this.distributedNumber.getXmNum());
            this.budgetProjectMapper.insert(project);
        }

        // ????????????
        BudgetProjectlendsum lendSum = new BudgetProjectlendsum();
        lendSum.setProjectid(project.getId());
        lendSum.setProjectno(project.getProjectno());
        lendSum.setProjectname(project.getName());
        lendSum.setYearid(budgetUnit.getYearid());
        lendSum.setUnitid(unitId);
        lendSum.setType(type);
        lendSum.setPaymoneyunitid(unitBank.getBillingUnitId());
        lendSum.setCashmoney(StringUtils.isNotEmpty(cashStr) ? new BigDecimal(cashStr) : BigDecimal.ZERO);
        lendSum.setTransfermoney(StringUtils.isNotEmpty(transferStr) ? new BigDecimal(transferStr) : BigDecimal.ZERO);
        lendSum.setGiftmoney(StringUtils.isNotEmpty(giftStr) ? new BigDecimal(giftStr) : BigDecimal.ZERO);
        lendSum.setTotal(new BigDecimal(sumStr));
        lendSum.setVerifyflag(0);
        lendSum.setCreator(user.getUserName());
        lendSum.setCreatorname(user.getDisplayName());
        lendSum.setCreatetime(new Date());
        lendSum.setSubmitbxstatus(0);
        this.budgetProjectlendsumMapper.insert(lendSum);

        // ????????????????????????????????????????????????0?????????????????????????????????
        if (StringUtils.isNotBlank(cashStr) && new BigDecimal(cashStr).compareTo(BigDecimal.ZERO) > 0) {
//            // ??????????????????
//            String shouAccountId = mainTableValue.get("zhmc");
//            // ?????????????????????
//            String shouBank = mainTableValue.get("khh");
            // ??????????????????
            String shouAccount = mainTableValue.get("yhzh");
            if (StringUtils.isBlank(shouAccount)) {
                // ??????????????????????????????
                return;
            }

            WbBanks bank = this.wbBanksMapper.selectByAccount(shouAccount);
            if (bank == null) {
                throw new RuntimeException("?????????????????????" + shouAccount + "????????????????????????");
            }

            BudgetPaymoney payMoney = new BudgetPaymoney();
            payMoney.setPaymoneytype(PaymoneyTypeEnum.LEND_PAY.type);
            payMoney.setPaymoneyobjectcode(project.getProjectno());
            payMoney.setPaymoneyobjectid(lendSum.getId());
            payMoney.setPaymoney(new BigDecimal(cashStr));
            payMoney.setPaytype(0);
            payMoney.setPaymoneystatus(PaymoneyStatusEnum.RECEIVE_PAY.type);
            payMoney.setReceivetime(new Date());
            payMoney.setLendtype(LendTypeEnum.LEND_TYPE_13.getType());
            payMoney.setBunitname(unitBank.getBillingUnitName());
            payMoney.setBunitbankaccount(unitBank.getBankAccount());
            payMoney.setBunitaccountbranchcode(unitBank.getSubBranchCode());
            payMoney.setBunitaccountbranchname(unitBank.getBankName());
            payMoney.setBankaccount(bank.getBankAccount());
            payMoney.setBankaccountbranchcode(bank.getSubBranchCode());
            payMoney.setBankaccountbranchname(bank.getBankName());
            payMoney.setBankaccountname(bank.getAccountName());
            payMoney.setOpenbank(project.getName());
            payMoney.setRemark("????????????????????????");
            payMoney.setCreatetime(new Date());
            payMoney.setPaymoneycode(this.distributedNumber.getPaymoneyNum());
            this.budgetPaymoneyMapper.insert(payMoney);
        }
    }

    /**
     * ?????????????????????????????????
     */
    public List<Map<String, Object>> getBudgetLendMoneyList(Map<String, Object> params) {
        if (params.get("empno") == null) {
            throw new RuntimeException("??????????????????");
        }

        List<Map<String, Object>> blmList = this.budgetLendmoneyMapper.getBudgetLendMoneyList(params);
        blmList = blmList.stream().filter(e -> {
            int lendType = Integer.parseInt(e.get("lendtype").toString());
            if (lendType == LendTypeEnum.LEND_TYPE_13.getType()) {
                return e.get("chargebillflag") != null && "true".equals(e.get("chargebillflag").toString());
            }
            return true;
        }).peek(e -> {
            int lendType = Integer.parseInt(e.get("lendtype").toString());
            e.put("lendTypeName", LendTypeEnum.getValue(lendType));
        }).collect(Collectors.toList());
        return blmList;
    }

    /**
     * ????????????????????????????????????
     */
    public List<Map<String, Object>> getRepayMoneyList(Integer lendMoneyId, String startDate, String endDate) {
        return this.budgetLendmoneyMapper.getRepayMoneyList(lendMoneyId, startDate, endDate);
    }

    /**
     * ??????????????????
     */
    public void moblieRepayMoney(Long lendMoneyId, String repayMoney) throws Exception {
        this.cashRepayMoney(lendMoneyId, new BigDecimal(repayMoney), null);
    }
}
