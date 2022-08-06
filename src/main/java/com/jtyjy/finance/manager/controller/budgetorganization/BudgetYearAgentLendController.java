package com.jtyjy.finance.manager.controller.budgetorganization;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.jtyjy.core.auth.anno.ApiDataAuthAnno;
import com.jtyjy.core.local.JdbcSqlThreadLocal;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.bean.BudgetSubject;
import com.jtyjy.finance.manager.bean.BudgetYearAgent;
import com.jtyjy.finance.manager.bean.BudgetYearAgentlend;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.dto.YearAgentLendDTO;
import com.jtyjy.finance.manager.easyexcel.YearAgentLendExcelData;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.service.BudgetUnitService;
import com.jtyjy.finance.manager.service.BudgetYearAgentlendService;
import com.jtyjy.finance.manager.service.FineOAService;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.vo.BudgetYearAgentLendVO;
import com.jtyjy.finance.manager.vo.YearAgentLendVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author User
 */
@Api(tags = {"预算编制-年度预算-预算拆借"})
@RestController
@CrossOrigin
@RequestMapping("/api/yearAgentLend")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetYearAgentLendController extends BaseController<BudgetYearAgentlend> {

    private final BudgetUnitService budgetUnitService;
    private final BudgetYearAgentlendService budgetYearAgentlendService;
    private final FineOAService oaService;

    @GetMapping(value = "/a")
    public void a(String empno){
        List<Map<String,Object>> list = oaService.getSpecialPerson();
        Map<String, Object> map = list.stream().filter(e -> empno.equals(e.get("EMPNO").toString())).findFirst().orElse(null);
        if(map!=null && map.get("USERMSGS")!=null){
            System.out.println(map.get("USERMSGS").toString());
        }
    }

    @ApiOperation(value = "查询预算拆借列表（分页）", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "届别Id", name = "yearId", dataType = "Long"),
            @ApiImplicitParam(value = "预算单位名称", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "是否跨部门", name = "isAcross", dataType = "Boolean", required = true),
            @ApiImplicitParam(value = "追加单号", name = "orderNumber", dataType = "String"),
            @ApiImplicitParam(value = "审核状态", name = "requestStatus", dataType = "String"),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @ApiDataAuthAnno
    @GetMapping(value = "/listYearAgentLendPage")
    public ResponseEntity<PageResult<BudgetYearAgentLendVO>> listYearAgentLendPage(Long yearId,
                                                                                   String name,
                                                                                   String orderNumber,
                                                                                   String requestStatus,
                                                                                   @RequestParam(value = "isAcross", defaultValue = "false") Boolean isAcross,
                                                                                   @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                                   @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        HashMap<String, Object> paramMap = getQueryConditions(yearId, name, isAcross, orderNumber, requestStatus);

        return ResponseEntity.ok(this.budgetYearAgentlendService.listYearAgentLendPage(page, rows, paramMap));
    }

    private HashMap<String, Object> getQueryConditions(Long yearId, String name, Boolean isAcross, String orderNumber, String requestStatus) {
        HashMap<String, Object> paramMap = new HashMap<>(5);
        paramMap.put("yearId", yearId);
        paramMap.put("isAcross", isAcross);
        paramMap.put("name", name);
        paramMap.put("orderNumber", orderNumber);
        paramMap.put("requestStatus", StringUtils.isNotBlank(requestStatus) ? Long.parseLong(requestStatus) : null);
        paramMap.put("userId", UserThreadLocal.get().getUserId());

        String authSql = JdbcSqlThreadLocal.get();
        if (StringUtils.isNotBlank(authSql)) {
            List<String> baseUnitIds = this.budgetUnitService.getBaseUnitIdListByAuthCenter(authSql);
            if (!baseUnitIds.isEmpty()) {
                paramMap.put("baseUnitIds", String.join(",", baseUnitIds));
            }
        }
        return paramMap;
    }

    @ApiOperation(value = "获取可拆借预算科目", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位Id", name = "budgetUnitId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/listLendSubject")
    public ResponseEntity<List<BudgetSubject>> listLendSubject(@RequestParam Long budgetUnitId) {
        return ResponseEntity.ok(this.budgetYearAgentlendService.listLendSubject(budgetUnitId));
    }

    @ApiOperation(value = "获取可拆借预算动因", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位Id", name = "budgetUnitId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "预算科目Id", name = "budgetSubjectId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/listLendAgent")
    public ResponseEntity<List<BudgetYearAgent>> listLendAgent(@RequestParam Long budgetUnitId, @RequestParam Long budgetSubjectId) {
        return ResponseEntity.ok(this.budgetYearAgentlendService.listLendAgent(budgetUnitId, budgetSubjectId));
    }



    @ApiOperation(value = "详情", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "拆借Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/getYearLendDetail")
    public ResponseEntity<YearAgentLendVO> getYearLendDetail(@RequestParam Long id) {
        YearAgentLendVO result = this.budgetYearAgentlendService.getYearLendDetail(id);
        return ResponseEntity.ok(result);
    }


    @ApiOperation(value = "新增预算拆借", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "Object", name = "bean", dataType = "YearAgentLendDTO", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/addYearAgentLend")
    public ResponseEntity<String> addYearAgentLend(@Validated @RequestBody YearAgentLendDTO bean) throws Exception {
        List<Map<String,Object>> list = oaService.getSpecialPerson();
        this.budgetYearAgentlendService.saveYearAgentLend(bean,list);
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "修改预算拆借", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "Object", name = "bean", dataType = "YearAgentLendDTO", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/updateYearAgentLend")
    public ResponseEntity<String> updateYearAgentLend(@Valid @RequestBody YearAgentLendDTO bean) throws Exception {
        List<Map<String,Object>> list = oaService.getSpecialPerson();
        this.budgetYearAgentlendService.saveYearAgentLend(bean,list);
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "提交至OA系统", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/submitLend")
    public ResponseEntity<String> submitLend(@RequestParam Long id) throws Exception {
        List<Map<String,Object>> list = oaService.getSpecialPerson();
        this.budgetYearAgentlendService.commitYearAgentLend(id, false,list);
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "删除预算拆借", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算Ids", name = "ids", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/delete")
    public ResponseEntity<String> delete(@RequestBody List<Long> ids) {
        this.budgetYearAgentlendService.deleteYearAgentLend(ids);
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "年度预算拆借导出", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "届别Id", name = "yearId", dataType = "Long"),
            @ApiImplicitParam(value = "预算单位名称", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "是否跨部门", name = "isAcross", dataType = "Boolean", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @ApiDataAuthAnno
    @GetMapping(value = "/exportAgentYearLend")
    public void exportAgentYearLend(Long yearId,
                                    String name,
                                    String orderNumber,
                                    String requestStatus,
                                    @RequestParam(value = "isAcross", defaultValue = "false") Boolean isAcross,
                                    HttpServletResponse response) throws Exception {
        HashMap<String, Object> paramMap = getQueryConditions(yearId, name, isAcross, orderNumber, requestStatus);

        List<YearAgentLendExcelData> excelDataList = this.budgetYearAgentlendService.exportAgentYearLend(paramMap);

        HashMap<String, Object> headMap = new HashMap<>(2);
        headMap.put("exportTime", Constants.FORMAT_10.format(new Date()));

        String templateName = "yearAgentLendTemplate.xlsx";
        if (isAcross) {
            templateName = "yearAgentLendAcrossTemplate.xlsx";
        }
        try (InputStream inputStream = EasyExcelUtil.getTemplateInputStream(templateName)) {
            ExcelWriter workBook = EasyExcelUtil.getExcelWriter(response, "年度预算拆借明细表", inputStream, YearAgentLendExcelData.class);
            WriteSheet sheet = EasyExcel.writerSheet(0).build();
            sheet.setSheetName("年度预算拆借明细");
            workBook.fill(headMap, sheet);
            workBook.fill(excelDataList, sheet);
            workBook.finish();
        }
    }


}
