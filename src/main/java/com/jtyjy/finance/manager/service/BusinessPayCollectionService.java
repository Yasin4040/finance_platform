package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BusinessPayCollection;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jtyjy.finance.manager.dto.commission.BusinessPayCollectionErrorDTO;
import com.jtyjy.finance.manager.query.commission.CommissionQuery;
import com.jtyjy.finance.manager.query.commission.UpdateViewRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
* @author User
* @description 针对表【budget_business_pay_collection(商务导入 回款明细表)】的数据库操作Service
* @createDate 2022-09-17 16:14:04
*/
public interface BusinessPayCollectionService extends IService<BusinessPayCollection> {

    IPage<BusinessPayCollection> selectPage(CommissionQuery query);

    List<BusinessPayCollectionErrorDTO> importCollection(MultipartFile multipartFile);

    List<BusinessPayCollection> exportCollection(CommissionQuery query);

    void updateView(UpdateViewRequest request);
}
