package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BusinessPayCollection;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.finance.manager.vo.application.CommissionImportDetailVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author User
* @description 针对表【budget_business_pay_collection(商务导入 回款明细表)】的数据库操作Mapper
* @createDate 2022-09-17 16:14:04
* @Entity com.jtyjy.finance.manager.bean.BusinessPayCollection
*/
public interface BusinessPayCollectionMapper extends BaseMapper<BusinessPayCollection> {

    IPage<BusinessPayCollection> selectPageForCommercialCommission(Page<Object> objectPage, @Param("employeeName") String employeeName, @Param("departmentName") String departmentName
            , @Param("yearId") String yearId, @Param("monthId") String monthId, @Param("extractMonth") String extractMonth);
    IPage<BusinessPayCollection> selectPageForManager(Page<Object> objectPage,@Param("employeeName") String employeeName,@Param("departmentName") String departmentName
            ,@Param("yearId") String yearId,@Param("monthId") String monthId,@Param("extractMonth") String extractMonth,@Param("empNo") String empNo);
    IPage<BusinessPayCollection> selectPageForBigManager(Page<Object> objectPage,@Param("employeeName") String employeeName,@Param("departmentName") String departmentName
            ,@Param("yearId") String yearId,@Param("monthId") String monthId,@Param("extractMonth") String extractMonth,@Param("deptIdList") List<String> deptIdList);

}




