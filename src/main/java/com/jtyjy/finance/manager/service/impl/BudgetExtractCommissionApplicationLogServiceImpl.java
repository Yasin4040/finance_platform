package com.jtyjy.finance.manager.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.ecology.EcologyClient;
import com.jtyjy.ecology.EcologyParams;
import com.jtyjy.ecology.EcologyWorkFlowValue;
import com.jtyjy.finance.manager.bean.BudgetExtractCommissionApplicationLog;
import com.jtyjy.finance.manager.enmus.OperationNodeEnum;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.service.BudgetExtractCommissionApplicationLogService;
import com.jtyjy.finance.manager.mapper.BudgetExtractCommissionApplicationLogMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
* @author User
* @description 针对表【budget_extract_commission_application_log(申请单 oa 审批日志记录)】的数据库操作Service实现
* @createDate 2022-08-26 11:08:05
*/
@Service
public class BudgetExtractCommissionApplicationLogServiceImpl extends ServiceImpl<BudgetExtractCommissionApplicationLogMapper, BudgetExtractCommissionApplicationLog>
    implements BudgetExtractCommissionApplicationLogService{
    private final RedisClient redisClient;

    public BudgetExtractCommissionApplicationLogServiceImpl(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

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

    @Override
    public void doRecordOA(EcologyParams params) {
        String requestId = params.getRequestid();
        EcologyWorkFlowValue value = EcologyClient.getWorkflowValue(params);
        Map<String, String> mainTableValue = value.getMaintablevalue();
        Map<String, List<Map<String, String>>> detailTableValues = value.getDetailtablevalues();

        // 工号
        String empNo = mainTableValue.get("gh");
        if (StringUtils.isNotBlank(empNo) && empNo.length() > 5) {
            empNo = empNo.substring(0, 5);
        }
        // 经办日期
        String lendDateStr = mainTableValue.get("jbrq");
        // 预计报销日期
        String planDateStr = mainTableValue.get("yjbxrq");
        // 付款届别Id
        String yearId = mainTableValue.get("bxjb");
        // 合同Id
        String contractNameId = mainTableValue.get("htmc");
        // 合同总额
        String nonContractAmtStr = mainTableValue.get("xmzje");
        // 付款事由
        String payRemark = mainTableValue.get("fksy");
        // 支付方式  0 现金；1 转账
        String payType = mainTableValue.get("yqzffs");
        // 流程编号
        String requestCode = mainTableValue.get("lcbh");

        // 是否签订合同  0 是 ，1 否
        String isContract = mainTableValue.get("sfqdht");
        // 非合同名称
        String nonContractName = mainTableValue.get("xmmc");

        if (StringUtils.isNotBlank(payRemark)) {
            payRemark = payRemark.replace("&nbsp;", " ");
        }
        //insertLog
    }
}




