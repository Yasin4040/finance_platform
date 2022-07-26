package com.jtyjy.finance.manager.controller.base;

import com.jtyjy.finance.manager.bean.BudgetAgentExecuteView;
import com.jtyjy.finance.manager.bean.BudgetProject;
import com.jtyjy.finance.manager.bean.BudgetUnit;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.service.BudgetProjectService;
import com.jtyjy.finance.manager.service.BudgetUnitService;
import com.jtyjy.finance.manager.vo.BudgetUnitSubjectVO;
import com.jtyjy.finance.manager.vo.BudgetUnitVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.core.result.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shubo
 */
@Api(tags = { "项目信息管理接口" })
@RestController
@RequestMapping("/api/base/budgetProject")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetProjectController extends BaseController<BudgetAgentExecuteView> {	
	
    
    private final BudgetProjectService service;

	/**
     * 新增/修改（修改时需送id）
     */
    @ApiOperation(value = "新增/修改（修改时需送id）", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("addOrUpdate")
    public ResponseEntity<String> addOrUpdate(@Valid @RequestBody BudgetProject bean, BindingResult bindingResult) {
        String retError = this.getResult(bindingResult);
        if (StringUtils.isNotBlank(retError)) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL, retError, null);
        }     
        if (null == bean.getId() || 0 == bean.getId().intValue()) {
            this.service.save(bean);
            return ResponseEntity.ok();
        }else {
            this.service.updateById(bean);
            return ResponseEntity.ok();
        }
        
        
    }

    /**
     * 查询项目信息
     */
    @ApiOperation(value = "查询项目信息", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "届别id", name = "yearId", dataType = "Integer"),
            @ApiImplicitParam(value = "项目编号（模糊查询）", name = "projectno", dataType = "String"),
            @ApiImplicitParam(value = "项目类型：1项目预领 2项目借支 3个人借支", name = "type", dataType = "Integer"),
            @ApiImplicitParam(value = "确认状态1：是 0:否", name = "confirmflag", dataType = "Integer"),
            @ApiImplicitParam(value = "项目名称（模糊查询）", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "预算单位id", name = "unitId", dataType = "Long"),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("query")
    public ResponseEntity<Page<BudgetProject>> query(
            @RequestParam(value = "yearId", required = true) Integer yearId,
            @RequestParam(value = "projectno", required = false) String projectno,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "confirmflag", required = false) Integer confirmflag,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "unitId", required = false) Long unitId,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "rows", required = false, defaultValue = "20") Integer rows) throws Exception {
        Map<String, Object> conditionMap = new HashMap<String, Object>();
        conditionMap.put("yearId", yearId);
        conditionMap.put("projectno", projectno);
        conditionMap.put("type", type);
        conditionMap.put("confirmflag", confirmflag);
        conditionMap.put("name", name);
        conditionMap.put("unitId", unitId);
        Page<BudgetProject> voList = this.service.queryByYear(conditionMap, page, rows);
        return ResponseEntity.ok(voList);
    }
    
    
    /**
     * 按照ID查询
     */
    @ApiOperation(value = "按照主键查询", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键", name = "id", dataType = "Serializable", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("getById")
    public ResponseResult getById(Serializable id) {
        return ResponseResult.ok(this.service.getById(id));
    }
}
