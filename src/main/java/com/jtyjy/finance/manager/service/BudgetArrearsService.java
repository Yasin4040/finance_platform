package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.BudgetArrears;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.enmus.LendTypeEnum;
import com.jtyjy.finance.manager.mapper.BudgetArrearsMapper;
import com.jtyjy.finance.manager.mapper.BudgetLendmoneyMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.vo.ArrearsDetailsVO;
import com.jtyjy.finance.manager.vo.ExcelBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
//@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetArrearsService extends DefaultBaseService<BudgetArrearsMapper, BudgetArrears> {

    private final TabChangeLogMapper loggerMapper;
    private final BudgetArrearsMapper budgetArrearsMapper;
    private final BudgetLendmoneyMapper budgetLendmoneyMapper;
    private final WbUserService wbUserService;

    public BudgetArrearsService(TabChangeLogMapper loggerMapper, BudgetArrearsMapper budgetArrearsMapper, BudgetLendmoneyMapper budgetLendmoneyMapper, WbUserService wbUserService) {
        this.loggerMapper = loggerMapper;
        this.budgetArrearsMapper = budgetArrearsMapper;
        this.budgetLendmoneyMapper = budgetLendmoneyMapper;
        this.wbUserService = wbUserService;
    }

    @Override
    public BaseMapper<TabChangeLog> getLoggerMapper() {
        return loggerMapper;
    }

    @Override
    public void setBaseLoggerBean() {
        DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_arrears_new"));
    }

    /**
     * ??????????????????
     */
    public BudgetArrears getByEmpNo(String empNo) {
        QueryWrapper<BudgetArrears> wrapper = new QueryWrapper<>();
        wrapper.eq("empno", empNo);
        return this.getOne(wrapper);
    }

    /**
     * ??????????????????????????????
     */
    public PageResult<BudgetArrears> listArrearsPage(Integer page, Integer rows, String name, Integer repaymentStatus) {
        Page<BudgetArrears> pageBean = new Page<>(page, rows);
        HashMap<String, Object> paramMap = new HashMap<>(5);
        paramMap.put("name", name);
        paramMap.put("repaymentStatus", repaymentStatus);
        List<BudgetArrears> resultList = this.budgetArrearsMapper.listArrearsPage(pageBean, paramMap);
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    /**
     * ?????????????????????????????????
     */
    public void editEmpCredit(Long id, Integer overdueRecords, Integer badCredit) {
        BudgetArrears budgetArrears = this.budgetArrearsMapper.selectById(id);
        if (budgetArrears == null) {
            throw new RuntimeException("ID????????????");
        }

        BudgetArrears updateArrears = new BudgetArrears();
        updateArrears.setId(budgetArrears.getId());
        updateArrears.setOverduerecords(overdueRecords);
        updateArrears.setBadcredit(badCredit);
        this.budgetArrearsMapper.updateById(updateArrears);
    }

    /**
     * ????????????????????????
     */
    public PageResult<ArrearsDetailsVO> getArrearsDetails(Integer page, Integer rows, String empNo) {
        Page<BudgetArrears> pageBean = new Page<>(page, rows);
        List<ArrearsDetailsVO> arrearsDetails = this.budgetLendmoneyMapper.getArrearsDetails(pageBean, empNo);
        arrearsDetails.forEach(v -> v.setOrderTypeDesc(LendTypeEnum.getValue(v.getOrderType())));
        return PageResult.apply(pageBean.getTotal(), arrearsDetails);
    }

    /**
     * ?????????????????????
     */
    public List<List<String>> importEmpCredit(List<List<String>> excelDataList) {
        List<BudgetArrears> successList = new ArrayList<>();
        List<ExcelBean> errorList = new ArrayList<>();
        int size = excelDataList.size();
        for (int i = 0; i < size; i++) {
            // ??????????????????????????????
            if (i < 1) {
                continue;
            }
            empCreditValidate(excelDataList.get(i), successList, errorList);
        }

        if (!successList.isEmpty()) {
            this.updateBatchById(successList);
        }
        return ExcelBean.transformList(errorList);
    }

    private void empCreditValidate(List<String> row, List<BudgetArrears> successList, List<ExcelBean> errorList) {
        int totalColumn = 4;
        try {
            BudgetArrears budgetArrears = new BudgetArrears();

            int columnSize = row.size();
            if (columnSize < totalColumn) {
                throw new RuntimeException("?????????????????????");
            }
            for (int i = 1; i <= columnSize; i++) {
                String data = row.get(i - 1);
                switch (i) {
                    case 1:
                        isNotBlank(data, "??????");
                        budgetArrears.setEmpno(data);
                        break;
                    case 2:
                        isNotBlank(data, "??????");
                        this.wbUserService.validateUser(budgetArrears.getEmpno(), data);
                        BudgetArrears arrears = this.budgetArrearsMapper.selectOne(new QueryWrapper<BudgetArrears>().eq("empno", budgetArrears.getEmpno()));
                        if (arrears == null) {
                            throw new RuntimeException(data + "(" + budgetArrears.getEmpno() + ")??????????????????!");
                        }
                        budgetArrears.setId(arrears.getId());
                        break;
                    case 3:
                        isNotBlank(data, "????????????");
                        int overdueRecords;
                        try {
                            overdueRecords = Integer.parseInt(data);
                        } catch (Exception e) {
                            throw new RuntimeException("????????????????????????");
                        }
                        budgetArrears.setOverduerecords(overdueRecords);
                        break;
                    case 4:
                        isNotBlank(data, "??????????????????");
                        int badCredit;
                        try {
                            badCredit = Integer.parseInt(data);
                        } catch (Exception e) {
                            throw new RuntimeException("??????????????????????????????");
                        }
                        budgetArrears.setBadcredit(badCredit);
                        break;
                    default:
                }
            }
            // ??????????????????
            successList.add(budgetArrears);
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
}
