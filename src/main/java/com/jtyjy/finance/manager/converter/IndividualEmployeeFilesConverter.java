package com.jtyjy.finance.manager.converter;

import com.jtyjy.finance.manager.bean.IndividualEmployeeFiles;
import com.jtyjy.finance.manager.dto.individual.IndividualEmployeeFilesDTO;
import com.jtyjy.finance.manager.dto.individual.IndividualExportDTO;
import com.jtyjy.finance.manager.dto.individual.IndividualImportDTO;
import com.jtyjy.finance.manager.vo.individual.IndividualEmployeeFilesVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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


    IndividualExportDTO entityToExportDTO(IndividualEmployeeFiles entity);
    List<IndividualExportDTO> entityToExportDTOList(List<IndividualEmployeeFiles> entities);

    @Mapping(target = "accountType",ignore = true)
    IndividualEmployeeFiles importDTOToEntity(IndividualImportDTO importDTO);
    List<IndividualEmployeeFiles> importDTOToEntities(List<IndividualImportDTO> importDTOList);
}
