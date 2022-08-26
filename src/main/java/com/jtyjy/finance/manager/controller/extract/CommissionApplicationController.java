package com.jtyjy.finance.manager.controller.extract;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.vo.application.CommissionApplicationInfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description: 支付申请单
 * Created by ZiYao Lee on 2022/08/26.
 * Time: 14:36
 */
@Api(tags = {"提成支付申请单"})
@RestController
@RequestMapping("/api/commissionApplication")
public class CommissionApplicationController {
    /**
     * 提成支付申请单
     */
    @ApiOperation(value = "提成支付申请单", httpMethod = "GET")
    @ApiImplicitParam(value = "提成单号", name = "extractSumId", dataType = "String", required = true)
    @GetMapping("/getApplicationInfo")
    public ResponseEntity<PageResult<CommissionApplicationInfoVO>> getApplicationInfo(Integer extractSumId) throws Exception {
        Page<CommissionApplicationInfoVO> page = new Page<>();
//        Commission
        return ResponseEntity.ok(PageResult.apply(page.getTotal(), page.getRecords()));
    }
}
