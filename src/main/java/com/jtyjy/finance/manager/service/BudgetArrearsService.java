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
     * 按照工号获取
     */
    public BudgetArrears getByEmpNo(String empNo) {
        QueryWrapper<BudgetArrears> wrapper = new QueryWrapper<>();
        wrapper.eq("empno", empNo);
        return this.getOne(wrapper);
    }

    /**
     * 查询员工台账（分页）
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
     * 修改逾期记录及不良征信
     */
    public void editEmpCredit(Long id, Integer overdueRecords, Integer badCredit) {
        BudgetArrears budgetArrears = this.budgetArrearsMapper.selectById(id);
        if (budgetArrears == null) {
            throw new RuntimeException("ID参数错误");
        }

        BudgetArrears updateArrears = new BudgetArrears();
        updateArrears.setId(budgetArrears.getId());
        updateArrears.setOverduerecords(overdueRecords);
        updateArrears.setBadcredit(badCredit);
        this.budgetArrearsMapper.updateById(updateArrears);
    }

    /**
     * 查询员工台账明细
     */
    public PageResult<ArrearsDetailsVO> getArrearsDetails(Integer page, Integer rows, String empNo) {
        Page<BudgetArrears> pageBean = new Page<>(page, rows);
        List<ArrearsDetailsVO> arrearsDetails = this.budgetLendmoneyMapper.getArrearsDetails(pageBean, empNo);
        arrearsDetails.forEach(v -> v.setOrderTypeDesc(LendTypeEnum.getValue(v.getOrderType())));
        return PageResult.apply(pageBean.getTotal(), arrearsDetails);
    }

    /**
     * 逾期及征信导入
     */
    public List<List<String>> importEmpCredit(List<List<String>> excelDataList) {
        List<BudgetArrears> successList = new ArrayList<>();
        List<ExcelBean> errorList = new ArrayList<>();
        int size = excelDataList.size();
        for (int i = 0; i < size; i++) {
            // 表格正文从第二行开始
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
                throw new RuntimeException("内容填写不完整");
            }
            for (int i = 1; i <= columnSize; i++) {
                String data = row.get(i - 1);
                switch (i) {
                    case 1:
                        isNotBlank(data, "工号");
                        budgetArrears.setEmpno(data);
                        break;
                    case 2:
                        isNotBlank(data, "姓名");
                        this.wbUserService.validateUser(budgetArrears.getEmpno(), data);
                        BudgetArrears arrears = this.budgetArrearsMapper.selectOne(new QueryWrapper<BudgetArrears>().eq("empno", budgetArrears.getEmpno()));
                        if (arrears == null) {
                            throw new RuntimeException(data + "(" + budgetArrears.getEmpno() + ")没有借款信息!");
                        }
                        budgetArrears.setId(arrears.getId());
                        break;
                    case 3:
                        isNotBlank(data, "逾期记录");
                        int overdueRecords;
                        try {
                            overdueRecords = Integer.parseInt(data);
                        } catch (Exception e) {
                            throw new RuntimeException("逾期记录格式错误");
                        }
                        budgetArrears.setOverduerecords(overdueRecords);
                        break;
                    case 4:
                        isNotBlank(data, "不良征信记录");
                        int badCredit;
                        try {
                            badCredit = Integer.parseInt(data);
                        } catch (Exception e) {
                            throw new RuntimeException("不良征信记录格式错误");
                        }
                        budgetArrears.setBadcredit(badCredit);
                        break;
                    default:
                }
            }
            // 添加成功记录
            successList.add(budgetArrears);
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
}
