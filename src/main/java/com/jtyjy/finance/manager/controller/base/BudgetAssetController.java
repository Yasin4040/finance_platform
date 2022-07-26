package com.jtyjy.finance.manager.controller.base;

import com.jtyjy.core.auth.anno.ApiDataAuthAnno;
import com.jtyjy.core.auth.anno.NoLoginAnno;
import com.jtyjy.core.local.JdbcSqlThreadLocal;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.bean.BudgetAgentExecuteView;
import com.jtyjy.finance.manager.bean.BudgetMonthAgent;
import com.jtyjy.finance.manager.bean.BudgetSubject;
import com.jtyjy.finance.manager.bean.BudgetUnit;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.service.BudgetSubjectService;
import com.jtyjy.finance.manager.service.BudgetUnitService;
import com.jtyjy.finance.manager.vo.BudgetSubjectAgentVO;
import com.jtyjy.finance.manager.vo.BudgetSubjectVO;
import com.jtyjy.finance.manager.vo.BudgetUnitVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ldw
 */
@Api(tags = { "固定资产--预算单位管理接口" })
@RestController
@RequestMapping("/api/asset")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetAssetController extends BaseController<BudgetAgentExecuteView> {

    private final BudgetUnitService unitService;


    private final BudgetSubjectService subjectService;


    @ApiOperation(value = "查询预算单位", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "届别id", name = "yearId", dataType = "int")
    })
    @NoLoginAnno
    @GetMapping("getUnitInfo")
    public ResponseEntity<List<Map<String,Object>>> infoNoAuth(@RequestParam(value = "yearId", required = true) Long yearId) throws Exception {
        List<Map<String,Object>> voList = this.unitService.getBudgetUnitForAsset(yearId);
        return ResponseEntity.ok(voList);
    }

    @ApiOperation(value = "根据预算单位查询科目", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "届别Id", name = "yearId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "预算单位Id", name = "unitId", dataType = "Long"),
            @ApiImplicitParam(value = "月Id", name = "monthId", dataType = "Long")
    })
    @NoLoginAnno
    @GetMapping(value = "/getSubjectInfo")
    public ResponseEntity<List<BudgetSubject>> getSubjectInfo(@RequestParam(value = "monthId", required = true)Long monthId,
                                                              @RequestParam(value = "unitId", required = true)Long unitId,
                                                              @RequestParam(value = "yearId", required = true)Long yearId){

        HashMap<String, Object> paramMap = new HashMap<>(10);
        paramMap.put("yearId", yearId);
        paramMap.put("unitId", unitId);
        paramMap.put("monthId", monthId);
        return ResponseEntity.ok(this.subjectService.getSubjectInfoForAsset(paramMap));
    }

    @ApiOperation(value = "查询月度动因", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "届别Id", name = "yearId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "预算单位Id", name = "unitId", dataType = "Long"),
            @ApiImplicitParam(value = "月Id", name = "monthId", dataType = "Long"),
            @ApiImplicitParam(value = "科目", name = "subjectId", dataType = "Long")
    })
    @NoLoginAnno
    @GetMapping("getMonthAgentInfo")
    public ResponseEntity<List<BudgetMonthAgent>> getMonthAgentInfo(@RequestParam(value = "monthId", required = true)Long monthId,
                                                                    @RequestParam(value = "unitId", required = true)Long unitId,
                                                                    @RequestParam(value = "yearId", required = true)Long yearId,
                                                                    @RequestParam(value = "subjectId", required = true)Long subjectId) throws Exception {
        HashMap<String, Object> paramMap = new HashMap<>(10);
        paramMap.put("yearId", yearId);
        paramMap.put("unitId", unitId);
        paramMap.put("monthId", monthId);
        paramMap.put("subjectId", subjectId);
        List<BudgetMonthAgent> voList = this.unitService.getMonthAgentInfo(paramMap);
        return ResponseEntity.ok(voList);
    }


}
