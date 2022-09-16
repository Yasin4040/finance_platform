package com.jtyjy.finance.manager.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jtyjy.finance.manager.bean.BudgetExtractImportdetail;
import com.jtyjy.finance.manager.bean.WbUser;
import com.jtyjy.finance.manager.enmus.ViewStatusEnum;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.BudgetExtractImportdetailMapper;
import com.jtyjy.finance.manager.query.commission.CommissionQuery;
import com.jtyjy.finance.manager.service.CommissionApplicationDetailsService;
import com.jtyjy.finance.manager.vo.application.CommissionImportDetailVO;
import org.springframework.stereotype.Service;

/**
 * Description:
 * Created by ZiYao Lee on 2022/09/16.
 * Time: 11:15
 */
@Service
public class CommissionApplicationDetailsServiceImpl extends ServiceImpl<BudgetExtractImportdetailMapper, BudgetExtractImportdetail>
        implements CommissionApplicationDetailsService {
    @Override
    public IPage<CommissionImportDetailVO> selectCommissionPage(CommissionQuery query) {
        String deptId="";
        String empNo="";
        //假设当前用户为业务经理。1，只看工号。 empNo。
        //假设当前用户为业务经理 2，看部门下，deptId。获取部门。
        //假设当前用户为商务组

        WbUser loginUser = UserThreadLocal.get();
        //部门id
        if(2==2) {
            empNo = loginUser.getUserName();
        }else if(1==1) {
            deptId = loginUser.getUserId();
        }else{
            //other
        }
        IPage<CommissionImportDetailVO> page = this.baseMapper.selectCommissionPage(new Page<>(query.getPage(),query.getRows())
                ,query.getEmployeeName(),query.getDepartmentName(),query.getYearId(),query.getMonthId(),query.getExtractMonth(),empNo,deptId);

        BudgetExtractImportdetail budgetExtractImportdetail = new BudgetExtractImportdetail();
        ViewStatusEnum viewStatusEnum;

        return null;
    }
}
