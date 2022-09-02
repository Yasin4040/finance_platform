package com.jtyjy.finance.manager.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jtyjy.finance.manager.bean.BudgetExtractCommissionApplicationLog;
import com.jtyjy.finance.manager.enmus.ExtractTypeEnum;
import com.jtyjy.finance.manager.enmus.OperationNodeEnum;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.service.BudgetExtractCommissionApplicationLogService;
import com.jtyjy.finance.manager.mapper.BudgetExtractCommissionApplicationLogMapper;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
* @author User
* @description 针对表【budget_extract_commission_application_log(申请单 oa 审批日志记录)】的数据库操作Service实现
* @createDate 2022-08-26 11:08:05
*/
@Service
public class BudgetExtractCommissionApplicationLogServiceImpl extends ServiceImpl<BudgetExtractCommissionApplicationLogMapper, BudgetExtractCommissionApplicationLog>
    implements BudgetExtractCommissionApplicationLogService{

    @Override
    public void saveLog(Long applicationId) {
        BudgetExtractCommissionApplicationLog log = new BudgetExtractCommissionApplicationLog();
        log.setApplicationId(applicationId);
        log.setCreatorName(UserThreadLocal.getEmpName());
        log.setCreateBy(UserThreadLocal.getEmpNo());
        log.setCreateTime(new Date());
        log.setNode(OperationNodeEnum.SUBMITTED.getType());
        log.setStatus(0);//无操作。默认提交
        this.save(log);
    }
}




