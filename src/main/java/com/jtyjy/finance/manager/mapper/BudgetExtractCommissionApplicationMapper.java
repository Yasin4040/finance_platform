package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetExtractCommissionApplication;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.finance.manager.vo.BudgetSubjectAgentVO;
import com.jtyjy.finance.manager.vo.application.BudgetSubjectVO;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

/**
* @author User
* @description 针对表【budget_extract_commission_application(提成支付申请单  主表 )】的数据库操作Mapper
* @createDate 2022-08-26 11:08:05
* @Entity com.jtyjy.finance.manager.bean.BudgetExtractCommissionApplication
*/
public interface BudgetExtractCommissionApplicationMapper extends BaseMapper<BudgetExtractCommissionApplication> {

    /**
     * 查询月度科目动因
     *
     * @param pageBean 分页
     * @param paramMap 查询条件
     * @return 结果集
     */
    List<BudgetSubjectVO> listSubjectMonthAgentByMap(@Param("pageBean") Page<BudgetSubjectVO> pageBean, @Param("map") HashMap<String, Object> paramMap);

}




