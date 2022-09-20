package com.jtyjy.finance.manager.controller.commercial;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.query.commission.CommissionQuery;
import com.jtyjy.finance.manager.query.commission.UpdateViewRequest;
import com.jtyjy.finance.manager.service.CommissionApplicationDetailsService;
import com.jtyjy.finance.manager.vo.application.CommissionImportDetailPowerVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

/**
 * Description:
 * Created by ZiYao Lee on 2022/09/16.
 * Time: 10:17
 */@Api(tags = { "商务提成明细接口" })
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
    public ResponseEntity<PageResult<CommissionImportDetailPowerVO>> selectCommissionPage(@ModelAttribute CommissionQuery query) throws Exception {
        IPage<CommissionImportDetailPowerVO> page = detailsService.selectCommissionPage(query);
        return ResponseEntity.ok(PageResult.apply(page.getTotal(), page.getRecords()));
    }

    /********************************回款**********/

    //商务提成组接口
    /**
     * 批量 修改是否允许
     */
    @ApiOperation(value = "批量 修改是否允许", httpMethod = "POST")
    @PostMapping("/updateView")
    public ResponseEntity updateView(@RequestBody UpdateViewRequest request) throws Exception {
        try {
            detailsService.updateView(request);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.error("修改失败");
        }
        return ResponseEntity.ok();
    }

}
