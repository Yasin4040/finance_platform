package com.jtyjy.finance.manager.controller.authorfee;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;

import javax.validation.Valid;

import com.jtyjy.finance.manager.bean.BudgetBillingUnitAccount;
import com.jtyjy.finance.manager.bean.WbBanks;
import com.jtyjy.finance.manager.mapper.BudgetBillingUnitAccountMapper;
import com.jtyjy.finance.manager.mapper.WbBanksMapper;
import com.jtyjy.finance.manager.service.BudgetBillingUnitAccountService;
import com.jtyjy.finance.manager.service.WbBanksService;
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
import com.jtyjy.finance.manager.bean.BudgetAuthorfeepayRuledetail;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.service.BudgetAuthorfeepayRuledetailService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;

/*
 * Author: ldw
 * Description: 稿费发放规则明细
 * Date: 2021/4/22 11:15
 */
@Api(tags = { "稿费发放规则明细" })
@RestController
@RequestMapping("/api/budgetAuthorfeepayRuledetaildtl")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("all")
public class AuthorFeePayRuleDetailController extends BaseController<BudgetAuthorfeepayRuledetail> {

    @Autowired
    private BudgetAuthorfeepayRuledetailService service;

    @Autowired
    private BudgetBillingUnitAccountService accountService;
    @Autowired
    private WbBanksService wbBanksService;

    /**
     * 新增
     */
    @ApiOperation(value = "新增", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("save")
    public ResponseEntity save(@Valid @RequestBody BudgetAuthorfeepayRuledetail bean, BindingResult bindingResult) {
        String retError = this.getResult(bindingResult);
        if (StringUtils.isNotBlank(retError)) {
            return ResponseEntity.error(retError);
        }
        BudgetBillingUnitAccount unitAccount = accountService.getById(bean.getBillunitaccountid());
        WbBanks bank = wbBanksService.getOne(new QueryWrapper<WbBanks>().eq("sub_branch_code", unitAccount.getBranchcode()));
        Optional.ofNullable(bank).orElseThrow(()->new RuntimeException("找不到开户行。电子银联号为【"+unitAccount.getBranchcode()+"】"));
        bean.setBillunitopenbank(bank.getSubBranchName());
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
    public ResponseEntity update(@Valid @RequestBody BudgetAuthorfeepayRuledetail bean, BindingResult bindingResult) {
        String retError = this.getResult(bindingResult);
        if (StringUtils.isNotBlank(retError)) {
            return ResponseEntity.error(retError);
        }
        BudgetBillingUnitAccount unitAccount = accountService.getById(bean.getBillunitaccountid());
        WbBanks bank = wbBanksService.getOne(new QueryWrapper<WbBanks>().eq("sub_branch_code", unitAccount.getBranchcode()));
        Optional.ofNullable(bank).orElseThrow(()->new RuntimeException("找不到开户行。电子银联号为【"+unitAccount.getBranchcode()+"】"));
        bean.setBillunitopenbank(bank.getSubBranchName());
        this.service.updateById(bean);
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
            @ApiImplicitParam(value = "发放规则主表id", name = "payRuleId", dataType = "Long"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("pageLike")
    public ResponseEntity<PageResult<BudgetAuthorfeepayRuledetail>> page(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer rows,
    		@RequestParam(name="payRuleId")Long payRuleId) throws Exception {
    	Page<BudgetAuthorfeepayRuledetail> pageCond = new Page<>(page,rows);
    	pageCond = this.service.page(pageCond, new QueryWrapper<BudgetAuthorfeepayRuledetail>().eq("payruleid", payRuleId));
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
    public ResponseEntity<BudgetAuthorfeepayRuledetail> getById(@RequestParam(name="id",required = true)Long id) {
        return ResponseEntity.ok(this.service.getById(id));
    }
    
    
}
