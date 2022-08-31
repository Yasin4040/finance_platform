package com.jtyjy.finance.manager.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jtyjy.finance.manager.bean.IndividualEmployeeFiles;
import com.jtyjy.finance.manager.bean.IndividualEmployeeTicketReceiptInfo;
import com.jtyjy.finance.manager.converter.IndividualTicketConverter;
import com.jtyjy.finance.manager.dto.individual.IndividualTicketDTO;
import com.jtyjy.finance.manager.dto.individual.IndividualTicketDetailsDTO;
import com.jtyjy.finance.manager.dto.individual.IndividualTicketImportDTO;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.listener.easyexcel.PageReadListener;
import com.jtyjy.finance.manager.query.individual.IndividualTicketQuery;
import com.jtyjy.finance.manager.service.IndividualEmployeeFilesService;
import com.jtyjy.finance.manager.service.IndividualEmployeeTicketReceiptInfoService;
import com.jtyjy.finance.manager.mapper.IndividualEmployeeTicketReceiptInfoMapper;
import com.jtyjy.finance.manager.vo.individual.IndividualTicketVO;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
    public void importTicket(MultipartFile multipartFile) {
        try {
            EasyExcel.read(multipartFile.getInputStream(), IndividualTicketImportDTO.class,
                    new PageReadListener<IndividualTicketImportDTO>(dataList -> {
                        List<IndividualEmployeeTicketReceiptInfo> entities= new ArrayList<>();
                        //获取相应的档案信息
                        for (int i = 0; i < dataList.size(); i++) {
                            String individualName = dataList.get(i).getIndividualName();
                            Optional<IndividualEmployeeFiles> individualEmployeeFiles = employeeFilesService.lambdaQuery().like(IndividualEmployeeFiles::getAccountName, individualName).last("limit 1").oneOpt();
                            if(individualEmployeeFiles.isPresent()){
                                IndividualEmployeeFiles file = individualEmployeeFiles.get();
                                IndividualEmployeeTicketReceiptInfo info = IndividualTicketConverter.INSTANCE.importDTOToEntity(dataList.get(i));
                                info.setIndividualEmployeeInfoId(file.getId());
                                info.setIndividualName(file.getAccountName());
                                info.setEmployeeJobNum(file.getEmployeeJobNum());
                                entities.add(info);
                            }
                        }
                        this.saveBatch(entities);
                    })).sheet().doRead();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}




