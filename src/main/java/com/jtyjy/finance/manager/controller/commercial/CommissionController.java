package com.jtyjy.finance.manager.controller.commercial;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.bean.IndividualEmployeeFiles;
import com.jtyjy.finance.manager.dto.individual.*;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.query.commission.CommissionQuery;
import com.jtyjy.finance.manager.query.commission.UpdateViewRequest;
import com.jtyjy.finance.manager.query.individual.IndividualFilesQuery;
import com.jtyjy.finance.manager.service.CommissionApplicationDetailsService;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.vo.application.CommissionImportDetailPowerVO;
import com.jtyjy.finance.manager.vo.application.CommissionImportDetailVO;
import com.jtyjy.finance.manager.vo.individual.IndividualEmployeeFilesVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.jtyjy.finance.manager.constants.Constants.IMPORT_INDIVIDUAL_FILE;

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
