package com.jtyjy.finance.manager.controller.base;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.finance.manager.bean.BudgetAgentExecuteView;
import com.jtyjy.finance.manager.bean.BudgetProductCategory;
import com.jtyjy.finance.manager.controller.BaseController;

import com.jtyjy.finance.manager.service.BudgetProductCategoryService;
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
@Api(tags = { "产品分类管理接口" })
@RestController
@RequestMapping("/api/base/pdCategory")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetProductCategoryController extends BaseController<BudgetAgentExecuteView> {	
	
    private final BudgetProductCategoryService service;

	/**
     * 新增/修改（修改时需送id）
     */
    @ApiOperation(value = "新增/修改（修改时需送id）", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "产品分类id（修改必送）", name = "id", dataType = "Integer"),
            @ApiImplicitParam(value = "产品分类名称", name = "name", dataType = "String", required = true),
            @ApiImplicitParam(value = "停用标识 0：启用 1：停用", name = "stopflag", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "排序号", name = "orderno", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "父id（新增时上送，不送默认为0）", name = "pid", dataType = "Integer"),
            @ApiImplicitParam(value = "备注", name = "remark", dataType = "String"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("addOrUpdate")
    public ResponseEntity<String> addOrUpdate(@Valid BudgetProductCategory bean, BindingResult bindingResult) {
        String retError = this.getResult(bindingResult);
        if (StringUtils.isNotBlank(retError)) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL, retError, null);
        }
        BudgetProductCategory sameName = this.service.getOne(new QueryWrapper<BudgetProductCategory>().eq("name", bean.getName()));
        
        if(null == bean.getId() || 0 == bean.getId().intValue()) {
            if (null != sameName && bean.getName().equals(sameName.getName())) {
                return ResponseEntity.apply(StatusCodeEnmus.DATA_IS_EXIST, bean.getName() + "已存在！");
            }
            String msg = this.service.add(bean);
            if (!"成功".equals(msg)) {
                return ResponseEntity.error(msg);
            }
            return ResponseEntity.ok();
        }else {
            if (null != sameName && !sameName.getId().equals(bean.getId())) {
                return ResponseEntity.apply(StatusCodeEnmus.DATA_IS_EXIST, bean.getName() + "已存在！");
            }
            String msg = this.service.modify(bean);
            if (!"成功".equals(msg)) {
                return ResponseEntity.error(msg);
            }
            return ResponseEntity.ok();
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
     * 产品分类移动
     */
    @ApiOperation(value = "产品分类移动", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "要移动的id", name = "id", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "新的父级id（最外层为0）", name = "pid", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("move")
    public ResponseEntity<String> move(
            @RequestParam(value = "id") Integer id, 
            @RequestParam(value = "pid") Integer pid) throws Exception {
        String msg = this.service.movePdCategory(id.longValue(), pid.longValue());
        if ("成功".equals(msg)) {
            return ResponseEntity.ok();
        }else {
            return ResponseEntity.error(msg);
        }
        
    }
    
    /**
     * 查询产品分类
     */
    @ApiOperation(value = "查询产品分类", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "产品分类名称（模糊查询）", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "停用标识 0：启用，1：停用", name = "stopflag", dataType = "Integer"),
            @ApiImplicitParam(value = "预算单位id", name = "unitId", dataType = "Long"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("query")
    public ResponseEntity<List<BudgetProductCategory>> baseUnitInfoPage(
            @RequestParam(value = "name") String name, 
            @RequestParam(value = "unitId") Long unitId, 
            @RequestParam(value = "stopflag") Integer stopflag) throws Exception {
        List<BudgetProductCategory> voList = this.service.getPdCategoryInfo(name, unitId, stopflag);
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
