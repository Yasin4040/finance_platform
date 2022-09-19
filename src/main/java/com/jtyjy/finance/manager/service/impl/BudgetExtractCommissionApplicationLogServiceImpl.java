package com.jtyjy.finance.manager.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jtyjy.ecology.EcologyParams;
import com.jtyjy.ecology.EcologyRequestManager;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.enmus.ExtractStatusEnum;
import com.jtyjy.finance.manager.enmus.ExtractUserTypeEnum;
import com.jtyjy.finance.manager.enmus.LogStatusEnum;
import com.jtyjy.finance.manager.enmus.OperationNodeEnum;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.BudgetExtractCommissionApplicationMapper;
import com.jtyjy.finance.manager.mapper.BudgetExtractImportdetailMapper;
import com.jtyjy.finance.manager.mapper.BudgetExtractsumMapper;
import com.jtyjy.finance.manager.oadao.OAMapper;
import com.jtyjy.finance.manager.service.BudgetExtractCommissionApplicationLogService;
import com.jtyjy.finance.manager.mapper.BudgetExtractCommissionApplicationLogMapper;
import com.jtyjy.finance.manager.service.BudgetExtractTaxHandleRecordService;
import com.jtyjy.finance.manager.service.BudgetReimbursementorderService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
* @author User
* @description 针对表【budget_extract_commission_application_log(申请单 oa 审批日志记录)】的数据库操作Service实现
* @createDate 2022-08-26 11:08:05
*/
@Service
public class BudgetExtractCommissionApplicationLogServiceImpl extends ServiceImpl<BudgetExtractCommissionApplicationLogMapper, BudgetExtractCommissionApplicationLog>
    implements BudgetExtractCommissionApplicationLogService{

    private final BudgetExtractCommissionApplicationMapper applicationMapper;
    private final BudgetExtractsumMapper extractSumMapper;
    private final BudgetExtractImportdetailMapper importDetailMapper;
    private final OAMapper oaMapper;
    private final BudgetReimbursementorderService reimburseService;
    private final BudgetExtractTaxHandleRecordService taxHandleRecordService;

    public BudgetExtractCommissionApplicationLogServiceImpl(BudgetExtractCommissionApplicationMapper applicationMapper, BudgetExtractsumMapper extractSumMapper, BudgetExtractImportdetailMapper importDetailMapper, OAMapper oaMapper, BudgetReimbursementorderService reimburseService, BudgetExtractTaxHandleRecordService taxHandleRecordService) {
        this.applicationMapper = applicationMapper;
        this.extractSumMapper = extractSumMapper;
        this.importDetailMapper = importDetailMapper;
        this.oaMapper = oaMapper;
        this.reimburseService = reimburseService;
        this.taxHandleRecordService = taxHandleRecordService;
    }

    @Override
    public void saveLog(Long applicationId, OperationNodeEnum nodeEnum, LogStatusEnum logStatusEnum) {
        BudgetExtractCommissionApplicationLog log = new BudgetExtractCommissionApplicationLog();
        log.setApplicationId(applicationId);
        log.setCreatorName(UserThreadLocal.getEmpName());
        log.setCreateBy(UserThreadLocal.getEmpNo());
        log.setCreateTime(new Date());
        log.setNode(nodeEnum.getType());
        log.setStatus(logStatusEnum.getCode());//无操作。默认提交
        log.setStatusName(logStatusEnum.getMsg());
        log.setNodeName(nodeEnum.getValue());
        this.save(log);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doRecordOA(EcologyParams params) {
        //打印参数 观察。
        System.out.println("6666开始");
        System.out.println( JSONObject.toJSONString(params));
        EcologyRequestManager requestManager = params.getRequestManager();
        String requestId = params.getRequestid();
        int nodeId = requestManager.getNodeid();
        //7111
        //通过ia获取
        String nodeName = oaMapper.getNodeName(nodeId);
        //先找到相应的nodeId  flowType 对应的 节点信息。
        OperationNodeEnum nodeEnum = OperationNodeEnum.getTypeEnumByDesc(nodeName);
        String requestname = requestManager.getRequestname();
        //哪一个是操作类型。
        String nodeType = requestManager.getNodetype();
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
        Long sumId=application.getExtractSumId();

        //node  对应  OperationNodeEnum
        BudgetExtractCommissionApplicationLog extractLog = new BudgetExtractCommissionApplicationLog();
        extractLog.setNode(nodeEnum!=null?nodeEnum.getType():nodeId);
        extractLog.setNodeName(nodeEnum!=null?nodeEnum.getValue():nodeName);
        extractLog.setApplicationId(application.getId());
        extractLog.setCreateTime(new Date());
        extractLog.setOaRequestId(requestId);
        extractLog.setOaNodeId(String.valueOf(nodeId));
        extractLog.setCreateBy(empNo);
        extractLog.setCreatorName(username);
        extractLog.setCreateTime(new Date());
        // 财务       0完成。 1 同意 2退回
        //oa中    0批准   3和1 哪个是拒绝 5 删除
        Integer.valueOf(nodeType);
        //3未知
        Integer logStatus=3;
        if(nodeType.equals("0")){
            logStatus = LogStatusEnum.PASS.getCode();
        }else if (nodeType.equals("1")){
            logStatus = LogStatusEnum.REJECT.getCode();
        }else {
            logStatus = -1;
        }
        extractLog.setStatus(logStatus);
        extractLog.setRemarks(remark);
        this.save(extractLog);
        //如果节点通过

        //如果拒绝了  就是删除报销单。退回申请单。
        //删除报销表
        if(nodeType.equals("1")) {
            if (application.getReimbursementId() != null) {
                BudgetReimbursementorder reimbursementOrder = reimburseService.getById(application.getReimbursementId());
                reimburseService.removeById(reimbursementOrder);
            }
            BudgetExtractsum budgetExtractsum = extractSumMapper.selectById(sumId);
            budgetExtractsum.setStatus(ExtractStatusEnum.RETURN.getType());
            extractSumMapper.updateById(budgetExtractsum);
//            applicationMapper.updateById(application);
        }else{
            //财务负责人同意  同意
            if(nodeEnum.getType()==OperationNodeEnum.FINANCIAL_DIRECTOR.getType()){
                //处理计算记录
                BudgetExtractsum budgetExtractsum = extractSumMapper.selectById(sumId);
                budgetExtractsum.setStatus(ExtractStatusEnum.APPROVED.getType());
                extractSumMapper.updateById(budgetExtractsum);
//                dealHandleRecord(sumId);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dealHandleRecord(Long sumId) {

        //不用申请单的状态 用主单状态
//                applicationMapper.updateById(application);

//        List<BudgetExtractImportdetail> importDetailList = importDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractImportdetail>().eq(BudgetExtractImportdetail::getExtractsumid, sumId));
//
//        long selfCount = importDetailList.stream().filter(x -> x.getBusinessType().equals(ExtractUserTypeEnum.SELF_EMPLOYED_EMPLOYEES)).count();
//        BudgetExtractTaxHandleRecord  handleRecord;
//        //没有个体户
//        if(selfCount==0){
//            handleRecord = taxHandleRecordService.lambdaQuery().eq(BudgetExtractTaxHandleRecord::getExtractMonth, budgetExtractsum.getExtractmonth()).last("limit 1").one();
//            if(handleRecord == null){
//                handleRecord = new BudgetExtractTaxHandleRecord();
//            }
//            //oa 审批通过。增加判断 批次所有通过，改变记录表  如果批次所有通过
//            handleRecord.setExtractMonth(budgetExtractsum.getExtractmonth());
//            handleRecord.setIsCalComplete(false);
//            handleRecord.setIsSetExcessComplete(false);
//            handleRecord.setIsPersonalityComplete(true);
//            taxHandleRecordService.saveOrUpdate(handleRecord);
//        }else if(selfCount==importDetailList.size()){
//            handleRecord = taxHandleRecordService.lambdaQuery().eq(BudgetExtractTaxHandleRecord::getExtractMonth, budgetExtractsum.getExtractmonth()).last("limit 1").one();
//            //全是个体户
//            if(handleRecord == null){
//                handleRecord = new BudgetExtractTaxHandleRecord();
//            }
//            handleRecord.setExtractMonth(budgetExtractsum.getExtractmonth());
//            handleRecord.setIsCalComplete(true);
//            handleRecord.setIsSetExcessComplete(true);
//            handleRecord.setIsPersonalityComplete(false);
//            taxHandleRecordService.saveOrUpdate(handleRecord);
//        }
    }

//    @Override
//    public void afterPropertiesSet() throws Exception {
////        List<String> values = OperationNodeEnum.getValues();
//        List<String> values = new ArrayList<>();
//        values.add(OperationNodeEnum.DEPARTMENT_HEAD.getValue());
//        values.add(OperationNodeEnum.FUNCTIONAL_DEPARTMENT.getValue());
//        values.add(OperationNodeEnum.FINANCIAL_SALES_TEAM.getValue());
//        values.add(OperationNodeEnum.FINANCIAL_SALES_TEAM_HEAD.getValue());
//        values.add(OperationNodeEnum.FINANCIAL_DIRECTOR.getValue());
//        List<Map<String,String>> allMap =  oaMapper.getNodeList(values);
//        allMap.stream().forEach(x->{
//            nodeMap.put(x.get("id"),x.get("name"));
//        });
//    }
}




