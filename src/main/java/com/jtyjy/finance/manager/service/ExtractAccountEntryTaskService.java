package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.ExtractAccountEntryTask;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jtyjy.finance.manager.dto.commission.EntryCompletedDTO;
import com.jtyjy.finance.manager.query.AccountEntryQuery;

import java.util.List;

/**
* @author User
* @description 针对表【budget_extract_account_entry_task(核算入账)】的数据库操作Service
* @createDate 2022-09-14 11:09:41
*/
public interface ExtractAccountEntryTaskService extends IService<ExtractAccountEntryTask> {

    void entryCompleted(EntryCompletedDTO dto);

    void addEntryTask(Boolean isDelay, List<String> delayList, String extractMonth);

    Page<ExtractAccountEntryTask> getList(AccountEntryQuery query);
}
