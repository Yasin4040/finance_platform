package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jtyjy.finance.manager.bean.BudgetExtractCommissionApplicationBudgetDetails;
import com.jtyjy.finance.manager.bean.BudgetExtractImportdetail;
import com.jtyjy.finance.manager.bean.BudgetExtractdetail;
import com.jtyjy.finance.manager.query.commission.CommissionQuery;
import com.jtyjy.finance.manager.query.commission.UpdateViewRequest;
import com.jtyjy.finance.manager.vo.application.CommissionImportDetailVO;

/**
* @author User
* @description 针对表【budget_extract_commission_application_budget_details(提成支付申请单  附表 预算明细)】的数据库操作Service
* @createDate 2022-08-26 11:08:05
*/
public interface CommissionApplicationDetailsService extends IService<BudgetExtractImportdetail> {

    IPage<CommissionImportDetailVO> selectCommissionPage(CommissionQuery query);

    void updateView(UpdateViewRequest request);
}
