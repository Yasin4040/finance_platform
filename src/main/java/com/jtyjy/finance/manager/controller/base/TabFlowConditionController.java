package com.jtyjy.finance.manager.controller.base;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.core.result.ResponseResult;
import com.jtyjy.finance.manager.bean.TabFlowCondition;
import com.jtyjy.finance.manager.bean.TabProcedure;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.service.TabFlowConditionService;
import com.jtyjy.finance.manager.service.TabProcedureService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;

/**
 * @author Admin
 */
@Api(tags = { "流程前置条件管理" })
@RestController
@CrossOrigin
@RequestMapping("/api/tabFlowCondition")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TabFlowConditionController extends BaseController<TabFlowCondition> {

	private final TabFlowConditionService service;
    private final TabProcedureService procedureService;
	
	/**
     * 新增
	 * @throws Exception 
     */
    @ApiOperation(value = "新增/修改（修改时需送id）", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键id（修改必送）", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "流程类型：1：报销  2：其他", name = "flowType", dataType = "String", required = true),
            @ApiImplicitParam(value = "环节代码", name = "stepDm", dataType = "String", required = true),
            @ApiImplicitParam(value = "前置条件环节代码", name = "conditionStepDm", dataType = "String", required = true),
            @ApiImplicitParam(value = "流程模板id", name = "theVersion", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "条件 报销条件（1：已接收 2：审核通过）", name = "theCondition", dataType = "String", required = true),        
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("addOrUpdate")
    public ResponseEntity addOrUpdate(@Valid TabFlowCondition bean, BindingResult bindingResult) throws Exception {
        String retError = this.getResult(bindingResult);
        if (StringUtils.isNotBlank(retError)) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL, retError);
        }
        if(null == bean.getId() || 0 == bean.getId().intValue()) {
            this.service.save(bean);
        }else {
            this.service.updateById(bean);
        }
        return ResponseEntity.ok();
    }

    /**
     * 按照主键批量删除
     */
    @ApiOperation(value = "按照主键批量删除", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键（多个主键以“,”分割）", name = "ids", dataType = "String", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("deleteByIds")
    public ResponseEntity deleteByIds(String ids) {
        this.service.removeByIds(Arrays.asList(ids.split(",")));
        return ResponseEntity.ok();
    }

    /**
     * 分页条件查询
     */
    @ApiOperation(value = "分页条件查询", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("pageLike")
    public ResponseEntity<Page<TabFlowCondition>> page(@RequestBody TabFlowCondition conditionBean, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer rows) throws Exception {
        
        return ResponseEntity.ok(this.service.pageInfo(page, rows, conditionBean));
    }

    /**
     * 初始化
     */
    @ApiOperation(value = "模板间复制初始化前置条件", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "源模板id", name = "sourceId", dataType = "String", required = true),
            @ApiImplicitParam(value = "模板模板id", name = "targetId", dataType = "String", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("copyInit")
    public ResponseEntity copyInit(Long sourceId, Long targetId) {
        StringBuffer errMsg = new StringBuffer();
        int success = this.service.copyInit(sourceId, targetId, errMsg);
        if (success > 0) {
            return ResponseEntity.ok(success);
        }else {
            return ResponseEntity.apply(StatusCodeEnmus.OTHER, errMsg.toString());
        }
        
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
    public ResponseEntity getById(Serializable id) {
        return ResponseEntity.ok(this.service.getById(id));
    }
}
