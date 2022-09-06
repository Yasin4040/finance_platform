package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jtyjy.finance.manager.bean.IndividualEmployeeTicketReceipt;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jtyjy.finance.manager.query.individual.IndividualTicketQuery;
import com.jtyjy.finance.manager.vo.individual.IndividualTicketPageVO;
import com.jtyjy.finance.manager.vo.individual.IndividualTicketVO;

import java.util.List;

/**
* @author User
* @description 针对表【budget_individual_employee_ticket_receipt(员工个体户收票信息主表 维护档案)】的数据库操作Service
* @createDate 2022-09-06 16:44:38
*/
public interface IndividualEmployeeTicketReceiptService extends IService<IndividualEmployeeTicketReceipt> {

    IPage<IndividualTicketPageVO> selectPage(IndividualTicketQuery query);

    List<String> getAllCodes();
}
