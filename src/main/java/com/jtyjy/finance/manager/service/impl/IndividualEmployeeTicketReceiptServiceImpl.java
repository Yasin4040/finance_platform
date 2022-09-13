package com.jtyjy.finance.manager.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jtyjy.finance.manager.bean.IndividualEmployeeTicketReceipt;
import com.jtyjy.finance.manager.bean.WbDept;
import com.jtyjy.finance.manager.cache.DeptCache;
import com.jtyjy.finance.manager.query.individual.IndividualTicketQuery;
import com.jtyjy.finance.manager.service.IndividualEmployeeTicketReceiptService;
import com.jtyjy.finance.manager.mapper.IndividualEmployeeTicketReceiptMapper;
import com.jtyjy.finance.manager.vo.individual.IndividualTicketPageVO;
import com.jtyjy.finance.manager.vo.individual.IndividualTicketVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
* @author User
* @description 针对表【budget_individual_employee_ticket_receipt(员工个体户收票信息主表 维护档案)】的数据库操作Service实现
* @createDate 2022-09-06 16:44:38
*/
@Service
public class IndividualEmployeeTicketReceiptServiceImpl extends ServiceImpl<IndividualEmployeeTicketReceiptMapper, IndividualEmployeeTicketReceipt>
    implements IndividualEmployeeTicketReceiptService{

    @Override
    public IPage<IndividualTicketPageVO> selectPage(IndividualTicketQuery query) {
//        IPage<Map> mapIPage = this.baseMapper.selectTicketPageMap(new Page<>(1,10));
        IPage<IndividualTicketPageVO> individualTicketPageVOIPage = this.baseMapper.selectTicketPage(new Page<>(query.getPageNum(), query.getPageSize()), query);
        List<IndividualTicketPageVO> records = individualTicketPageVOIPage.getRecords();
        for (IndividualTicketPageVO record : records) {
            if(StringUtils.isNotBlank( record.getDepartmentNo())){
                WbDept dept = DeptCache.getByDeptId(record.getDepartmentNo());
                if(dept!=null){
                    record.setDepartmentName(dept.getDeptFullname());
                }
            }
        }
//        departmentName  部门一级二级
        return individualTicketPageVOIPage;
    }

    @Override
    public List<String> getAllCodes() {
        return this.baseMapper.getAllCodes();
    }
}




