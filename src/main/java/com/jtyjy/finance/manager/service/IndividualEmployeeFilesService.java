package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.IndividualEmployeeFiles;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jtyjy.finance.manager.query.IndividualFilesQuery;
import com.jtyjy.finance.manager.vo.IndividualEmployeeFilesVO;

/**
* @author User
* @description 针对表【budget_individual_employee_files(员工个体户档案)】的数据库操作Service
* @createDate 2022-08-25 13:28:19
*/
public interface IndividualEmployeeFilesService extends IService<IndividualEmployeeFiles> {

    Page<IndividualEmployeeFilesVO> selectPage(IndividualFilesQuery query);
}
