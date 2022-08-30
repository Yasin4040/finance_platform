package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jtyjy.finance.manager.bean.IndividualEmployeeTicketReceiptInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.finance.manager.query.individual.IndividualTicketQuery;
import com.jtyjy.finance.manager.vo.individual.IndividualTicketVO;

/**
* @author User
* @description 针对表【budget_individual_employee_ticket_receipt_info(员工个体户收票信息维护档案)】的数据库操作Mapper
* @createDate 2022-08-25 13:28:19
* @Entity com.jtyjy.finance.manager.bean.IndividualEmployeeTicketReceiptInfo
*/
public interface IndividualEmployeeTicketReceiptInfoMapper extends BaseMapper<IndividualEmployeeTicketReceiptInfo> {

    IPage<IndividualTicketVO> selectTicketPage(IPage<?> page,IndividualTicketQuery query);
}




