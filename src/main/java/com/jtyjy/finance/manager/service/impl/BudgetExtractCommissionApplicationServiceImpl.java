package com.jtyjy.finance.manager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iamxiongx.util.message.exception.BusinessException;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.cache.UserCache;
import com.jtyjy.finance.manager.controller.reimbursement.ReimbursementController;
import com.jtyjy.finance.manager.dto.ReimbursementRequest;
import com.jtyjy.finance.manager.enmus.ExtractStatusEnum;
import com.jtyjy.finance.manager.enmus.ExtractTypeEnum;
import com.jtyjy.finance.manager.enmus.ExtractUserTypeEnum;
import com.jtyjy.finance.manager.enmus.ReimbursementFromEnmu;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.service.BudgetExtractCommissionApplicationBudgetDetailsService;
import com.jtyjy.finance.manager.service.BudgetExtractCommissionApplicationLogService;
import com.jtyjy.finance.manager.service.BudgetExtractCommissionApplicationService;
import com.jtyjy.finance.manager.service.IndividualEmployeeFilesService;
import com.jtyjy.finance.manager.vo.application.BudgetDetailsVO;
import com.jtyjy.finance.manager.vo.application.CommissionApplicationInfoVO;
import com.jtyjy.finance.manager.vo.application.CommissionDetailsVO;
import com.jtyjy.finance.manager.vo.application.DistributionDetailsVO;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;

/**
* @author User
* @description 针对表【budget_extract_commission_application(提成支付申请单  主表 )】的数据库操作Service实现
* @createDate 2022-08-26 11:08:05
*/
@Service
public class BudgetExtractCommissionApplicationServiceImpl extends ServiceImpl<BudgetExtractCommissionApplicationMapper, BudgetExtractCommissionApplication>
    implements BudgetExtractCommissionApplicationService{

    private final BudgetExtractsumMapper extractSumMapper;
    private final BudgetExtractOuterpersonMapper outPersonMapper;
    private final IndividualEmployeeFilesService individualService;
    private final BudgetExtractImportdetailMapper extractImportDetailMapper;
    private final BudgetYearPeriodMapper yearMapper;
    private final BudgetExtractCommissionApplicationBudgetDetailsService budgetDetailsService;
    private final BudgetExtractCommissionApplicationLogService applicationLogService;
    private final ReimbursementController reimbursementController;
    public BudgetExtractCommissionApplicationServiceImpl(BudgetExtractsumMapper extractSumMapper, BudgetExtractOuterpersonMapper outPersonMapper, IndividualEmployeeFilesService individualService, BudgetExtractImportdetailMapper extractImportDetailMapper, BudgetYearPeriodMapper yearMapper, BudgetExtractCommissionApplicationBudgetDetailsService budgetDetailsService, BudgetExtractCommissionApplicationLogService applicationLogService, ReimbursementController reimbursementController) {
        this.extractSumMapper = extractSumMapper;
        this.outPersonMapper = outPersonMapper;
        this.individualService = individualService;
        this.extractImportDetailMapper = extractImportDetailMapper;
        this.yearMapper = yearMapper;
        this.budgetDetailsService = budgetDetailsService;
        this.applicationLogService = applicationLogService;
        this.reimbursementController = reimbursementController;
    }

    @Override
    public CommissionApplicationInfoVO getApplicationInfo(String sumId) {
        BudgetExtractsum budgetExtractsum = extractSumMapper.selectById(sumId);
        CommissionApplicationInfoVO infoVO = new CommissionApplicationInfoVO();
        Optional<BudgetExtractCommissionApplication> applicationOptional = getApplicationBySumId(sumId);
        if (applicationOptional.isPresent()) {
            BudgetExtractCommissionApplication application = applicationOptional.get();

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
                detailsVO.setYearId(importDetail.getYearid().toString()+"届");
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
        }

        return infoVO;
    }
    @Override
    public Optional<BudgetExtractCommissionApplication> getApplicationBySumId(String sumId) {
        return this.lambdaQuery().eq(BudgetExtractCommissionApplication::getExtractSumId, sumId).last("limit 1").oneOpt();
    }

    @Override
    public void updateStatusBySumId(String sumId, Integer status) {
        Optional<BudgetExtractCommissionApplication> applicationOptional = getApplicationBySumId(sumId);
        if (applicationOptional.isPresent()) {
            BudgetExtractCommissionApplication application = applicationOptional.get();
            switch (status) {
                case -2:
                    //作废操作  申请单状态必须是-1
                    if (!application.getStatus().equals(-1)) {
                        throw new BusinessException("作废失败,申请单必须是退回状态！");
                    }
                    break;
                case 0:
                    //撤回  申请单必须没有人审批过
                    if (application.getStatus().equals(1)) {
                        //1 已提交
                        Integer count = applicationLogService.lambdaQuery()
                                .eq(BudgetExtractCommissionApplicationLog::getApplicationId, application.getId())
                                .eq(BudgetExtractCommissionApplicationLog::getStatus, 1).count();
                        if (count>0) {
                            throw new BusinessException("撤回失败,申请单已审批！");
                        }
                    }else{
                        throw new BusinessException("撤回失败,申请单不是提交状态！");
                    }
                    break;
                default:
                    break;
            }
            this.lambdaUpdate().eq(BudgetExtractCommissionApplication::getExtractSumId,sumId).set(BudgetExtractCommissionApplication::getStatus,status);
            BudgetExtractsum budgetExtractsum = extractSumMapper.selectById(sumId);
            budgetExtractsum.setStatus(ExtractStatusEnum.DRAFT.getType());
            extractSumMapper.updateById(budgetExtractsum);
        }
    }


    @SneakyThrows
    @Override
    public void generateReimbursement(Long sumId) {
        Optional<BudgetExtractCommissionApplication> applicationBySumId = this.getApplicationBySumId(String.valueOf(sumId));
        if (applicationBySumId.isPresent()) {
            BudgetExtractCommissionApplication application = applicationBySumId.get();
            Long applicationId = application.getId();
            //
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
                    otherTotalMoney.add(budgetDetails.getBudgetAmount());
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
                    reimbursementController.opt(reimbursementRequest);
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
//
        String paymentReason = "支付"+extract.getYearid()+extract.getExtractmonth()+ Arrays.stream(params).findFirst().orElse("") +badDebt;
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
        String isCompanyEmp = data.get(0); //业务类型
        //新增员工个体户。

        String empNo = data.get(1); //工号
        String empName = data.get(2); //姓名
        String isDebt = data.get(3); //是否坏账   index = 3,实际是第4列
        String tcPeriod = data.get(4); //提成届别

        String extractType = data.get(5);//提成类型
        String totalPrice = data.get(6);//码洋  实际是第7列,index= 6

        String currentCollection = data.get(7);//本期回款  实际是第8列,index= 7
        String floorPrice = data.get(8);//   底价9 index= 8
        String settlementCommission = data.get(9);//结算提成
        String reservedCommission = data.get(10);//预留提成

        String shouldSendExtract = data.get(11);//申请提成
        String tax = data.get(12);//提成个税

        //综合税

        String returnCommissionIncomeTax = data.get(13);//返提成个税
        String deductTheCostPreviousAccounts = data.get(14);//扣往届扎帐成本
        String deductExcessTaxOnInvoice = data.get(15);//扣发票超额税金
        String refundExcessTaxInvoice = data.get(16);//返发票超额税金

        String dutyWithholdingReturningGoods = data.get(17);//扣退货品承担
        String currentDeduction = data.get(18);//往来扣款
        String deductionGuarantee = data.get(19);//扣担保
        String deductCreditInformation = data.get(20);//扣征信

        String subtotalOfDeduction = data.get(21);//扣款小计
        String copeextract = data.get(22);//实发金额


        BudgetYearPeriod yearPeriod = getPeriodByName(tcPeriod);
        //判断是否存在
        BudgetExtractImportdetail extractImportdetail = null;
        if (Objects.nonNull(extractImportdetail)) {
            extractImportdetail.setConsotax(new BigDecimal(tax)); // 综合税
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



            extractImportdetail.setTotalPrice(getBigDecimal(totalPrice));
            extractImportdetail.setCurrentCollection(getBigDecimal(currentCollection));
            extractImportdetail.setFloorPrice(getBigDecimal(floorPrice));
            extractImportdetail.setSettlementCommission(getBigDecimal(settlementCommission));
            extractImportdetail.setReservedCommission(getBigDecimal(reservedCommission));
            extractImportdetail.setShouldSendExtract(getBigDecimal(shouldSendExtract));

            extractImportdetail.setTax(getBigDecimal(tax));


            extractImportdetail.setReturnCommissionIncomeTax(getBigDecimal(returnCommissionIncomeTax));
            //字段有差别
            extractImportdetail.setDeductCostPreviousAccounts(getBigDecimal(deductTheCostPreviousAccounts));
            extractImportdetail.setDeductExcessTaxInvoice(getBigDecimal(deductExcessTaxOnInvoice));
            extractImportdetail.setRefundExcessTaxInvoice(getBigDecimal(refundExcessTaxInvoice));

            extractImportdetail.setDutyholdingreturninggoods(getBigDecimal(dutyWithholdingReturningGoods));
            extractImportdetail.setCurrentDeduction(getBigDecimal(currentDeduction));
            extractImportdetail.setDeductionGuarantee(getBigDecimal(deductionGuarantee));
            extractImportdetail.setDeductCreditInformation(getBigDecimal(deductCreditInformation));

            extractImportdetail.setSubtotalDeduction(getBigDecimal(subtotalOfDeduction));

//            extractImportdetail.setConsotax(new BigDecimal(tax)); // 综合税
            extractImportdetail.setCopeextract(new BigDecimal(copeextract));// 实发提成

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
        extractImportdetail.setIscompanyemp("是".equals(isCompanyEmp) ? true : false);
        switch (ExtractUserTypeEnum.valueOf(isCompanyEmp)) {
            case COMPANY_STAFF:
                WbUser user = getUserByEmpno(empNo);
                extractImportdetail.setEmpid(user.getUserId());
                extractImportdetail.setIdnumber(user.getIdNumber());
                break;
            case EXTERNAL_STAFF:
                BudgetExtractOuterperson outerPerson = getExtractOuterpersonByEmpnoAndEmpname(empNo, empName);
                extractImportdetail.setEmpid(outerPerson.getId().toString());
                extractImportdetail.setIdnumber(outerPerson.getIdnumber());
                break;
            case SELF_EMPLOYED_EMPLOYEES:
                //todo 个体户
                IndividualEmployeeFiles employeeFiles = individualService.lambdaQuery().eq(IndividualEmployeeFiles::getEmployeeJobNum, empNo).eq(IndividualEmployeeFiles::getAccountName, empName).last("limit 1").one();
                WbUser user2 = getUserByEmpno(empNo);
                extractImportdetail.setEmpid(user2.getUserId());
                extractImportdetail.setIdnumber(user2.getIdNumber());
                extractImportdetail.setIndividualEmployeeId(employeeFiles.getId());
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




