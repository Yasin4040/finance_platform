package com.jtyjy.finance.manager.config;

import com.jtyjy.core.filter.params.RequestResponseFilter;
import com.jtyjy.core.filter.strategy.impl.RequestResponseReplaceStrategy;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class FilterConfig {

    @Bean
    public RequestResponseReplaceStrategy requestResponseReplaceStrategy() throws Exception {
        RequestResponseReplaceStrategy strategy = new RequestResponseReplaceStrategy();
        //strategy.add("\\r", "rhuanhang");
       //strategy.add("\\n", "nhuanhang");
        //strategy.add("\\r\\n", "allhuanhang");
        return strategy;
    }

    @Bean
    public FilterRegistrationBean<RequestResponseFilter> requestResponseFilter() throws Exception {
        List<String> apis = new ArrayList<>();

        //上传文件
        apis.add("/api/common/upload");
        apis.add("/api/common/uploadFile");

        //发放人员模板 和导出
        apis.add("/api/commissionApplication/downLoadIssuedTemplate");
        apis.add("/api/commissionApplication/exportIssuedTemplate");
        //费用导入
        apis.add("/api/commissionApplication/importFeeTemplate");


        //提成明细导入导出
        apis.add("/api/commissionApplication/downLoadTemplate");
        apis.add("/api/commissionApplication/exportTemplate");
        apis.add("/api/commissionApplication/importTemplate");
        apis.add("/api/commissionApplication/downImportExtractErrorDetail");
        //个体户发票维护
        apis.add("/api/individualTicket/downLoadTemplate");
        apis.add("/api/individualTicket/exportTicket");
        apis.add("/api/individualTicket/importTicket");


        //个人维护档案
        apis.add("/api/individualEmployee/downLoadTemplate");
        apis.add("/api/individualEmployee/exportIndividual");
        apis.add("/api/individualEmployee/importIndividual");

        apis.add("/api/test/upload");
        //稿费信息
        apis.add("/api/budgetAuthorfee/downAuthorFeeImportTemplate");
        apis.add("/api/budgetAuthorfee/exportDetail");
        apis.add("/api/budgetAuthorfee/importDetail");
        apis.add("/api/budgetAuthorfee/downAuthorFeeImportErrorDetail");
        apis.add("/api/budgetAuthorfee/exportAuthorFeeSumList");
        apis.add("/api/budgetAuthorfee/exportBatchContributionFee");
        //稿费报表
        apis.add("/api/authorfeeReport/exportAuthorFeeEntyDetails");
        apis.add("/api/authorfeeReport/exportDeptAuthorFeePayDetails");
        //提成信息
        apis.add("/api/extractInfo/exportExtractDeductionReport");
        apis.add("/api/extractInfo/downExtractImportTemplate");
        apis.add("/api/extractInfo/importExtractDetail");
        apis.add("/api/extractInfo/downImportExtractErrorDetail");
        apis.add("/api/extractInfo/exportExtractExcessDetail");
        apis.add("/api/extractInfo/importExtractExcessDetail");
        apis.add("/api/extractInfo/downImportExtractExcessErrorDetail");
        apis.add("/api/extractInfo/exportExtractPaymentDetail");
        apis.add("/api/extractInfo/exportExtractCCLPayDetail");
        apis.add("/api/extractInfo/exportCurMonthExtractIncomeDetail");
        apis.add("/api/extractInfo/downExtractFeepayTemplate");
        apis.add("/api/extractInfo/exportExtractPay");
        apis.add("/api/extractInfo/exportExtractPaySum");
        apis.add("/api/extractInfo/downImportExtractFeePayErrorDetail");
        apis.add("/api/extractInfo/importExtractFeePay");
        apis.add("/api/extractInfo/exportExtractOutUnitPayDetail");
        apis.add("/api/extractInfo/exportPersonalityDetail");
        apis.add("/api/extractInfo/importPersonalityPayDetail");
        apis.add("/api/extractInfo/importInitPersonalityPayDetail");
        apis.add("/api/extractInfo/downInitPersonalityPayDetailTemplate");
        apis.add("/api/extractInfo/exportPersonalityPayDetail");

        //消息推送
        apis.add("/api/msg/downWarningTemplate");
        apis.add("/api/msg/downPublicTemplate");
        apis.add("/api/msg/downResultTemplate");
        apis.add("/api/msg/importMsgDetail");
        apis.add("/api/msg/downMsgImportErrorDetail");
        // 预算编制
        apis.add("/api/yearAgent/exportYearAgent");
        apis.add("/api/yearAgent/importYearAgent");
        apis.add("/api/yearAgent/exportErrors");
        apis.add("/api/monthAgent/exportMonthAgent");
        apis.add("/api/monthAgent/importMonthAgent");
        apis.add("/api/monthAgent/exportErrors");
        apis.add("/api/yearAgentAdd/exportAgentYearAdd");
        apis.add("/api/monthAgentAdd/exportAgentMonthAdd");
        apis.add("/api/yearSubject/exportYearAgentDetail");
        apis.add("/api/yearSubject/exportYearAgentCollect");
        apis.add("/api/monthSubject/exportMonthAgentDetail");
        apis.add("/api/monthSubject/exportMonthAgentCollect");
        apis.add("/api/budgetAudit/exportCompanyMonthAgentCollect");
        apis.add("/api/yearAgentLend/exportAgentYearLend");
        // 借款管理
        apis.add("/api/arrears/exportCreditTemplate");
        apis.add("/api/arrears/importEmpCredit");
        apis.add("/api/arrears/exportErrors");
        apis.add("/api/lendMoney/exportRepayMoneyTemplate");
        apis.add("/api/lendMoney/importRepayMoney");
        apis.add("/api/lendMoney/exportErrors");
        apis.add("/api/lendMoney/exportLendMoney");
        apis.add("/api/projectLend/exportProjectLend");
        apis.add("/api/projectLend/exportValidate");
        apis.add("/api/projectLend/exportValidateTemplate");
        apis.add("/api/projectLend/importValidate");
        apis.add("/api/projectLend/exportValidateErrors");
        apis.add("/api/projectLend/exportInterest");
        apis.add("/api/projectLend/exportInterestTemplate");
        apis.add("/api/projectLend/importInterest");
        apis.add("/api/projectLend/exportInterestErrors");
        apis.add("/api/projectLend/exportRepayMoneyDetail");
        apis.add("/api/projectLend/exportLendTemplate");
        apis.add("/api/projectLend/importProjectLend");
        apis.add("/api/projectLend/exportLendErrors");
        apis.add("/api/otherLend/exportOtherLendTemplate");
        apis.add("/api/otherLend/importOtherLend");
        apis.add("/api/otherLend/exportErrors");
        apis.add("/api/contract/exportContract");
        apis.add("/api/contract/exportContractLend");

        //基础模块
        apis.add("/api/base/author/downloadTemplate");

        //基础模块
        apis.add("/api/base/author/downloadTemplate");

        apis.add("/api/base/author/importSave");
        apis.add("/api/base/author/exportErrors");
        apis.add("/api/base/bankAccount/export");
        apis.add("/api/base/budgetSubject/export");
        apis.add("/api/base/budgetSubject/exportJinDie");
        apis.add("/api/base/budgetSubject/importJinDie");
        apis.add("/api/base/budgetSubject/downloadJinDieErrors");
        apis.add("/api/base/baseSubject/downloadTemplate");
        apis.add("/api/base/baseSubject/importSave");
        apis.add("/api/base/baseSubject/exportErrors");
        apis.add("/api/base/baseUnit/downloadTemplate");
        apis.add("/api/base/baseUnit/importSave");
        apis.add("/api/base/baseUnit/exportErrors");

        apis.add("/api/base/bankAccount/stopTemplate");
        apis.add("/api/base/bankAccount/exportErrors");
        apis.add("/api/base/bankAccount/downloadTemplate");
        apis.add("/api/base/bankAccount/importSave");
        apis.add("/api/base/bankAccount/importStop");
        //报销执行
        apis.add("/api/reimbursement/downBxApplyImportTemplate");
        apis.add("/api/reimbursement/importApply");
        apis.add("/api/reimbursement/downBxApplyImportError");
        apis.add("/api/reimbursement/exportPreparePay");
        apis.add("/api/reimbursement/exportBxInfo");
        apis.add("/api/reimbursement/exportStrick");
        apis.add("/api/reimbursement/exportBxDetails");
        apis.add("/api/reimbursement/exportEntertainSum");
        apis.add("/api/reimbursement/exportTravelSum");
        apis.add("/api/reimbursement/exportBxTime");
        apis.add("/api/reimbursement/exportReturnReason");
        apis.add("/api/reimbursement/downPayErrorImportTemplate");
        apis.add("/api/reimbursement/importPayError");
        apis.add("/api/reimbursement/downImporPayErrorDetail");
        apis.add("/api/reimbursement/downPayVerifyImportTemplate");
        apis.add("/api/reimbursement/importPayVerify");
        apis.add("/api/reimbursement/downImporPayVerifyDetail");
        apis.add("/api/reimbursement/exportExpense");
        apis.add("/api/reimbursement/exportLackBill");
        FilterRegistrationBean<RequestResponseFilter> registration = new FilterRegistrationBean<RequestResponseFilter>();
        registration.setFilter(new RequestResponseFilter(this.requestResponseReplaceStrategy(), apis));
        List<String> urlList = new ArrayList<String>();
        urlList.add("/*");
        registration.setUrlPatterns(urlList);
        registration.setName("requestResponseFilter");
        registration.setOrder(1);
        return registration;
    }

}
