package com.jtyjy.finance.manager.controller.commercial;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.query.commission.CommissionQuery;
import com.jtyjy.finance.manager.service.CommissionApplicationDetailsService;
import com.jtyjy.finance.manager.vo.application.CommissionImportDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description:
 * Created by ZiYao Lee on 2022/09/16.
 * Time: 10:17
 */@Api(tags = { "商务提成接口" })
@RestController
@RequestMapping("/api/commercial")
public class CommissionController {

     private final CommissionApplicationDetailsService detailsService;

    public CommissionController(CommissionApplicationDetailsService detailsService) {
        this.detailsService = detailsService;
    }
    //商务提成组接口
    /**
     * 根据不同登陆用户 获取相应提成数据
     */
    @ApiOperation(value = "根据不同登陆用户 获取相应提成数据", httpMethod = "GET")
    @GetMapping("/selectCommissionPage")
    public ResponseEntity<PageResult<CommissionImportDetailVO>> selectCommissionPage(@ModelAttribute CommissionQuery query) throws Exception {
        IPage<CommissionImportDetailVO> page = detailsService.selectCommissionPage(query);
        return ResponseEntity.ok(PageResult.apply(page.getTotal(), page.getRecords()));
    }
}
