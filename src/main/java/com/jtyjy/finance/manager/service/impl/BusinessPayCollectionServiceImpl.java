package com.jtyjy.finance.manager.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.cache.DeptCache;
import com.jtyjy.finance.manager.cache.PersonCache;
import com.jtyjy.finance.manager.dto.commission.BusinessPayCollectionErrorDTO;
import com.jtyjy.finance.manager.enmus.RoleNameEnum;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.listener.easyexcel.PageReadListener;
import com.jtyjy.finance.manager.mapper.BudgetYearPeriodMapper;
import com.jtyjy.finance.manager.query.commission.CommissionQuery;
import com.jtyjy.finance.manager.service.BusinessPayCollectionService;
import com.jtyjy.finance.manager.mapper.BusinessPayCollectionMapper;
import com.jtyjy.finance.manager.utils.BeanMapUtil;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
* @author User
* @description 针对表【budget_business_pay_collection(商务导入 回款明细表)】的数据库操作Service实现
* @createDate 2022-09-17 16:14:04
*/
@Service
public class BusinessPayCollectionServiceImpl extends ServiceImpl<BusinessPayCollectionMapper, BusinessPayCollection>
    implements BusinessPayCollectionService{

    private final BudgetYearPeriodMapper yearMapper;

    public BusinessPayCollectionServiceImpl(BudgetYearPeriodMapper yearMapper) {
        this.yearMapper = yearMapper;
    }

    @Override
    public IPage<BusinessPayCollection> selectPage(CommissionQuery query) {
        IPage<BusinessPayCollection> page = new Page<>();
        String deptId="";
        String empNo="";
        //假设当前用户为业务经理。1，只看工号。 empNo。
        //假设当前用户为业务经理 2，看部门下，deptId。获取部门。
        //假设当前用户为商务组

        WbUser loginUser = UserThreadLocal.get();
        //部门id
        RoleNameEnum mainRole = null;
        List<String> roleNameList = loginUser.getRoleNameList();
        //有商务就是商务，其次就是大区经理，最后就是业务经理
        if (roleNameList.contains(RoleNameEnum.COMMERCIAL_COMMISSION.getValue())) {
            mainRole = RoleNameEnum.COMMERCIAL_COMMISSION;
        }else if(roleNameList.contains(RoleNameEnum.BIG_MANAGER.getValue())){
            mainRole = RoleNameEnum.BIG_MANAGER;
        }else if(roleNameList.contains(RoleNameEnum.MANAGER.getValue())){
            mainRole = RoleNameEnum.MANAGER;
        }
        //赋值year
        if (StringUtils.isNotBlank(query.getYearId())) {
            query.setYearId(yearMapper.getNameById(Long.valueOf(query.getYearId())));
        }
        if (StringUtils.isNotBlank(query.getMonthId())) {
            query.setMonthId(query.getMonthId()+"月");
        }
        switch (mainRole){
            case COMMERCIAL_COMMISSION:
                page  = this.baseMapper.selectPageForCommercialCommission(new Page<>(query.getPage(),query.getRows())
                        ,query.getEmployeeName(),query.getDepartmentName(),query.getYearId(),query.getMonthId(),query.getExtractMonth(),empNo,deptId);
                break;
            case BIG_MANAGER:
                deptId = loginUser.getUserId();
                page  = this.baseMapper.selectPageForBigManager(new Page<>(query.getPage(),query.getRows())
                        ,query.getEmployeeName(),query.getDepartmentName(),query.getYearId(),query.getMonthId(),query.getExtractMonth(),empNo,deptId);

                break;
            case MANAGER:
                empNo = loginUser.getUserName();
                page  = this.baseMapper.selectPageForManager(new Page<>(query.getPage(),query.getRows())
                        ,query.getEmployeeName(),query.getDepartmentName(),query.getYearId(),query.getMonthId(),query.getExtractMonth(),empNo,deptId);
                break;
            default:
                break;
        }
        List<BusinessPayCollection> records = page.getRecords();
        for (BusinessPayCollection record : records) {
            record.setIfBigManagerView(record.getIfBigManager().equals(-1)?"关闭":"允许");
            record.setIfManagerView(record.getIfManager().equals(-1)?"关闭":"允许");
        }
        return page;
    }

    @Override
    public List<BusinessPayCollectionErrorDTO> importCollection(MultipartFile multipartFile) {
        List<BusinessPayCollectionErrorDTO> errList = new ArrayList<>();
        List<Map> errorMap = new ArrayList<>();
        try {
            EasyExcel.read(multipartFile.getInputStream(), BusinessPayCollection.class,
                    new PageReadListener<BusinessPayCollection>(dataList -> {

                        for (BusinessPayCollection entity : dataList) {
                            try {
                                String collectionEmpNo = entity.getEmpNo();
                                WbPerson thisPerson = PersonCache.getPersonByEmpNo(collectionEmpNo);
                                if(thisPerson==null){
                                    throw new RuntimeException("工号不存在");
                                }
                                //放置完整单位名称
                                WbDept byDeptId = DeptCache.getByDeptId(thisPerson.getDeptId());
                                if(byDeptId == null){
                                    throw new RuntimeException("员工部门不存在");
                                }
                                entity.setDeptId(byDeptId.getDeptId());
                                entity.setDeptFullName(byDeptId.getDeptFullname());
                                entity.setCreateTime(new Date());
                                entity.setCreateBy(UserThreadLocal.get().getUserName());
                                entity.setUpdateTime(new Date());
                                entity.setUpdateBy(UserThreadLocal.get().getUserName());

//                                entity.setIfManager(entity.getIfManagerView());

                                try {
                                    this.save(entity);
                                } catch (Exception e) {
//                                    throw new DuplicateKeyException("工号:"+employeeJobNum+"户名:"+entity.getAccountName()+"已存在");
                                }
                            } catch (DuplicateKeyException e){
                                throw e;
                            } catch (RuntimeException e) {
                                BusinessPayCollectionErrorDTO errorDTO = new BusinessPayCollectionErrorDTO();
                                try {
                                    PropertyUtils.copyProperties(errorDTO,entity);
                                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                                    ex.printStackTrace();
                                }
                                errorDTO.setInsertDatabaseError(e.getMessage()==null? e.toString():e.getMessage());
                                errList.add(errorDTO);
                            }
                        }
                        System.out.println(dataList);
                    }, errorMap)).sheet().doRead();

            for (Map map : errorMap) {
                BusinessPayCollectionErrorDTO errorDTO = new BusinessPayCollectionErrorDTO();
                SimpleDateFormat sf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                if (map.get("createTime")!=null) {
                    Date socialSecurityStopDate=sf.parse((String)map.get("createTime"));
                    map.put("createTime", socialSecurityStopDate);
                }
                if (map.get("updateTime")!=null) {
                    Date leaveDate=sf.parse((String)map.get("updateTime"));
                    map.put("updateTime", leaveDate);
                }
                errorDTO = BeanMapUtil.mapToBean(map,BusinessPayCollectionErrorDTO.class);
                errList.add(errorDTO);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return errList;
    }

    @Override
    public List<BusinessPayCollection> exportCollection(CommissionQuery query) {
        query.setPage(1);
        query.setRows(-1);
        IPage<BusinessPayCollection> page = selectPage(query);
        List<BusinessPayCollection> list = page.getRecords();
//        List<IndividualExportDTO> dtoList = IndividualEmployeeFilesConverter.INSTANCE.entityToExportDTOList(list);
//        for (BusinessPayCollection dto : list) {
//            dto.seti(dto.getAccountType().equals("1")?"个卡":"公户");
//            dto.setSelfOrAgency(dto.getAccountType().equals("1")?"自办":"代办");
//            if (dto.getDepositBank()!=null) {
//                WbBanks bank = BankCache.getBankByBranchCode(dto.getDepositBank());
//                if (bank!=null) {
//                    dto.setProvince(bank.getProvince());
//                    dto.setCity(bank.getCity());
//                    dto.setBankType(bank.getBankName());
//                    dto.setDepositBank(bank.getSubBranchName());
//                    dto.setElectronicInterBankNo(bank.getSubBranchCode());
//                }
//            }
//            //部门名称
//            if(StringUtils.isNotBlank( dto.getDepartmentNo())){
//                WbDept dept = DeptCache.getByDeptId(dto.getDepartmentNo());
//                if(dept!=null){
//                    dto.setDepartmentName(dept.getDeptFullname());
//                }
//            }
//            dto.setIssuedUnit(UnitCache.get(dto.getIssuedUnit())!=null?
//                    UnitCache.get(dto.getIssuedUnit()).getName():dto.getIssuedUnit());
//        }
        return list;
    }
}




