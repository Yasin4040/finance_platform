package com.jtyjy.finance.manager.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jtyjy.finance.manager.bean.BudgetExtractImportdetail;
import com.jtyjy.finance.manager.bean.WbDept;
import com.jtyjy.finance.manager.bean.WbPerson;
import com.jtyjy.finance.manager.bean.WbUser;
import com.jtyjy.finance.manager.cache.DeptCache;
import com.jtyjy.finance.manager.cache.PersonCache;
import com.jtyjy.finance.manager.enmus.RoleNameEnum;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.BudgetExtractImportdetailMapper;
import com.jtyjy.finance.manager.mapper.BudgetYearPeriodMapper;
import com.jtyjy.finance.manager.query.commission.CommissionQuery;
import com.jtyjy.finance.manager.query.commission.UpdateViewRequest;
import com.jtyjy.finance.manager.service.CommissionApplicationDetailsService;
import com.jtyjy.finance.manager.vo.application.CommissionImportDetailPowerVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description:
 * Created by ZiYao Lee on 2022/09/16.
 * Time: 11:15
 */
@Service
public class CommissionApplicationDetailsServiceImpl extends ServiceImpl<BudgetExtractImportdetailMapper, BudgetExtractImportdetail>
        implements CommissionApplicationDetailsService {
    private final BudgetYearPeriodMapper yearPeriodMapper;

    public CommissionApplicationDetailsServiceImpl(BudgetYearPeriodMapper yearPeriodMapper) {
        this.yearPeriodMapper = yearPeriodMapper;
    }

    @Override
    public IPage<CommissionImportDetailPowerVO> selectCommissionPage(CommissionQuery query) {
        IPage<CommissionImportDetailPowerVO> page = new Page<>();
        String deptId="";
//        String empNo="";
        List<String> empNoList = new ArrayList<>();
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
        if(mainRole==null){
            return page;
        }
        switch (mainRole){
            case COMMERCIAL_COMMISSION:
                if (StringUtils.isNotBlank(query.getDepartmentName())) {
                    //查询的完整部门code名称
                    List<String> deptParentIds = DeptCache.getAllDept().stream().filter(x -> x.getDeptFullname().contains(query.getDepartmentName())).map(x -> x.getParentIds()).collect(Collectors.toList());
                    Set<String> allDeptIds = new HashSet<>();
                    for (String deptParentId : deptParentIds) {
                        Set<String> tempTempIds   = DeptCache.getAllDept().stream().filter(x -> x.getParentIds().contains(deptParentId)).map(WbDept::getDeptId).collect(Collectors.toSet());
                        allDeptIds.addAll(tempTempIds);
                    }
                    empNoList = PersonCache.EMP_NO_USER_MAP.values().stream().filter(x -> allDeptIds.contains(x.getDeptId())).map(WbPerson::getPersonCode).collect(Collectors.toList());
                }
                page  = this.baseMapper.selectCommissionPageForCommercialCommission(new Page<>(query.getPage(),query.getRows())
                        ,query.getEmployeeName(),query.getDepartmentName(),query.getYearId(),query.getMonthId(),query.getExtractMonth(),query.getBudgetUnitName(),empNoList);
                break;
            case BIG_MANAGER:
                //当前工号的 大区下的工号。
                empNoList = getViewEmpNoByLoginUser(loginUser.getUserName());
                page  = this.baseMapper.selectCommissionPageForBigManager(new Page<>(query.getPage(),query.getRows())
                        ,query.getEmployeeName(),query.getDepartmentName(),query.getYearId(),query.getMonthId(),query.getExtractMonth(),query.getBudgetUnitName(),empNoList);

                break;
            case MANAGER:
                //自己工号
                empNoList.add(loginUser.getUserName());
                page  = this.baseMapper.selectCommissionPageForManager(new Page<>(query.getPage(),query.getRows())
                        ,query.getEmployeeName(),query.getDepartmentName(),query.getYearId(),query.getMonthId(),query.getExtractMonth(),query.getBudgetUnitName(),empNoList);
                break;
            default:
                break;
        }
        List<CommissionImportDetailPowerVO> records = page.getRecords();
        for (CommissionImportDetailPowerVO record : records) {
            WbDept dept = getDeptByEmpNo(record.getEmpno());
            if(dept!=null) {
                record.setEmpDeptFullName(dept.getDeptFullname());
            }
            record.setYearName(yearPeriodMapper.getNameById(Long.valueOf(record.getMainYearId())));
            record.setMonthName(Integer.parseInt(record.getExtractMonth().substring(4, 6)) + "月");
            record.setIfBigManagerView(!record.getIfBigManager().equals(-1)?"允许":"关闭");
            record.setIfManagerView(!record.getIfManager().equals(-1)?"允许":"关闭");
        }
        return page;
    }

    @Override
    public List<String> getViewEmpNoByLoginUser(String empNo) {
        WbDept dept = getDeptByEmpNo(empNo);
        //004IH0DMOLX94-004IH0DMOLX9Y-
        String parentFullId = dept.getParentIds();
        List<String> allDeptIds = DeptCache.getAllDept().stream().filter(x -> x.getParentIds().contains(parentFullId)).map(WbDept::getDeptId).collect(Collectors.toList());
        List<String> empNoList = PersonCache.EMP_NO_USER_MAP.values().stream().filter(x -> allDeptIds.contains(x.getDeptId())).map(WbPerson::getPersonCode).collect(Collectors.toList());
        return empNoList;
    }

    private WbDept getDeptByEmpNo(String empNo) {
        WbPerson person = PersonCache.getPersonByEmpNo(empNo);
        if(person==null){
            return null;
        }
        //获取员工部门，及下级部门 。所有的工号。
        WbDept dept = DeptCache.getByDeptId(person.getDeptId());
        return dept;
    }

    @Override
    public void updateView(UpdateViewRequest request) {
        List<BudgetExtractImportdetail> list = this.lambdaQuery().in(BudgetExtractImportdetail::getId, request.getIds()).list();
        Boolean ifBig = request.getIfBig();
        if(ifBig){
            if(request.getIfAllow()){
                list.forEach(x->x.setIfBigManager(1));
            }else{
                list.forEach(x->x.setIfBigManager(-1));
            }
        }else{
            if(request.getIfAllow()){
                list.forEach(x->x.setIfManager(1));
            }else{
                list.forEach(x->x.setIfManager(-1));
            }
        }
        for (BudgetExtractImportdetail entity : list) {
            entity.setUpdateBy(UserThreadLocal.getEmpNo());
            entity.setUpdateBy(UserThreadLocal.getEmpName());
        }
        this.saveOrUpdateBatch(list);
    }
}
