package com.jtyjy.finance.manager.controller.budgetorganization;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.jtyjy.core.auth.anno.ApiDataAuthAnno;
import com.jtyjy.core.local.JdbcSqlThreadLocal;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.bean.BudgetMonthAgent;
import com.jtyjy.finance.manager.bean.BudgetMonthAgentaddinfo;
import com.jtyjy.finance.manager.bean.BudgetSubject;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.dto.MonthAgentAddInfoDTO;
import com.jtyjy.finance.manager.easyexcel.MonthAgentAddInfoExcelData;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.service.BudgetMonthAgentaddinfoService;
import com.jtyjy.finance.manager.service.FineOAService;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.vo.BudgetMonthAddInfoVO;
import com.jtyjy.finance.manager.vo.BudgetMonthAgentAddVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author User
 */
@Api(tags = {"预算编制-月度预算-月度追加"})
@RestController
@CrossOrigin
@RequestMapping("/api/monthAgentAdd")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetMonthAgentAddInfoController extends BaseController<BudgetMonthAgentaddinfo> {

    private final BudgetMonthAgentaddinfoService budgetMonthAgentaddinfoService;
    private final FineOAService oaService;

    @ApiOperation(value = "查询月度追加（分页）", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "届别Id", name = "yearId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "预算单位名称", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "追加单号", name = "yearAddCode", dataType = "String"),
            @ApiImplicitParam(value = "审核状态", name = "requestStatus", dataType = "String"),
            @ApiImplicitParam(value = "月份", name = "monthId", dataType = "Long"),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @ApiDataAuthAnno
    @GetMapping(value = "/monthAgentAddInfoPage")
    public ResponseEntity<PageResult<BudgetMonthAddInfoVO>> monthAgentAddInfoPage(Long yearId,
                                                                                  String name,
                                                                                  String monthAddCode,
                                                                                  String requestStatus,
                                                                                  Long monthId,
                                                                                  @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                                  @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        HashMap<String, Object> paramMap = getQueryConditions(yearId, name, monthAddCode, requestStatus, monthId);

        return ResponseEntity.ok(this.budgetMonthAgentaddinfoService.monthAgentAddInfoPage(page, rows, paramMap));
    }

    private HashMap<String, Object> getQueryConditions(Long yearId, String name, String monthAddCode, String requestStatus, Long monthId) {
        HashMap<String, Object> paramMap = new HashMap<>(5);
        paramMap.put("yearId", yearId);
        paramMap.put("name", name);
        paramMap.put("monthAddCode", monthAddCode);
        paramMap.put("requestStatus", StringUtils.isNotBlank(requestStatus) ? Long.parseLong(requestStatus) : null);
        paramMap.put("monthId", monthId);
        paramMap.put("userId", UserThreadLocal.get().getUserId());
        paramMap.put("authSql", JdbcSqlThreadLocal.get());
        return paramMap;
    }

    @ApiOperation(value = "获取月度可追加科目", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位Id", name = "budgetUnitId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "月份Id", name = "monthId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/listCanAddSubjects")
    public ResponseEntity<List<BudgetSubject>> listCanAddSubjects(@RequestParam(value = "budgetUnitId") Long budgetUnitId,
                                                                  @RequestParam(value = "monthId") Long monthId) {
        return ResponseEntity.ok(this.budgetMonthAgentaddinfoService.listCanAddSubjects(budgetUnitId, monthId));
    }

    @ApiOperation(value = "获取月度可追加动因", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位Id", name = "budgetUnitId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "预算科目Id", name = "budgetSubjectId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "月份Id", name = "monthId", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/listCanAddAgents")
    public ResponseEntity<List<BudgetMonthAgent>> getCanAddAgentList(@RequestParam(value = "budgetUnitId") Long budgetUnitId,
                                                                     @RequestParam(value = "budgetSubjectId") Long budgetSubjectId,
                                                                     @RequestParam(value = "monthId") Long monthId) {
        return ResponseEntity.ok(this.budgetMonthAgentaddinfoService.listCanAddAgents(budgetUnitId, budgetSubjectId, monthId));
    }

    @ApiOperation(value = "查询单个月度追加动因列表", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键Id", name = "infoId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/listAddAgentByInfoId")
    public ResponseEntity<List<BudgetMonthAgentAddVO>> listAddAgentByInfoId(@RequestParam Long infoId) {
        return ResponseEntity.ok(this.budgetMonthAgentaddinfoService.listAddAgentByInfoId(infoId));
    }

    @ApiOperation(value = "获取月度动因可追加信息", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "月度动因Id", name = "yearAgentId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/getMonthAgentInfo")
    public ResponseEntity<BudgetMonthAgentAddVO> getMonthAgentInfo(@RequestParam Long monthAgentId) {
        return ResponseEntity.ok(this.budgetMonthAgentaddinfoService.getMonthAgentInfo(monthAgentId));
    }

    @ApiOperation(value = "月度预算追加", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "月度预算追加", name = "bean", dataType = "MonthAgentAddInfoDTO", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/monthAgentAddMoney")
    public ResponseEntity<String> monthAgentAddMoney(@RequestBody MonthAgentAddInfoDTO bean) throws Exception {
        List<Map<String,Object>> list = oaService.getSpecialPerson();
        this.budgetMonthAgentaddinfoService.monthAgentAddMoney(bean,list);
        return ResponseEntity.ok("月度预算追加成功");
    }

    @ApiOperation(value = "提交审核", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键id", name = "infoId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/submitVerify")
    public ResponseEntity<String> submitVerify(Long infoId) throws Exception {
        List<Map<String,Object>> list = oaService.getSpecialPerson();
        this.budgetMonthAgentaddinfoService.submitVerify(infoId, false,list);
        return ResponseEntity.ok("月度预算追加成功");
    }

    @ApiOperation(value = "删除月度预算追加", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键ids", name = "ids", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/delete")
    public ResponseEntity<String> delete(@RequestBody List<Long> ids) {
        this.budgetMonthAgentaddinfoService.deleteMonthAgentAdd(ids);
        return ResponseEntity.ok("月度预算追加成功");
    }

    @ApiOperation(value = "月度预算追加导出", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "届别Id", name = "yearId", dataType = "Long"),
            @ApiImplicitParam(value = "预算单位名称", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "追加单号", name = "yearAddCode", dataType = "String"),
            @ApiImplicitParam(value = "审核状态", name = "requestStatus", dataType = "String"),
            @ApiImplicitParam(value = "月份", name = "monthId", dataType = "Long"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @ApiDataAuthAnno
    @GetMapping(value = "/exportAgentMonthAdd")
    public void exportAgentMonthAdd(Long yearId,
                                    String name,
                                    String monthAddCode,
                                    String requestStatus,
                                    Long monthId,
                                    HttpServletResponse response) throws Exception {
        HashMap<String, Object> paramMap = getQueryConditions(yearId, name, monthAddCode, requestStatus, monthId);

        List<MonthAgentAddInfoExcelData> excelDataList = this.budgetMonthAgentaddinfoService.exportAgentMonthAdd(paramMap);

        BigDecimal totalMoney = BigDecimal.ZERO;
        for (MonthAgentAddInfoExcelData v : excelDataList) {
            totalMoney = totalMoney.add(v.getTotal());
        }
        HashMap<String, Object> headMap = new HashMap<>(2);
        headMap.put("exportTime", Constants.FORMAT_10.format(new Date()));
        headMap.put("totalMoney", totalMoney);

        try (InputStream inputStream = EasyExcelUtil.getTemplateInputStream("monthAgentAddInfoTemplate.xlsx")) {
            FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
            ExcelWriter workBook = EasyExcelUtil.getExcelWriter(response, "月度追加明细表", inputStream, MonthAgentAddInfoExcelData.class);
            WriteSheet sheet = EasyExcel.writerSheet(0).build();
            sheet.setSheetName("月度追加明细");
            workBook.fill(headMap, sheet);
            workBook.fill(excelDataList, fillConfig, sheet);
            workBook.finish();
        }
    }

}
