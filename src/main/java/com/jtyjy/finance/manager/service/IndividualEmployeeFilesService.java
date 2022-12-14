package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jtyjy.finance.manager.bean.IndividualEmployeeFiles;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jtyjy.finance.manager.dto.individual.*;
import com.jtyjy.finance.manager.query.individual.IndividualFilesQuery;
import com.jtyjy.finance.manager.vo.individual.IndividualEmployeeFilesVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
* @author User
* @description 针对表【budget_individual_employee_files(员工个体户档案)】的数据库操作Service
* @createDate 2022-08-25 13:28:19
*/
public interface IndividualEmployeeFilesService extends IService<IndividualEmployeeFiles> {

    IPage<IndividualEmployeeFilesVO> selectPage(IndividualFilesQuery query);

    void addIndividual(IndividualEmployeeFilesDTO dto);

    void updateIndividual(IndividualEmployeeFiles file);

    void updateIndividualStatus(IndividualEmployeeFilesStatusDTO statusDTO);

    List<IndividualExportDTO> exportIndividual(IndividualFilesQuery query);

    List<IndividualImportErrorDTO> importIndividual(MultipartFile multipartFile);

    Integer findRepeat(IndividualRepeatDTO dto);
}
