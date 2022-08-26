package com.jtyjy.finance.manager.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jtyjy.finance.manager.bean.IndividualEmployeeFiles;
import com.jtyjy.finance.manager.query.IndividualFilesQuery;
import com.jtyjy.finance.manager.service.IndividualEmployeeFilesService;
import com.jtyjy.finance.manager.mapper.IndividualEmployeeFilesMapper;
import com.jtyjy.finance.manager.vo.IndividualEmployeeFilesVO;
import org.springframework.stereotype.Service;

/**
* @author User
* @description 针对表【budget_individual_employee_files(员工个体户档案)】的数据库操作Service实现
* @createDate 2022-08-25 13:28:19
*/
@Service
public class IndividualEmployeeFilesServiceImpl extends ServiceImpl<IndividualEmployeeFilesMapper, IndividualEmployeeFiles>
    implements IndividualEmployeeFilesService{

    @Override
    public Page<IndividualEmployeeFilesVO> selectPage(IndividualFilesQuery query) {
        return null;
    }
}




