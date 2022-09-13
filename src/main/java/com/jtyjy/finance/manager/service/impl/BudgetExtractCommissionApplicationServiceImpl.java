package com.jtyjy.finance.manager.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iamxiongx.util.message.exception.BusinessException;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.cache.UnitCache;
import com.jtyjy.finance.manager.cache.UserCache;
import com.jtyjy.finance.manager.controller.reimbursement.ReimbursementWorker;
import com.jtyjy.finance.manager.converter.CommonAttachmentConverter;
import com.jtyjy.finance.manager.dto.ReimbursementRequest;
import com.jtyjy.finance.manager.dto.commission.FeeImportErrorDTO;
import com.jtyjy.finance.manager.dto.commission.IndividualIssueExportDTO;
import com.jtyjy.finance.manager.enmus.ExtractStatusEnum;
import com.jtyjy.finance.manager.enmus.ExtractTypeEnum;
import com.jtyjy.finance.manager.enmus.ExtractUserTypeEnum;
import com.jtyjy.finance.manager.enmus.ReimbursementFromEnmu;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.listener.easyexcel.PageReadListener;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.query.commission.FeeQuery;
import com.jtyjy.finance.manager.service.*;
import com.jtyjy.finance.manager.utils.FileUtils;
import com.jtyjy.finance.manager.vo.application.*;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.locale.converters.DateLocaleConverter;
import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.StorageClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
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
    private final BudgetExtractdetailService  extractDetailService;
    private final BudgetExtractOuterpersonMapper outPersonMapper;
    private final IndividualEmployeeFilesService individualService;
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
    public BudgetExtractCommissionApplicationServiceImpl(BudgetExtractTaxHandleRecordService taxHandleRecordService, BudgetExtractsumMapper extractSumMapper, BudgetExtractdetailService extractDetailService, BudgetExtractOuterpersonMapper outPersonMapper, IndividualEmployeeFilesService individualService, BudgetExtractImportdetailMapper extractImportDetailMapper, BudgetYearPeriodMapper yearMapper, BudgetExtractCommissionApplicationBudgetDetailsService budgetDetailsService, BudgetExtractCommissionApplicationLogService applicationLogService, BudgetCommonAttachmentService attachmentService, StorageClient storageClient, ReimbursementWorker reimbursementWorker, BudgetReimbursementorderService reimbursementorderService, BudgetExtractFeePayDetailMapper feePayDetailMapper, HrService hrService) {
        this.taxHandleRecordService = taxHandleRecordService;
        this.extractSumMapper = extractSumMapper;
        this.extractDetailService = extractDetailService;
        this.outPersonMapper = outPersonMapper;
        this.individualService = individualService;
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
            for (BudgetExtractImportdetail importDetail : importDetails) {
                CommissionDetailsVO detailsVO = new CommissionDetailsVO();
                detailsVO.setId(importDetail.getId());
                detailsVO.setCommissionTypeName(importDetail.getExtractType());

                //用 cache mapper层缓存
                String yearName = yearMapper.getNameById(importDetail.getYearid());
                detailsVO.setYearId(yearName);
//                detailsVO.setYearId(importDetail.getYearid().toString()+"届");
                detailsVO.setApplyAmount(importDetail.getShouldSendExtract());
                detailsVO.setActualAmount(importDetail.getCopeextract());
                detailsVO.setDeductionAmount(importDetail.getCopeextract().subtract(importDetail.getShouldSendExtract()));
                commissionList.add(detailsVO);
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

            List<BudgetCommonAttachmentVO> attachmentVOList = CommonAttachmentConverter.INSTANCE.toVOList(attachmentList);
            infoVO.setAttachmentList(attachmentVOList);

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
        if (extractsum.getStatus() != ExtractStatusEnum.DRAFT.getType()||extractsum.getStatus() != ExtractStatusEnum.RETURN.getType())
            throw new RuntimeException("操作失败！只能修改退回和草稿状态的提成明细");
        BudgetExtractCommissionApplication application = this.getById(updateVO.getApplicationId());
        application.setRemarks(updateVO.getRemarks());
        application.setPaymentReason(updateVO.getPaymentReason());
        List<BudgetDetailsVO> budgetList = updateVO.getBudgetList();
        for (BudgetDetailsVO budgetDetailsVO : budgetList) {
            Long budgetId = budgetDetailsVO.getId();
            BudgetExtractCommissionApplicationBudgetDetails budgetDetail = new BudgetExtractCommissionApplicationBudgetDetails();
            if(budgetId != null){
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

        List<BudgetCommonAttachment> deleteAttachments = oldAttachments.stream().filter(x -> !nowIdList.contains(x.getId())).collect(Collectors.toList());
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
            attachment.setFileUrl(attachmentVO.getFileUrl());
            attachment.setFileExtName(FileUtils.getFileType(attachmentVO.getFileName()));
            attachment.setCreator(UserThreadLocal.getEmpNo());
            attachment.setCreateTime(new Date());
            attachments.add(attachment);
        }
        attachmentService.saveOrUpdateBatch(attachments);
        extractsum.setStatus(ExtractStatusEnum.DRAFT.getType());
        extractSumMapper.updateById(extractsum);

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



        List<String> empNos = issueExportDTOList.stream().map(x->String.valueOf(x.getEmployeeJobNum())).collect(Collectors.toList());
        List<Map<String, String>> unitByEmpNoList = hrService.getSalaryUnitByEmpNos(empNos);
        Map<String, String> unitByEmpNos = unitByEmpNoList.stream().collect(Collectors.toMap(x -> x.get("empNo"), x -> String.valueOf( x.get("companyId")), (k1, k2) -> k1));

        for (IndividualIssueExportDTO dto : issueExportDTOList) {
            //发放单位
            String unitId = unitByEmpNos.get(String.valueOf(dto.getEmployeeJobNum()));
            if(StringUtils.isNotBlank(unitId)){
                BudgetBillingUnit billingUnit = UnitCache.getByOutKey(unitId);
                if(billingUnit != null){
                    //没有就置空
                    dto.setIssuedUnit(billingUnit.getName());
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
                                errorDTO.setInsertDatabaseError(e.getCause().getMessage());
                                errList.add(errorDTO);
                            }
                        }
                        System.out.println(dataList);
                    }, errorMap)).sheet().doRead();

            for (Map map : errorMap) {
                FeeImportErrorDTO errorDTO = new FeeImportErrorDTO();
                try {
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
        Page<BudgetExtractFeePayDetailBeforeCal> beforeCalPage =  feePayDetailMapper.selectFeePage(new Page<>(query.getPageNum(), query.getPageSize()),query);

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

    private void validData(IndividualIssueExportDTO dto) {
        //发放单位验证
//        String issuedUnit = dto.getIssuedUnit();
//        BudgetBillingUnit billingUnit = UnitCache.getByName(issuedUnit);
//        if(billingUnit==null){
//            throw new RuntimeException("请填写正确的发放单位");
//        }
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
                        throw new BusinessException("退回失败！任务已计算！");
                    }
                    //个体户导入
                    BudgetExtractTaxHandleRecord recordServiceOne = taxHandleRecordService.getOne(new LambdaQueryWrapper<BudgetExtractTaxHandleRecord>().eq(BudgetExtractTaxHandleRecord::getExtractMonth, extractMonth));

                    if (recordServiceOne != null) {
                        if(recordServiceOne.getIsCalComplete()||recordServiceOne.getIsSetExcessComplete()||recordServiceOne.getIsPersonalityComplete()){
                            throw new BusinessException("退回失败！任务已计算！");
                        }
                    }
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
    public void generateReimbursement(Long sumId) {
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
                BudgetReimbursementorder order = new BudgetReimbursementorder();
                //报销单来源 0：普通报销单（预算员手动填写的）1：稿费 2：提成 3：工资 4:项目预领
                order.setOrderscrtype(ReimbursementFromEnmu.COMMISSION.getCode());
                order.setOthermoney(otherTotalMoney);
                reimbursementRequest.setSubmit("1");
                reimbursementRequest.setOrder(order);
                reimbursementRequest.setOrderDetail(orderDetailList);

                try {
                    //报销id
                    String returnId = reimbursementWorker.saveReturnId(reimbursementRequest, true);
                    if (StringUtils.isNotBlank(returnId)) {
                        application.setReimbursementId(Long.valueOf(returnId));
                        this.save(application);
                    }
//                    reimbursementController.opt(reimbursementRequest);
                } catch (Exception e) {
                    throw e;
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
        String isCompanyEmp = data.get(0); //业务类型  是否是公司员工？？
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
            setUserTypeValue(isCompanyEmp, empNo, empName, extractImportdetail);

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
                break;
            case EXTERNAL_STAFF:
                BudgetExtractOuterperson outerPerson = getExtractOuterpersonByEmpnoAndEmpname(empNo, empName);
                extractImportdetail.setEmpid(outerPerson.getId().toString());
                extractImportdetail.setIdnumber(outerPerson.getIdnumber());
                extractImportdetail.setIscompanyemp(false);
                break;
            case SELF_EMPLOYED_EMPLOYEES:
                //todo 个体户
                IndividualEmployeeFiles employeeFiles = individualService.lambdaQuery().eq(IndividualEmployeeFiles::getEmployeeJobNum, empNo).eq(IndividualEmployeeFiles::getAccountName, empName).last("limit 1").one();
                WbUser user2 = getUserByEmpno(empNo);
                extractImportdetail.setEmpid(user2.getUserId());
                extractImportdetail.setIdnumber(user2.getIdNumber());
                extractImportdetail.setIndividualEmployeeId(employeeFiles.getId());
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

}




