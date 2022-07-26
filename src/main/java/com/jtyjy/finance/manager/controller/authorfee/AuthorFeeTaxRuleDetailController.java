package com.jtyjy.finance.manager.controller.authorfee;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.core.result.ResponseResult;
import com.jtyjy.finance.manager.bean.BudgetAuthorfeetaxRuledetail;
import com.jtyjy.finance.manager.bean.BudgetExtractquotaRuledetail;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.service.BudgetAuthorfeetaxRuledetailService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;

/*
 * Author: ldw
 * Description: 稿费计税规则明细
 * Date: 2021/4/22 11:15
 */
@Api(tags = { "稿费计税规则明细" })
@RestController
@RequestMapping("/api/budgetAuthorfeetaxRuledetaildtl")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("all")
public class AuthorFeeTaxRuleDetailController extends BaseController<BudgetAuthorfeetaxRuledetail> {

    @Autowired
    private BudgetAuthorfeetaxRuledetailService service;

    /**
     * 新增
     */
    @ApiOperation(value = "新增", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("save")
    public ResponseEntity save(@Valid @RequestBody BudgetAuthorfeetaxRuledetail bean, BindingResult bindingResult) {
        String retError = this.getResult(bindingResult);
        if (StringUtils.isNotBlank(retError)) {
            return ResponseEntity.error(retError);
        }
        if(bean.getMin().compareTo(bean.getMax()) >= 0) throw new RuntimeException("最大金额应该大于最小金额");
        service.list(new QueryWrapper<BudgetAuthorfeetaxRuledetail>().eq("taxruleid", bean.getTaxruleid())).forEach(e->{
			BigDecimal min = e.getMin();
			BigDecimal max = e.getMax();
			boolean flag = false;
			if(bean.getMin().compareTo(min)<0 && bean.getMax().compareTo(min)<=0) flag=true;
			if(bean.getMin().compareTo(max)>=0 && bean.getMax().compareTo(max)>0) flag=true;
			if(!flag) throw new RuntimeException("新增失败！金额存在交叉！");
		});
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
    public ResponseEntity update(@Valid @RequestBody BudgetAuthorfeetaxRuledetail bean, BindingResult bindingResult) {
        String retError = this.getResult(bindingResult);
        if (StringUtils.isNotBlank(retError)) {
            return ResponseEntity.error(retError);
        }
        if(bean.getMin().compareTo(bean.getMax()) >= 0) throw new RuntimeException("最大金额应该大于最小金额");
        service.list(new QueryWrapper<BudgetAuthorfeetaxRuledetail>().ne("id", bean.getId()).eq("taxruleid", bean.getTaxruleid())).forEach(e->{
			BigDecimal min = e.getMin();
			BigDecimal max = e.getMax();
			boolean flag = false;
			if(bean.getMin().compareTo(min)<0 && bean.getMax().compareTo(min)<=0) flag=true;
			if(bean.getMin().compareTo(max)>=0 && bean.getMax().compareTo(max)>0) flag=true;
			if(!flag) throw new RuntimeException("修改失败！金额存在交叉！");
		});
        
        this.service.updateOneById(bean);
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
     * 分页模糊查询
     */
    @ApiOperation(value = "分页模糊查询", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "计税规则主表id", name = "taxRuleId", dataType = "Long"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("pageLike")
    public ResponseEntity<PageResult<BudgetAuthorfeetaxRuledetail>> page(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer rows,
    		@RequestParam(name="taxRuleId") Long taxRuleId) throws Exception {
    	Page<BudgetAuthorfeetaxRuledetail> pageCond = new Page<>(page,rows);
    	pageCond = this.service.page(pageCond, new QueryWrapper<BudgetAuthorfeetaxRuledetail>().eq("taxruleid", taxRuleId));
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
    public ResponseEntity<BudgetAuthorfeetaxRuledetail> getById(@RequestParam(name="id",required = true)Long id) {
        return ResponseEntity.ok(this.service.getById(id));
    }
}
