package com.jtyjy.finance.manager.controller.base;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.finance.manager.bean.BudgetAgentExecuteView;
import com.jtyjy.finance.manager.bean.TabLinkLimit;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.service.TabLinkLimitService;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Admin
 */
@Api(tags = { "环节限制管理接口" })
@RestController
@RequestMapping("/api/linkLimit")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TabLinkLimitController extends BaseController<BudgetAgentExecuteView> {	
	
    private final TabLinkLimitService service;

	/**
     * 新增/修改（修改时需送id）
     */
    @ApiOperation(value = "新增/修改（修改时需送id）", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键id（修改必送）", name = "id", dataType = "Long"),
            @ApiImplicitParam(value = "流程id", name = "procedureId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "预算科目ids（多个用，隔开）", name = "subjectIds", dataType = "String", required = true),
            @ApiImplicitParam(value = "环节代码", name = "linkDm", dataType = "String", required = true),
            @ApiImplicitParam(value = "最小限度", name = "minLimit", dataType = "Double"),
            @ApiImplicitParam(value = "最大限度", name = "maxLimit", dataType = "Double", required = true),
            @ApiImplicitParam(value = "是否启用 0否 1是", name = "isActive", dataType = "String", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("addOrUpdate")
    public ResponseEntity<String> addOrUpdate(@Valid TabLinkLimit bean, BindingResult bindingResult) {
        String retError = this.getResult(bindingResult);
        if (StringUtils.isNotBlank(retError)) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL, retError, null);
        }
        if(null == bean.getId() || 0 == bean.getId().intValue()) {
            String subjectIds = bean.getSubjectIds();
            if (StringUtils.isNotBlank(subjectIds)) {
                for(String subjectId : subjectIds.split(",")) {
                    bean.setSubjectId(Long.valueOf(subjectId));
                    StringBuffer errMsg = new StringBuffer();
                    if (this.service.checkData(bean, errMsg)) {
                        this.service.save(bean);
                    }else {
                        return ResponseEntity.apply(StatusCodeEnmus.OTHER, errMsg.toString());
                    }
                    
                }
            }

            return ResponseEntity.ok();
        }else {
            bean.setSubjectId(Long.valueOf(bean.getSubjectIds()));
            StringBuffer errMsg = new StringBuffer();
            if (this.service.checkData(bean, errMsg)) {
                this.service.updateById(bean);
                return ResponseEntity.ok();
            }else {
                return ResponseEntity.apply(StatusCodeEnmus.OTHER, errMsg.toString());
            }
        }
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
    public ResponseResult deleteByIds(String ids) {
        this.service.removeByIds(Arrays.asList(ids.split(",")));
        return ResponseResult.ok();
    }
    
    /**
     * 判断环节是否执行
     */
    @ApiOperation(value = "判断环节是否执行", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "流程模板id", name = "procedureId", dataType = "Long"),
            @ApiImplicitParam(value = "预算科目id", name = "subjectId", dataType = "Long"),
            @ApiImplicitParam(value = "环节代码", name = "linkDm", dataType = "String"),
            @ApiImplicitParam(value = "预算科目名称", name = "subjectName", dataType = "String"),
            @ApiImplicitParam(value = "金额", name = "amount", dataType = "Double"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("judge")
    public ResponseEntity<Boolean> judge(
            @RequestParam(value = "procedureId") Long procedureId,
            @RequestParam(value = "subjectId") Long subjectId, 
            @RequestParam(value = "linkDm") String linkDm, 
            @RequestParam(value = "subjectName") String subjectName, 
            @RequestParam(value = "amount") Double amount) throws Exception {
        boolean result = this.service.judgeLinkLimit(procedureId, subjectId, linkDm, subjectName, amount);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 查询环节限制
     */
    @ApiOperation(value = "查询环节限制", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "流程模板id", name = "procedureId", dataType = "Long"),
            @ApiImplicitParam(value = "预算科目id", name = "subjectId", dataType = "Long"),
            @ApiImplicitParam(value = "环节代码", name = "linkDm", dataType = "String"),
            @ApiImplicitParam(value = "预算科目名称", name = "subjectName", dataType = "String"),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("query")
    public ResponseEntity<Page<TabLinkLimit>> query(
            @RequestParam(value = "procedureId") Long procedureId,
            @RequestParam(value = "subjectId") Long subjectId, 
            @RequestParam(value = "linkDm") String linkDm,
            @RequestParam(value = "subjectName") String subjectName,
            @RequestParam(defaultValue = "1") Integer page, 
            @RequestParam(defaultValue = "20") Integer rows) throws Exception {
        Page<TabLinkLimit> voList = this.service.getLinkLimitInfo(page, rows, procedureId, subjectId, linkDm, subjectName);
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
    @GetMapping("init")
    public void init(){
        this.service.initLimit();

    }
}
