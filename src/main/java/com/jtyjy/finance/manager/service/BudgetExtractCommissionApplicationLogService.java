package com.jtyjy.finance.manager.service;

import com.jtyjy.ecology.EcologyParams;
import com.jtyjy.finance.manager.bean.BudgetExtractCommissionApplicationLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author User
* @description 针对表【budget_extract_commission_application_log(申请单 oa 审批日志记录)】的数据库操作Service
* @createDate 2022-08-26 11:08:05
*/
public interface BudgetExtractCommissionApplicationLogService extends IService<BudgetExtractCommissionApplicationLog> {

    void saveLog(Long applicationId);

    void doRecordOA(EcologyParams params);
}
