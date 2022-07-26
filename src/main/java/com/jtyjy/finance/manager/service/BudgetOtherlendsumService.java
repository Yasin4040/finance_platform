package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.easyexcel.BudgetOtherLendExcelData;
import com.jtyjy.finance.manager.enmus.LendTypeEnum;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.trade.DistributedNumber;
import com.jtyjy.finance.manager.vo.BudgetLendMoneyVO;
import com.jtyjy.finance.manager.vo.BudgetOtherLendSumVO;
import com.jtyjy.finance.manager.vo.ExcelBean;
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
public class BudgetOtherlendsumService extends DefaultBaseService<BudgetOtherlendsumMapper, BudgetOtherlendsum> {

    private final TabChangeLogMapper loggerMapper;
    private final WbDeptMapper wbDeptMapper;
    private final WbPersonMapper wbPersonMapper;
    private final BudgetContractMapper budgetContractMapper;
    private final BudgetLendmoneyMapper budgetLendmoneyMapper;
    private final BudgetBankAccountMapper budgetBankAccountMapper;
    private final BudgetOtherlendsumMapper budgetOtherlendsumMapper;
    private final BudgetProjectlendsumMapper budgetProjectlendsumMapper;
    private final BudgetYearPeriodMapper budgetYearPeriodMapper;
    private final BudgetUnitMapper budgetUnitMapper;
    private final BudgetProjectMapper budgetProjectMapper;

    private final WbUserService wbUserService;
    private final DistributedNumber distributedNumber;
    private final BudgetSysService budgetSysService;

    @Override
    public BaseMapper<TabChangeLog> getLoggerMapper() {
        return loggerMapper;
    }

    @Override
    public void setBaseLoggerBean() {
        DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_otherlendsum"));
    }

    /**
     * 查询其它借款（分页）
     */
    public PageResult<BudgetOtherLendSumVO> listOtherLendPage(Integer page, Integer rows, String name, String status) {
        Page<BudgetOtherLendSumVO> pageBean = new Page<>(page, rows);
        HashMap<String, Object> paramMap = new HashMap<>(5);
        paramMap.put("name", name);
        paramMap.put("status", StringUtils.isNotBlank(status) ? Integer.parseInt(status) : null);
        List<BudgetOtherLendSumVO> resultList = this.budgetOtherlendsumMapper.listOtherLendPage(pageBean, paramMap);
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    /**
     * 审核
     */
    public void verify(Long id) throws Exception {
        BudgetOtherlendsum otherLend = this.budgetOtherlendsumMapper.selectById(id);
        if (otherLend == null) {
            throw new RuntimeException("不存在该其它借款");
        } else if (otherLend.getStatus() == 1) {
            throw new RuntimeException("该批次借款已被审核");
        }

        WbUser user = UserThreadLocal.get();

        BudgetOtherlendsum updateOtherLend = new BudgetOtherlendsum();
        updateOtherLend.setId(otherLend.getId());
        updateOtherLend.setVerifyor(user.getUserName());
        updateOtherLend.setVerifyname(user.getDisplayName());
        updateOtherLend.setVerifytime(new Date());
        updateOtherLend.setStatus(1);
        this.budgetOtherlendsumMapper.updateById(updateOtherLend);

        HashMap<Long, BudgetProjectlendsum> projectLendMap = new HashMap<>(5);
        HashMap<Long, BudgetContract> contractMap = new HashMap<>(5);
        List<BudgetLendmoney> lendMoneyList = this.budgetLendmoneyMapper.selectList(new QueryWrapper<BudgetLendmoney>().eq("deleteflag", 0).eq("otherlendsumid", id));
        for (BudgetLendmoney v : lendMoneyList) {
            if (v.getLendtype() == LendTypeEnum.LEND_TYPE_13.getType()) {
                BudgetProjectlendsum updateProjectLendSum = null;
                if (projectLendMap.containsKey(v.getProjectlendsumid())) {
                    updateProjectLendSum = projectLendMap.get(v.getProjectlendsumid());
                } else {
                    BudgetProjectlendsum projectLend = this.budgetProjectlendsumMapper.selectById(v.getProjectlendsumid());
                    if (projectLend != null) {
                        updateProjectLendSum = new BudgetProjectlendsum();
                        updateProjectLendSum.setId(projectLend.getId());
                        updateProjectLendSum.setCashmoney(projectLend.getCashmoney() != null ? projectLend.getCashmoney() : BigDecimal.ZERO);
                        updateProjectLendSum.setTransfermoney(projectLend.getTransfermoney() != null ? projectLend.getTransfermoney() : BigDecimal.ZERO);
                        updateProjectLendSum.setGiftmoney(projectLend.getGiftmoney() != null ? projectLend.getGiftmoney() : BigDecimal.ZERO);
                        projectLendMap.put(v.getProjectlendsumid(), updateProjectLendSum);
                    }
                }

                if (updateProjectLendSum != null) {
                    switch (v.getProjectlendtype()) {
                        case "1":
                            updateProjectLendSum.setCashmoney(updateProjectLendSum.getCashmoney().add(v.getLendmoney()));
                            break;
                        case "2":
                            updateProjectLendSum.setTransfermoney(updateProjectLendSum.getTransfermoney().add(v.getLendmoney()));
                            break;
                        case "3":
                            updateProjectLendSum.setGiftmoney(updateProjectLendSum.getGiftmoney().add(v.getLendmoney()));
                            break;
                        default:
                    }
                }
            }

            if (v.getLendtype() == LendTypeEnum.LEND_TYPE_15.getType()) {
                BudgetContract updateContract = null;
                if (contractMap.containsKey(v.getContractid())) {
                    updateContract = contractMap.get(v.getContractid());
                } else {
                    BudgetContract contract = this.budgetContractMapper.selectById(v.getContractid());
                    if (contract != null) {
                        updateContract = new BudgetContract();
                        updateContract.setId(contract.getId());
                        updateContract.setContractmoney(contract.getContractmoney() != null ? contract.getContractmoney() : 0f);
                        contractMap.put(v.getContractid(), updateContract);
                    }
                }

                if (updateContract != null) {
                    BigDecimal bigDecimal = new BigDecimal(Float.toString(updateContract.getContractmoney()));
                    updateContract.setContractmoney(bigDecimal.add(v.getLendmoney()).floatValue());
                }
            }
            this.budgetSysService.lendMoney(v);
        }

        // 统计项目借款现金、转账、礼品金额
        for (BudgetProjectlendsum projectLendSum : projectLendMap.values()) {
            projectLendSum.setVerifyflag(1);
            projectLendSum.setTotal(projectLendSum.getCashmoney().add(projectLendSum.getTransfermoney().add(projectLendSum.getGiftmoney())));
            this.budgetProjectlendsumMapper.updateById(projectLendSum);
        }

        // 统计合同借款金额
        for (BudgetContract contract : contractMap.values()) {
            // 仅更新非OA流程的合同签订金额
            if (contract.getRequestid() == null) {
                this.budgetContractMapper.updateById(contract);
            }
        }
    }

    /**
     * 其它借款导入
     */
    public List<List<String>> importOtherLend(List<List<String>> excelDataList) {
        List<BudgetOtherLendExcelData> successList = new ArrayList<>();
        List<ExcelBean> errorList = new ArrayList<>();
        int size = excelDataList.size();
        for (int i = 0; i < size; i++) {
            // 表格正文从第二行开始
            if (i < 1) {
                continue;
            }
            // 1、校验表格内容格式是否填写正确
            validateOtherLend(excelDataList.get(i), successList, errorList);
        }

        if (errorList.isEmpty() && !successList.isEmpty()) {
            // 二次校验
            long count = successList.stream().map(BudgetOtherLendExcelData::getImportBatchNumber).distinct().count();
            if (count > 1) {
                throw new RuntimeException("导入批次号填写需一致");
            }

            // 查询导入批次
            String importBatchNumber = successList.get(0).getImportBatchNumber();
            BudgetOtherlendsum otherLend = this.budgetOtherlendsumMapper.selectOne(new QueryWrapper<BudgetOtherlendsum>()
                    .eq("importbatchnumber", importBatchNumber));
            if (otherLend != null && otherLend.getStatus() == 1) {
                throw new RuntimeException("该批次已被审核, 无法导入");
            }

            HashSet<String> hashSet = new HashSet<>();
            for (int i = 0; i < size; i++) {
                // 表格正文从第二行开始
                if (i < 1) {
                    continue;
                }
                // 2、校验工号、姓名是否存在以及匹配正确（项目借款再校验届别、预算单位是否存在）
                validateOtherLend2(excelDataList.get(i), successList.get(i - 1), errorList, hashSet);
            }

            if (errorList.isEmpty()) {
                if (otherLend == null) {
                    WbUser user = UserThreadLocal.get();
                    otherLend = new BudgetOtherlendsum();
                    otherLend.setStatus(0);
                    otherLend.setImportor(user.getUserName());
                    otherLend.setImportorname(user.getDisplayName());
                    otherLend.setImporttime(new Date());
                    otherLend.setImportbatchnumber(importBatchNumber);
                    this.budgetOtherlendsumMapper.insert(otherLend);
                }

                HashMap<String, BudgetContract> contractMap = new HashMap<>(2);
                HashMap<String, BudgetProjectlendsum> projectLendMap = new HashMap<>(2);
                for (BudgetOtherLendExcelData excelData : successList) {
                    // 3、校验合同借款导入、项目借款导入是否存在
                    validateOtherLend3(excelData, contractMap, projectLendMap);
                }

                // 4、校验合同借款导入、项目借款导入数据是否超额、重复
                validateOtherLend4(successList);

                // 保存借款记录
                saveOtherLend(otherLend, successList);
            }
        }
        return ExcelBean.transformList(errorList);
    }

    private void saveOtherLend(BudgetOtherlendsum otherLend, List<BudgetOtherLendExcelData> successList) {
        WbUser operator = UserThreadLocal.get();

        // 新增借款记录
        Date currentDate = new Date();
        for (BudgetOtherLendExcelData v : successList) {
            try {
                BudgetLendmoney insertLendMoney = new BudgetLendmoney();
                insertLendMoney.setDeleteflag(false);
                insertLendMoney.setEmpid(v.getEmpId());
                insertLendMoney.setEmpno(v.getEmpNo());
                insertLendMoney.setEmpname(v.getEmpName());
                insertLendMoney.setOperatorEmpId(operator.getUserId());
                insertLendMoney.setOperatorEmpNo(operator.getUserName());
                insertLendMoney.setOperatorEmpName(operator.getDisplayName());

                if (v.getLendType() != LendTypeEnum.LEND_TYPE_16.getType() && v.getEmpId() != null) {
                    WbPerson wbPerson = this.wbPersonMapper.selectOne(new QueryWrapper<WbPerson>().eq("user_id", v.getEmpId()));
                    WbDept dept = this.wbDeptMapper.selectById(wbPerson.getDeptId());
                    insertLendMoney.setDeptid(dept.getDeptId());
                    insertLendMoney.setDeptname(dept.getDeptName());
                } else {
                    insertLendMoney.setDeptid(v.getBankAccountName());
                    insertLendMoney.setDeptname(null);
                }
                insertLendMoney.setLendmoneycode(this.distributedNumber.getLendNum());
                insertLendMoney.setEffectflag(false);
                insertLendMoney.setLendtype(v.getLendType());
                insertLendMoney.setLendmoney(v.getLendMoney());
                insertLendMoney.setRepaidinterestmoney(BigDecimal.ZERO);
                insertLendMoney.setRepaidmoney(BigDecimal.ZERO);

                insertLendMoney.setLenddate(v.getLendDate());
                insertLendMoney.setPlanpaydate(v.getPlanPayDate());

                insertLendMoney.setCreatetime(currentDate);
                insertLendMoney.setRemark(v.getRemark());
                insertLendMoney.setInterestmoney(BigDecimal.ZERO);
                insertLendMoney.setOtherlendsumid(otherLend.getId());

                if (v.getLendType() == LendTypeEnum.LEND_TYPE_13.getType()) {
                    insertLendMoney.setYearid(v.getYearId());
                    insertLendMoney.setProjectlendsumid(v.getProjectLendSumId());
                    insertLendMoney.setConfirmflag(false);
                    insertLendMoney.setProjectlendtype(v.getProjectLendType());

                    // 判断是否已存在借款
                    BudgetLendmoney existLend = this.budgetLendmoneyMapper.selectOne(new QueryWrapper<BudgetLendmoney>()
                            .eq("projectlendsumid", v.getProjectLendSumId())
                            .eq("projectlendtype", v.getProjectLendType())
                            .eq("empno", v.getEmpNo()));
                    if (existLend != null) {
                        insertLendMoney.setId(existLend.getId());
                        insertLendMoney.setDeleteflag(false);
                        this.budgetLendmoneyMapper.updateById(insertLendMoney);
                        continue;
                    }
                } else if (v.getLendType() == LendTypeEnum.LEND_TYPE_15.getType()) {
                    insertLendMoney.setContractid(v.getContractId());
                }
                this.budgetLendmoneyMapper.insert(insertLendMoney);
            } catch (Exception e) {
                e.printStackTrace();
                if (e.getMessage().contains("Duplicate entry")) {
                    throw new RuntimeException("员工【" + v.getEmpName() + "(" + v.getEmpNo() + ")】存在借款类型、借款金额、借款时间都相同的借款记录");
                }
                throw e;
            }
        }
    }

    private void validateOtherLend4(List<BudgetOtherLendExcelData> successList) {
        // 项目借款校验 同一个人同项目借款类型只能有一条记录
        List<BudgetOtherLendExcelData> projectLendList = successList.stream()
                .filter(v -> v.getLendType() == LendTypeEnum.LEND_TYPE_13.getType())
                .collect(Collectors.toList());
        if (!projectLendList.isEmpty()) {
            long duplication = projectLendList.stream()
                    .map(v -> v.getProjectLendSumId() + v.getEmpNo() + v.getProjectLendType())
                    .distinct().count();
            if (duplication != projectLendList.size()) {
                throw new RuntimeException("销售政策支持借款, 同一员工存在多条相同项目借款类型记录!");
            }
        }

        // 合同借款校验 如果是oa流程借款，借款金额不允许超出合同签订总金额
        List<BudgetOtherLendExcelData> contractList = successList.stream()
                .filter(v -> v.getLendType() == LendTypeEnum.LEND_TYPE_15.getType())
                .collect(Collectors.toList());
        if (!contractList.isEmpty()) {
            Map<Long, List<BudgetOtherLendExcelData>> contractMap = contractList.stream().collect(Collectors.groupingBy(BudgetOtherLendExcelData::getContractId));
            for (Map.Entry<Long, List<BudgetOtherLendExcelData>> entry : contractMap.entrySet()) {
                BudgetContract contract = this.budgetContractMapper.selectById(entry.getKey());
                if (contract.getRequestid() != null) {
                    // 当前导入合同借款总金额
                    BigDecimal importMoney = entry.getValue().stream().map(BudgetOtherLendExcelData::getLendMoney).reduce(BigDecimal.ZERO, BigDecimal::add);

                    // 已保存成功的借款金额
                    List<BudgetLendmoney> existList = this.budgetLendmoneyMapper.selectList(new QueryWrapper<BudgetLendmoney>().eq("contractid", contract.getId()));
                    BigDecimal lendMoney = existList.stream().map(BudgetLendmoney::getLendmoney).reduce(BigDecimal.ZERO, BigDecimal::add);

                    // 如果借款金额大于合同签订金额，不允许通过
                    BigDecimal totalLendMoney = importMoney.add(lendMoney);
                    BigDecimal contractMoney = new BigDecimal(Float.toString(contract.getContractmoney()));
                    if (totalLendMoney.compareTo(contractMoney) > 0) {
                        throw new RuntimeException("已存在的借款和现在导入的借款总金额【" + totalLendMoney.setScale(2, BigDecimal.ROUND_HALF_UP)
                                + "】大于合同【" + contract.getContractname()
                                + "】的签订金额【" + contractMoney.setScale(2, BigDecimal.ROUND_HALF_UP)
                                + "】");
                    }
                }
            }
        }

    }

    private void validateOtherLend3(BudgetOtherLendExcelData
                                            excelData, HashMap<String, BudgetContract> contractMap, HashMap<String, BudgetProjectlendsum> projectLendMap) {
        // 合同信息验证
        if (excelData.getLendType() == LendTypeEnum.LEND_TYPE_15.getType()) {
            String key = excelData.getContractNo() + "-" + excelData.getContractName();
            if (!contractMap.containsKey(key)) {
                BudgetContract budgetContract1 = this.budgetContractMapper.selectOne(new QueryWrapper<BudgetContract>()
                        .eq("contractcode", excelData.getContractNo()));
                BudgetContract budgetContract2 = this.budgetContractMapper.selectOne(new QueryWrapper<BudgetContract>()
                        .eq("contractname", excelData.getContractName()));
                if (budgetContract1 == null && budgetContract2 == null) {
                    // 合同编号与合同名称都不存在
                    BudgetContract budgetContract = new BudgetContract();
                    budgetContract.setId(null);
                    budgetContract.setContractname(excelData.getContractName());
                    budgetContract.setContractcode(excelData.getContractNo());
                    budgetContract.setContractmoney(0f);
                    budgetContract.setSigndate(new Date());
                    budgetContract.setTerminationflag(0);
                    budgetContract.setOtherpartyunit("");
                    budgetContract.setCreatetime(new Date());
                    budgetContract.setContracttype("0");
                    this.budgetContractMapper.insert(budgetContract);

                    contractMap.put(key, budgetContract);
                } else if (budgetContract1 != null && budgetContract2 != null) {
                    // 合同编号与合同名称都存在
                    if (!budgetContract1.getId().equals(budgetContract2.getId())) {
                        throw new RuntimeException("合同编号【" + excelData.getContractNo() + "】与合同名称【" + excelData.getContractName() + "】不匹配,请检查后重试");
                    }
                    contractMap.put(key, budgetContract1);
                } else {
                    // 合同编号与合同名称只存在其一
                    throw new RuntimeException("合同编号【" + excelData.getContractNo() + "】与合同名称【" + excelData.getContractName() + "】不匹配,请检查后重试");
                }
            }
            excelData.setContractId(contractMap.get(key).getId());
        }

        // 项目信息验证
        if (excelData.getLendType() == LendTypeEnum.LEND_TYPE_13.getType()) {
            String key = excelData.getYearName() + "-" + excelData.getUnitName() + "-" + excelData.getProjectName() + "-" + excelData.getProjectType();
            if (!projectLendMap.containsKey(key)) {
                BudgetProject budgetProject = this.budgetProjectMapper.selectOne(new QueryWrapper<BudgetProject>()
                        .eq("yearid", excelData.getYearId())
                        .eq("unitids", excelData.getUnitId())
                        .eq("name", excelData.getProjectName())
                        .eq("type", excelData.getProjectType()));
                if (budgetProject == null) {
                    budgetProject = new BudgetProject();
                    budgetProject.setName(excelData.getProjectName());
                    budgetProject.setType(excelData.getProjectType());
                    budgetProject.setStopflag(false);
                    budgetProject.setYearid(excelData.getYearId());
                    budgetProject.setUnitids(excelData.getUnitId().toString());
                    budgetProject.setUnitnames(excelData.getUnitName());
                    budgetProject.setCreatetime(new Date());
                    budgetProject.setProjectno(this.distributedNumber.getXmNum());
                    this.budgetProjectMapper.insert(budgetProject);
                }

                BudgetProjectlendsum projectLendSum = this.budgetProjectlendsumMapper.selectOne(new QueryWrapper<BudgetProjectlendsum>()
                        .eq("projectid", budgetProject.getId())
                        .eq("yearid", excelData.getYearId())
                        .eq("unitid", excelData.getUnitId()));
                if (projectLendSum == null) {
                    projectLendSum = new BudgetProjectlendsum();
                    projectLendSum.setProjectid(budgetProject.getId());
                    projectLendSum.setProjectno(budgetProject.getProjectno());
                    projectLendSum.setProjectname(budgetProject.getName());
                    projectLendSum.setType(budgetProject.getType());
                    projectLendSum.setYearid(excelData.getYearId());
                    projectLendSum.setUnitid(excelData.getUnitId());
                    projectLendSum.setVerifyflag(0);
                    projectLendSum.setCreatetime(new Date());
                    projectLendSum.setSubmitbxstatus(0);
                    this.budgetProjectlendsumMapper.insert(projectLendSum);
                } else if (projectLendSum.getVerifyflag() == 1) {
                    // 如果该项目借款已审核，不允许导入借款
                    throw new RuntimeException("销售政策支持借款【" + projectLendSum.getProjectname() + "】已审核通过, 不允许导入借款信息");
                }
                projectLendMap.put(key, projectLendSum);
            }
            BudgetProjectlendsum projectLendSum = projectLendMap.get(key);
            excelData.setYearId(projectLendSum.getYearid());
            excelData.setProjectLendSumId(projectLendSum.getId());
        }
    }

    private void validateOtherLend2(List<String> row, BudgetOtherLendExcelData excelData, List<ExcelBean> errorList, HashSet<String> hashSet) {
        try {
            // 借款账户验证
            if (excelData.getLendType() != LendTypeEnum.LEND_TYPE_15.getType() && excelData.getLendType() != LendTypeEnum.LEND_TYPE_16.getType()) {
                // 员工工号和姓名验证
                WbUser user = this.wbUserService.validateUser(excelData.getEmpNo(), excelData.getEmpName());
                excelData.setEmpId(user.getUserId());
            } else {
                // 银行账户验证
                List<BudgetBankAccount> bankAccounts = this.budgetBankAccountMapper.selectList(new QueryWrapper<BudgetBankAccount>()
                        .eq("code", excelData.getEmpNo())
                        .eq("stopflag", 0));
                if (bankAccounts.isEmpty()) {
                    throw new RuntimeException("编号【" + excelData.getEmpNo() + "】不存在!");
                }
                long account = bankAccounts.stream().filter(e -> e.getAccountname().equals(excelData.getEmpName())).count();
                if (account == 0) {
                    throw new RuntimeException("编号【" + excelData.getEmpNo() + "】与户名【" + excelData.getEmpName() + "】不符!");
                }
                BudgetBankAccount bankAccount = bankAccounts.get(0);
                excelData.setBankAccountId(bankAccount.getId().toString());
                excelData.setBankAccountName(bankAccount.getAccountname());
            }

            // 届别预算单位验证
            if (excelData.getLendType() == LendTypeEnum.LEND_TYPE_13.getType()) {
                String key = excelData.getYearName() + "-" + excelData.getUnitName();
                if (!hashSet.contains(key)) {
                    BudgetYearPeriod yearPeriod = this.budgetYearPeriodMapper.selectOne(new QueryWrapper<BudgetYearPeriod>()
                            .eq("period", excelData.getYearName()));
                    if (yearPeriod == null) {
                        throw new RuntimeException("届别【" + excelData.getYearName() + "】不存在");
                    }
                    BudgetUnit budgetUnit = this.budgetUnitMapper.selectOne(new QueryWrapper<BudgetUnit>()
                            .eq("yearid", yearPeriod.getId())
                            .eq("name", excelData.getUnitName()));
                    if (budgetUnit == null) {
                        throw new RuntimeException("届别【" + yearPeriod.getPeriod() + "】下不存在预算单位【" + excelData.getUnitName() + "】");
                    }
                    excelData.setYearId(yearPeriod.getId());
                    excelData.setUnitId(budgetUnit.getId());
                    hashSet.add(key);
                }
            }
        } catch (Exception e) {
            // 解决异常: Transaction rolled back because it has been marked as rollback-only
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            errorList.add(ExcelBean.transformBean(row, 15, e.getMessage()));
        }
    }

    private void validateOtherLend(List<String> row, List<BudgetOtherLendExcelData> successList, List<ExcelBean> errorList) {
        int totalColumn = 8;
        try {
            int columnSize = row.size();
            if (columnSize < totalColumn) {
                throw new RuntimeException("内容填写不完整");
            }
            BudgetOtherLendExcelData excelData = new BudgetOtherLendExcelData();
            for (int i = 1; i <= columnSize; i++) {
                String data = row.get(i - 1);
                switch (i) {
                    case 1:
                        isNotBlank(data, "工号");
                        excelData.setEmpNo(data);
                        break;
                    case 2:
                        isNotBlank(data, "姓名");
                        excelData.setEmpName(data);
                        break;
                    case 3:
                        isNotBlank(data, "借款金额");
                        try {
                            BigDecimal money = new BigDecimal(data);
                            if (BigDecimal.ZERO.compareTo(money) > 0) {
                                throw new RuntimeException("借款金额不能小于等于零");
                            }
                            excelData.setLendMoney(money);
                        } catch (Exception ignored) {
                            throw new RuntimeException("借款金额格式错误");
                        }
                        break;
                    case 4:
                        isNotBlank(data, "借款事由");
                        excelData.setRemark(data);
                        break;
                    case 5:
                        isNotBlank(data, "导入批次号");
                        if (!data.matches("\\d{8}")) {
                            throw new RuntimeException("批次号格式错误");
                        }
                        excelData.setImportBatchNumber(data);
                        break;
                    case 6:
                        isNotBlank(data, "借款日期");
                        try {
                            Date lendDate = Constants.FORMAT_10.parse(data);
                            excelData.setLendDate(lendDate);
                        } catch (Exception ignored) {
                            throw new RuntimeException("借款日期格式错误");
                        }
                        break;
                    case 7:
                        isNotBlank(data, "还款日期");
                        try {
                            Date planPayDate = Constants.FORMAT_10.parse(data);
                            excelData.setPlanPayDate(planPayDate);
                        } catch (Exception ignored) {
                            throw new RuntimeException("计划还款日期格式错误");
                        }
                        if (excelData.getPlanPayDate().compareTo(excelData.getLendDate()) <= 0) {
                            throw new RuntimeException("还款日期应该在借款日期之后!");
                        }
                        break;
                    case 8:
                        isNotBlank(data, "借款类型");
                        // 11：个人借款 12：费用借款 13：销售政策支持借款申请 14：备用金借款 15: 合同借款 16：非合同借款
                        switch (data) {
                            case "个人借款":
                                excelData.setLendType(11);
                                break;
                            case "费用借款":
                                excelData.setLendType(12);
                                break;
                            case "销售政策支持借款申请":
                                excelData.setLendType(13);
                                break;
                            case "备用金借款":
                                excelData.setLendType(14);
                                break;
                            case "合同借款":
                                excelData.setLendType(15);
                                break;
                            case "非合同借款":
                                excelData.setLendType(16);
                                break;
                            default:
                                throw new RuntimeException("借款类型【个人借款、费用借款、销售政策支持借款申请、备用金借款、合同借款、非合同借款】不匹配");
                        }
                        break;
                    case 9:
                        if (excelData.getLendType() == LendTypeEnum.LEND_TYPE_15.getType()) {
                            isNotBlank(data, "合同编号");
                            excelData.setContractNo(data);
                        }
                        break;
                    case 10:
                        if (excelData.getLendType() == LendTypeEnum.LEND_TYPE_15.getType()) {
                            isNotBlank(data, "合同名称");
                            excelData.setContractName(data);
                        }
                        break;
                    case 11:
                        if (excelData.getLendType() == LendTypeEnum.LEND_TYPE_13.getType()) {
                            isNotBlank(data, "届别");
                            excelData.setYearName(data);
                        }
                        break;
                    case 12:
                        if (excelData.getLendType() == LendTypeEnum.LEND_TYPE_13.getType()) {
                            isNotBlank(data, "预算单位");
                            excelData.setUnitName(data);
                        }
                        break;
                    case 13:
                        if (excelData.getLendType() == LendTypeEnum.LEND_TYPE_13.getType()) {
                            isNotBlank(data, "项目名称");
                            excelData.setProjectName(data);
                        }
                        break;
                    case 14:
                        if (excelData.getLendType() == LendTypeEnum.LEND_TYPE_13.getType()) {
                            isNotBlank(data, "项目类型");
                            if (!"项目预领".equals(data) && !"项目借支".equals(data)) {
                                throw new RuntimeException("项目类型只能为【项目预领、项目借支】");
                            }
                            excelData.setProjectType("项目预领".equals(data) ? 1 : 2);
                        }
                        break;
                    case 15:
                        if (excelData.getLendType() == LendTypeEnum.LEND_TYPE_13.getType()) {
                            isNotBlank(data, "项目借款类型");
                            if (!"现金".equals(data) && !"转账".equals(data) && !"礼品".equals(data)) {
                                throw new RuntimeException("项目借款类型只能为【现金、转账、礼品】");
                            }
                            excelData.setProjectLendType("现金".equals(data) ? "1" : "转账".equals(data) ? "2" : "3");
                        }
                        break;
                    default:
                }
            }
            successList.add(excelData);
        } catch (Exception e) {
            errorList.add(ExcelBean.transformBean(row, 15, e.getMessage()));
        }
    }

    private void isNotBlank(String data, String message) {
        if (StringUtils.isBlank(data)) {
            throw new RuntimeException(message + "不能为空");
        }
    }

    /**
     * 借款明细
     */
    public PageResult<BudgetLendMoneyVO> listLendDetail(Integer page, Integer rows, Long id) {
        Page<BudgetLendMoneyVO> pageBean = new Page<>(page, rows);
        List<BudgetLendMoneyVO> resultList = this.budgetOtherlendsumMapper.listLendDetail(pageBean, id);
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    /**
     * 删除借款
     */
    public void deleteLendMoney(List<Long> ids) {
        List<BudgetLendmoney> lendMoneyList = this.budgetLendmoneyMapper.selectBatchIds(ids);
        if (!lendMoneyList.isEmpty()) {
            List<Long> collect = lendMoneyList.stream().map(BudgetLendmoney::getOtherlendsumid).filter(Objects::nonNull).collect(Collectors.toList());
            if (lendMoneyList.size() != collect.size()) {
                throw new RuntimeException("需要删除借款单存在非批次导入借款");
            }
            HashSet<Long> hashSet = new HashSet<>(collect);
            if (hashSet.size() != 1) {
                throw new RuntimeException("需要删除借款单不是同一批次的借款");
            }
            BudgetOtherlendsum otherLendSum = this.budgetOtherlendsumMapper.selectById(collect.get(0));
            if (otherLendSum == null) {
                throw new RuntimeException("导入批次记录不存在或已删除");
            } else if (otherLendSum.getStatus() == 1) {
                throw new RuntimeException("该导入批次记录已审核");
            }
            this.budgetLendmoneyMapper.deleteBatchIds(ids);
        }
    }
}
