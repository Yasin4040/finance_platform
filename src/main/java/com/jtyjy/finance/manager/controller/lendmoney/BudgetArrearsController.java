package com.jtyjy.finance.manager.controller.lendmoney;

import com.alibaba.fastjson.JSON;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.bean.BudgetArrears;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.service.BudgetArrearsService;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.utils.ResponseUtil;
import com.jtyjy.finance.manager.vo.ArrearsDetailsVO;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.List;

/**
 * @author User
 */
@Api(tags = {"借款管理-员工台账"})
@RestController
@CrossOrigin
@RequestMapping("/api/arrears")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetArrearsController extends BaseController<BudgetArrears> {

    @Value("${file.shareDir}")
    private String fileShareDir;

    @Value("${redis.file.key.expiretime}")
    private Integer expireTime;

    private final RedisClient redisClient;
    private final BudgetArrearsService budgetArrearsService;

    @ApiOperation(value = "查询员工台账（分页）", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "员工姓名/工号", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "付款状态", name = "repaymentStatus", dataType = "Integer"),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/listArrearsPage")
    public ResponseEntity<PageResult<BudgetArrears>> listArrearsPage(String name,
                                                                     Integer repaymentStatus,
                                                                     @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                     @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        return ResponseEntity.ok(this.budgetArrearsService.listArrearsPage(page, rows, name, repaymentStatus));
    }

    @ApiOperation(value = "修改逾期记录及不良征信", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键Id", name = "id", dataType = "String", required = true),
            @ApiImplicitParam(value = "逾期记录", name = "overdueRecords", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "不良征信记录", name = "badCredit", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/editEmpCredit")
    public ResponseEntity<List<ArrearsDetailsVO>> editEmpCredit(@RequestParam Long id,
                                                                @RequestParam Integer overdueRecords,
                                                                @RequestParam Integer badCredit) {
        this.budgetArrearsService.editEmpCredit(id, overdueRecords, badCredit);
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "查询员工台账明细", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "员工工号", name = "empNo", dataType = "String", required = true),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/getArrearsDetails")
    public ResponseEntity<PageResult<ArrearsDetailsVO>> getArrearsDetails(@RequestParam String empNo,
                                                                          @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                          @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        return ResponseEntity.ok(this.budgetArrearsService.getArrearsDetails(page, rows, empNo));
    }

    @ApiOperation(value = "逾期及征信导入模板下载", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/exportCreditTemplate")
    public void exportCreditTemplate(HttpServletResponse response) throws Exception {
        // 文件导出
        ResponseUtil.exportCreditExcelFile(null, EasyExcelUtil.getOutputStream("导入逾期及征信模板", response));
    }

    @ApiOperation(value = "逾期及征信导入", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("/importEmpCredit")
    public ResponseEntity<String> importEmpCredit(@RequestParam("file") MultipartFile srcFile, HttpSession session) throws Exception {
        List<List<String>> excelDataList = ResponseUtil.getSingleExcelContent(srcFile);

        List<List<String>> errorDataList = this.budgetArrearsService.importEmpCredit(excelDataList);
        if (!errorDataList.isEmpty()) {
            String empNo = UserThreadLocal.get().getUserName();
            String errorFileName = this.fileShareDir + File.separator + empNo + "_逾期及征信导入错误明细_" + System.currentTimeMillis() + ".json";

            // 创建错误明细文件
            FileUtils.writeStringToFile(new File(errorFileName), JSON.toJSONString(errorDataList), "UTF-8");

            // 存入Redis键值记录, 并设置过期时间
            this.redisClient.set(empNo + "_empCreditErrorData", errorFileName, this.expireTime);
            return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT, "文件导入有错误,请点击此处下载!");
        }
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "逾期及征信导入错误明细", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("/exportErrors")
    public void exportErrors(HttpServletResponse response) throws Exception {
        String redisKey = UserThreadLocal.get().getUserName() + "_empCreditErrorData";
        String redisValue = this.redisClient.get(redisKey);
        if (redisValue == null) {
            throw new RuntimeException("逾期及征信导入错误明细不存在或已删除");
        }
        File file = new File(redisValue);
        String errorData = FileUtils.readFileToString(file, "UTF-8");
        List<List<String>> errorList = JSON.parseObject(errorData, List.class);

        // 文件导出
        ResponseUtil.exportCreditExcelFile(errorList, EasyExcelUtil.getOutputStream("逾期及征信导入错误明细", response));

        // 删除文件
        FileUtils.forceDeleteOnExit(file);
        this.redisClient.delete(redisKey);
    }

}
