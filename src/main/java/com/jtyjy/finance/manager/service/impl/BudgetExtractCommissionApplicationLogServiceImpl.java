package com.jtyjy.finance.manager.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jtyjy.ecology.EcologyParams;
import com.jtyjy.ecology.EcologyRequestManager;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.enmus.ExtractStatusEnum;
import com.jtyjy.finance.manager.enmus.LogStatusEnum;
import com.jtyjy.finance.manager.enmus.OperationNodeEnum;
import com.jtyjy.finance.manager.enmus.RoleNameEnum;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.oadao.OAMapper;
import com.jtyjy.finance.manager.service.*;
import com.jtyjy.weixin.message.MessageSender;
import com.jtyjy.weixin.message.QywxTextMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.*;

/**
* @author User
* @description 针对表【budget_extract_commission_application_log(申请单 oa 审批日志记录)】的数据库操作Service实现
* @createDate 2022-08-26 11:08:05
*/
@Service
@Slf4j
public class BudgetExtractCommissionApplicationLogServiceImpl extends ServiceImpl<BudgetExtractCommissionApplicationLogMapper, BudgetExtractCommissionApplicationLog>
    implements BudgetExtractCommissionApplicationLogService{

    private final BudgetExtractCommissionApplicationMapper applicationMapper;
    private final BudgetExtractsumMapper extractSumMapper;
    private final BudgetExtractImportdetailMapper importDetailMapper;
    private final OAMapper oaMapper;
    private final BudgetReimbursementorderService reimburseService;
    private final BudgetExtractTaxHandleRecordService taxHandleRecordService;

    private final CommonService commonService;
    private final BudgetYearPeriodMapper yearMapper;
    private final TabDmMapper dmMapper;
    private final MessageSender sender;

    public BudgetExtractCommissionApplicationLogServiceImpl(BudgetExtractCommissionApplicationMapper applicationMapper, BudgetExtractsumMapper extractSumMapper, BudgetExtractImportdetailMapper importDetailMapper, OAMapper oaMapper, BudgetReimbursementorderService reimburseService, BudgetExtractTaxHandleRecordService taxHandleRecordService, CommonService commonService, BudgetYearPeriodMapper yearMapper, TabDmMapper dmMapper, MessageSender sender) {
        this.applicationMapper = applicationMapper;
        this.extractSumMapper = extractSumMapper;
        this.importDetailMapper = importDetailMapper;
        this.oaMapper = oaMapper;
        this.reimburseService = reimburseService;
        this.taxHandleRecordService = taxHandleRecordService;
        this.commonService = commonService;
        this.yearMapper = yearMapper;
        this.dmMapper = dmMapper;
        this.sender = sender;
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

        //获取oa中日志的 状态。日志中  0 同意  3拒绝。
        // 财务       0完成。 1 同意 2退回
        //oa中    0批准    1拒绝。 1 保存。 退回
//        String oaLogStatus =  oaMapper.getLogStatus(requestId,nodeId);
        //获取src  reject  拒绝  submit 同意      "src": "submit", "src": "reject",
        String operSrc = requestManager.getSrc();
        Integer logStatus = -3;//未知
        if(operSrc.equals("submit")){
            logStatus = LogStatusEnum.PASS.getCode();
        }else if(operSrc.equals("reject")){
            logStatus = LogStatusEnum.REJECT.getCode();
       }else{
            System.out.println("oa审批log日志");
            return;
        }
        extractLog.setStatus(logStatus);

        //Document parse = Jsoup.parse(html);
        //        String p1 = parse.getElementsByTag("p").text();
        //解析html
        if(StringUtils.isNotBlank(remark)){
            if (remark.contains("p")){
                Document parse = Jsoup.parse(remark);
                remark = parse.getElementsByTag("p").text();
            }
        }
        extractLog.setRemarks(remark);
        this.save(extractLog);
        //如果节点通过
        //如果拒绝了  就是删除报销单。退回申请单。
        //删除报销表
        if(Objects.equals(logStatus, LogStatusEnum.REJECT.getCode())) {
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
            if (nodeEnum != null) {
                if(Objects.equals((nodeEnum).getType(),OperationNodeEnum.FINANCIAL_DIRECTOR.getType())){
                    //处理计算记录
                    BudgetExtractsum budgetExtractsum = extractSumMapper.selectById(sumId);
                    budgetExtractsum.setStatus(ExtractStatusEnum.APPROVED.getType());
                    extractSumMapper.updateById(budgetExtractsum);
//                dealHandleRecord(sumId);
                    this.doMsgTask(budgetExtractsum.getExtractmonth(),ExtractStatusEnum.APPROVED, String.valueOf(sumId));
                }
            }
        }
    }
    public void doMsgTask(String extractMonth,  ExtractStatusEnum statusEnum,String sumId) {
        BudgetExtractsum extractSum = extractSumMapper.selectById(sumId);
        List<BudgetExtractsum> extractSumList = extractSumMapper.selectList(new QueryWrapper<BudgetExtractsum>().eq("extractmonth", extractMonth).eq("deleteflag", 0));
        //不为本订单，存在不为审核通过的数量。
        long count = extractSumList.stream().filter(x->!x.getId().toString().equals(sumId)).filter(x -> !Objects.equals(x.getStatus(), statusEnum.getType())).count();
        if(count>0){
            return;
        }
        String yearName = yearMapper.getNameById(extractSum.getYearid());
        Integer month = Integer.valueOf(extractMonth.substring(4, 6));
        Integer batchNo = Integer.valueOf(extractMonth.substring(6));
        String code =  MessageFormat.format("{0}届{1}月第{2}批{3}单",yearName,month,batchNo,extractSum.getCode());
        switch (statusEnum){
            case APPROVED:
                String toUsers = "";
                if (count==0) {
                    //XX届XX月XX批
                    if (this.isTest()) {
                        toUsers = this.getTestNotice();
                    }else{
                        List<String> empNoList = commonService.getEmpNoListByRoleNames(RoleNameEnum.TAX.getValue());
                        toUsers=String.join("|", empNoList);
                    }
                    if(StringUtils.isNotBlank(toUsers))
                        sender.sendQywxMsg(new QywxTextMsg(toUsers, null, null, 0,code+"提成支付申请单已审核通过，可进行提成发放操作！" ,null));
                }
                break;
            default:
                break;
        }
    }
    public boolean isTest(){
        TabDm dm = this.dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", "EXTRACTCAL").eq("dm", "is_test"));
        return dm != null && StringUtils.isNotBlank(dm.getDmValue()) && "1".equals(dm.getDmValue());
    }
    public String getTestNotice(){
        TabDm dm1 = this.dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", "EXTRACTCAL").eq("dm", "test_notice"));
        return dm1.getDmValue();
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




