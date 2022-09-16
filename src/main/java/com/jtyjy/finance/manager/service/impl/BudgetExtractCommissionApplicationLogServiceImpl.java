package com.jtyjy.finance.manager.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jtyjy.ecology.EcologyParams;
import com.jtyjy.ecology.EcologyRequestManager;
import com.jtyjy.finance.manager.bean.BudgetExtractCommissionApplication;
import com.jtyjy.finance.manager.bean.BudgetExtractCommissionApplicationLog;
import com.jtyjy.finance.manager.enmus.OperationNodeEnum;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.BudgetExtractCommissionApplicationMapper;
import com.jtyjy.finance.manager.oadao.OAMapper;
import com.jtyjy.finance.manager.service.BudgetExtractCommissionApplicationLogService;
import com.jtyjy.finance.manager.mapper.BudgetExtractCommissionApplicationLogMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author User
* @description 针对表【budget_extract_commission_application_log(申请单 oa 审批日志记录)】的数据库操作Service实现
* @createDate 2022-08-26 11:08:05
*/
@Service
public class BudgetExtractCommissionApplicationLogServiceImpl extends ServiceImpl<BudgetExtractCommissionApplicationLogMapper, BudgetExtractCommissionApplicationLog>
    implements BudgetExtractCommissionApplicationLogService, InitializingBean {

    private final BudgetExtractCommissionApplicationMapper applicationMapper;
    private final OAMapper oaMapper;
    private Map<String,String> nodeMap = new HashMap<>();

    public BudgetExtractCommissionApplicationLogServiceImpl(BudgetExtractCommissionApplicationMapper applicationMapper, OAMapper oaMapper) {
        this.applicationMapper = applicationMapper;
        this.oaMapper = oaMapper;
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
        //打印参数 观察。
        System.out.println("6666开始");
        System.out.println( JSONObject.toJSONString(params));
        EcologyRequestManager requestManager = params.getRequestManager();
        int nodeId = requestManager.getNodeid();
        String value = nodeMap.get(nodeId);
        //先找到相应的nodeId  flowType 对应的 节点信息。
//        OperationNodeEnum.getTypeEnum();

        //流程类型 请求 表名
        String flowtype = requestManager.getBillTableName();

        String requestId = params.getRequestid();

        //nodeId 也是固定的。
        //获取当前节点nodeId 和当前requestId  就可以生成相应的日志记录

        String requestname = requestManager.getRequestname();
        //哪一个是操作类型。
        String nodetype = requestManager.getNodetype();
        String remark = requestManager.getRemark();
        //工号
        String username = requestManager.getUser().getUsername();
        String empNo = requestManager.getUser().getLoginid();
        //操作类型
        System.out.println("请求名称"+requestname);
        //logType  操作类型  0是同意 2是拒绝

        BudgetExtractCommissionApplication application = applicationMapper.selectOne(new LambdaQueryWrapper<BudgetExtractCommissionApplication>().eq(BudgetExtractCommissionApplication::getRequestId, requestId).last("limit 1"));
        //insertLog
        if (application == null) {
            return;
        }

        //node  对应  OperationNodeEnum
        BudgetExtractCommissionApplicationLog applicationLog = new BudgetExtractCommissionApplicationLog();
        BudgetExtractCommissionApplicationLog extractLog = new BudgetExtractCommissionApplicationLog();
        extractLog.setNode(OperationNodeEnum.DEPARTMENT_HEAD.getType());
        extractLog.setApplicationId(application.getId());
        extractLog.setCreateTime(new Date());
        extractLog.setOaRequestId(requestId);
//        extractLog.setOaNodeId(String.valueOf(nodeid));

        extractLog.setCreateBy(empNo);
        extractLog.setCreatorName(username);
        extractLog.setCreateTime(new Date());
        //）0已完成   1 同意 2退回  todo
        extractLog.setStatus(0);
        extractLog.setRemarks(remark);
        this.save(extractLog);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        List<String> values = OperationNodeEnum.getValues();
        List<Map<String,String>> allMap =  oaMapper.getNodeList(values);
        allMap.stream().forEach(x->{
            nodeMap.put(x.get("id"),x.get("name"));
        });
    }
}




