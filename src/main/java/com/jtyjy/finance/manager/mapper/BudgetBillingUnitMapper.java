package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetBillingUnit;
import com.jtyjy.finance.manager.vo.BillingUnitVO;
import com.jtyjy.finance.manager.vo.PaymentUnitVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author Admin
 */
public interface BudgetBillingUnitMapper extends BaseMapper<BudgetBillingUnit> {
    // 查询开票单位信息
    List<BillingUnitVO> getBillUnitPageList(Page<BillingUnitVO> pageCond,
                                            Map<String, Object> conditionMap,
                                            String authSql);

    List<PaymentUnitVO> curUserPaymentUnitAccount(@Param("ids") String ids);
}
