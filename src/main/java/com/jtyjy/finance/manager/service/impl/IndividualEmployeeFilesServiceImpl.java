package com.jtyjy.finance.manager.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iamxiongx.util.message.exception.BusinessException;
import com.jtyjy.finance.manager.bean.IndividualEmployeeFiles;
import com.jtyjy.finance.manager.converter.IndividualEmployeeFilesConverter;
import com.jtyjy.finance.manager.dto.individual.IndividualEmployeeFilesDTO;
import com.jtyjy.finance.manager.dto.individual.IndividualEmployeeFilesStatusDTO;
import com.jtyjy.finance.manager.dto.individual.IndividualExportDTO;
import com.jtyjy.finance.manager.dto.individual.IndividualImportDTO;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.IndividualEmployeeFilesMapper;
import com.jtyjy.finance.manager.query.IndividualFilesQuery;
import com.jtyjy.finance.manager.service.IndividualEmployeeFilesService;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.vo.IndividualEmployeeFilesVO;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
* @author User
* @description 针对表【budget_individual_employee_files(员工个体户档案)】的数据库操作Service实现
* @createDate 2022-08-25 13:28:19
*/
@Service
public class IndividualEmployeeFilesServiceImpl extends ServiceImpl<IndividualEmployeeFilesMapper, IndividualEmployeeFiles>
    implements IndividualEmployeeFilesService{

    @Override
    public IPage<IndividualEmployeeFilesVO> selectPage(IndividualFilesQuery query) {
        Page<IndividualEmployeeFiles> page = this.lambdaQuery()
                .like(StringUtils.isNotBlank(query.getAccount()), IndividualEmployeeFiles::getAccount, query.getAccount())
                .like(StringUtils.isNotBlank(query.getAccountName()), IndividualEmployeeFiles::getAccountName, query.getAccountName())
                .like(StringUtils.isNotBlank(query.getEmployeeName()), IndividualEmployeeFiles::getEmployeeName, query.getEmployeeName())
                .like(StringUtils.isNotBlank(query.getBatchNo()), IndividualEmployeeFiles::getBatchNo, query.getBatchNo())
                .like(StringUtils.isNotBlank(query.getDepartmentName()), IndividualEmployeeFiles::getDepartmentName, query.getDepartmentName())
                .like(StringUtils.isNotBlank(query.getDepositBank()), IndividualEmployeeFiles::getDepositBank, query.getDepositBank())
                .like(StringUtils.isNotBlank(query.getIssuedUnit()), IndividualEmployeeFiles::getIssuedUnit, query.getIssuedUnit())
                .like(StringUtils.isNotBlank(query.getProvinceOrRegion()), IndividualEmployeeFiles::getProvinceOrRegion, query.getProvinceOrRegion())
                .like(StringUtils.isNotBlank(query.getReleaseOpinions()), IndividualEmployeeFiles::getReleaseOpinions, query.getReleaseOpinions())
                .like(query.getEmployeeJobNum() != null, IndividualEmployeeFiles::getEmployeeJobNum, query.getEmployeeJobNum())
                .eq(query.getStatus() != null, IndividualEmployeeFiles::getStatus, query.getStatus())
                .page(new Page<>(query.getPageNum(), query.getPageSize()));
        if(CollectionUtils.isEmpty(page.getRecords()) ){
            return new Page<>();
        }
        return page.convert(IndividualEmployeeFilesConverter.INSTANCE::toVO);

    }

    @Override
    public void addIndividual(IndividualEmployeeFilesDTO dto) {
        Optional<IndividualEmployeeFiles> existsOptional = this.lambdaQuery().eq(IndividualEmployeeFiles::getEmployeeJobNum, dto.getEmployeeJobNum())
                .eq(IndividualEmployeeFiles::getAccountName, dto.getAccountName()).oneOpt();

        if (existsOptional.isPresent()) {
            throw new BusinessException("保存失败!户名不允许重复!");
        }
        IndividualEmployeeFiles entity = IndividualEmployeeFilesConverter.INSTANCE.dtoToEntity(dto);

        entity.setCreateTime(new Date());
        entity.setCreateBy(UserThreadLocal.get().getUserName());

        entity.setUpdateTime(new Date());
        entity.setUpdateBy(UserThreadLocal.get().getUserName());
        this.save(entity);
    }

    @Override
    public void updateIndividual(IndividualEmployeeFiles file) {
        Optional<IndividualEmployeeFiles> existsOptional = this.lambdaQuery().eq(IndividualEmployeeFiles::getEmployeeJobNum, file.getEmployeeJobNum())
                .eq(IndividualEmployeeFiles::getAccountName, file.getAccountName()).ne(IndividualEmployeeFiles::getId,file.getId()).oneOpt();
        if (existsOptional.isPresent()) {
            throw new BusinessException("保存失败!户名不允许重复!");
        }
        file.setUpdateTime(new Date());
        file.setUpdateBy(UserThreadLocal.get().getUserName());
        this.saveOrUpdate(file);
    }

    @Override
    public void updateIndividualStatus(IndividualEmployeeFilesStatusDTO statusDTO) {
        IndividualEmployeeFiles employeeFiles = this.getById(statusDTO.getId());
        employeeFiles.setStatus(statusDTO.getStatus());
        employeeFiles.setUpdateTime(new Date());
        employeeFiles.setUpdateBy(UserThreadLocal.get().getUserName());
        this.saveOrUpdate(employeeFiles);
    }

    @Override
    public List<IndividualExportDTO> exportIndividual(IndividualFilesQuery query) {
        return this.lambdaQuery()
                .like(StringUtils.isNotBlank(query.getAccount()), IndividualEmployeeFiles::getAccount, query.getAccount())
                .like(StringUtils.isNotBlank(query.getAccountName()), IndividualEmployeeFiles::getAccountName, query.getAccountName())
                .like(StringUtils.isNotBlank(query.getEmployeeName()), IndividualEmployeeFiles::getEmployeeName, query.getEmployeeName())
                .like(StringUtils.isNotBlank(query.getBatchNo()), IndividualEmployeeFiles::getBatchNo, query.getBatchNo())
                .like(StringUtils.isNotBlank(query.getDepartmentName()), IndividualEmployeeFiles::getDepartmentName, query.getDepartmentName())
                .like(StringUtils.isNotBlank(query.getDepositBank()), IndividualEmployeeFiles::getDepositBank, query.getDepositBank())
                .like(StringUtils.isNotBlank(query.getIssuedUnit()), IndividualEmployeeFiles::getIssuedUnit, query.getIssuedUnit())
                .like(StringUtils.isNotBlank(query.getProvinceOrRegion()), IndividualEmployeeFiles::getProvinceOrRegion, query.getProvinceOrRegion())
                .like(StringUtils.isNotBlank(query.getReleaseOpinions()), IndividualEmployeeFiles::getReleaseOpinions, query.getReleaseOpinions())
                .like(query.getEmployeeJobNum() != null, IndividualEmployeeFiles::getEmployeeJobNum, query.getEmployeeJobNum())
                .eq(query.getStatus() != null, IndividualEmployeeFiles::getStatus, query.getStatus())
                .list().stream().map(IndividualEmployeeFilesConverter.INSTANCE::toExportDTO).collect(Collectors.toList());
    }

    @SneakyThrows
    @Override
    public void importIndividual(MultipartFile multipartFile) {

//        List<IndividualImportDTO> excelContent = EasyExcelUtil.getExcelContent(multipartFile.getInputStream(), IndividualImportDTO.class);
//        System.out.println(excelContent);
        try {
            EasyExcel.read(multipartFile.getInputStream(), IndividualImportDTO.class,
                    new PageReadListener<IndividualImportDTO>(dataList -> {
                        //save
//                        for (IndividualExportDTO dto : dataList) {
//                            log.info("读取到一条数据{}", JSON.toJSONString(demoData));
//                        }
                        //新增--
                        List<IndividualEmployeeFiles> entities = IndividualEmployeeFilesConverter.INSTANCE.importDTOToEntities(dataList);
                        System.out.println(dataList);
                        this.saveBatch(entities);
                    })).sheet().doRead();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}




