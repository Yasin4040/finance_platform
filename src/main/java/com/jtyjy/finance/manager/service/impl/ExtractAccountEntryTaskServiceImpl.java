package com.jtyjy.finance.manager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iamxiongx.util.message.exception.BusinessException;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.dto.commission.EntryCompletedDTO;
import com.jtyjy.finance.manager.enmus.ExtractStatusEnum;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.BudgetExtractDelayApplicationMapper;
import com.jtyjy.finance.manager.mapper.BudgetExtractsumMapper;
import com.jtyjy.finance.manager.mapper.BudgetYearPeriodMapper;
import com.jtyjy.finance.manager.query.AccountEntryQuery;
import com.jtyjy.finance.manager.service.BudgetExtractPerPayDetailService;
import com.jtyjy.finance.manager.service.BudgetExtractsumService;
import com.jtyjy.finance.manager.service.BudgetUnitService;
import com.jtyjy.finance.manager.service.ExtractAccountEntryTaskService;
import com.jtyjy.finance.manager.mapper.ExtractAccountEntryTaskMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final BudgetExtractsumMapper extractSumMapper;
    private final BudgetYearPeriodMapper yearMapper;
    private final BudgetExtractPerPayDetailService perPayDetailService;
    private final BudgetExtractDelayApplicationMapper delayApplicationMapper;
    private final BudgetUnitService budgetUnitService;
    public ExtractAccountEntryTaskServiceImpl(BudgetExtractsumMapper extractSumMapper, BudgetYearPeriodMapper yearMapper, BudgetExtractPerPayDetailService perPayDetailService, BudgetExtractDelayApplicationMapper delayApplicationMapper, BudgetUnitService budgetUnitService) {
        this.extractSumMapper = extractSumMapper;
        this.yearMapper = yearMapper;
        this.perPayDetailService = perPayDetailService;
        this.delayApplicationMapper = delayApplicationMapper;
        this.budgetUnitService = budgetUnitService;
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
    @Transactional(rollbackFor = Exception.class)
    public void addEntryTask(Boolean isDelay, List<String> delayList, String extractMonth) {
        //延期。
        List<ExtractAccountEntryTask> taskList = new ArrayList<>();
        if (isDelay) {
            List<BudgetExtractDelayApplication> delayApplications = delayApplicationMapper.selectList(new LambdaQueryWrapper<BudgetExtractDelayApplication>()
                    .in(BudgetExtractDelayApplication::getDelayCode, delayList));
            for (BudgetExtractDelayApplication delayApplication : delayApplications) {
                //获取extractSum
                BudgetExtractsum extractSum = extractSumMapper.selectOne(new LambdaQueryWrapper<BudgetExtractsum>()
                        .eq(BudgetExtractsum::getCode, delayApplication.getRelationExtractCode()));
                //获取单个任务
                getSingleEntryTask(extractMonth, taskList, delayApplication.getDelayCode(), extractSum);
            }
        }else {
            List<BudgetExtractsum> curBatchExtractSum =
                    extractSumMapper.selectList(new LambdaQueryWrapper<BudgetExtractsum>().eq(BudgetExtractsum::getExtractmonth, extractMonth).eq(BudgetExtractsum::getDeleteflag, 0).ne(BudgetExtractsum::getStatus, ExtractStatusEnum.REJECT.getType()));
            long count = curBatchExtractSum.stream().filter(x -> !x.getStatus().equals(ExtractStatusEnum.ACCOUNT.type)).count();
            if (count>0) {
                throw new BusinessException("存在没有做账完成的订单。");
            }
            //是否维护了 预算会计
            List<String> deptIdList = curBatchExtractSum.stream().map(BudgetExtractsum::getDeptid).collect(Collectors.toList());

            List<BudgetUnit> budgetUnitList = budgetUnitService.lambdaQuery().in(BudgetUnit::getId, deptIdList).list();
            List<BudgetUnit> noAccountingUnitList = budgetUnitList.stream().filter(x -> StringUtils.isBlank(x.getAccounting())).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(noAccountingUnitList)){
                String unitName ="";
                for (BudgetUnit x : noAccountingUnitList) {
                    unitName =  x.getName()+";";
                }
                throw new BusinessException(unitName+"还没有维护收入会计");
            }
            for (BudgetExtractsum extractSum : curBatchExtractSum) {
                getSingleEntryTask(extractMonth, taskList,extractSum.getCode(), extractSum);
            }
        }
        this.saveOrUpdateBatch(taskList);

    }

    @Override
    public Page<ExtractAccountEntryTask> getList(AccountEntryQuery query) {
        //加上人员权限。TODO
        //获取当前人员 拥有哪些预算单位。
        WbUser wbUser = UserThreadLocal.get();
//        String empNo = UserThreadLocal.getEmpNo();
        List<String> deptIds = budgetUnitService.getBaseUnitIdListByAccountingNo(wbUser.getUserId());
        if(CollectionUtils.isEmpty(deptIds)){
            return new Page<>();
        }
        Page<ExtractAccountEntryTask> page = this.page(new Page<>(query.getPage(), query.getRows()), new LambdaQueryWrapper<ExtractAccountEntryTask>()
                .eq( StringUtils.isNotBlank(query.getYearId()),ExtractAccountEntryTask::getYearId, query.getYearId())
                .eq( StringUtils.isNotBlank(query.getMonthId()),ExtractAccountEntryTask::getMonthId, query.getMonthId())
                .eq(query.getStatus()!=null,ExtractAccountEntryTask::getStatus, query.getStatus())
                .in(CollectionUtils.isNotEmpty(deptIds),ExtractAccountEntryTask::getDeptId, deptIds)
                .like(StringUtils.isNotBlank( query.getExtractCode()),ExtractAccountEntryTask::getExtractCode, query.getExtractCode())
                .like(StringUtils.isNotBlank( query.getExtractMonth()),ExtractAccountEntryTask::getExtractMonth, query.getExtractMonth())
                .like(StringUtils.isNotBlank( query.getDeptName()),ExtractAccountEntryTask::getDeptName, query.getDeptName()));
        List<ExtractAccountEntryTask> records = page.getRecords();
        for (ExtractAccountEntryTask record : records) {
            record.setStatusName(record.getStatus()==0?"核算中":"入账完成");
        }
        return page;
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




