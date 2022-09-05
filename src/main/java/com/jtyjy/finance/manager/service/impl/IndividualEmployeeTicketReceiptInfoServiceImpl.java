package com.jtyjy.finance.manager.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iamxiongx.util.message.exception.BusinessException;
import com.jtyjy.finance.manager.bean.IndividualEmployeeFiles;
import com.jtyjy.finance.manager.bean.IndividualEmployeeTicketReceiptInfo;
import com.jtyjy.finance.manager.converter.IndividualTicketConverter;
import com.jtyjy.finance.manager.dto.individual.*;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.listener.easyexcel.PageReadListener;
import com.jtyjy.finance.manager.query.individual.IndividualTicketQuery;
import com.jtyjy.finance.manager.service.IndividualEmployeeFilesService;
import com.jtyjy.finance.manager.service.IndividualEmployeeTicketReceiptInfoService;
import com.jtyjy.finance.manager.mapper.IndividualEmployeeTicketReceiptInfoMapper;
import com.jtyjy.finance.manager.vo.individual.IndividualTicketVO;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.locale.converters.DateLocaleConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
* @author User
* @description 针对表【budget_individual_employee_ticket_receipt_info(员工个体户收票信息维护档案)】的数据库操作Service实现
* @createDate 2022-08-25 13:28:19
*/
@Service
public class IndividualEmployeeTicketReceiptInfoServiceImpl extends ServiceImpl<IndividualEmployeeTicketReceiptInfoMapper, IndividualEmployeeTicketReceiptInfo>
    implements IndividualEmployeeTicketReceiptInfoService{

    private final IndividualEmployeeFilesService employeeFilesService;

    public IndividualEmployeeTicketReceiptInfoServiceImpl(IndividualEmployeeFilesService employeeFilesService) {
        this.employeeFilesService = employeeFilesService;
    }

    @Override
    public IPage<IndividualTicketVO> selectPage(IndividualTicketQuery query) {
        return this.baseMapper.selectTicketPage(new Page<>(query.getPageNum(),query.getPageSize()),query);
    }

    @Override
    public void addTicket(IndividualTicketDTO dto) {
        List<IndividualTicketDetailsDTO> detailsDTOList = dto.getDetailsDTOList();
        List<IndividualEmployeeTicketReceiptInfo> infoList = new ArrayList<>();
        for (int i = 0; i < detailsDTOList.size(); i++) {
            Integer count = this.lambdaQuery()
                    .eq(IndividualEmployeeTicketReceiptInfo::getIndividualEmployeeInfoId, dto.getIndividualEmployeeInfoId())
                    .eq(IndividualEmployeeTicketReceiptInfo::getYear, detailsDTOList.get(i).getYear())
                    .eq(IndividualEmployeeTicketReceiptInfo::getMonth, detailsDTOList.get(i).getMonth()).count();
            if (count>0) {
                throw new BusinessException("该档案下,存在重复的同年同月的记录。");
            }
            IndividualEmployeeTicketReceiptInfo info = new IndividualEmployeeTicketReceiptInfo();

            IndividualTicketDetailsDTO singleDTO = detailsDTOList.get(i);
            info.setYear(singleDTO.getYear());
            info.setMonth(singleDTO.getMonth());
            info.setInvoiceAmount(singleDTO.getInvoiceAmount());

            info.setEmployeeJobNum(dto.getEmployeeJobNum());
            info.setIndividualEmployeeInfoId(dto.getIndividualEmployeeInfoId());
            info.setIndividualName(dto.getIndividualName());
            info.setRemarks(dto.getRemarks());

            info.setCreateTime(new Date());
            info.setCreateBy(UserThreadLocal.getEmpNo());

            info.setUpdateTime(new Date());
            info.setUpdateBy(UserThreadLocal.getEmpNo());
            infoList.add(info);
        }
        this.saveBatch(infoList);
    }

    @Override
    public void updateTicket(IndividualTicketDTO dto) {
        List<IndividualTicketDetailsDTO> detailsDTOList = dto.getDetailsDTOList();
        List<IndividualEmployeeTicketReceiptInfo> infoList = new ArrayList<>();
        for (int i = 0; i < detailsDTOList.size(); i++) {

            IndividualEmployeeTicketReceiptInfo info = this.getById(detailsDTOList.get(i).getId());

            IndividualTicketDetailsDTO singleDTO = detailsDTOList.get(i);
            info.setYear(singleDTO.getYear());
            info.setMonth(singleDTO.getMonth());
            info.setInvoiceAmount(singleDTO.getInvoiceAmount());

//            info.setEmployeeJobNum(dto.getEmployeeJobNum());
//            info.setIndividualEmployeeInfoId(dto.getIndividualEmployeeInfoId());
//            info.setIndividualName(dto.getIndividualName());
            info.setRemarks(dto.getRemarks());

            info.setUpdateTime(new Date());
            info.setUpdateBy(UserThreadLocal.getEmpNo());
            infoList.add(info);
        }
        this.saveOrUpdateBatch(infoList);
    }
    @SneakyThrows
    @Override
    public List<IndividualTicketImportErrorDTO> importTicket(MultipartFile multipartFile) {
        List<IndividualTicketImportErrorDTO> errorDTOList = new ArrayList<>();
        List<Map> errorMap = new ArrayList<>();
        try {
            EasyExcel.read(multipartFile.getInputStream(), IndividualTicketImportDTO.class,
                    new PageReadListener<IndividualTicketImportDTO>(dataList -> {
                        //获取相应的档案信息
                        for (int i = 0; i < dataList.size(); i++) {
                            try {
                                String individualName = dataList.get(i).getIndividualName();
                                Optional<IndividualEmployeeFiles> individualEmployeeFiles = employeeFilesService.lambdaQuery().like(IndividualEmployeeFiles::getAccountName, individualName).last("limit 1").oneOpt();
                                if(individualEmployeeFiles.isPresent()){
                                    IndividualEmployeeFiles file = individualEmployeeFiles.get();
                                    IndividualEmployeeTicketReceiptInfo info = IndividualTicketConverter.INSTANCE.importDTOToEntity(dataList.get(i));
                                    info.setIndividualEmployeeInfoId(file.getId());
                                    info.setIndividualName(file.getAccountName());
                                    info.setEmployeeJobNum(file.getEmployeeJobNum());
                                    this.save(info);
                                }
                            } catch (Exception e) {
                                IndividualTicketImportErrorDTO errorDTO = new IndividualTicketImportErrorDTO();

                                try {
                                    PropertyUtils.copyProperties(errorDTO,dataList.get(i));
                                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                                    ex.printStackTrace();
                                }
                                errorDTO.setInsertDatabaseError(e.getCause().getMessage());
                                errorDTOList.add(errorDTO);
                            }
                        }

                    },errorMap)).sheet().doRead();

            for (Map map : errorMap) {
                IndividualTicketImportErrorDTO errorDTO = new IndividualTicketImportErrorDTO();
                try {
                    ConvertUtils.register(new DateLocaleConverter(), Date.class);//BeanUtils.populate对日期类型进行处理，否则无法封装
                    BeanUtils.populate(errorDTO,map);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                errorDTOList.add(errorDTO);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return errorDTOList;
    }
}




