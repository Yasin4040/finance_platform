package com.jtyjy.finance.manager.controller.lendmoney;

import com.alibaba.fastjson.JSON;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.bean.BudgetProjectlendsum;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.service.BudgetOtherlendsumService;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.utils.ResponseUtil;
import com.jtyjy.finance.manager.vo.BudgetLendMoneyVO;
import com.jtyjy.finance.manager.vo.BudgetOtherLendSumVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.List;

/**
 * @author User
 */
@Api(tags = {"借款管理-其他借款"})
@RestController
@CrossOrigin
@RequestMapping("/api/otherLend")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetOtherLendController extends BaseController<BudgetProjectlendsum> {

    @Value("${file.shareDir}")
    private String fileShareDir;

    @Value("${redis.file.key.expiretime}")
    private Integer expireTime;

    private final RedisClient redisClient;
    private final BudgetOtherlendsumService budgetOtherlendsumService;

    @ApiOperation(value = "查询其它借款（分页）", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "批次号名称", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "审核状态 0未审核 1已审核", name = "status", dataType = "Integer"),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/listOtherLendPage")
    public ResponseEntity<PageResult<BudgetOtherLendSumVO>> listOtherLendPage(String name,
                                                                              String status,
                                                                              @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                              @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        return ResponseEntity.ok(this.budgetOtherlendsumService.listOtherLendPage(page, rows, name, status));
    }

    @ApiOperation(value = "审核", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/verify")
    public ResponseEntity<String> verify(Long id) throws Exception {
        this.budgetOtherlendsumService.verify(id);
        return ResponseEntity.ok("审核成功");
    }

    @ApiOperation(value = "下载其它借款导入模板", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/exportOtherLendTemplate")
    public void exportOtherLendTemplate(HttpServletResponse response) throws Exception {
        // 文件导出
        ResponseUtil.exportOtherLendExcelFile(null, EasyExcelUtil.getOutputStream("其它借款导入模板", response));
    }

    @ApiOperation(value = "其它借款导入", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("/importOtherLend")
    public ResponseEntity<String> importOtherLend(@RequestParam("file") MultipartFile srcFile,
                                                  HttpSession session) throws Exception {
        List<List<String>> excelDataList = ResponseUtil.getSingleExcelContent(srcFile);

        List<List<String>> errorDataList = this.budgetOtherlendsumService.importOtherLend(excelDataList);
        if (!errorDataList.isEmpty()) {
            String empNo = UserThreadLocal.get().getUserName();
            String errorFileName = this.fileShareDir + File.separator + empNo + "_其它借款导入错误明细_" + System.currentTimeMillis() + ".json";

            // 创建错误明细文件
            FileUtils.writeStringToFile(new File(errorFileName), JSON.toJSONString(errorDataList), "UTF-8");

            // 存入Redis键值记录, 并设置过期时间
            this.redisClient.set(empNo + "_otherLendErrorData", errorFileName, this.expireTime);
            return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT, "文件导入有错误,请点击此处下载!");
        }
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "其它借款导入错误明细", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("/exportErrors")
    public void exportErrors(HttpServletResponse response) throws Exception {
        String redisKey = UserThreadLocal.get().getUserName() + "_otherLendErrorData";
        String redisValue = this.redisClient.get(redisKey);
        if (redisValue == null) {
            throw new RuntimeException("其它借款导入错误明细不存在或已删除");
        }
        File file = new File(redisValue);
        String errorData = FileUtils.readFileToString(file, "UTF-8");
        List<List<String>> errorList = JSON.parseObject(errorData, List.class);

        // 文件导出
        ResponseUtil.exportOtherLendExcelFile(errorList, EasyExcelUtil.getOutputStream("其它借款导入错误明细", response));

        // 删除文件
        FileUtils.forceDeleteOnExit(file);
        this.redisClient.delete(redisKey);
    }

    @ApiOperation(value = "借款明细", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键Id", name = "Id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/listLendDetail")
    public ResponseEntity<PageResult<BudgetLendMoneyVO>> listLendDetail(Long id,
                                                                        @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                        @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        return ResponseEntity.ok(this.budgetOtherlendsumService.listLendDetail(page, rows, id));
    }

    @ApiOperation(value = "删除借款", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键Id", name = "ids", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/deleteLendMoney")
    public ResponseEntity<String> deleteLendMoney(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL);
        }
        this.budgetOtherlendsumService.deleteLendMoney(ids);
        return ResponseEntity.ok("删除借款成功");
    }

}
