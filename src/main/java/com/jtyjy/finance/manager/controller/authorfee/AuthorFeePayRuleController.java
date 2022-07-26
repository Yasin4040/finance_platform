package com.jtyjy.finance.manager.controller.authorfee;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.core.result.ResponseResult;
import com.jtyjy.finance.manager.bean.BudgetAuthorfeepayRule;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.service.BudgetAuthorfeepayRuleService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;

/*
 * Author: ldw
 * Description: 稿费发放规则
 * Date: 2021/4/22 11:15
 */
@Api(tags = { "稿费发放规则" })
@RestController
@RequestMapping("/api/budgetAuthorfeepayRule")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("all")
public class AuthorFeePayRuleController extends BaseController<BudgetAuthorfeepayRule> {

    @Autowired
    private BudgetAuthorfeepayRuleService service;

    
    @InitBinder
    public void initBinder(ServletRequestDataBinder binder) {
        /*** 自动转换日期类型的字段格式
         */
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        binder.registerCustomEditor(Date.class, new CustomDateEditor(sdf,true));
    }    
    /**
     * 新增
     */
    @ApiOperation(value = "新增", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("save")
    public ResponseEntity save(@Valid @RequestBody BudgetAuthorfeepayRule bean, BindingResult bindingResult) {
    	bean.setTaxflag(true);
        String retError = this.getResult(bindingResult);
        if (StringUtils.isNotBlank(retError)) {
            return ResponseEntity.error(retError);
        }

        int size = this.service.list(new QueryWrapper<BudgetAuthorfeepayRule>().eq("name", bean.getName())).size();
        if(size>0) return ResponseEntity.error("规则名称【"+bean.getName()+"】已存在");
        bean.setCreatetime(new Date());
        bean.setUpdatetime(new Date());
        this.service.saveOne(bean);
        return ResponseEntity.ok();
    }

    /**
     * 修改
     */
    @ApiOperation(value = "修改", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("update")
    public ResponseEntity update(@Valid @RequestBody BudgetAuthorfeepayRule bean, BindingResult bindingResult) {
    	bean.setTaxflag(true);
        String retError = this.getResult(bindingResult);
        if (StringUtils.isNotBlank(retError)) {
            return ResponseEntity.error(retError);
        }
        int size = this.service.list(new QueryWrapper<BudgetAuthorfeepayRule>().eq("name", bean.getName()).ne("id",bean.getId())).size();
        if(size>0) return ResponseEntity.error("规则名称【"+bean.getName()+"】已存在");
        this.service.updateOneById(bean);
        return ResponseEntity.ok();
    }

    /**
     * 按照主键批量删除
     */
//    @ApiOperation(value = "按照主键批量删除", httpMethod = "POST")
//    @ApiImplicitParams(value = {
//            @ApiImplicitParam(value = "主键（多个主键以“,”分割）", name = "ids", dataType = "String", required = true),
//            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
//    })
//    @PostMapping("deleteByIds")
//    public ResponseResult deleteByIds(String ids) {
//        this.service.removeByIds(Arrays.asList(ids.split(",")));
//        return ResponseResult.ok();
//    }

    /**
     * 分页模糊查询
     */
    @ApiOperation(value = "分页模糊查询", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("pageLike")
    public ResponseEntity<PageResult<BudgetAuthorfeepayRule>> page(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer rows) throws Exception {
    	Page<BudgetAuthorfeepayRule> pageCond = new Page(page,rows);
    	pageCond = this.service.page(pageCond);
        return ResponseEntity.ok(PageResult.apply(pageCond.getTotal(), pageCond.getRecords()));
    }

    /**
     * 按照ID查询
     */
    @ApiOperation(value = "按照主键查询", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("getById")
    public ResponseEntity<BudgetAuthorfeepayRule> getById(@RequestParam(name="id",required = true)Long id) {
        return ResponseEntity.ok(this.service.getById(id));
    }
}
