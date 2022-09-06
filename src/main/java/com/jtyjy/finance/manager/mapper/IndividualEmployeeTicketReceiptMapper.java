package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.IndividualEmployeeTicketReceipt;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.finance.manager.query.individual.IndividualTicketQuery;
import com.jtyjy.finance.manager.vo.individual.IndividualTicketPageVO;
import com.jtyjy.finance.manager.vo.individual.IndividualTicketVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author User
* @description 针对表【budget_individual_employee_ticket_receipt(员工个体户收票信息主表 维护档案)】的数据库操作Mapper
* @createDate 2022-09-06 16:44:38
* @Entity com.jtyjy.finance.manager.bean.IndividualEmployeeTicketReceipt
*/
public interface IndividualEmployeeTicketReceiptMapper extends BaseMapper<IndividualEmployeeTicketReceipt> {

    IPage<IndividualTicketPageVO> selectTicketPage(IPage<?> page,@Param("query") IndividualTicketQuery query);

    IPage<Map> selectTicketPageMap(IPage<Object> page);

    List<String> getAllCodes();
}




