package com.jtyjy.finance.manager.converter;

import com.jtyjy.finance.manager.bean.IndividualEmployeeFiles;
import com.jtyjy.finance.manager.dto.individual.IndividualEmployeeFilesDTO;
import com.jtyjy.finance.manager.dto.individual.IndividualExportDTO;
import com.jtyjy.finance.manager.dto.individual.IndividualImportDTO;
import com.jtyjy.finance.manager.vo.IndividualEmployeeFilesVO;
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
public interface IndividualEmployeeFilesConverter {
    IndividualEmployeeFilesConverter INSTANCE = Mappers.getMapper(IndividualEmployeeFilesConverter.class);

    IndividualEmployeeFilesVO toVO(IndividualEmployeeFiles files);

    List<IndividualEmployeeFilesVO> toVOList(List<IndividualEmployeeFiles> filesList);



    IndividualEmployeeFiles dtoToEntity(IndividualEmployeeFilesDTO dto);

    IndividualExportDTO toExportDTO(IndividualEmployeeFiles files);


    IndividualEmployeeFiles exportDTOToEntity(IndividualExportDTO exportDTO);
    List<IndividualEmployeeFiles> exportDTOToEntities(List<IndividualExportDTO> exportDTOList);

    IndividualEmployeeFiles importDTOToEntity(IndividualImportDTO importDTO);
    List<IndividualEmployeeFiles> importDTOToEntities(List<IndividualImportDTO> importDTOList);
}
