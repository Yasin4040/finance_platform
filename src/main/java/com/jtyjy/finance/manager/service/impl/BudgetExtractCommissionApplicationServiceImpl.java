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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
* @description 针对表【budget_extract_commission_application(提成支付申请单  主表 )】的数据库操作Service实现
* @createDate 2022-08-26 11:08:05
*/
@Service
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

    private  String tcWorkFlowId = "5263";
    public BudgetExtractCommissionApplicationServiceImpl(BudgetExtractTaxHandleRecordService taxHandleRecordService, BudgetExtractsumMapper extractSumMapper, BudgetExtractOuterpersonMapper outPersonMapper, BudgetExtractImportdetailMapper extractImportDetailMapper, BudgetYearPeriodMapper yearMapper, BudgetExtractCommissionApplicationBudgetDetailsService budgetDetailsService, BudgetExtractCommissionApplicationLogService applicationLogService, BudgetCommonAttachmentService attachmentService, StorageClient storageClient, ReimbursementWorker reimbursementWorker, BudgetReimbursementorderService reimbursementorderService, BudgetExtractFeePayDetailMapper feePayDetailMapper, HrService hrService, OaService oaService, TabDmMapper tabDmMapper, OAMapper oaMapper, BudgetExtractCommissionApplicationLogService logService) {
        this.taxHandleRecordService = taxHandleRecordService;
        this.extractSumMapper = extractSumMapper;
        this.outPersonMapper = outPersonMapper;
        this.extractImportDetailMapper = extractImportDetailMapper;
        this.yearMapper = yearMapper;
        this.budgetDetailsService = budgetDetailsService;
        this.applicationLogService = applicationLogService;
        this.attachmentService = attachmentService;
        this.storageClient = storageClient;
        this.reimbursementWorker = reimbursementWorker;
        this.reimbursementorderService = reimbursementorderService;
        this.feePayDetailMapper = feePayDetailMapper;
        this.hrService = hrService;
        this.oaService = oaService;
        this.tabDmMapper = tabDmMapper;
        this.oaMapper = oaMapper;

        this.logService = logService;
    }

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
            //set提成明细
            List<CommissionDetailsVO> commissionList = new ArrayList<>();
            infoVO.setCommissionList(commissionList);
            //提成明细for
            List<BudgetExtractImportdetail> importDetails = extractImportDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractImportdetail>()
                    .eq(BudgetExtractImportdetail::getExtractsumid, budgetExtractsum.getId()));
            if (CollectionUtils.isNotEmpty(importDetails)) {
                //提成类型+届别
                Map<String, List<BudgetExtractImportdetail>> typeList = importDetails.stream().collect(Collectors.groupingBy(x -> x.getExtractType()+"-"+x.getYearid()));
                for (Map.Entry<String, List<BudgetExtractImportdetail>> entry : typeList.entrySet()) {
                    BudgetExtractImportdetail importDetail = entry.getValue().get(0);
                    CommissionDetailsVO detailsVO = new CommissionDetailsVO();
                    detailsVO.setId(importDetail.getId());
                    detailsVO.setCommissionTypeName(importDetail.getExtractType());
                    //用 cache mapper层缓存
                    String yearName = yearMapper.getNameById(importDetail.getYearid());
                    detailsVO.setYearId(yearName);
                    //                detailsVO.setYearId(importDetail.getYearid().toString()+"届");
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
            //set预算明细
            List<BudgetDetailsVO> budgetList = new ArrayList<>();
            infoVO.setBudgetList(budgetList);
            //提成明细for
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
            //发放明细。。。
            //set预算明细
            List<DistributionDetailsVO> distributionList = new ArrayList<>();
            infoVO.setDistributionList(distributionList);
            //附件明細
            List<BudgetCommonAttachment> attachmentList = attachmentService.lambdaQuery().eq(BudgetCommonAttachment::getContactId, application.getId()).list();
            if (CollectionUtils.isNotEmpty(attachmentList)) {
                 infoVO.setOaPassword(attachmentList.get(0).getOaPassword());
            }
            List<BudgetCommonAttachmentVO> attachmentVOList = CommonAttachmentConverter.INSTANCE.toVOList(attachmentList);
            infoVO.setAttachmentList(attachmentVOList);

            List<BudgetExtractCommissionApplicationLog> applicationLogs = logService.list(new LambdaQueryWrapper<BudgetExtractCommissionApplicationLog>().eq(BudgetExtractCommissionApplicationLog::getApplicationId, application.getId()));
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
        //状态判断。
        BudgetExtractsum extractsum = extractSumMapper.selectById (updateVO.getExtractSumId());
        //判断udpateVO
        this.validateApplicationByUpdateVO(updateVO);
        if (extractsum.getStatus() != ExtractStatusEnum.DRAFT.getType() && extractsum.getStatus() != ExtractStatusEnum.RETURN.getType())
            throw new RuntimeException("操作失败！只能修改退回和草稿状态的提成明细");
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

        //先删除。
        //附件逻辑
        List<BudgetCommonAttachment> attachments = new ArrayList<>();
        List<BudgetCommonAttachment> oldAttachments = attachmentService.lambdaQuery().eq(BudgetCommonAttachment::getContactId, application.getId()).list();
        List<BudgetCommonAttachmentVO> giveAttachmentList = updateVO.getAttachmentList();

        List<BudgetCommonAttachmentVO> addAttachmentList = giveAttachmentList.stream().filter(x -> x.getId() == null).collect(Collectors.toList());
        List<Long> nowIdList = giveAttachmentList.stream()
                .map(BudgetCommonAttachmentVO::getId).filter(Objects::nonNull).collect(Collectors.toList());

        //有id的。就删除。
        List<BudgetCommonAttachment> deleteAttachments = oldAttachments.stream().filter(x -> !nowIdList.contains(x.getId())).collect(Collectors.toList());
        //updateId
        List<BudgetCommonAttachment> updateAttachments = oldAttachments.stream().filter(x -> nowIdList.contains(x.getId())).collect(Collectors.toList());
         List<Long> delIds = deleteAttachments.stream().map(BudgetCommonAttachment::getId).collect(Collectors.toList());
        //原来有id，1、保留下来就保留下来。2、没有保留下来，就删除
        //原来没有id，新增。
         attachmentService.removeByIds(delIds);
         //删除资源
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
        //提成明细for
        List<BudgetExtractImportdetail> importDetails = extractImportDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractImportdetail>()
                .eq(BudgetExtractImportdetail::getExtractsumid, updateVO.getExtractSumId()));
        //是否存在 存在绩效奖和预提绩效奖时
        List<BudgetExtractImportdetail> awardList = importDetails.stream()
                .filter(x -> ExtractTypeEnum.ACCRUED_PERFORMANCE_AWARD.value.equals(x.getExtractType()) || ExtractTypeEnum.PERFORMANCE_AWARD_COMMISSION.value.equals(x.getExtractType())).collect(Collectors.toList());

        if(CollectionUtils.isNotEmpty(awardList)) {
            List<BudgetDetailsVO> budgetList = updateVO.getBudgetList();
            if(CollectionUtils.isEmpty(budgetList)) {
                throw new RuntimeException("未填写预算明细！");
            }
            Optional<BigDecimal> bigDecimal = budgetList.stream().map(x -> x.getBudgetAmount()).reduce(BigDecimal::add);
            Optional<BigDecimal> reduce = awardList.stream().map(x -> x.getShouldSendExtract()).reduce(BigDecimal::add);
            if (bigDecimal.isPresent() && reduce.isPresent()) {
                int i = bigDecimal.get().compareTo(reduce.get());
                if (i != 0) {
                    throw new RuntimeException("申请提成金额应该和预算金额相等！");
                }
            }
        }

//            //budget 不能为空。
//            Optional<BudgetExtractCommissionApplication> applicationBySumId = this.getApplicationBySumId(String.valueOf(extractSum.getId()));
//            if(applicationBySumId.isPresent()){
//                List<BudgetExtractCommissionApplicationBudgetDetails> budgetDetailList
//                        =budgetDetailsService.lambdaQuery().eq(BudgetExtractCommissionApplicationBudgetDetails::getApplicationId, applicationBySumId.get().getId()).list();
//                if(CollectionUtils.isEmpty(budgetDetailList)) {
//                    throw new RuntimeException("未填写预算明细！");
//                }
//                Optional<BigDecimal> bigDecimal = budgetDetailList.stream().map(x -> x.getBudgetAmount()).reduce(BigDecimal::add);
//                Optional<BigDecimal> reduce = awardList.stream().map(x -> x.getShouldSendExtract()).reduce(BigDecimal::add);
//                if(bigDecimal.isPresent()&&reduce.isPresent()){
//                    int i = bigDecimal.get().compareTo(reduce.get());
//                    if (i!=0) {
//                        throw new RuntimeException("提成金额应该和预算金额相等！");
//                    }
//                }
//            }
    }

    @Override
    /**
     * 根据预算单位及月份查询动因
     */
    public PageResult<BudgetSubjectVO> listSubjectMonthAgent(HashMap<String, Object> paramMap, Integer page, Integer rows) {
        Page<BudgetSubjectVO> pageBean = new Page<>(page, rows);
        List<BudgetSubjectVO> resultList = this.baseMapper.listSubjectMonthAgentByMap(pageBean, paramMap);
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    @Override
    public List<IndividualIssueExportDTO> exportIssuedTemplate(String extractMonth) {
        List<IndividualIssueExportDTO> exportDTOList = new ArrayList<>();
        //根据批次，找到所有主表。
        //只返回，工号，姓名，实发金额，发放单位。 累加实发金额。
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
                    //没有就置空
                    dto.setIssuedUnit(billingUnit.getName());
                }
            }else {
                //发放单位
                String unitId = unitByEmpNos.get(String.valueOf(dto.getEmployeeJobNum()));
                if (StringUtils.isNotBlank(unitId)) {
                     billingUnit = UnitCache.getByOutKey(unitId);
                    if (billingUnit != null) {
                        //没有就置空
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
                                //验证
                                validData(dto);
                                //相同的先删除再insert
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
                                //费用金额 实发金额
                                payDetail.setFeePay(dto.getPaymentAmount());
                                payDetail.setCopeextract(dto.getCopeextract());
                                //发放单位
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
                    ConvertUtils.register(new DateLocaleConverter(), Date.class);//BeanUtils.populate对日期类型进行处理，否则无法封装
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
//                        //模糊搜索
//                        .and(StringUtils.isNotBlank(query.getEmployeeName()),i->i.like(BudgetExtractFeePayDetailBeforeCal::getEmpName,query.getEmployeeName())
//                        .or()
//                        .like(BudgetExtractFeePayDetailBeforeCal::getEmpNo,query.getEmployeeName()))
//        );
        return beforeCalPage;
    }

    @Override
    public void validateApplication(BudgetExtractsum extractSum) {
        //提成明细for
        List<BudgetExtractImportdetail> importDetails = extractImportDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractImportdetail>()
                .eq(BudgetExtractImportdetail::getExtractsumid, extractSum.getId()));
        //是否存在 存在绩效奖和预提绩效奖时
        List<BudgetExtractImportdetail> awardList = importDetails.stream()
                .filter(x -> ExtractTypeEnum.ACCRUED_PERFORMANCE_AWARD.value.equals(x.getExtractType()) || ExtractTypeEnum.PERFORMANCE_AWARD_COMMISSION.value.equals(x.getExtractType())).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(awardList)){
            //budget 不能为空。
            Optional<BudgetExtractCommissionApplication> applicationBySumId = this.getApplicationBySumId(String.valueOf(extractSum.getId()));
            if(applicationBySumId.isPresent()){
                List<BudgetExtractCommissionApplicationBudgetDetails> budgetDetailList
                        =budgetDetailsService.lambdaQuery().eq(BudgetExtractCommissionApplicationBudgetDetails::getApplicationId, applicationBySumId.get().getId()).list();
                if(CollectionUtils.isEmpty(budgetDetailList)) {
                    throw new RuntimeException("未填写预算明细！");
                }
                Optional<BigDecimal> bigDecimal = budgetDetailList.stream().map(x -> x.getBudgetAmount()).reduce(BigDecimal::add);
                Optional<BigDecimal> reduce = awardList.stream().map(x -> x.getShouldSendExtract()).reduce(BigDecimal::add);
                if(bigDecimal.isPresent()&&reduce.isPresent()){
                    int i = bigDecimal.get().compareTo(reduce.get());
                    if (i!=0) {
                        throw new RuntimeException("申请提成金额应该和预算金额相等！");
                    }
                }
            }

        }
    }

    @SneakyThrows
    @Override
    public void uploadOA(BudgetExtractCommissionApplication application) {
        Long extractSumId = application.getExtractSumId();
        BudgetExtractsum extractSum = extractSumMapper.selectById(extractSumId);

        WbUser user = UserThreadLocal.get();
        //获取oa 用户
        String userIdDeptId = oaMapper.getOaUserId(user.getUserName());
//        String userIdDeptId = oaService.getOaUserId(user.getUserName(),new ArrayList<>());
        String oaUserId = userIdDeptId.split(",")[0];
        String oaDeptId = userIdDeptId.split(",")[1];
//        oaUserId = "5001";
        if(oaUserId.equals("0")){
            throw new RuntimeException("环境问题找不到oa的userId");
        }
        application.setOaCreatorId(oaUserId);
        //todo 需要更新

        String userName = user.getDisplayName();
        WorkflowInfo wi = new WorkflowInfo();
        wi.setCreatorId(oaUserId);
        wi.setRequestLevel("0");
        wi.setRequestName("提成申请单流程--" + userName);
        OAApplicationDTO oaDTO = new OAApplicationDTO();
        oaDTO.setSqr(user.getUserName());
        oaDTO.setBm(oaDeptId);
        oaDTO.setZbrq(DateUtil.getStrYMDByDate(application.getCreateTime()) );
        oaDTO.setZfsy(application.getPaymentReason());
        oaDTO.setBz(StringUtils.isNotBlank(application.getRemarks())?application.getRemarks():"");
        oaDTO.setBh(extractSum.getCode());
        oaDTO.setWfid(tcWorkFlowId);

        //附件上传
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
                    throw new RuntimeException("系统错误!创建文档失败!");
                }
                //docCode
                String charName = ",";
                String temp = String.valueOf(code);
                //不是最后一个就拼 ","
                if (i != attachments.size() - 1) {
                    temp = temp + charName;
                }
                fj = fj + temp;
            }
        }
        oaDTO.setFj(fj);

        List<BudgetExtractImportdetail> importDetailList = extractImportDetailMapper.selectList
                (new LambdaQueryWrapper<BudgetExtractImportdetail>().eq(BudgetExtractImportdetail::getExtractsumid,extractSumId));
        List<OAApplicationDetailDTO> oaDetailList = new ArrayList<>();
        for (BudgetExtractImportdetail detail : importDetailList) {
            OAApplicationDetailDTO dto = new OAApplicationDetailDTO();
            dto.setTclx(detail.getExtractType());
            //归属届别
            String yearName = yearMapper.getNameById(detail.getYearid());
            dto.setGslx(yearName);

            dto.setSqtc(String.valueOf(detail.getShouldSendExtract()));


            dto.setSfje(String.valueOf(detail.getCopeextract()));
            dto.setKkje(String.valueOf(detail.getCopeextract().subtract(detail.getShouldSendExtract())));
            dto.setWfid(tcWorkFlowId);
            oaDetailList.add(dto);
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
            throw new RuntimeException("提交失败，oa系统未找到你的上级人员，请联系oa管理员。");
        }
        application.setRequestId(requestId);
        application.setOaCreatorId(oaUserId);
    }

    @Override
    public void validateExtractMonth(String extractMonth) {
        //全部都得是已经审核。
        List<BudgetExtractsum> nowSums = extractSumMapper.selectList(new LambdaQueryWrapper<BudgetExtractsum>().eq(BudgetExtractsum::getExtractmonth, extractMonth));

        long count = nowSums.stream().filter(x -> !x.getStatus().equals(ExtractStatusEnum.APPROVED.getType())).count();
        if(count!=0){
            throw new BusinessException("批次需要全部审核通过，才能进行导入费用");
        }
        //是否计算。
        BudgetExtractTaxHandleRecord recordServiceOne =
                taxHandleRecordService.getOne(new LambdaQueryWrapper<BudgetExtractTaxHandleRecord>().eq(BudgetExtractTaxHandleRecord::getExtractMonth, extractMonth));
        if (recordServiceOne != null) {
            List<BudgetExtractImportdetail> importDetailList = extractImportDetailMapper.getAllByExtractMonth(extractMonth);
            long selfCount = importDetailList.stream().filter(x -> x.getBusinessType().equals(ExtractUserTypeEnum.SELF_EMPLOYED_EMPLOYEES)).count();
            if (selfCount == 0) {
                //个体户为0
                if (recordServiceOne.getIsCalComplete() || recordServiceOne.getIsSetExcessComplete()) {
                    throw new BusinessException("导入失败！批次已经进行过计算！");
                }
            } else {
                if (recordServiceOne.getIsCalComplete() || recordServiceOne.getIsSetExcessComplete() || recordServiceOne.getIsPersonalityComplete()) {
                    throw new BusinessException("导入失败！批次已经进行过计算！");
                }
            }
        }
    }

    private void validData(IndividualIssueExportDTO dto) {
        //发放单位验证
        String issuedUnit = dto.getIssuedUnit();
        BudgetBillingUnit billingUnit = UnitCache.getByName(issuedUnit);
        if(billingUnit==null){
            throw new RuntimeException("请填写正确的发放单位");
        }
        //工号姓名验证
        WbUser user = getUserByEmpno(String.valueOf(dto.getEmployeeJobNum()));
        if (user == null) {
            throw new RuntimeException("工号【" + dto.getEmployeeJobNum() + "】不存在!");
        } else {
            if (!dto.getEmployeeName().equals(user.getDisplayName())) {
                throw new RuntimeException("工号与姓名不匹配!正确姓名为【" + user.getDisplayName() + "】");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatusBySumId(String sumId, Integer status) {
        Optional<BudgetExtractCommissionApplication> applicationOptional = getApplicationBySumId(sumId);
        BudgetExtractsum budgetExtractsum = extractSumMapper.selectById(sumId);
        if (budgetExtractsum == null) {
            throw new BusinessException("该申请单不存在！");
        }
        if (applicationOptional.isPresent()) {
            BudgetExtractCommissionApplication application = applicationOptional.get();
            ExtractStatusEnum willEnum = ExtractStatusEnum.getTypeEnume(status);
            switch (willEnum) {
                case REJECT:
                    //作废操作  申请单状态必须是-1
                    if (!budgetExtractsum.getStatus().equals(-1)) {
                        throw new BusinessException("作废失败,申请单必须是退回状态！");
                    }
                    break;
                case RETURN:
                    //税务退回
                    if (!budgetExtractsum.getStatus().equals(2)) {
                        throw new BusinessException("税务退回失败,申请单必须是审核通过状态！");
//                        退回失败！任务已计算！
                    }
                    //有费用导入。就不能税务退回
                    //批次号
                    String extractMonth = budgetExtractsum.getExtractmonth();
                    Integer returnCount =  feePayDetailMapper.selectCount(new LambdaQueryWrapper<BudgetExtractFeePayDetailBeforeCal>()
                            .eq(BudgetExtractFeePayDetailBeforeCal::getExtractMonth,extractMonth));
                    if (returnCount>0) {
                        throw new BusinessException("退回失败！该批次任务已计算！");
                    }
                    //计算任务
                    BudgetExtractTaxHandleRecord recordServiceOne = taxHandleRecordService.getOne(new LambdaQueryWrapper<BudgetExtractTaxHandleRecord>().eq(BudgetExtractTaxHandleRecord::getExtractMonth, extractMonth));
                    //如果个体户结果
//                    List<BudgetExtractImportdetail> importDetailList = extractImportDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractImportdetail>().eq(BudgetExtractImportdetail::getExtractsumid, sumId));
//                    long selfCount = importDetailList.stream().filter(x -> x.getBusinessType().equals(ExtractUserTypeEnum.SELF_EMPLOYED_EMPLOYEES)).count();
                    if(recordServiceOne.getIsCalComplete()||recordServiceOne.getIsSetExcessComplete()||recordServiceOne.getIsPersonalityComplete()){
                        throw new BusinessException("退回失败！任务已计算！");
                    }
                    applicationLogService.saveLog(application.getId(),OperationNodeEnum.TAX_RETURN, LogStatusEnum.REJECT);
                    break;
                case DRAFT:
                    //撤回  申请单必须没有人审批过
                    if (budgetExtractsum.getStatus().equals(1)) {
                        //1 已提交
                        Integer draftCount = applicationLogService.lambdaQuery()
                                .eq(BudgetExtractCommissionApplicationLog::getApplicationId, application.getId())
                                .eq(BudgetExtractCommissionApplicationLog::getStatus, 1).count();
                        if (draftCount>0) {
                            throw new BusinessException("撤回失败,申请单已审批！");
                        }
                    }else{
                        throw new BusinessException("撤回失败,申请单不是提交状态！");
                    }
                    break;
                default:
                    break;
            }
            //删除报销表
            if (application.getReimbursementId()!=null) {
                BudgetReimbursementorder reimbursementorder = reimbursementorderService.getById(application.getReimbursementId());
                reimbursementorderService.removeById(reimbursementorder);
            }
            this.lambdaUpdate().eq(BudgetExtractCommissionApplication::getExtractSumId,sumId).set(BudgetExtractCommissionApplication::getStatus,status);
            budgetExtractsum.setStatus(status);
            extractSumMapper.updateById(budgetExtractsum);
        }else {
            throw new BusinessException("申请单不存在");
        }
    }


    @SneakyThrows
    @Override
    public void generateReimbursement(Long sumId,BudgetExtractsum extractsum) {
        Optional<BudgetExtractCommissionApplication> applicationBySumId = this.getApplicationBySumId(String.valueOf(sumId));
        if (applicationBySumId.isPresent()) {
            BudgetExtractCommissionApplication application = applicationBySumId.get();
            Long applicationId = application.getId();
            //todo 清空报销单
//            BudgetExtractImportdetail


            List<BudgetExtractImportdetail> importDetails = extractImportDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractImportdetail>()
                    .eq(BudgetExtractImportdetail::getExtractsumid, sumId));

            //存在 提成。
            Boolean ifExistsCommission = importDetails.stream().anyMatch(x-> {
                return ExtractTypeEnum.PERFORMANCE_AWARD_COMMISSION.value.equals(x.getExtractType()) || ExtractTypeEnum.ACCRUED_PERFORMANCE_AWARD.value.equals(x.getExtractType());
            });
            if(ifExistsCommission){
                List<BudgetExtractCommissionApplicationBudgetDetails> budgetDetailsList =
                        budgetDetailsService.lambdaQuery().eq(BudgetExtractCommissionApplicationBudgetDetails::getApplicationId, applicationId).list();
                if (CollectionUtils.isEmpty(budgetDetailsList)) {
                    return;
                }
                //报销单 生成
                ReimbursementRequest reimbursementRequest = new ReimbursementRequest();
                List<BudgetReimbursementorderDetail> orderDetailList = new ArrayList<>();

                BigDecimal otherTotalMoney = BigDecimal.ZERO;
                for (BudgetExtractCommissionApplicationBudgetDetails budgetDetails : budgetDetailsList) {
                    BudgetReimbursementorderDetail reimbursement = new BudgetReimbursementorderDetail();
                    reimbursement.setSubjectid(budgetDetails.getSubjectId());
                    reimbursement.setSubjectCode(budgetDetails.getSubjectCode());
                    reimbursement.setSubjectname(budgetDetails.getSubjectName());
                    //动因
                    reimbursement.setMonthagentname(budgetDetails.getMotivationName());
                    reimbursement.setMonthagentid(budgetDetails.getMotivationId());
                    //U0068,陈彩莲(无票) 默认
                    reimbursement.setBunitid(68l);
                    reimbursement.setBunitname("陈彩莲(无票)");
                    //默认执行
                    reimbursement.setReimflag(true);

                    reimbursement.setReimmoney(budgetDetails.getBudgetAmount());
                    otherTotalMoney =  otherTotalMoney.add(budgetDetails.getBudgetAmount());
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
                    //报销id
                    String returnId = reimbursementWorker.saveReturnId(reimbursementRequest, true);
                    if (StringUtils.isNotBlank(returnId)) {
                        application.setReimbursementId(Long.valueOf(returnId));
                        this.saveOrUpdate(application);
                    }
//                    reimbursementController.opt(reimbursementRequest);
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage()==null?e.toString():e.getMessage());
                }
            }

        }

    }

    @Override
    public void importIndividual(MultipartFile multipartFile) {
        //多个。
//        multipartFile.
//        multipartFile.getInputStream();
    }
    private  BudgetReimbursementorder getTestBean(BudgetExtractsum extractsum) {
        //设置报销单信息
        BudgetReimbursementorder order = new BudgetReimbursementorder();
        //预算单位
//        commonData.getBxUnit().getId()
        order.setUnitid(Long.valueOf(extractsum.getDeptid()));
        order.setYearid(extractsum.getYearid());
        order.setReimperonsid(UserThreadLocal.get().getUserId());
        order.setPaymentmoney(BigDecimal.ZERO);
        order.setReimperonsname(UserThreadLocal.get().getDisplayName());
        String monthId = extractsum.getExtractmonth().substring(4, 6);
        order.setMonthid(Long.valueOf(monthId));
        order.setBxtype(ReimbursementTypeEnmu.COMMON.getCode());
        //报销单来源 0：普通报销单（预算员手动填写的）1：稿费 2：提成 3：工资 4:项目预领
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
        order.setRemark("申请单报销用例");
        return order;
    }
    @Override
    public void saveEntity(BudgetExtractsum extract,String badDebt,Object... params) {
        //生成提成明细申请单
        //支付+“届别”+“月份”+“批次”+“提成/坏账”
        //届别取“届别”字段；月份取“提成期间”中的月份；“提成/坏账”根据“坏账（是/否）”判断，若是则显示“坏账”；否则显示“提成”。
        BudgetExtractCommissionApplication application = new BudgetExtractCommissionApplication();
        //合并逻辑问题
//        extract.getStatus()
//              2020 10 06

        String yearName = yearMapper.getNameById(extract.getYearid());
        Integer month = Integer.valueOf(extract.getExtractmonth().substring(4, 6));
        Integer batchNo = Integer.valueOf(extract.getExtractmonth().substring(6));
        String paymentReason = MessageFormat.format("支付{0}{1}月第{2}批{3}",yearName,month,batchNo,badDebt);
        //0 草稿。

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
        String businessType = data.get(0); //业务类型  是否是公司员工？？
        //新增员工个体户。

        String empNo = data.get(1); //工号
        String empName = data.get(2); //姓名
        String isDebt = data.get(3); //是否坏账   index = 3,实际是第4列

        String extractType = data.get(4);//提成类型
        String tcPeriod = data.get(5); //提成届别 第6列

        //应发提成计算
        String totalPrice = data.get(6);//码洋  实际是第7列,index= 6
        String actualPrice = data.get(7);//实洋
        String collection = data.get(8);//回款
        String income = data.get(9);  //收入  第10列

        String helpCollectionHost = data.get(10);//   在职帮离职回款成本
        String strippingReceivedFunds = data.get(11);//到款剥离
        String regularCommission = data.get(12);//常规提成
        String takeOverTheCommission = data.get(13);//接手提成
        String specialCommission = data.get(14);//特价提成

        String totalRoyalty = data.get(15);//总提成  16列

        String paidCommission = data.get(16);//已发提成
        String reservedCommission = data.get(17);//预留提成
        String shouldSendExtract = data.get(18);//应发提成


        //代收代缴款
        String tax = data.get(19);//提成个税
        String taxReduction = data.get(20);//返提成个税
        String consotax = data.get(21);//综合税
        String invoiceExcessTax = data.get(22);//发票超额税金
        String invoiceExcessTaxReduction = data.get(23);//返发票超额税金
        String excessTaxPreviousInvoices = data.get(24);//往届发票超额税金  第25列

        //业务扣款
        String lateFee = data.get(25);//滞纳金
        String deliveryLogisticsFee = data.get(26);//发货物流费
        String shippingCost = data.get(27);//发件费用
        String sampleIssuingCost = data.get(28);//发样成本
        String returnLogisticsFee = data.get(29);//退货物流费   30 列

        //业务扣款--费用
        String returnCost = data.get(30);//退货成本
        String distributionCost = data.get(31);//铺货成本
        String shiftPackingFee = data.get(32);//分班打包费
        String giftFee = data.get(33);//礼品费
        String badDebtAssessment = data.get(34);//坏账考核   30 列
        String nonConformancePenalty = data.get(35);//未达标罚款   30 列

        //其他罚款
        String currentDeduction = data.get(36);//往来扣款
        String deductionGuarantee = data.get(37);//扣担保
        String deductCreditInformation = data.get(38);//扣征信

        String salesmanAdvance = data.get(39);//业务员垫支
        String otherTypesDeduction = data.get(40);//其他类型 扣款
        String subtotalOfDeduction = data.get(41);//扣款小计
        String copeextract = data.get(42);//实发金额


        BudgetYearPeriod yearPeriod = getPeriodByName(tcPeriod);
        //判断是否存在
        BudgetExtractImportdetail extractImportdetail = null;
        if (Objects.nonNull(extractImportdetail)) {
            extractImportdetail.setTax(new BigDecimal(tax)); // 个税
            extractImportdetail.setConsotax(new BigDecimal(consotax)); // 综合税
            extractImportdetail.setCopeextract(new BigDecimal(copeextract));// 实发金额
            extractImportdetail.setUpdatetime(new Date());
            extractImportdetail.setUpdateBy(UserThreadLocal.getEmpNo());
            extractImportDetailMapper.updateById(extractImportdetail);
        } else {
            extractImportdetail = new BudgetExtractImportdetail();
            extractImportdetail.setId(null);
            extractImportdetail.setExtractsumid(extractSum.getId());
            //赋值 员工类型
            setUserTypeValue(businessType, empNo, empName, extractImportdetail);

            extractImportdetail.setEmpno(empNo);
            extractImportdetail.setEmpname(empName);
            extractImportdetail.setIsbaddebt("是".equals(isDebt) ? true : false);
            extractImportdetail.setYearid(yearPeriod.getId());
            extractImportdetail.setExtractType(extractType);



            //应发提成计算
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


            //代收代缴款
            extractImportdetail.setTax(getBigDecimal(tax));
            extractImportdetail.setTaxReduction(getBigDecimal(taxReduction));
            extractImportdetail.setConsotax(getBigDecimal(consotax));
            extractImportdetail.setInvoiceExcessTax(getBigDecimal(invoiceExcessTax));
            extractImportdetail.setInvoiceExcessTaxReduction(getBigDecimal(invoiceExcessTaxReduction));
            extractImportdetail.setExcessTaxPreviousInvoices(getBigDecimal(excessTaxPreviousInvoices));


            //业务扣款
            extractImportdetail.setLateFee(getBigDecimal(lateFee));
            extractImportdetail.setDeliveryLogisticsFee(getBigDecimal(deliveryLogisticsFee));
            extractImportdetail.setShippingCost(getBigDecimal(shippingCost));
            extractImportdetail.setSampleIssuingCost(getBigDecimal(sampleIssuingCost));
            extractImportdetail.setReturnLogisticsFee(getBigDecimal(returnLogisticsFee));

            //业务扣款--费用
            extractImportdetail.setReturnCost(getBigDecimal(returnCost));
            extractImportdetail.setDistributionCost(getBigDecimal(distributionCost));
            extractImportdetail.setShiftPackingFee(getBigDecimal(shiftPackingFee));
            extractImportdetail.setGiftFee(getBigDecimal(giftFee));
            extractImportdetail.setBadDebtAssessment(getBigDecimal(badDebtAssessment));
            extractImportdetail.setNonConformancePenalty(getBigDecimal(nonConformancePenalty));

            //其他罚款
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
//        extractImportdetail.setIscompanyemp("是".equals(isCompanyEmp) ? true : false);
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
                //todo 个体户
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
    private String getOuterPayUnit() {
        //EXTRACTPAY,OUTER_PAYUNIT,提成外部人员发放单位,1,,"69,74,76"
        TabDm tabDm = tabDmMapper.selectOne(new LambdaQueryWrapper<TabDm>()
                .eq(TabDm::getDmType, "EXTRACTPAY")
                .eq(TabDm::getDm, "OUTER_PAYUNIT"));
        String dmValue = tabDm.getDmValue();
        //69,74,76
        String[] split = dmValue.split(",");
        return split[0];
    }
}




