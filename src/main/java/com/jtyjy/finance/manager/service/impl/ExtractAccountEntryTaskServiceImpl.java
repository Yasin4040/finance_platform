package com.jtyjy.finance.manager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jtyjy.finance.manager.bean.BudgetExtractDelayApplication;
import com.jtyjy.finance.manager.bean.BudgetExtractPerPayDetail;
import com.jtyjy.finance.manager.bean.BudgetExtractsum;
import com.jtyjy.finance.manager.bean.ExtractAccountEntryTask;
import com.jtyjy.finance.manager.dto.commission.EntryCompletedDTO;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.BudgetExtractDelayApplicationMapper;
import com.jtyjy.finance.manager.mapper.BudgetYearPeriodMapper;
import com.jtyjy.finance.manager.service.BudgetExtractPerPayDetailService;
import com.jtyjy.finance.manager.service.BudgetExtractsumService;
import com.jtyjy.finance.manager.service.ExtractAccountEntryTaskService;
import com.jtyjy.finance.manager.mapper.ExtractAccountEntryTaskMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
    private final BudgetExtractPerPayDetailService perPayDetailService;
    private final BudgetExtractDelayApplicationMapper delayApplicationMapper;;
    public ExtractAccountEntryTaskServiceImpl(BudgetExtractsumService extractSumService, BudgetYearPeriodMapper yearMapper, BudgetExtractPerPayDetailService perPayDetailService, BudgetExtractDelayApplicationMapper delayApplicationMapper) {
        this.extractSumService = extractSumService;
        this.yearMapper = yearMapper;
        this.perPayDetailService = perPayDetailService;
        this.delayApplicationMapper = delayApplicationMapper;
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
    public void addEntryTask(Boolean isDelay, List<String> delayList, String extractMonth) {
        //延期。
        List<ExtractAccountEntryTask> taskList = new ArrayList<>();
        if (isDelay) {
            List<BudgetExtractDelayApplication> delayApplications = delayApplicationMapper.selectList(new LambdaQueryWrapper<BudgetExtractDelayApplication>()
                    .in(BudgetExtractDelayApplication::getDelayCode, delayList));
            for (BudgetExtractDelayApplication delayApplication : delayApplications) {
                BudgetExtractsum extractSum = extractSumService.getById(delayApplication.getRelationExtractCode());
                //获取单个任务
                getSingleEntryTask(extractMonth, taskList, delayApplication.getDelayCode(), extractSum);
            }
        }else {
            List<BudgetExtractsum> curBatchExtractSum = extractSumService.getCurBatchExtractSum(extractMonth);

            for (BudgetExtractsum extractSum : curBatchExtractSum) {
                getSingleEntryTask(extractMonth, taskList,extractSum.getCode(), extractSum);
            }
        }
        this.saveOrUpdateBatch(taskList);

    }

    private void getSingleEntryTask(String extractMonth, List<ExtractAccountEntryTask> taskList, String nowCode, BudgetExtractsum extractSum) {
        ExtractAccountEntryTask entryTask = new ExtractAccountEntryTask();
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
        //获取发放金额
        entryTask.setIssuedAmount(getIssuedAmount(nowCode));
        taskList.add(entryTask);
    }

    private BigDecimal getIssuedAmount(String extractCode){
        List<BudgetExtractPerPayDetail> list = perPayDetailService.lambdaQuery().eq(BudgetExtractPerPayDetail::getExtractCode, extractCode).list();
        return list.stream().map(x -> x.getPayMoney()).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);

    }
}




