package com.jtyjy.finance.manager.converter;

import com.jtyjy.finance.manager.bean.IndividualEmployeeFiles;
import com.jtyjy.finance.manager.bean.IndividualEmployeeTicketReceiptInfo;
import com.jtyjy.finance.manager.dto.individual.IndividualEmployeeFilesDTO;
import com.jtyjy.finance.manager.dto.individual.IndividualExportDTO;
import com.jtyjy.finance.manager.dto.individual.IndividualImportDTO;
import com.jtyjy.finance.manager.dto.individual.IndividualTicketImportDTO;
import com.jtyjy.finance.manager.vo.individual.IndividualEmployeeFilesVO;
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
public interface IndividualTicketConverter {
    IndividualTicketConverter INSTANCE = Mappers.getMapper(IndividualTicketConverter.class);


//    IndividualEmployeeFiles exportDTOToEntity(IndividualTicketImportDTO exportDTO);
//    List<IndividualEmployeeFiles> exportDTOToEntities(List<IndividualTicketImportDTO> exportDTOList);

    IndividualEmployeeTicketReceiptInfo importDTOToEntity(IndividualTicketImportDTO importDTO);
    List<IndividualEmployeeTicketReceiptInfo> importDTOToEntities(List<IndividualTicketImportDTO> importDTOList);
}
