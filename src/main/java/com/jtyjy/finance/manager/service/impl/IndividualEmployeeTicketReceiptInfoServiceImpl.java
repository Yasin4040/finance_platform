package com.jtyjy.finance.manager.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jtyjy.finance.manager.bean.IndividualEmployeeFiles;
import com.jtyjy.finance.manager.bean.IndividualEmployeeTicketReceipt;
import com.jtyjy.finance.manager.bean.IndividualEmployeeTicketReceiptInfo;
import com.jtyjy.finance.manager.converter.IndividualEmployeeFilesConverter;
import com.jtyjy.finance.manager.converter.IndividualTicketConverter;
import com.jtyjy.finance.manager.dto.individual.*;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.listener.easyexcel.PageReadListener;
import com.jtyjy.finance.manager.query.individual.IndividualTicketQuery;
import com.jtyjy.finance.manager.service.IndividualEmployeeFilesService;
import com.jtyjy.finance.manager.service.IndividualEmployeeTicketReceiptInfoService;
import com.jtyjy.finance.manager.mapper.IndividualEmployeeTicketReceiptInfoMapper;
import com.jtyjy.finance.manager.service.IndividualEmployeeTicketReceiptService;
import com.jtyjy.finance.manager.trade.DistributedNumber;
import com.jtyjy.finance.manager.vo.individual.IndividualEmployeeFilesVO;
import com.jtyjy.finance.manager.vo.individual.IndividualTicketVO;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.locale.converters.DateLocaleConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author User
 * @description 针对表【budget_individual_employee_ticket_receipt_info(员工个体户收票信息维护档案)】的数据库操作Service实现
 * @createDate 2022-08-25 13:28:19
 */
@Service
public class IndividualEmployeeTicketReceiptInfoServiceImpl extends ServiceImpl<IndividualEmployeeTicketReceiptInfoMapper, IndividualEmployeeTicketReceiptInfo>
        implements IndividualEmployeeTicketReceiptInfoService {

    private final IndividualEmployeeFilesService employeeFilesService;
    private final IndividualEmployeeTicketReceiptService mainService;
    private final DistributedNumber distributedNumber;
    public IndividualEmployeeTicketReceiptInfoServiceImpl(IndividualEmployeeFilesService employeeFilesService, IndividualEmployeeTicketReceiptService mainService, DistributedNumber distributedNumber) {
        this.employeeFilesService = employeeFilesService;
        this.mainService = mainService;
        this.distributedNumber = distributedNumber;
    }

    @Override
    public IPage<IndividualTicketVO> selectPage(IndividualTicketQuery query) {
        return this.baseMapper.selectTicketPage(new Page<>(query.getPageNum(), query.getPageSize()), query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTicket(IndividualTicketDTO dto) {
        List<IndividualTicketDetailsDTO> detailsDTOList = dto.getDetailsDTOList();
        List<IndividualEmployeeTicketReceiptInfo> infoList = new ArrayList<>();
        IndividualEmployeeTicketReceipt receipt = new IndividualEmployeeTicketReceipt();
        BigDecimal invoiceAmount = receipt.getInvoiceAmount() != null ? receipt.getInvoiceAmount() : BigDecimal.ZERO;

        //SP+年月日+4位流水号
        //SP 2022 09 06  10

        receipt.setTicketCode(generateWaterCode());

        receipt.setEmployeeJobNum(dto.getEmployeeJobNum());
        receipt.setIndividualEmployeeInfoId(dto.getIndividualEmployeeInfoId());
        receipt.setIndividualName(dto.getIndividualName());
        receipt.setRemarks(dto.getRemarks());
        receipt.setCreateTime(new Date());
        receipt.setCreateBy(UserThreadLocal.getEmpNo());
        receipt.setUpdateTime(new Date());
        receipt.setUpdateBy(UserThreadLocal.getEmpNo());

        for (int i = 0; i < detailsDTOList.size(); i++) {
//            Integer count = this.lambdaQuery()
//                    .eq(IndividualEmployeeTicketReceiptInfo::getIndividualEmployeeInfoId, dto.getIndividualEmployeeInfoId())
//                    .eq(IndividualEmployeeTicketReceiptInfo::getYear, detailsDTOList.get(i).getYear())
//                    .eq(IndividualEmployeeTicketReceiptInfo::getMonth, detailsDTOList.get(i).getMonth()).count();
//            if (count>0) {
//                throw new BusinessException("该档案下,存在重复的同年同月的记录。");
//            }
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
            invoiceAmount = invoiceAmount.add(singleDTO.getInvoiceAmount());
        }
        receipt.setInvoiceAmount(invoiceAmount);
        mainService.save(receipt);
        infoList.forEach(x -> x.setTicketId(receipt.getId()));
        this.saveBatch(infoList);
    }

    private String generateWaterCode() {
       return distributedNumber.getInvoiceNum();
//        DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");
//        String yearMd = LocalDate.now().format(yyyyMMdd);
//        List<String> ticketCodeList = mainService.getAllCodes();
//        String waterCode = "0001";
//        if (CollectionUtils.isNotEmpty(ticketCodeList)) {
//            Optional<Integer> max = ticketCodeList.stream().filter(x -> x.contains("SP" + yearMd)).map(x -> Integer.valueOf(x.substring(10))).max(Comparator.comparing(Integer::new));
//            DecimalFormat g1 = new DecimalFormat("0000");
//            if (max.isPresent()){
//                waterCode = g1.format(max.get() + 1);
//            }else {
//                waterCode = g1.format( 1);
//            }
//
//
//        }
//        String ticketCode = MessageFormat.format("SP{0}{1}", yearMd, waterCode);
//        return ticketCode;
    }

    @Override
    public void updateTicket(IndividualTicketDTO dto) {
        List<IndividualTicketDetailsDTO> detailsDTOList = dto.getDetailsDTOList();

        IndividualEmployeeTicketReceipt mainReceipt = mainService.getById(dto.getTicketId());
        BigDecimal invoiceAmount = mainReceipt.getInvoiceAmount();
        List<IndividualEmployeeTicketReceiptInfo> infoList = new ArrayList<>();
        for (int i = 0; i < detailsDTOList.size(); i++) {
            Long id = detailsDTOList.get(i).getId();
            IndividualEmployeeTicketReceiptInfo info = new IndividualEmployeeTicketReceiptInfo();
            if(id != null){
                info = this.getById(id);
            }
            IndividualTicketDetailsDTO singleDTO = detailsDTOList.get(i);
            info.setYear(singleDTO.getYear());
            info.setMonth(singleDTO.getMonth());
            info.setInvoiceAmount(singleDTO.getInvoiceAmount());
            info.setTicketId(dto.getTicketId());

            info.setEmployeeJobNum(dto.getEmployeeJobNum());
            info.setIndividualEmployeeInfoId(dto.getIndividualEmployeeInfoId());
            info.setIndividualName(dto.getIndividualName());

            info.setRemarks(detailsDTOList.get(i).getRemarks());
            info.setUpdateTime(new Date());
            info.setUpdateBy(UserThreadLocal.getEmpNo());
            infoList.add(info);
//            this.saveOrUpdate(info);
            invoiceAmount = invoiceAmount.add(info.getInvoiceAmount());
        }
        mainReceipt.setInvoiceAmount(invoiceAmount);
        mainService.updateById(mainReceipt);
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

                        Map<String, List<IndividualTicketImportDTO>> listMap = dataList.stream().collect(Collectors.groupingBy(x -> x.getEmployeeJobNum() + "-" + x.getIndividualName()));
                        for (String key : listMap.keySet()) {

                            String individualName = key.split("-")[1];
                            Integer employeeJobNum = Integer.valueOf(key.split("-")[0]);
                            Optional<IndividualEmployeeFiles> individualEmployeeFiles = employeeFilesService.lambdaQuery().like(IndividualEmployeeFiles::getAccountName, individualName).last("limit 1").oneOpt();
                            if (individualEmployeeFiles.isPresent()) {
                                IndividualEmployeeFiles file = individualEmployeeFiles.get();

                                IndividualEmployeeTicketReceipt receipt = new IndividualEmployeeTicketReceipt();
                                BigDecimal invoiceAmount = receipt.getInvoiceAmount() != null ? receipt.getInvoiceAmount() : BigDecimal.ZERO;
                                //SP+年月日+4位流水号
                                //SP 2022 09 06  10
                                receipt.setTicketCode(generateWaterCode());
                                receipt.setInvoiceAmount(invoiceAmount);

                                receipt.setIndividualEmployeeInfoId(file.getId());
                                receipt.setIndividualName(file.getAccountName());
                                receipt.setEmployeeJobNum(file.getEmployeeJobNum());

                                receipt.setCreateTime(new Date());
                                receipt.setCreateBy(UserThreadLocal.getEmpNo());
                                receipt.setUpdateTime(new Date());
                                receipt.setUpdateBy(UserThreadLocal.getEmpNo());

                                mainService.save(receipt);
                                List<IndividualTicketImportDTO> importDTOList = listMap.get(key);
                                for (IndividualTicketImportDTO dto : importDTOList) {
                                    try {
                                        IndividualEmployeeTicketReceiptInfo info = IndividualTicketConverter.INSTANCE.importDTOToEntity(dto);

                                        info.setIndividualEmployeeInfoId(file.getId());
                                        info.setIndividualName(file.getAccountName());
                                        info.setEmployeeJobNum(file.getEmployeeJobNum());

                                        info.setTicketId(receipt.getId());
                                        info.setCreateTime(new Date());
                                        info.setCreateBy(UserThreadLocal.getEmpNo());
                                        info.setUpdateTime(new Date());
                                        info.setUpdateBy(UserThreadLocal.getEmpNo());
                                        this.save(info);
                                    } catch (Exception e) {
                                        IndividualTicketImportErrorDTO errorDTO = new IndividualTicketImportErrorDTO();
                                        try {
                                            PropertyUtils.copyProperties(errorDTO, dto);
                                        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                                            ex.printStackTrace();
                                        }
                                        errorDTO.setInsertDatabaseError(e.getCause().getMessage());
                                        errorDTOList.add(errorDTO);
                                    }
                                }
                            }
                        }
                    }, errorMap)).sheet().doRead();

            for (Map map : errorMap) {
                IndividualTicketImportErrorDTO errorDTO = new IndividualTicketImportErrorDTO();
                try {
                    ConvertUtils.register(new DateLocaleConverter(), Date.class);//BeanUtils.populate对日期类型进行处理，否则无法封装
                    BeanUtils.populate(errorDTO, map);
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

    @Override
    public IndividualTicketInfoDTO getIndividualInfo(String ticketId) {
        IndividualEmployeeTicketReceipt ticketReceipt = mainService.getById(ticketId);
        IndividualEmployeeFiles files = employeeFilesService.getById(ticketReceipt.getIndividualEmployeeInfoId());

        IndividualTicketInfoDTO dto = new IndividualTicketInfoDTO();

        IndividualEmployeeFilesVO individualEmployeeFilesVO = IndividualEmployeeFilesConverter.INSTANCE.toVO(files);
        dto.setFilesVO(individualEmployeeFilesVO);
        dto.setTicketId(ticketReceipt.getId());
        dto.setTicketCode(ticketReceipt.getTicketCode());
        List<IndividualEmployeeTicketReceiptInfo> list = this.lambdaQuery().eq(IndividualEmployeeTicketReceiptInfo::getTicketId, ticketId).list();
        List<IndividualTicketDetailsDTO> individualTicketDetailsDTOS = IndividualTicketConverter.INSTANCE.toDetailDTOList(list);

        dto.setDetailsDTOList(individualTicketDetailsDTOS);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delTicket(List<Long> ids) {
        for (Long id : ids) {
            mainService.removeById(id);
            this.remove(new LambdaQueryWrapper<IndividualEmployeeTicketReceiptInfo>().eq(IndividualEmployeeTicketReceiptInfo::getTicketId,id));
        }

    }
}




