package com.jtyjy.finance.manager.converter;

import com.jtyjy.finance.manager.bean.BudgetExtractImportdetail;
import com.jtyjy.finance.manager.bean.IndividualEmployeeTicketReceiptInfo;
import com.jtyjy.finance.manager.dto.commission.CommissionDetailsImportDTO;
import com.jtyjy.finance.manager.dto.individual.IndividualTicketDetailsDTO;
import com.jtyjy.finance.manager.dto.individual.IndividualTicketImportDTO;
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
public interface CommissionConverter {
    CommissionConverter INSTANCE = Mappers.getMapper(CommissionConverter.class);


    CommissionDetailsImportDTO toDTO(BudgetExtractImportdetail entity);
    List<CommissionDetailsImportDTO> toDTOList(List<BudgetExtractImportdetail> entities);



}
