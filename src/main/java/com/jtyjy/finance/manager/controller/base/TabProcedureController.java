package com.jtyjy.finance.manager.controller.base;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.finance.manager.bean.BudgetAgentExecuteView;
import com.jtyjy.finance.manager.bean.TabDm;
import com.jtyjy.finance.manager.bean.TabProcedure;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.service.TabProcedureService;
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
@Api(tags = { "流程模板管理接口" })
@RestController
@RequestMapping("/api/procedure")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TabProcedureController extends BaseController<BudgetAgentExecuteView> {	
	
    private final TabProcedureService service;

	/**
     * 新增/修改（修改时需送id）
     */
    @ApiOperation(value = "新增/修改（修改时需送id）", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "流程id（修改必送）", name = "id", dataType = "Integer"),
            @ApiImplicitParam(value = "届别id", name = "yearid", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "流程名称", name = "procedureName", dataType = "String", required = true),
            @ApiImplicitParam(value = "流程类型 1报销 2其他", name = "procedureType", dataType = "String", required = true),
            @ApiImplicitParam(value = "环节顺序（环节代码用,隔开）", name = "procedureLinkOrder", dataType = "String", required = true),
            @ApiImplicitParam(value = "是否启用 0否 1是", name = "isActive", dataType = "String", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("addOrUpdate")
    public ResponseEntity<String> addOrUpdate(@Valid TabProcedure bean, BindingResult bindingResult) {
        String retError = this.getResult(bindingResult);
        if (StringUtils.isNotBlank(retError)) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL, retError, null);
        }
        StringBuffer errMsg = new StringBuffer();
        if (this.service.checkData(bean, errMsg)) {
            if ("1".equals(bean.getIsActive())) {
                UpdateWrapper<TabProcedure> wrapper = new UpdateWrapper<TabProcedure>();
                wrapper.set("is_active", "0");
                wrapper.eq("is_active", 1);
                wrapper.eq("yearid", bean.getYearid());
                this.service.update(wrapper);
            }
            if(null == bean.getId() || 0 == bean.getId().intValue()) {
                this.service.save(bean);
                return ResponseEntity.ok();
            }else {
                this.service.updateById(bean);
                return ResponseEntity.ok();
            }
        }else {
            return ResponseEntity.apply(StatusCodeEnmus.OTHER, errMsg.toString());
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
        try {
            this.service.deleteProcedure(ids);
            return ResponseResult.ok();
        } catch (Exception e) {
            return ResponseResult.error(e.getMessage());
        }
    }
    
    /**
     * 查询流程信息
     */
    @ApiOperation(value = "查询流程信息", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "环节代码（包含此环节）", name = "linkOrder", dataType = "String"),
            @ApiImplicitParam(value = "流程类型 1报销 2其他", name = "procedureType", dataType = "String"),
            @ApiImplicitParam(value = "是否启用 0未启用 1启用", name = "isActive", dataType = "String"),
            @ApiImplicitParam(value = "届别Id", name = "yearId", dataType = "Long"),
            @ApiImplicitParam(value = "模板名称（模糊查询）", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("query")
    public ResponseEntity<List<TabProcedure>> query(
            @RequestParam(value = "linkOrder") String linkOrder, 
            @RequestParam(value = "procedureType", defaultValue = "") String procedureType,
            @RequestParam(value = "isActive", defaultValue = "") String isActive,
            @RequestParam(value = "yearId", defaultValue = "0") Long yearId,
            @RequestParam(value = "name", defaultValue = "") String name) throws Exception {
        List<TabProcedure> voList = this.service.getProcedureInfo(isActive, procedureType, linkOrder, yearId, name);
        return ResponseEntity.ok(voList);
    }

    /**
     * 获取流程模板的所有环节代码
     */
    @ApiOperation(value = "获取流程模板的环节代码", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "流程id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "要排除的环节代码", name = "linkCode", dataType = "String"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("getLinkCodeByPid")
    public ResponseEntity<List<TabDm>> getLinkCodeByPid(@RequestParam(value = "id", defaultValue = "")Long id, @RequestParam(value = "linkCode", defaultValue = "")String linkCode) {
        return ResponseEntity.ok(this.service.getLinkCodeByPid(id, linkCode));
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
