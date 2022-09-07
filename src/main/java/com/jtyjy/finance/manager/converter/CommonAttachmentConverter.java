package com.jtyjy.finance.manager.converter;

import com.jtyjy.finance.manager.bean.BudgetCommonAttachment;
import com.jtyjy.finance.manager.bean.IndividualEmployeeTicketReceiptInfo;
import com.jtyjy.finance.manager.dto.individual.IndividualTicketDetailsDTO;
import com.jtyjy.finance.manager.dto.individual.IndividualTicketImportDTO;
import com.jtyjy.finance.manager.vo.application.BudgetCommonAttachmentVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Description:
 * Created by ZiYao Lee on 2022/08/26.
 * Time: 17:29
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommonAttachmentConverter {
    CommonAttachmentConverter INSTANCE = Mappers.getMapper(CommonAttachmentConverter.class);


//    IndividualEmployeeFiles exportDTOToEntity(IndividualTicketImportDTO exportDTO);
//    List<IndividualEmployeeFiles> exportDTOToEntities(List<IndividualTicketImportDTO> exportDTOList);

    BudgetCommonAttachmentVO toVO(BudgetCommonAttachment attachment);
    List<BudgetCommonAttachmentVO> toVOList(List<BudgetCommonAttachment> attachments);
//
//
//    IndividualTicketDetailsDTO toDetailDTO(IndividualEmployeeTicketReceiptInfo info);
//    List<IndividualTicketDetailsDTO> toDetailDTOList(List<IndividualEmployeeTicketReceiptInfo> infoList);



}
