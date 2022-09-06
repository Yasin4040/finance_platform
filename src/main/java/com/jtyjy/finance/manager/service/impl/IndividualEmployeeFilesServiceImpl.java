package com.jtyjy.finance.manager.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iamxiongx.util.message.exception.BusinessException;
import com.jtyjy.finance.manager.bean.IndividualEmployeeFiles;
import com.jtyjy.finance.manager.cache.BankCache;
import com.jtyjy.finance.manager.cache.UnitCache;
import com.jtyjy.finance.manager.converter.IndividualEmployeeFilesConverter;
import com.jtyjy.finance.manager.dto.individual.*;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.listener.easyexcel.PageReadListener;
import com.jtyjy.finance.manager.mapper.IndividualEmployeeFilesMapper;
import com.jtyjy.finance.manager.query.individual.IndividualFilesQuery;
import com.jtyjy.finance.manager.service.IndividualEmployeeFilesService;
import com.jtyjy.finance.manager.vo.individual.IndividualEmployeeFilesVO;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.locale.converters.DateLocaleConverter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

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
        Page<IndividualEmployeeFiles> page = getSimplePage(query);
        if(CollectionUtils.isEmpty(page.getRecords()) ){
            return new Page<>();
        }
        IPage<IndividualEmployeeFilesVO> convert = page.convert(IndividualEmployeeFilesConverter.INSTANCE::toVO);

        List<IndividualEmployeeFilesVO> records = convert.getRecords();
        for (IndividualEmployeeFilesVO record : records) {
            record.setDepositBankName(BankCache.getBankByBranchCode(record.getDepositBank())!=null
                    ?BankCache.getBankByBranchCode(record.getDepositBank()).getSubBranchName():record.getDepositBank());
            record.setIssuedUnitName(UnitCache.get(record.getIssuedUnit())!=null?
                    UnitCache.get(record.getIssuedUnit()).getName():record.getIssuedUnit());
        }

        return convert;

    }

    private Page<IndividualEmployeeFiles>  getSimplePage(IndividualFilesQuery query){
        List<String> unitsIds = new ArrayList<>();
        if(StringUtils.isNotBlank(query.getIssuedUnit())){
            unitsIds = UnitCache.getByFuzzyName(query.getIssuedUnit());
        }
        Page<IndividualEmployeeFiles> page = this.lambdaQuery()
                .like(StringUtils.isNotBlank(query.getAccount()), IndividualEmployeeFiles::getAccount, query.getAccount())
                .like(StringUtils.isNotBlank(query.getAccountName()), IndividualEmployeeFiles::getAccountName, query.getAccountName())
                .like(StringUtils.isNotBlank(query.getEmployeeName()), IndividualEmployeeFiles::getEmployeeName, query.getEmployeeName())
                .like(StringUtils.isNotBlank(query.getBatchNo()), IndividualEmployeeFiles::getBatchNo, query.getBatchNo())
                .like(StringUtils.isNotBlank(query.getDepartmentName()), IndividualEmployeeFiles::getDepartmentName, query.getDepartmentName())
                .like(StringUtils.isNotBlank(query.getDepositBank()), IndividualEmployeeFiles::getDepositBank, query.getDepositBank())
                .in(CollectionUtils.isNotEmpty(unitsIds), IndividualEmployeeFiles::getIssuedUnit, unitsIds)
                .like(StringUtils.isNotBlank(query.getProvinceOrRegion()), IndividualEmployeeFiles::getProvinceOrRegion, query.getProvinceOrRegion())
                .like(StringUtils.isNotBlank(query.getReleaseOpinions()), IndividualEmployeeFiles::getReleaseOpinions, query.getReleaseOpinions())
                .like(query.getEmployeeJobNum() != null, IndividualEmployeeFiles::getEmployeeJobNum, query.getEmployeeJobNum())
                .eq(query.getStatus() != null, IndividualEmployeeFiles::getStatus, query.getStatus())
                .eq(query.getAccountType() != null, IndividualEmployeeFiles::getAccountType, query.getAccountType())
                .orderByDesc(IndividualEmployeeFiles::getCreateTime)
                .page(new Page<>(query.getPageNum(), query.getPageSize()));
        return page;
    }

    @Override
    public void addIndividual(IndividualEmployeeFilesDTO dto) {
        //银行和单位 都是放id
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
        List<Long> ids = statusDTO.getIds();
        List<IndividualEmployeeFiles> filesList = new ArrayList<>();
        for (Long id : ids) {
            IndividualEmployeeFiles employeeFiles = this.getById(id);
            employeeFiles.setStatus(statusDTO.getStatus());
            employeeFiles.setUpdateTime(new Date());
            employeeFiles.setUpdateBy(UserThreadLocal.get().getUserName());
            filesList.add(employeeFiles);
        }
        this.saveOrUpdateBatch(filesList);
    }

    @Override
    public List<IndividualExportDTO> exportIndividual(IndividualFilesQuery query) {
        query.setPageSize(-1);
        Page<IndividualEmployeeFiles> page = getSimplePage(query);
        List<IndividualEmployeeFiles> list = page.getRecords();
        List<IndividualExportDTO> dtoList = IndividualEmployeeFilesConverter.INSTANCE.entityToExportDTOList(list);
        for (IndividualExportDTO dto : dtoList) {
            dto.setAccountType(dto.getAccountType().equals("1")?"个卡":"公户");
            dto.setDepositBank(BankCache.getBankByBranchCode(dto.getDepositBank())!=null
                    ?BankCache.getBankByBranchCode(dto.getDepositBank()).getSubBranchName():dto.getDepositBank());
            dto.setIssuedUnit(UnitCache.get(dto.getIssuedUnit())!=null?
                    UnitCache.get(dto.getIssuedUnit()).getName():dto.getIssuedUnit());
        }
        return dtoList;
    }

    @SneakyThrows
    @Override
    public List<IndividualImportErrorDTO> importIndividual(MultipartFile multipartFile) {
;
        List<IndividualImportErrorDTO> errList = new ArrayList<>();
        List<Map> errorMap = new ArrayList<>();
        List<String> errorList = new ArrayList<>();
        try {
            EasyExcel.read(multipartFile.getInputStream(), IndividualImportDTO.class,
                    new PageReadListener<IndividualImportDTO>(dataList -> {
                        //save
//                        for (IndividualExportDTO dto : dataList) {
//                            log.info("读取到一条数据{}", JSON.toJSONString(demoData));
//                        }
                        //新增--
//                            List<IndividualEmployeeFiles> entities = IndividualEmployeeFilesConverter.INSTANCE.importDTOToEntities(dataList);
                        for (IndividualImportDTO dto : dataList) {
                            try {
                                    IndividualEmployeeFiles entity = IndividualEmployeeFilesConverter.INSTANCE.importDTOToEntity(dto);
                                    entity.setAccountType(dto.getAccountType().equals("个卡")?1:2);
                                    //通过名称 找id
                                    entity.setDepositBank(BankCache.getBankByBranchName(entity.getDepositBank()) != null ?
                                            BankCache.getBankByBranchName(entity.getDepositBank()).getSubBranchCode() : entity.getDepositBank());
                                    entity.setIssuedUnit(UnitCache.getByName(entity.getIssuedUnit()) != null ?
                                            UnitCache.getByName(entity.getIssuedUnit()).getName() : entity.getIssuedUnit());

                                    entity.setCreateTime(new Date());
                                    entity.setCreateBy(UserThreadLocal.get().getUserName());

                                    entity.setUpdateTime(new Date());
                                    entity.setUpdateBy(UserThreadLocal.get().getUserName());
                                    entity.setStatus(1);
                                    this.save(entity);
                                }
                               catch (Exception e) {
                                IndividualImportErrorDTO errorDTO = new IndividualImportErrorDTO();

                                try {
                                    PropertyUtils.copyProperties(errorDTO,dto);
                                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                                    ex.printStackTrace();
                                }
                                errorDTO.setInsertDatabaseError(e.getCause().getMessage());
                                errList.add(errorDTO);
                            }
                        }
                        System.out.println(dataList);
                    }, errorMap)).sheet().doRead();

            for (Map map : errorMap) {
                IndividualImportErrorDTO errorDTO = new IndividualImportErrorDTO();
                try {
                    ConvertUtils.register(new DateLocaleConverter(), Date.class);//BeanUtils.populate对日期类型进行处理，否则无法封装
                    BeanUtils.populate(errorDTO,map);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                errList.add(errorDTO);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return errList;
    }

}




