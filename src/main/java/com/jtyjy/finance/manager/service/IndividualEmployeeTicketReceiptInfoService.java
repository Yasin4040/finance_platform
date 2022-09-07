package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jtyjy.finance.manager.bean.IndividualEmployeeTicketReceiptInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jtyjy.finance.manager.dto.individual.IndividualTicketDTO;
import com.jtyjy.finance.manager.dto.individual.IndividualTicketImportErrorDTO;
import com.jtyjy.finance.manager.dto.individual.IndividualTicketInfoDTO;
import com.jtyjy.finance.manager.query.individual.IndividualTicketQuery;
import com.jtyjy.finance.manager.vo.individual.IndividualTicketVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
* @author User
* @description 针对表【budget_individual_employee_ticket_receipt_info(员工个体户收票信息维护档案)】的数据库操作Service
* @createDate 2022-08-25 13:28:19
*/
public interface IndividualEmployeeTicketReceiptInfoService extends IService<IndividualEmployeeTicketReceiptInfo> {

    IPage<IndividualTicketVO> selectPage(IndividualTicketQuery query);

    void addTicket(IndividualTicketDTO dto);

    void updateTicket(IndividualTicketDTO dto);

    List<IndividualTicketImportErrorDTO> importTicket(MultipartFile multipartFile);

    IndividualTicketInfoDTO getIndividualInfo(String ticketId);

    void delTicket(List<Long> ids);
}
