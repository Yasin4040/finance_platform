package com.jtyjy.finance.manager.controller.budgetorganization;

import com.alibaba.fastjson.JSON;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.bean.BudgetYearAgent;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.dto.UpdateBudgetYearAgentDTO;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.service.BudgetYearAgentService;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.utils.ResponseUtil;
import com.jtyjy.finance.manager.vo.BudgetSubjectVO;
import com.jtyjy.finance.manager.vo.BudgetYearAgentVO;
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
import javax.validation.Valid;
import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author User
 */
@Api(tags = {"预算编制-年度预算-年度动因、产品、分解"})
@RestController
@CrossOrigin
@RequestMapping("/api/yearAgent")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetYearAgentController extends BaseController<BudgetYearAgent> {

    @Value("${file.shareDir}")
    private String fileShareDir;

    @Value("${redis.file.key.expiretime}")
    private Integer expireTime;

    private final RedisClient redisClient;
    private final BudgetYearAgentService budgetYearAgentService;

    @ApiOperation(value = "年度动因科目", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "届别Id", name = "yearId", dataType = "String", required = true),
            @ApiImplicitParam(value = "基础单位Id", name = "baseUnitId", dataType = "String", required = true),
            @ApiImplicitParam(value = "类型（1普通 2产品 3分解）", name = "type", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/subjectList")
    public ResponseEntity<List<BudgetSubjectVO>> subjectList(@RequestParam(value = "yearId") Long yearId,
                                                             @RequestParam(value = "baseUnitId") Long baseUnitId,
                                                             @RequestParam(value = "type") Integer type) {
        return ResponseEntity.ok(this.budgetYearAgentService.listSubject(yearId, baseUnitId, type));
    }

    @ApiOperation(value = "查询年度动因（分页）", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位Id", name = "budgetUnitId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "预算科目Id", name = "budgetSubjectId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "动因名称", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "产品分类", name = "category", dataType = "String"),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/yearAgentPage")
    public ResponseEntity<PageResult<BudgetYearAgentVO>> yearAgentPage(@RequestParam(value = "budgetUnitId") Long budgetUnitId,
                                                                       @RequestParam(value = "budgetSubjectId") Long budgetSubjectId,
                                                                       @RequestParam(value = "name", required = false) String name,
                                                                       @RequestParam(value = "category", required = false) String category,
                                                                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                       @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        return ResponseEntity.ok(this.budgetYearAgentService.yearAgentPage(budgetUnitId, budgetSubjectId, name, page, rows,category));
    }

    @ApiOperation(value = "新增年度动因", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/addYearAgent")
    public ResponseEntity<String> addYearAgent(@Valid @RequestBody UpdateBudgetYearAgentDTO bean) {
        bean.setMonthMoney();
        this.budgetYearAgentService.addYearAgent(bean);
        return ResponseEntity.ok("新增年度动因成功");
    }

    @ApiOperation(value = "修改年度动因", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/updateYearAgent")
    public ResponseEntity<String> updateYearAgent(@Valid @RequestBody UpdateBudgetYearAgentDTO bean) {
        bean.setMonthMoney();
        this.budgetYearAgentService.updateYearAgent(bean);
        return ResponseEntity.ok("修改年度动因成功");
    }

    @ApiOperation(value = "设置弹性动因", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "年度动因Id", name = "yearAgentId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "是否弹性", name = "elasticFlag", dataType = "Boolean", required = true),
            @ApiImplicitParam(value = "报销上限", name = "elasticMax", dataType = "BigDecimal", required = true),
            @ApiImplicitParam(value = "上浮比例", name = "elasticRatio", dataType = "BigDecimal", required = true),
            @ApiImplicitParam(value = "占比科目", name = "subjectId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/setElastic")
    public ResponseEntity<String> setElastic(@RequestParam(value = "yearAgentId") Long yearAgentId,
                                             @RequestParam(value = "elasticFlag") Boolean elasticFlag,
                                             @RequestParam(value = "elasticMax") BigDecimal elasticMax,
                                             @RequestParam(value = "elasticRatio") BigDecimal elasticRatio,
                                             @RequestParam(value = "subjectId") Long subjectId) {
        if (elasticRatio != null) {
            elasticRatio = elasticRatio.divide(new BigDecimal("100"));
        }
        this.budgetYearAgentService.setElastic(yearAgentId, elasticFlag, elasticMax, elasticRatio, subjectId);
        return ResponseEntity.ok("设置弹性动因成功");
    }

    @ApiOperation(value = "删除年度动因", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "年度动因Ids", name = "yearAgentIds", dataType = "List", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/deleteYearAgent")
    public ResponseEntity<String> deleteYearAgent(@RequestBody List<Long> yearAgentIds) {
        if (yearAgentIds == null || yearAgentIds.isEmpty()) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL, "年度动因Ids不能为空");
        }
        this.budgetYearAgentService.deleteYearAgent(yearAgentIds);
        return ResponseEntity.ok("删除年度动因成功");
    }

    // 年度动因（普通、产品、分解）导入、导出 ----------------------------------------------------------------------------------------------------

    @ApiOperation(value = "年度动因导入模板下载", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位Id", name = "budgetUnitId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "下载类型（1普通 2产品 3分解）", name = "type", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/exportYearAgent")
    public void exportYearAgent(@RequestParam("budgetUnitId") Long budgetUnitId,
                                @RequestParam("type") Integer type,
                                HttpServletResponse response) throws Exception {
        // 查询该预算单位下所有的年度动因
        Map<String, List<BudgetYearAgent>> templateMap = this.budgetYearAgentService.exportYearAgent(budgetUnitId, type);

        // 文件导出
        ResponseUtil.exportYearAgentExcelFile(templateMap, type, EasyExcelUtil.getOutputStream(this.getYearAgentFileName(type) + "导入模板", response));
    }

    @ApiOperation(value = "年度动因导入", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位Id", name = "budgetUnitId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "文件", name = "file", dataType = "File", required = true),
            @ApiImplicitParam(value = "导入类型（1普通 2产品 3分解）", name = "type", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("/importYearAgent")
    public ResponseEntity<String> importYearAgent(@RequestParam("budgetUnitId") Long budgetUnitId,
                                                  @RequestParam("file") MultipartFile srcFile,
                                                  @RequestParam("type") Integer type) throws Exception {
        Map<String, List<List<String>>> errorDataMap = new LinkedHashMap<>();
        Map<String, List<List<String>>> excelDataMap = ResponseUtil.getMultipleExcelContent(srcFile);

        this.budgetYearAgentService.importYearAgentExcel(budgetUnitId, type, excelDataMap, errorDataMap);
        if (!errorDataMap.isEmpty()) {
            String empNo = UserThreadLocal.get().getUserName();
            String errorFileName = this.fileShareDir + File.separator + empNo + "_" + this.getYearAgentFileName(type)
                    + "错误明细_" + System.currentTimeMillis() + ".json";

            // 创建错误明细文件
            FileUtils.writeStringToFile(new File(errorFileName), JSON.toJSONString(errorDataMap), "UTF-8");

            // 存入Redis键值记录, 并设置过期时间
            this.redisClient.set(empNo + "_yearAgentErrorData", errorFileName, this.expireTime);
            return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT, "文件导入有错误,请点击此处下载!");
        }
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "年度动因文件导入错误明细", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "导入类型（1普通 2产品 3分解）", name = "type", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("/exportErrors")
    public void exportErrors(@RequestParam("type") Integer type, HttpServletResponse response) throws Exception {
        String redisKey = UserThreadLocal.get().getUserName() + "_yearAgentErrorData";
        String redisValue = this.redisClient.get(redisKey);
        if (redisValue == null) {
            throw new RuntimeException(this.getYearAgentFileName(type) + "错误明细不存在或已删除");
        }
        File file = new File(redisValue);
        String errorData = FileUtils.readFileToString(file, "UTF-8");
        HashMap<String, List<List<String>>> errorMap = JSON.parseObject(errorData, HashMap.class);

        // 文件导出
        ResponseUtil.exportYearAgentExcelFile(errorMap, type, EasyExcelUtil.getOutputStream(this.getYearAgentFileName(type) + "导入错误明细", response));

        // 删除文件
        FileUtils.forceDeleteOnExit(file);
        this.redisClient.delete(redisKey);
    }

    private String getYearAgentFileName(Integer type) {
        String name = "年度动因";
        switch (type) {
            case 2:
                name = "年度产品";
                break;
            case 3:
                name = "年度分解";
                break;
            default:
        }
        return name;
    }

    @ApiOperation(value = "追加年度动因", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/appendYearAgent")
    public ResponseEntity<String> appendYearAgent(@Valid @RequestBody UpdateBudgetYearAgentDTO bean) {
        bean.setMonthMoney();
        this.budgetYearAgentService.appendYearAgent(bean);
        return ResponseEntity.ok("新增年度动因成功");
    }

}
