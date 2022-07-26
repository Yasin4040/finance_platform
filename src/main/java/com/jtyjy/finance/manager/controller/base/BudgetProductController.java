package com.jtyjy.finance.manager.controller.base;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.common.tools.PinyinTools;
import com.jtyjy.finance.manager.bean.BudgetAgentExecuteView;
import com.jtyjy.finance.manager.bean.BudgetProduct;
import com.jtyjy.finance.manager.controller.BaseController;

import com.jtyjy.finance.manager.service.BudgetProductService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Admin
 */
@Api(tags = { "产品信息管理接口" })
@RestController
@RequestMapping("/api/base/product")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetProductController extends BaseController<BudgetAgentExecuteView> {	
	
    private final BudgetProductService service;

	/**
     * 新增/修改（修改时需送id）
     */
    @ApiOperation(value = "新增/修改（修改时需送id）", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "产品id（修改必送）", name = "id", dataType = "Integer"),
            @ApiImplicitParam(value = "产品分类id", name = "procategoryid", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "产品名称", name = "name", dataType = "String", required = true),
            @ApiImplicitParam(value = "停用标识 0：启用 1：停用", name = "stopflag", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "排序号", name = "orderno", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "备注", name = "remark", dataType = "String"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("addOrUpdate")
    public ResponseEntity<String> addOrUpdate(@Valid BudgetProduct bean, BindingResult bindingResult) {
        String retError = this.getResult(bindingResult);
        if (StringUtils.isNotBlank(retError)) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL, retError, null);
        }
        bean.setFirstspell(PinyinTools.getFirstspell(bean.getName()));
        bean.setFullspell(PinyinTools.getPinYin(bean.getName()));
        BudgetProduct sameName = this.service.getOne(new QueryWrapper<BudgetProduct>().eq("name", bean.getName()));
        
        if(null == bean.getId() || 0 == bean.getId().intValue()) {
            if (null != sameName && bean.getName().equals(sameName.getName())) {
                return ResponseEntity.apply(StatusCodeEnmus.DATA_IS_EXIST, bean.getName() + "已存在！");
            }
            if (this.service.save(bean)) {
                bean.setProductno("P00" + bean.getId());
                this.service.updateById(bean);
            }
            return ResponseEntity.ok();
        }else {
            if (null != sameName && !sameName.getId().equals(bean.getId())) {
                return ResponseEntity.apply(StatusCodeEnmus.DATA_IS_EXIST, bean.getName() + "已存在！");
            }
            this.service.updateById(bean);
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
     * 分页查询产品信息
     */
    @ApiOperation(value = "分页查询产品信息", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "产品名称（模糊查询）", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "停用标识 0：启用，1：停用", name = "stopflag", dataType = "Integer"),
            @ApiImplicitParam(value = "产品分类id", name = "procategoryid", dataType = "Integer"),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("page")
    public ResponseEntity<Page<BudgetProduct>> page(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "stopflag", required = false) Integer stopflag,
            @RequestParam(value = "procategoryid", required = false) Integer procategoryid,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "rows", required = false, defaultValue = "20") Integer rows) throws Exception {
        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("name", name);
        conditionMap.put("stopflag", stopflag);
        conditionMap.put("procategoryid", procategoryid);
        
        Page<BudgetProduct> voList = this.service.getProductInfo(conditionMap, page, rows);
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
