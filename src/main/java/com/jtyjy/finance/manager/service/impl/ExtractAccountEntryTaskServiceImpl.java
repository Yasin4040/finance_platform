package com.jtyjy.finance.manager.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jtyjy.finance.manager.bean.BudgetExtractsum;
import com.jtyjy.finance.manager.bean.ExtractAccountEntryTask;
import com.jtyjy.finance.manager.dto.commission.EntryCompletedDTO;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.BudgetYearPeriodMapper;
import com.jtyjy.finance.manager.service.BudgetExtractsumService;
import com.jtyjy.finance.manager.service.ExtractAccountEntryTaskService;
import com.jtyjy.finance.manager.mapper.ExtractAccountEntryTaskMapper;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
* @author User
* @description 针对表【budget_extract_account_entry_task(核算入账)】的数据库操作Service实现
* @createDate 2022-09-14 11:09:41
*/
@Service
public class ExtractAccountEntryTaskServiceImpl extends ServiceImpl<ExtractAccountEntryTaskMapper, ExtractAccountEntryTask>
    implements ExtractAccountEntryTaskService{

    private final BudgetExtractsumService extractSumService;
    private final BudgetYearPeriodMapper yearMapper;
    public ExtractAccountEntryTaskServiceImpl(BudgetExtractsumService extractSumService, BudgetYearPeriodMapper yearMapper) {
        this.extractSumService = extractSumService;
        this.yearMapper = yearMapper;
    }

    @Override
    public void entryCompleted(EntryCompletedDTO dto) {
        ExtractAccountEntryTask entryTask = this.getById(dto.getId());
        entryTask.setAccountantEmpNo(UserThreadLocal.getEmpNo());
        entryTask.setAccountantEmpName(UserThreadLocal.getEmpName());
        entryTask.setAccountantTime(new Date());

        entryTask.setStatus(1);
        entryTask.setVoucherNo(dto.getVoucherNo());
        this.saveOrUpdate(entryTask);

    }

    @Override
    public void addEntryTask(String sumId) {
        ExtractAccountEntryTask entryTask = new ExtractAccountEntryTask();


        BudgetExtractsum extractSum = extractSumService.getById(sumId);


        String extractMonth = extractSum.getExtractmonth();

        entryTask.setExtractCode(extractSum.getCode());
        entryTask.setExtractMonth(extractSum.getExtractmonth());

        entryTask.setYearId(String.valueOf(extractSum.getYearid()));
        String yearName = yearMapper.getNameById(extractSum.getYearid());
        entryTask.setYearName(yearName);

        //2020 11 08
        String monthId = extractMonth.substring(4, 6);
        entryTask.setMonthId(monthId);
        entryTask.setMonthName(monthId+"月");
        //用 cache mapper层缓存

        entryTask.setStatus(0);
        entryTask.setDeptName(extractSum.getDeptname());
        entryTask.setDeptId(extractSum.getDeptid());
        entryTask.setSumId(String.valueOf(extractSum.getId()));
        entryTask.setExtractCode(extractSum.getCode());
        entryTask.setCreateTime(new Date());
    }
}




