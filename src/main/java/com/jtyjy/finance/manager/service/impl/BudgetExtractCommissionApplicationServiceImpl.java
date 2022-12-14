package com.jtyjy.finance.manager.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iamxiongx.util.message.DateUtil;
import com.iamxiongx.util.message.exception.BusinessException;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.ecology.webservice.workflow.WorkflowInfo;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.cache.UnitCache;
import com.jtyjy.finance.manager.cache.UserCache;
import com.jtyjy.finance.manager.controller.reimbursement.ReimbursementWorker;
import com.jtyjy.finance.manager.converter.CommonAttachmentConverter;
import com.jtyjy.finance.manager.dto.ReimbursementRequest;
import com.jtyjy.finance.manager.dto.commission.FeeImportErrorDTO;
import com.jtyjy.finance.manager.dto.commission.IndividualIssueExportDTO;
import com.jtyjy.finance.manager.dto.commission.OAApplicationDTO;
import com.jtyjy.finance.manager.dto.commission.OAApplicationDetailDTO;
import com.jtyjy.finance.manager.enmus.*;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.listener.easyexcel.PageReadListener;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.oadao.OAMapper;
import com.jtyjy.finance.manager.query.commission.FeeQuery;
import com.jtyjy.finance.manager.service.*;
import com.jtyjy.finance.manager.utils.FileUtils;
import com.jtyjy.finance.manager.vo.application.*;
import com.jtyjy.weixin.message.MessageSender;
import com.jtyjy.weixin.message.QywxTextMsg;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.locale.converters.DateLocaleConverter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.StorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author User
* @description ????????????budget_extract_commission_application(?????????????????????  ?????? )?????????????????????Service??????
* @createDate 2022-08-26 11:08:05
*/
@Service
@RequiredArgsConstructor
public class BudgetExtractCommissionApplicationServiceImpl extends ServiceImpl<BudgetExtractCommissionApplicationMapper, BudgetExtractCommissionApplication>
    implements BudgetExtractCommissionApplicationService{
    private final BudgetExtractTaxHandleRecordService taxHandleRecordService;
    private final BudgetExtractsumMapper extractSumMapper;
    private final BudgetExtractOuterpersonMapper outPersonMapper;
    private final BudgetExtractImportdetailMapper extractImportDetailMapper;
    private final BudgetYearPeriodMapper yearMapper;
    private final BudgetExtractCommissionApplicationBudgetDetailsService budgetDetailsService;
    private final BudgetExtractCommissionApplicationLogService applicationLogService;
    private final BudgetCommonAttachmentService attachmentService;
    private final StorageClient storageClient;
    private final ReimbursementWorker reimbursementWorker;
    private final BudgetReimbursementorderService reimbursementorderService;
    private final BudgetExtractFeePayDetailMapper feePayDetailMapper;
    private final HrService hrService;
    private final OaService oaService;
    private final TabDmMapper tabDmMapper;
    private final OAMapper oaMapper;
    private final BudgetExtractCommissionApplicationLogService logService;
    private final BudgetMonthAgentMapper monthAgentMapper;
    @Value("${commission.application.workflowid}")
    private  String tcWorkFlowId;

    @Override
    public CommissionApplicationInfoVO getApplicationInfo(String sumId) {
        BudgetExtractsum budgetExtractsum = extractSumMapper.selectById(sumId);

        String extractMonth = budgetExtractsum.getExtractmonth();
        Long yearid = budgetExtractsum.getYearid();
        String deptid = budgetExtractsum.getDeptid();
        CommissionApplicationInfoVO infoVO = new CommissionApplicationInfoVO();
        Optional<BudgetExtractCommissionApplication> applicationOptional = getApplicationBySumId(sumId);
        if (applicationOptional.isPresent()) {
            BudgetExtractCommissionApplication application = applicationOptional.get();
            infoVO.setApplicationId(application.getId());
            infoVO.setUnitId(deptid);
            infoVO.setYearId(yearid);
            //2020 11 08
            String monthId = extractMonth.substring(4, 6);
            infoVO.setMonthId(Integer.valueOf(monthId));
            infoVO.setPaymentReason(application.getPaymentReason());
            infoVO.setDepartmentName(application.getDepartmentName());
            infoVO.setExtractSumNo(budgetExtractsum.getCode());
            infoVO.setExtractSumId(budgetExtractsum.getId());
            infoVO.setCreateTime(application.getCreateTime());
            infoVO.setRemarks(application.getRemarks());
            //set????????????
            List<CommissionDetailsVO> commissionList = new ArrayList<>();
            infoVO.setCommissionList(commissionList);
            //????????????for
            List<BudgetExtractImportdetail> importDetails = extractImportDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractImportdetail>()
                    .eq(BudgetExtractImportdetail::getExtractsumid, budgetExtractsum.getId()));
            if (CollectionUtils.isNotEmpty(importDetails)) {
                //????????????+??????
                Map<String, List<BudgetExtractImportdetail>> typeList = importDetails.stream().collect(Collectors.groupingBy(x -> x.getExtractType()+"-"+x.getYearid()));
                for (Map.Entry<String, List<BudgetExtractImportdetail>> entry : typeList.entrySet()) {
                    BudgetExtractImportdetail importDetail = entry.getValue().get(0);
                    CommissionDetailsVO detailsVO = new CommissionDetailsVO();
                    detailsVO.setId(importDetail.getId());
                    detailsVO.setCommissionTypeName(importDetail.getExtractType());
                    //??? cache mapper?????????
                    String yearName = yearMapper.getNameById(importDetail.getYearid());
                    detailsVO.setYearId(yearName);
                    //                detailsVO.setYearId(importDetail.getYearid().toString()+"???");
                    BigDecimal applyAmount = BigDecimal.ZERO;
                    BigDecimal actualAmount = BigDecimal.ZERO;
                    for (BudgetExtractImportdetail temp : entry.getValue()) {
                        applyAmount = applyAmount.add(temp.getShouldSendExtract());
                        actualAmount = actualAmount.add(temp.getCopeextract());
                    }

                    detailsVO.setApplyAmount(applyAmount);
                    detailsVO.setActualAmount(actualAmount);
                    detailsVO.setDeductionAmount(actualAmount.subtract(applyAmount));
                    commissionList.add(detailsVO);
                }
            }
            //set????????????
            List<BudgetDetailsVO> budgetList = new ArrayList<>();
            infoVO.setBudgetList(budgetList);
            //????????????for
            List<BudgetExtractCommissionApplicationBudgetDetails> budgetDetails = budgetDetailsService.list(new LambdaQueryWrapper<BudgetExtractCommissionApplicationBudgetDetails>()
                    .eq(BudgetExtractCommissionApplicationBudgetDetails::getApplicationId, application.getId()));
            for (BudgetExtractCommissionApplicationBudgetDetails budgetDetail : budgetDetails) {
                BudgetDetailsVO budgetDetailsVO = new BudgetDetailsVO();
                budgetDetailsVO.setId(budgetDetail.getId());
                budgetDetailsVO.setSubjectId(budgetDetail.getSubjectId());
                budgetDetailsVO.setSubjectCode(budgetDetail.getSubjectCode());
                budgetDetailsVO.setSubjectName(budgetDetail.getSubjectName());
                budgetDetailsVO.setMotivationName(budgetDetail.getMotivationName());
                budgetDetailsVO.setBudgetAmount(budgetDetail.getBudgetAmount());
                budgetList.add(budgetDetailsVO);
            }
            //?????????????????????
            //set????????????
            List<DistributionDetailsVO> distributionList = new ArrayList<>();
            infoVO.setDistributionList(distributionList);
            //????????????
            List<BudgetCommonAttachment> attachmentList = attachmentService.lambdaQuery().eq(BudgetCommonAttachment::getContactId, application.getId()).list();
            if (CollectionUtils.isNotEmpty(attachmentList)) {
                 infoVO.setOaPassword(attachmentList.get(0).getOaPassword());
            }
            List<BudgetCommonAttachmentVO> attachmentVOList = CommonAttachmentConverter.INSTANCE.toVOList(attachmentList);
            infoVO.setAttachmentList(attachmentVOList);

            List<BudgetExtractCommissionApplicationLog> applicationLogs = logService.list(new LambdaQueryWrapper<BudgetExtractCommissionApplicationLog>().eq(BudgetExtractCommissionApplicationLog::getApplicationId, application.getId()));
            List<String> nodeList = new ArrayList<>();
            nodeList.add("???????????????");
            nodeList.add("??????????????????");
            nodeList.add("???????????????");
            nodeList.add("??????????????????");
            nodeList.add("???????????????");
            applicationLogs = applicationLogs.stream().filter(x->nodeList.contains(x.getNodeName())).collect(Collectors.toList());

//            DEPARTMENT_HEAD(2,"?????????????????????"),
//                    FUNCTIONAL_DEPARTMENT(3,"????????????????????????"),
//                    FINANCIAL_SALES_TEAM(4,"?????????????????????"),
//                    FINANCIAL_SALES_TEAM_HEAD(5,"????????????????????????"),
//                    FINANCIAL_DIRECTOR(6,"?????????????????????"),
            for (BudgetExtractCommissionApplicationLog record : applicationLogs) {
                record.setStatusName(LogStatusEnum.getValue(record.getStatus()));
                record.setNodeName(OperationNodeEnum.getValue(record.getNode())==null?record.getNodeName():OperationNodeEnum.getValue(record.getNode()));
            }
            infoVO.setLogList(applicationLogs);
        }


        return infoVO;
    }
    @Override
    public Optional<BudgetExtractCommissionApplication> getApplicationBySumId(String sumId) {
        return this.lambdaQuery().eq(BudgetExtractCommissionApplication::getExtractSumId, sumId).last("limit 1").oneOpt();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateApplicationInfo(CommissionApplicationInfoUpdateVO updateVO) {
        //???????????????
        BudgetExtractsum extractsum = extractSumMapper.selectById (updateVO.getExtractSumId());
        //??????udpateVO
        this.validateApplicationByUpdateVO(updateVO);
        if (extractsum.getStatus() != ExtractStatusEnum.DRAFT.getType() && extractsum.getStatus() != ExtractStatusEnum.RETURN.getType())
            throw new RuntimeException("???????????????????????????????????????????????????????????????");
        BudgetExtractCommissionApplication application = this.getById(updateVO.getApplicationId());
        application.setRemarks(updateVO.getRemarks());
        application.setPaymentReason(updateVO.getPaymentReason());
        List<BudgetDetailsVO> budgetList = updateVO.getBudgetList();

        List<BudgetExtractCommissionApplicationBudgetDetails> oldList =
                budgetDetailsService.lambdaQuery().eq(BudgetExtractCommissionApplicationBudgetDetails::getApplicationId, application.getId()).list();
        for (BudgetDetailsVO budgetDetailsVO : budgetList) {
            Long budgetId = budgetDetailsVO.getId();
            BudgetExtractCommissionApplicationBudgetDetails budgetDetail = new BudgetExtractCommissionApplicationBudgetDetails();
            if(budgetId != null){
                oldList.removeIf(x->x.getId().equals(budgetId));
                budgetDetail = budgetDetailsService.getById(budgetId);
                if (budgetDetail == null) {
                    break;
                }
                budgetDetail.setUpdateBy(UserThreadLocal.getEmpNo());
                budgetDetail.setUpdateTime(new Date());
//                budgetDetail.setu
            }else{
                budgetDetail.setApplicationId(updateVO.getApplicationId());

                budgetDetail.setCreateBy(UserThreadLocal.getEmpNo());
                budgetDetail.setCreateTime(new Date());
                budgetDetail.setUpdateBy(UserThreadLocal.getEmpNo());
                budgetDetail.setUpdateTime(new Date());
            }

            budgetDetail.setMotivationId(budgetDetailsVO.getMotivationId());
            budgetDetail.setMotivationName(budgetDetailsVO.getMotivationName());

            budgetDetail.setSubjectId(budgetDetailsVO.getSubjectId());
            budgetDetail.setSubjectCode(budgetDetailsVO.getSubjectCode());
            budgetDetail.setSubjectName(budgetDetailsVO.getSubjectName());
            budgetDetail.setBudgetAmount(budgetDetailsVO.getBudgetAmount());
            budgetDetailsService.removeByIds(oldList.stream().map(x->x.getId()).collect(Collectors.toList()));
            budgetDetailsService.saveOrUpdate(budgetDetail);
        }
//        UserThreadLocal.get()

        //????????????
        //????????????
        List<BudgetCommonAttachment> attachments = new ArrayList<>();
        List<BudgetCommonAttachment> oldAttachments = attachmentService.lambdaQuery().eq(BudgetCommonAttachment::getContactId, application.getId()).list();
        List<BudgetCommonAttachmentVO> giveAttachmentList = updateVO.getAttachmentList();

        List<BudgetCommonAttachmentVO> addAttachmentList = giveAttachmentList.stream().filter(x -> x.getId() == null).collect(Collectors.toList());
        List<Long> nowIdList = giveAttachmentList.stream()
                .map(BudgetCommonAttachmentVO::getId).filter(Objects::nonNull).collect(Collectors.toList());

        //???id??????????????????
        List<BudgetCommonAttachment> deleteAttachments = oldAttachments.stream().filter(x -> !nowIdList.contains(x.getId())).collect(Collectors.toList());
        //updateId
        List<BudgetCommonAttachment> updateAttachments = oldAttachments.stream().filter(x -> nowIdList.contains(x.getId())).collect(Collectors.toList());
         List<Long> delIds = deleteAttachments.stream().map(BudgetCommonAttachment::getId).collect(Collectors.toList());
        //?????????id???1?????????????????????????????????2?????????????????????????????????
        //????????????id????????????
         attachmentService.removeByIds(delIds);
         //????????????
         deleteAttachments.forEach(x->{
            try {
                storageClient.delete_file("group1",x.getFileUrl());
            } catch (IOException | MyException e) {
                e.printStackTrace();
            }
        });
        for (BudgetCommonAttachmentVO attachmentVO : addAttachmentList) {
            BudgetCommonAttachment attachment = new BudgetCommonAttachment();
            attachment.setContactId(application.getId());
            attachment.setFileName(attachmentVO.getFileName());
            attachment.setFileType(1);
            attachment.setOaPassword(updateVO.getOaPassword()==null?"":updateVO.getOaPassword());
            attachment.setFileUrl(attachmentVO.getFileUrl());
            attachment.setFileExtName(FileUtils.getFileType(attachmentVO.getFileName()));
            attachment.setCreator(UserThreadLocal.getEmpNo());
            attachment.setCreateTime(new Date());
            attachments.add(attachment);
        }
        updateAttachments.forEach(x->{
            x.setOaPassword(updateVO.getOaPassword()==null?"":updateVO.getOaPassword());
            attachments.add(x);
        });
        attachmentService.saveOrUpdateBatch(attachments);
        extractsum.setStatus(ExtractStatusEnum.DRAFT.getType());
        extractSumMapper.updateById(extractsum);
        this.updateById(application);
    }

    private void validateApplicationByUpdateVO(CommissionApplicationInfoUpdateVO updateVO) {
        //????????????for
        List<BudgetExtractImportdetail> importDetails = extractImportDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractImportdetail>()
                .eq(BudgetExtractImportdetail::getExtractsumid, updateVO.getExtractSumId()));
        //???????????? ????????????????????????????????????
        List<BudgetExtractImportdetail> awardList = importDetails.stream()
                .filter(x -> ExtractTypeEnum.ACCRUED_PERFORMANCE_AWARD.value.equals(x.getExtractType()) || ExtractTypeEnum.PERFORMANCE_AWARD_COMMISSION.value.equals(x.getExtractType())).collect(Collectors.toList());

        if(CollectionUtils.isNotEmpty(awardList)) {
            List<BudgetDetailsVO> budgetList = updateVO.getBudgetList();
            if(CollectionUtils.isEmpty(budgetList)) {
                throw new RuntimeException("????????????????????????");
            }
            Optional<BigDecimal> bigDecimal = budgetList.stream().map(x -> x.getBudgetAmount()).reduce(BigDecimal::add);
            Optional<BigDecimal> reduce = awardList.stream().map(x -> x.getShouldSendExtract()).reduce(BigDecimal::add);
            if (bigDecimal.isPresent() && reduce.isPresent()) {
                int i = bigDecimal.get().compareTo(reduce.get());
                if (i != 0) {
                    throw new RuntimeException("????????????????????????????????????????????????");
                }
            }
        }

//            //budget ???????????????
//            Optional<BudgetExtractCommissionApplication> applicationBySumId = this.getApplicationBySumId(String.valueOf(extractSum.getId()));
//            if(applicationBySumId.isPresent()){
//                List<BudgetExtractCommissionApplicationBudgetDetails> budgetDetailList
//                        =budgetDetailsService.lambdaQuery().eq(BudgetExtractCommissionApplicationBudgetDetails::getApplicationId, applicationBySumId.get().getId()).list();
//                if(CollectionUtils.isEmpty(budgetDetailList)) {
//                    throw new RuntimeException("????????????????????????");
//                }
//                Optional<BigDecimal> bigDecimal = budgetDetailList.stream().map(x -> x.getBudgetAmount()).reduce(BigDecimal::add);
//                Optional<BigDecimal> reduce = awardList.stream().map(x -> x.getShouldSendExtract()).reduce(BigDecimal::add);
//                if(bigDecimal.isPresent()&&reduce.isPresent()){
//                    int i = bigDecimal.get().compareTo(reduce.get());
//                    if (i!=0) {
//                        throw new RuntimeException("??????????????????????????????????????????");
//                    }
//                }
//            }
    }

    @Override
    /**
     * ???????????????????????????????????????
     */
    public PageResult<BudgetSubjectVO> listSubjectMonthAgent(HashMap<String, Object> paramMap, Integer page, Integer rows) {
        Page<BudgetSubjectVO> pageBean = new Page<>(page, rows);
        List<BudgetSubjectVO> resultList = this.baseMapper.listSubjectMonthAgentByMap(pageBean, paramMap);
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    @Override
    public List<IndividualIssueExportDTO> exportIssuedTemplate(String extractMonth) {
        List<IndividualIssueExportDTO> exportDTOList = new ArrayList<>();
        //????????????????????????????????????
        //???????????????????????????????????????????????????????????? ?????????????????????
        List<IndividualIssueExportDTO> issueExportDTOList = extractSumMapper.selectAllDetailList(extractMonth);
        String outerPayUnit= getOuterPayUnit();


        List<String> empNos = issueExportDTOList.stream().map(x->String.valueOf(x.getEmployeeJobNum())).collect(Collectors.toList());

        List<Map<String, String>> unitByEmpNoList = hrService.getSalaryUnitByEmpNos(empNos);
        Map<String, String> unitByEmpNos = unitByEmpNoList.stream().collect(Collectors.toMap(x -> x.get("empNo"), x -> String.valueOf( x.get("companyId")), (k1, k2) -> k1));

        BudgetBillingUnit billingUnit;
        for (IndividualIssueExportDTO dto : issueExportDTOList) {
            if (ExtractUserTypeEnum.EXTERNAL_STAFF.getCode().equals(dto.getBusinessType())) {
                billingUnit = UnitCache.get(outerPayUnit);
                if (billingUnit != null) {
                    //???????????????
                    dto.setIssuedUnit(billingUnit.getName());
                }
            }else {
                //????????????
                String unitId = unitByEmpNos.get(String.valueOf(dto.getEmployeeJobNum()));
                if (StringUtils.isNotBlank(unitId)) {
                     billingUnit = UnitCache.getByOutKey(unitId);
                    if (billingUnit != null) {
                        //???????????????
                        dto.setIssuedUnit(billingUnit.getName());
                    }
                }
            }
            exportDTOList.add(dto);
        }
        return exportDTOList;
    }

    @Override
    @SneakyThrows
    @Transactional(rollbackFor = Exception.class)
    public List<FeeImportErrorDTO> importFeeTemplate(MultipartFile multipartFile,String extractMonth) {
        List<FeeImportErrorDTO> errList = new ArrayList<>();
        List<Map> errorMap = new ArrayList<>();
        List<String> errorList = new ArrayList<>();
//        BudgetExtractsum budgetExtractsum = extractSumMapper.selectById(sumId);
        try {
            EasyExcel.read(multipartFile.getInputStream(), IndividualIssueExportDTO.class,
                    new PageReadListener<IndividualIssueExportDTO>(dataList -> {
                     List<BudgetExtractFeePayDetailBeforeCal> entities = new ArrayList<>();
                        for (IndividualIssueExportDTO dto : dataList) {
                            try {
                                //??????
                                validData(dto);
                                //?????????????????????insert
                                BudgetExtractFeePayDetailBeforeCal payDetail= new BudgetExtractFeePayDetailBeforeCal();
                                BudgetExtractFeePayDetailBeforeCal payDetailBeforeCal = feePayDetailMapper.selectOne(new LambdaQueryWrapper<BudgetExtractFeePayDetailBeforeCal>()
                                        .eq(BudgetExtractFeePayDetailBeforeCal::getExtractMonth, extractMonth)
                                        .eq(BudgetExtractFeePayDetailBeforeCal::getEmpNo, dto.getEmployeeJobNum()).last("limit 1"));
                                if(payDetailBeforeCal==null){
                                    payDetail.setEmpNo(String.valueOf(dto.getEmployeeJobNum()));
                                    payDetail.setEmpName(dto.getEmployeeName());
                                    payDetail.setExtractMonth(extractMonth);

                                    payDetail.setCreatorName(UserThreadLocal.getEmpName());
                                    payDetail.setCreator(UserThreadLocal.getEmpNo());
                                    payDetail.setCreateTime(new Date());
                                }else {
                                    payDetail = payDetailBeforeCal;
                                }

                                payDetail.setUpdateBy(UserThreadLocal.getEmpName());
                                payDetail.setUpdateTime(new Date());
                                //???????????? ????????????
                                payDetail.setFeePay(dto.getPaymentAmount());
                                payDetail.setCopeextract(dto.getCopeextract());
                                //????????????
                                BudgetBillingUnit billingUnit = UnitCache.getByName(dto.getIssuedUnit());
                                if (billingUnit!=null) {
                                    payDetail.setIssuedUnit(billingUnit.getId());
                                    payDetail.setIssuedUnitName(dto.getIssuedUnit());
                                }

                                if (payDetail.getId()!=null) {
                                    feePayDetailMapper.updateById(payDetail);
                                }else{
                                    feePayDetailMapper.insert(payDetail);
                                }
                            }
                            catch (Exception e) {
                                FeeImportErrorDTO errorDTO = new FeeImportErrorDTO();
                                try {
                                    PropertyUtils.copyProperties(errorDTO,dto);
                                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                                    ex.printStackTrace();
                                }
                                errorDTO.setInsertDatabaseError(e.getMessage()==null?e.toString():e.getMessage());
                                errList.add(errorDTO);
                            }
                        }
                        System.out.println(dataList);
                    }, errorMap)).sheet().doRead();

            for (Map map : errorMap) {
                FeeImportErrorDTO errorDTO = new FeeImportErrorDTO();
                try {
                    BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
                    ConvertUtils.register(new DateLocaleConverter(), Date.class);//BeanUtils.populate????????????????????????????????????????????????
                    BeanUtils.populate(errorDTO,map);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                errList.add(errorDTO);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return errList;
    }

    @Override
    public IPage<BudgetExtractFeePayDetailBeforeCal> selectFeePage(FeeQuery query) {

        //new Page<>(query.getPageSize(), query.getPageSize()
        Page<BudgetExtractFeePayDetailBeforeCal> beforeCalPage =  feePayDetailMapper.selectFeePage(new Page<>(query.getPage(), query.getRows()),query);

//        Page<BudgetExtractFeePayDetailBeforeCal> beforeCalPage = feePayDetailMapper.selectPage(new Page<BudgetExtractFeePayDetailBeforeCal>(query.getPageSize(), query.getPageSize()),
//                new LambdaQueryWrapper<BudgetExtractFeePayDetailBeforeCal>()
//                        .eq(BudgetExtractFeePayDetailBeforeCal::getExtractMonth, query.getExtractMonth())
//                        //????????????
//                        .and(StringUtils.isNotBlank(query.getEmployeeName()),i->i.like(BudgetExtractFeePayDetailBeforeCal::getEmpName,query.getEmployeeName())
//                        .or()
//                        .like(BudgetExtractFeePayDetailBeforeCal::getEmpNo,query.getEmployeeName()))
//        );
        return beforeCalPage;
    }

    @Override
    public void validateApplication(BudgetExtractsum extractSum) {
        //????????????for
        List<BudgetExtractImportdetail> importDetails = extractImportDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractImportdetail>()
                .eq(BudgetExtractImportdetail::getExtractsumid, extractSum.getId()));
        //???????????? ????????????????????????????????????
        List<BudgetExtractImportdetail> awardList = importDetails.stream()
                .filter(x -> ExtractTypeEnum.ACCRUED_PERFORMANCE_AWARD.value.equals(x.getExtractType()) || ExtractTypeEnum.PERFORMANCE_AWARD_COMMISSION.value.equals(x.getExtractType())).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(awardList)){
            //budget ???????????????
            Optional<BudgetExtractCommissionApplication> applicationBySumId = this.getApplicationBySumId(String.valueOf(extractSum.getId()));
            if(applicationBySumId.isPresent()){
                List<BudgetExtractCommissionApplicationBudgetDetails> budgetDetailList
                        =budgetDetailsService.lambdaQuery().eq(BudgetExtractCommissionApplicationBudgetDetails::getApplicationId, applicationBySumId.get().getId()).list();
                if(CollectionUtils.isEmpty(budgetDetailList)) {
                    throw new RuntimeException("????????????????????????");
                }
                Optional<BigDecimal> bigDecimal = budgetDetailList.stream().map(x -> x.getBudgetAmount()).reduce(BigDecimal::add);
                Optional<BigDecimal> reduce = awardList.stream().map(x -> x.getShouldSendExtract()).reduce(BigDecimal::add);
                if(bigDecimal.isPresent()&&reduce.isPresent()){
                    int i = bigDecimal.get().compareTo(reduce.get());
                    if (i!=0) {
                        throw new RuntimeException("????????????????????????????????????????????????");
                    }
                }
            }

        }
    }

//    @SneakyThrows
    @Override
    public void uploadOA(BudgetExtractCommissionApplication application) throws IOException {
        Long extractSumId = application.getExtractSumId();
        BudgetExtractsum extractSum = extractSumMapper.selectById(extractSumId);
        //????????????
        String deptName = extractSum.getDeptname();
        String oaDeptId = oaMapper.getDeptId(deptName);
        if(StringUtils.isBlank(oaDeptId)){
            throw new RuntimeException("????????????oa???????????????????????????");
        }
        WbUser user = UserThreadLocal.get();
        //??????oa ??????
        String userIdDeptId = oaMapper.getOaUserId(user.getUserName());
//        String userIdDeptId = oaService.getOaUserId(user.getUserName(),new ArrayList<>());
        String oaUserId = userIdDeptId.split(",")[0];
//        String oaDeptId = userIdDeptId.split(",")[1];
//        oaUserId = "5001";
        if(oaUserId.equals("0")){
            throw new RuntimeException("?????????????????????oa???userId!");
        }
        application.setOaCreatorId(oaUserId);
        //todo ????????????

        String userName = user.getDisplayName();
        WorkflowInfo wi = new WorkflowInfo();
        wi.setCreatorId(oaUserId);
        wi.setRequestLevel("0");
        wi.setRequestName("?????????????????????--" + userName);
        OAApplicationDTO oaDTO = new OAApplicationDTO();
        oaDTO.setSqr(oaUserId);
        oaDTO.setBm(oaDeptId);
        oaDTO.setZbrq(DateUtil.getStrYMDByDate(application.getCreateTime()) );
        oaDTO.setZfsy(application.getPaymentReason());
        oaDTO.setBz(StringUtils.isNotBlank(application.getRemarks())?application.getRemarks():"");
        oaDTO.setBh(extractSum.getCode());
        oaDTO.setWfid(tcWorkFlowId);

        //????????????
        List<BudgetCommonAttachment> attachments = attachmentService.lambdaQuery().eq(BudgetCommonAttachment::getContactId, application.getId()).list();
        String fj = "";
        for (int i = 0; i < attachments.size(); i++) {

            BudgetCommonAttachment attachment = attachments.get(i);
            String fileUrl = attachments.get(i).getFileUrl();
            //http://tuku.jtyjy.com/
            String prefix = "http://tuku.jtyjy.com/";
            if (!fileUrl.contains(prefix)) {
                fileUrl = prefix + fileUrl;
            }
            int code = -1;
            if (StringUtils.isNotBlank(fileUrl)) {

                String oaPassword = attachment.getOaPassword();
//                oaPassword = "1";
                URL url = new URL(fileUrl);
                URLConnection connection = url.openConnection();
                InputStream is = connection.getInputStream();
                String fileOriginName = attachment.getFileName();
                //todo
//                code = this.oaService.createDoc("20192", AesUtil.encrypt("067540"), is, fileOriginName, fileUrl, fileOriginName);
                code = this.oaService.createDoc(attachment.getCreator(), oaPassword, is, fileOriginName, fileUrl, fileOriginName);
                if (code == 0) {
                    throw new RuntimeException("????????????!??????????????????!");
                }
                //docCode
                String charName = ",";
                String temp = String.valueOf(code);
                //???????????????????????? ","
                if (i != attachments.size() - 1) {
                    temp = temp + charName;
                }
                fj = fj + temp;
                is.close();
            }
        }
        oaDTO.setFj(fj);

        List<BudgetExtractImportdetail> importDetailList = extractImportDetailMapper.selectList
                (new LambdaQueryWrapper<BudgetExtractImportdetail>().eq(BudgetExtractImportdetail::getExtractsumid,extractSumId));
        List<OAApplicationDetailDTO> oaDetailList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(importDetailList)) {
            //????????????+??????
            Map<String, List<BudgetExtractImportdetail>> typeList = importDetailList.stream().collect(Collectors.groupingBy(x -> x.getExtractType()+"-"+x.getYearid()));
            for (Map.Entry<String, List<BudgetExtractImportdetail>> entry : typeList.entrySet()) {
                BudgetExtractImportdetail detail = entry.getValue().get(0);
                OAApplicationDetailDTO dto = new OAApplicationDetailDTO();
                dto.setTclx(detail.getExtractType());
                //????????????
                String yearName = yearMapper.getNameById(detail.getYearid());
                dto.setGslx(yearName);
                //                detailsVO.setYearId(importDetail.getYearid().toString()+"???");
                BigDecimal applyAmount = BigDecimal.ZERO;
                BigDecimal actualAmount = BigDecimal.ZERO;
                for (BudgetExtractImportdetail temp : entry.getValue()) {
                    applyAmount = applyAmount.add(temp.getShouldSendExtract());
                    actualAmount = actualAmount.add(temp.getCopeextract());
                }
                dto.setSqtc(String.valueOf(applyAmount));
                dto.setSfje(String.valueOf(actualAmount));
                dto.setKkje(String.valueOf(actualAmount.subtract(applyAmount)));
                dto.setWfid(tcWorkFlowId);
                oaDetailList.add(dto);
            }
        }
        String oldRequestId = application.getRequestId();
        if (oldRequestId != null) {
            String oaCreatorId = application.getOaCreatorId();
            oaService.deleteRequest(oldRequestId, oaCreatorId);
        }
        Map<String, Object> main = (Map<String, Object>) JSON.toJSON(oaDTO);
        List<Map<String, Object>> list = (List<Map<String, Object>>) JSON.toJSON(oaDetailList);
        System.out.println(JSONObject.toJSON(wi));
//       http://192.168.4.63/workflow/workflow/addwf0.jsp?ajax=1&src=editwf&wfid=5263&isTemplate=0
        String requestId =  oaService.createWorkflow(wi, tcWorkFlowId, main, list);
        if (requestId == null || Integer.parseInt(requestId) < 0) {
            System.out.println(requestId+"???");
            throw new RuntimeException("???????????????oa?????????????????????????????????????????????oa????????????");
        }
        application.setRequestId(requestId);
        application.setOaCreatorId(oaUserId);
    }

    @Override
    public void validateExtractMonth(String extractMonth) {
        //??????????????????????????????
        List<BudgetExtractsum> nowSums = extractSumMapper.selectList(new LambdaQueryWrapper<BudgetExtractsum>().eq(BudgetExtractsum::getExtractmonth, extractMonth).
                ne(BudgetExtractsum::getStatus, ExtractStatusEnum.REJECT.type));

        long count = nowSums.stream().filter(x -> !x.getStatus().equals(ExtractStatusEnum.APPROVED.getType())).count();
        if(count!=0){
            throw new BusinessException("??????????????????["+extractMonth+"]???????????????????????????????????????????????????");
        }
        //???????????????
        BudgetExtractTaxHandleRecord recordServiceOne =
                taxHandleRecordService.getOne(new LambdaQueryWrapper<BudgetExtractTaxHandleRecord>().eq(BudgetExtractTaxHandleRecord::getExtractMonth, extractMonth));
        if (recordServiceOne != null) {
            List<BudgetExtractImportdetail> importDetailList = extractImportDetailMapper.getAllByExtractMonth(extractMonth);
            long selfCount = importDetailList.stream().filter(x -> x.getBusinessType().equals(ExtractUserTypeEnum.SELF_EMPLOYED_EMPLOYEES)).count();
            if (selfCount == 0) {
                //????????????0
                if (recordServiceOne.getIsCalComplete() || recordServiceOne.getIsSetExcessComplete()) {
                    throw new BusinessException("?????????????????????????????????????????????");
                }
            } else {
                if (recordServiceOne.getIsCalComplete() || recordServiceOne.getIsSetExcessComplete() || recordServiceOne.getIsPersonalityComplete()) {
                    throw new BusinessException("?????????????????????????????????????????????");
                }
            }
        }
    }
    @Override
    public void validStatusIsAllVerify(String extractMonth) {
        List<BudgetExtractsum> nowSums = extractSumMapper.selectList(new LambdaQueryWrapper<BudgetExtractsum>()
                .eq(BudgetExtractsum::getExtractmonth, extractMonth)
                .ne(BudgetExtractsum::getStatus, ExtractStatusEnum.REJECT.type));

        long count = nowSums.stream().filter(x -> !x.getStatus().equals(ExtractStatusEnum.APPROVED.getType())).count();
        if(count!=0){
            throw new BusinessException("??????????????????["+extractMonth+"]?????????????????????????????????");
        }
    }

    private void validData(IndividualIssueExportDTO dto) {
        //??????????????????
        String issuedUnit = dto.getIssuedUnit();
        BudgetBillingUnit billingUnit = UnitCache.getByName(issuedUnit);
        if(billingUnit==null){
            throw new RuntimeException("??????????????????????????????");
        }
        //??????????????????
        WbUser user = getUserByEmpno(String.valueOf(dto.getEmployeeJobNum()));
        BudgetExtractOuterperson outerperson = getOuterPersonByEmpNo(dto.getEmployeeJobNum());
        if (user == null && outerperson==null) {
            throw new RuntimeException("????????????????????????" + dto.getEmployeeJobNum() + "????????????!");
        } else if(user != null){
            if (!dto.getEmployeeName().equals(user.getDisplayName())) {
                throw new RuntimeException("????????????????????????!??????????????????" + user.getDisplayName() + "???");
            }
        }else if (outerperson != null){
            if (!dto.getEmployeeName().equals(outerperson.getName())) {
                throw new RuntimeException("????????????????????????????????????!??????????????????" + outerperson.getName() + "???");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatusBySumId(String sumId, Integer status) {
        Optional<BudgetExtractCommissionApplication> applicationOptional = getApplicationBySumId(sumId);
        BudgetExtractsum budgetExtractsum = extractSumMapper.selectById(sumId);
        if (budgetExtractsum == null) {
            throw new BusinessException("????????????????????????");
        }
        if (applicationOptional.isPresent()) {
            BudgetExtractCommissionApplication application = applicationOptional.get();
            ExtractStatusEnum willEnum = ExtractStatusEnum.getTypeEnume(status);
            switch (willEnum) {
                case REJECT:
                    //????????????  ????????????????????????-1
                    if (!budgetExtractsum.getStatus().equals(-1)) {
                        throw new BusinessException("????????????,?????????????????????????????????");
                    }
                    break;
                case RETURN:
                    //????????????
                    if (!budgetExtractsum.getStatus().equals(2)) {
                        throw new BusinessException("??????????????????,???????????????????????????????????????");
//                        ?????????????????????????????????
                    }
                    //???????????????????????????????????????
                    //?????????
                    String extractMonth = budgetExtractsum.getExtractmonth();
                    Integer returnCount =  feePayDetailMapper.selectCount(new LambdaQueryWrapper<BudgetExtractFeePayDetailBeforeCal>()
                            .eq(BudgetExtractFeePayDetailBeforeCal::getExtractMonth,extractMonth));
                    if (returnCount>0) {
                        throw new BusinessException("??????????????????????????????????????????");
                    }
                    //????????????
                    BudgetExtractTaxHandleRecord recordServiceOne = taxHandleRecordService.getOne(new LambdaQueryWrapper<BudgetExtractTaxHandleRecord>().eq(BudgetExtractTaxHandleRecord::getExtractMonth, extractMonth));
                    //?????????????????????
                    if(recordServiceOne!=null) {
//                    List<BudgetExtractImportdetail> importDetailList = extractImportDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractImportdetail>().eq(BudgetExtractImportdetail::getExtractsumid, sumId));
//                    long selfCount = importDetailList.stream().filter(x -> x.getBusinessType().equals(ExtractUserTypeEnum.SELF_EMPLOYED_EMPLOYEES)).count();
                        if (recordServiceOne.getIsCalComplete() || recordServiceOne.getIsSetExcessComplete() || recordServiceOne.getIsPersonalityComplete()) {
                            throw new BusinessException("?????????????????????????????????");
                        }
                    }
                    applicationLogService.saveLog(application.getId(),OperationNodeEnum.TAX_RETURN, LogStatusEnum.REJECT);
                    break;
                case DRAFT:
                    //??????  ?????????????????????????????????
                    if (Objects.equals(ExtractStatusEnum.VERIFYING.type,budgetExtractsum.getStatus())) {
                        //1 ?????????
                        BudgetExtractCommissionApplicationLog log = applicationLogService.lambdaQuery()
                                .eq(BudgetExtractCommissionApplicationLog::getApplicationId, application.getId())
                                .orderByDesc(BudgetExtractCommissionApplicationLog::getCreateTime).last("limit 1").one();
                        if(log != null) {
                            //???????????? ?????????/?????? ???????????? ??? ?????????????????????
                            if (Objects.equals(LogStatusEnum.PASS.getCode(), log.getStatus())) {
                                throw new BusinessException("????????????,?????????????????????");
                            } else{
                                // ?????????/??????  ??????????????????
                                String requestId = application.getRequestId();
                                String oaCreatorId = application.getOaCreatorId();
                                if (requestId != null) {
                                    this.oaService.deleteRequest(requestId, oaCreatorId);
                                }
                            }
                        }
                    }else{
                        throw new BusinessException("????????????,??????????????????????????????");
                    }
                    break;
                default:
                    break;
            }
            //???????????????
            if (application.getReimbursementId()!=null) {
                BudgetReimbursementorder reimbursementorder = reimbursementorderService.getById(application.getReimbursementId());
                if(reimbursementorder!=null) {
                    reimbursementorderService.removeById(reimbursementorder.getId());
                }
            }
            //?????????application?????????
//            this.lambdaUpdate().eq(BudgetExtractCommissionApplication::getExtractSumId,sumId).set(BudgetExtractCommissionApplication::getStatus,status);
            budgetExtractsum.setStatus(status);
            extractSumMapper.updateById(budgetExtractsum);
        }else {
            throw new BusinessException("??????????????????");
        }
    }


    @SneakyThrows
    @Override
    public void generateReimbursement(BudgetExtractCommissionApplication application, BudgetExtractsum extractsum) {
        Long applicationId = application.getId();
        //todo ???????????????
//            BudgetExtractImportdetail
        List<BudgetExtractImportdetail> importDetails = extractImportDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractImportdetail>()
                .eq(BudgetExtractImportdetail::getExtractsumid, extractsum.getId()));

        //?????? ?????????
        Boolean ifExistsCommission = importDetails.stream().anyMatch(x -> {
            return ExtractTypeEnum.PERFORMANCE_AWARD_COMMISSION.value.equals(x.getExtractType()) || ExtractTypeEnum.ACCRUED_PERFORMANCE_AWARD.value.equals(x.getExtractType());
        });
        TabDm tabDm = tabDmMapper.selectOne(new LambdaQueryWrapper<TabDm>().eq(TabDm::getDmType, "extract").eq(TabDm::getDm, "reimbursementDefalutBillingUnitId"));
        if(Objects.isNull(tabDm))throw new RuntimeException("???????????????????????????????????????????????????????????????");

        if (ifExistsCommission) {
            List<BudgetExtractCommissionApplicationBudgetDetails> budgetDetailsList =
                    budgetDetailsService.lambdaQuery().eq(BudgetExtractCommissionApplicationBudgetDetails::getApplicationId, applicationId).list();
            if (CollectionUtils.isEmpty(budgetDetailsList)) {
                return;
            }
            //????????? ??????
            ReimbursementRequest reimbursementRequest = new ReimbursementRequest();
            List<BudgetReimbursementorderDetail> orderDetailList = new ArrayList<>();

            BigDecimal otherTotalMoney = BigDecimal.ZERO;
            for (BudgetExtractCommissionApplicationBudgetDetails budgetDetails : budgetDetailsList) {
                BudgetReimbursementorderDetail reimbursement = new BudgetReimbursementorderDetail();
                reimbursement.setSubjectid(budgetDetails.getSubjectId());
                reimbursement.setSubjectCode(budgetDetails.getSubjectCode());
                reimbursement.setSubjectname(budgetDetails.getSubjectName());
                //??????
                reimbursement.setMonthagentname(budgetDetails.getMotivationName());
                reimbursement.setMonthagentid(budgetDetails.getMotivationId());
                //U0068,?????????(??????) ??????
                reimbursement.setBunitid(Long.valueOf(tabDm.getDmValue()));
                reimbursement.setBunitname( UnitCache.get(reimbursement.getBunitid().toString()).getName());
                //????????????
                reimbursement.setReimflag(true);
                BudgetMonthAgent budgetMonthAgent = monthAgentMapper.selectById(budgetDetails.getMotivationId());
                reimbursement.setMonthagentmoney(budgetMonthAgent.getTotal());
                reimbursement.setMonthagentunmoney(budgetMonthAgent.getTotal().add(budgetMonthAgent.getAddmoney()).subtract(budgetMonthAgent.getExecutemoney()));
                reimbursement.setYearagentmoney(budgetMonthAgent.getYearagentmoney());
                reimbursement.setYearagentunmoney(budgetMonthAgent.getYearagentmoney().add(budgetMonthAgent.getAddmoney()).add(budgetMonthAgent.getLendinmoney()).subtract(budgetMonthAgent.getLendoutmoney()).subtract(budgetMonthAgent.getExecutemoney()));
                reimbursement.setReimmoney(budgetDetails.getBudgetAmount());
                otherTotalMoney = otherTotalMoney.add(budgetDetails.getBudgetAmount());
                orderDetailList.add(reimbursement);
            }
            BudgetReimbursementorder order = getTestBean(extractsum);
            order.setOthermoney(otherTotalMoney);
            order.setReimmoney(otherTotalMoney);
            order.setLackBill(false);
            reimbursementRequest.setSubmit(String.valueOf(1));
            reimbursementRequest.setOrder(order);
            reimbursementRequest.setOrderDetail(orderDetailList);

            try {
                //??????id
                String returnId = reimbursementWorker.saveReturnId(reimbursementRequest, true);
                if (StringUtils.isNotBlank(returnId)) {
                    application.setReimbursementId(Long.valueOf(returnId));
//                        this.saveOrUpdate(application);
                }
//                    reimbursementController.opt(reimbursementRequest);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage() == null ? e.toString() : e.getMessage());
            }
        }

    }

    @Override
    public void importIndividual(MultipartFile multipartFile) {
        //?????????
//        multipartFile.
//        multipartFile.getInputStream();
    }
    private  BudgetReimbursementorder getTestBean(BudgetExtractsum extractsum) {
        //?????????????????????
        BudgetReimbursementorder order = new BudgetReimbursementorder();
        //????????????
//        commonData.getBxUnit().getId()
        order.setUnitid(Long.valueOf(extractsum.getDeptid()));
        order.setYearid(extractsum.getYearid());
        order.setReimperonsid(UserThreadLocal.get().getUserId());
        order.setPaymentmoney(BigDecimal.ZERO);
        order.setReimperonsname(UserThreadLocal.get().getDisplayName());
        String monthId = extractsum.getExtractmonth().substring(4, 6);
        order.setMonthid(Long.valueOf(monthId));
        order.setBxtype(ReimbursementTypeEnmu.COMMON.getCode());
        //??????????????? 0????????????????????????????????????????????????1????????? 2????????? 3????????? 4:????????????
        order.setOrderscrtype(ReimbursementFromEnmu.COMMISSION.getCode());

        order.setReimdate(new Date());
        order.setReimmoney(BigDecimal.ZERO);
        order.setNonreimmoney(BigDecimal.ZERO);
        order.setPaymentmoney(BigDecimal.ZERO);
        order.setTransmoney(BigDecimal.ZERO);
        order.setCashmoney(BigDecimal.ZERO);
        order.setAllocatedmoney(BigDecimal.ZERO);
        order.setOthermoney(BigDecimal.ZERO);
        order.setAttachcount(0);
        order.setRemark("?????????????????????");
        return order;
    }
    @Override
    public void saveEntity(BudgetExtractsum extract,String badDebt,Object... params) {
        //???????????????????????????
        //??????+????????????+????????????+????????????+?????????/?????????
        //?????????????????????????????????????????????????????????????????????????????????/??????????????????????????????/???????????????????????????????????????????????????????????????????????????
        BudgetExtractCommissionApplication application = new BudgetExtractCommissionApplication();
        //??????????????????
//        extract.getStatus()
//              2020 10 06

        String yearName = yearMapper.getNameById(extract.getYearid());
        Integer month = Integer.valueOf(extract.getExtractmonth().substring(4, 6));
        Integer batchNo = Integer.valueOf(extract.getExtractmonth().substring(6));
        String paymentReason = MessageFormat.format("??????{0}{1}??????{2}???{3}",yearName,month,batchNo,badDebt);
        //0 ?????????

        application.setPaymentReason(paymentReason);
        application.setDepartmentName(extract.getDeptname());
        application.setDepartmentNo(extract.getDeptid());
        application.setExtractSumId(extract.getId());
        application.setRemarks(extract.getRemark());

        application.setStatus(0);
        application.setCreateTime(new Date());
        application.setCreateBy(UserThreadLocal.getEmpNo());
        application.setUpdateTime(new Date());
        application.setUpdateBy(UserThreadLocal.getEmpNo());
        this.save(application);
    }


    @Override
    public void saveExtractImportDetails(Map<Integer, String> data, BudgetExtractsum extractSum) {
        String businessType = data.get(0); //????????????  ???????????????????????????
        //????????????????????????

        String empNo = data.get(1); //??????
        String empName = data.get(2); //??????
        String isDebt = data.get(3); //????????????   index = 3,????????????4???

        String extractType = data.get(4);//????????????
        String tcPeriod = data.get(5); //???????????? ???6???

        //??????????????????
        String totalPrice = data.get(6);//??????  ????????????7???,index= 6
        String actualPrice = data.get(7);//??????
        String collection = data.get(8);//??????
        String income = data.get(9);  //??????  ???10???

        String helpCollectionHost = data.get(10);//   ???????????????????????????
        String strippingReceivedFunds = data.get(11);//????????????
        String regularCommission = data.get(12);//????????????
        String takeOverTheCommission = data.get(13);//????????????
        String specialCommission = data.get(14);//????????????

        String totalRoyalty = data.get(15);//?????????  16???

        String paidCommission = data.get(16);//????????????
        String reservedCommission = data.get(17);//????????????
        String shouldSendExtract = data.get(18);//????????????


        //???????????????
        String tax = data.get(19);//????????????
        String taxReduction = data.get(20);//???????????????
        String consotax = data.get(21);//?????????
        String invoiceExcessTax = data.get(22);//??????????????????
        String invoiceExcessTaxReduction = data.get(23);//?????????????????????
        String excessTaxPreviousInvoices = data.get(24);//????????????????????????  ???25???

        //????????????
        String lateFee = data.get(25);//?????????
        String deliveryLogisticsFee = data.get(26);//???????????????
        String shippingCost = data.get(27);//????????????
        String sampleIssuingCost = data.get(28);//????????????
        String returnLogisticsFee = data.get(29);//???????????????   30 ???

        //????????????--??????
        String returnCost = data.get(30);//????????????
        String distributionCost = data.get(31);//????????????
        String shiftPackingFee = data.get(32);//???????????????
        String giftFee = data.get(33);//?????????
        String badDebtAssessment = data.get(34);//????????????   30 ???
        String nonConformancePenalty = data.get(35);//???????????????   30 ???

//        //????????????
//        String currentDeduction = data.get(36);//????????????
//        String deductionGuarantee = data.get(37);//?????????
//        String deductCreditInformation = data.get(38);//?????????
//
//        String salesmanAdvance = data.get(39);//???????????????
//        String otherTypesDeduction = data.get(40);//???????????? ??????
//        String subtotalOfDeduction = data.get(41);//????????????
//        String copeextract = data.get(42);//????????????

        //????????????
        String previousCost = data.get(36);//????????????
        String currentDeduction = data.get(37);//????????????
        String deductionGuarantee = data.get(38);//?????????
        String deductCreditInformation = data.get(39);//?????????

        String salesmanAdvance = data.get(40);//???????????????
        String otherTypesDeduction = data.get(41);//???????????? ??????
        String subtotalOfDeduction = data.get(42);//????????????
        String copeextract = data.get(43);//????????????


        BudgetYearPeriod yearPeriod = getPeriodByName(tcPeriod);
        //??????????????????
        BudgetExtractImportdetail extractImportdetail = null;
        if (Objects.nonNull(extractImportdetail)) {
            extractImportdetail.setTax(new BigDecimal(tax)); // ??????
            extractImportdetail.setConsotax(new BigDecimal(consotax)); // ?????????
            extractImportdetail.setCopeextract(new BigDecimal(copeextract));// ????????????
            extractImportdetail.setUpdatetime(new Date());
            extractImportdetail.setUpdateBy(UserThreadLocal.getEmpNo());
            extractImportDetailMapper.updateById(extractImportdetail);
        } else {
            extractImportdetail = new BudgetExtractImportdetail();
            extractImportdetail.setId(null);
            extractImportdetail.setExtractsumid(extractSum.getId());
            //?????? ????????????
            setUserTypeValue(businessType, empNo, empName, extractImportdetail);

            extractImportdetail.setEmpno(empNo);
            extractImportdetail.setEmpname(empName);
            extractImportdetail.setIsbaddebt("???".equals(isDebt) ? true : false);
            extractImportdetail.setYearid(yearPeriod.getId());
            extractImportdetail.setExtractType(extractType);



            //??????????????????
            extractImportdetail.setTotalPrice(getBigDecimal(totalPrice));
            extractImportdetail.setActualPrice(getBigDecimal(actualPrice));
            extractImportdetail.setCollection(getBigDecimal(collection));
            extractImportdetail.setIncome(getBigDecimal(income));

            extractImportdetail.setHelpCollectionHost(getBigDecimal(helpCollectionHost));
            extractImportdetail.setStrippingReceivedFunds(getBigDecimal(strippingReceivedFunds));
            extractImportdetail.setRegularCommission(getBigDecimal(regularCommission));
            extractImportdetail.setTakeOverTheCommission(getBigDecimal(takeOverTheCommission));
            extractImportdetail.setSpecialCommission(getBigDecimal(specialCommission));
            extractImportdetail.setTotalRoyalty(getBigDecimal(totalRoyalty));


            extractImportdetail.setPaidCommission(getBigDecimal(paidCommission));
            extractImportdetail.setReservedCommission(getBigDecimal(reservedCommission));
            extractImportdetail.setShouldSendExtract(getBigDecimal(shouldSendExtract));


            //???????????????
            extractImportdetail.setTax(getBigDecimal(tax));
            extractImportdetail.setTaxReduction(getBigDecimal(taxReduction));
            extractImportdetail.setConsotax(getBigDecimal(consotax));
            extractImportdetail.setInvoiceExcessTax(getBigDecimal(invoiceExcessTax));
            extractImportdetail.setInvoiceExcessTaxReduction(getBigDecimal(invoiceExcessTaxReduction));
            extractImportdetail.setExcessTaxPreviousInvoices(getBigDecimal(excessTaxPreviousInvoices));


            //????????????
            extractImportdetail.setLateFee(getBigDecimal(lateFee));
            extractImportdetail.setDeliveryLogisticsFee(getBigDecimal(deliveryLogisticsFee));
            extractImportdetail.setShippingCost(getBigDecimal(shippingCost));
            extractImportdetail.setSampleIssuingCost(getBigDecimal(sampleIssuingCost));
            extractImportdetail.setReturnLogisticsFee(getBigDecimal(returnLogisticsFee));

            //????????????--??????
            extractImportdetail.setReturnCost(getBigDecimal(returnCost));
            extractImportdetail.setDistributionCost(getBigDecimal(distributionCost));
            extractImportdetail.setShiftPackingFee(getBigDecimal(shiftPackingFee));
            extractImportdetail.setGiftFee(getBigDecimal(giftFee));
            extractImportdetail.setBadDebtAssessment(getBigDecimal(badDebtAssessment));
            extractImportdetail.setNonConformancePenalty(getBigDecimal(nonConformancePenalty));

            //????????????
            extractImportdetail.setPreviousCost(getBigDecimal(previousCost));
            extractImportdetail.setCurrentDeduction(getBigDecimal(currentDeduction));
            extractImportdetail.setDeductionGuarantee(getBigDecimal(deductionGuarantee));
            extractImportdetail.setDeductCreditInformation(getBigDecimal(deductCreditInformation));
            extractImportdetail.setSalesmanAdvance(getBigDecimal(salesmanAdvance));
            extractImportdetail.setOtherTypesDeduction(getBigDecimal(otherTypesDeduction));
            extractImportdetail.setSubtotalDeduction(getBigDecimal(subtotalOfDeduction));
            extractImportdetail.setCopeextract(getBigDecimal(copeextract));


            extractImportdetail.setCreatetime(new Date());
            extractImportdetail.setCreateBy(UserThreadLocal.getEmpNo());
            extractImportdetail.setUpdatetime(extractImportdetail.getCreatetime());
            extractImportdetail.setUpdateBy(UserThreadLocal.getEmpNo());
            extractImportDetailMapper.insert(extractImportdetail);
        }

    }



    private BigDecimal getBigDecimal(String object){
        if (StringUtils.isNotBlank(object)) {
            return new BigDecimal(object);
        }else {
            return BigDecimal.ZERO;
        }
    }
    private void setUserTypeValue(String isCompanyEmp, String empNo, String empName, BudgetExtractImportdetail extractImportdetail) {
//        extractImportdetail.setIscompanyemp("???".equals(isCompanyEmp) ? true : false);
        switch ( ExtractUserTypeEnum.getEnumByValue(isCompanyEmp)) {
            case COMPANY_STAFF:
                WbUser user = getUserByEmpno(empNo);
                extractImportdetail.setEmpid(user.getUserId());
                extractImportdetail.setIdnumber(user.getIdNumber());
                extractImportdetail.setIscompanyemp(true);
                extractImportdetail.setBusinessType(ExtractUserTypeEnum.COMPANY_STAFF.getCode());
                break;
            case EXTERNAL_STAFF:
                BudgetExtractOuterperson outerPerson = getExtractOuterpersonByEmpnoAndEmpname(empNo, empName);
                extractImportdetail.setEmpid(outerPerson.getId().toString());
                extractImportdetail.setIdnumber(outerPerson.getIdnumber());
                extractImportdetail.setIscompanyemp(false);
                extractImportdetail.setBusinessType(ExtractUserTypeEnum.EXTERNAL_STAFF.getCode());
                break;
            case SELF_EMPLOYED_EMPLOYEES:
                //todo ?????????
                WbUser user2 = getUserByEmpno(empNo);
                extractImportdetail.setEmpid(user2.getUserId());
                extractImportdetail.setIdnumber(user2.getIdNumber());
                extractImportdetail.setBusinessType(ExtractUserTypeEnum.SELF_EMPLOYED_EMPLOYEES.getCode());
                extractImportdetail.setIscompanyemp(false);
                break;
            default:
                break;
        }
    }

    private WbUser getUserByEmpno(String empNo) {
        //WbUser user = userMapper.selectOne(new LambdaQueryWrapper<WbUser>().eq(WbUser::getUserName, empNo));
        return UserCache.getUserByEmpNo(empNo);
        //return user;
    }

    private BudgetExtractOuterperson getExtractOuterpersonByEmpnoAndEmpname(String empNo, String empName) {
        return outPersonMapper.selectOne(new LambdaQueryWrapper<BudgetExtractOuterperson>()
                .eq(BudgetExtractOuterperson::getEmpno, empNo).eq(BudgetExtractOuterperson::getName, empName));
    }
    private BudgetYearPeriod getPeriodByName(String name) {
        BudgetYearPeriod yearPeriod = yearMapper.selectOne(new QueryWrapper<BudgetYearPeriod>().eq("period", name));
        return yearPeriod;
    }

    private BudgetExtractOuterperson getOuterPersonByEmpNo(String empNo) {
        return outPersonMapper.selectOne(new LambdaQueryWrapper<BudgetExtractOuterperson>()
                .eq(BudgetExtractOuterperson::getEmpno, empNo).last("limit 1"));
    }
    private String getOuterPayUnit() {
        //EXTRACTPAY,OUTER_PAYUNIT,??????????????????????????????,1,,"69,74,76"
        TabDm tabDm = tabDmMapper.selectOne(new LambdaQueryWrapper<TabDm>()
                .eq(TabDm::getDmType, "EXTRACTPAY")
                .eq(TabDm::getDm, "OUTER_PAYUNIT"));
        String dmValue = tabDm.getDmValue();
        //69,74,76
        String[] split = dmValue.split(",");
        return split[0];
    }
    public boolean isTest(){
        TabDm dm = this.tabDmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", "EXTRACTCAL").eq("dm", "is_test"));
        return dm != null && StringUtils.isNotBlank(dm.getDmValue()) && "1".equals(dm.getDmValue());
    }
    public String getTestNotice(){
        TabDm dm1 = this.tabDmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", "EXTRACTCAL").eq("dm", "test_notice"));
        return dm1.getDmValue();
    }
}




