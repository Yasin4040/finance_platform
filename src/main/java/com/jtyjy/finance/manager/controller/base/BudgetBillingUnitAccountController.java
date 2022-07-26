package com.jtyjy.finance.manager.controller.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.finance.manager.bean.BudgetAgentExecuteView;
import com.jtyjy.finance.manager.bean.BudgetBillingUnitAccount;
import com.jtyjy.finance.manager.bean.WbBanks;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.service.BudgetBillingUnitAccountService;
import com.jtyjy.finance.manager.vo.BillingUnitAccountVO;
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
import java.util.List;

/**
 * @author Admin
 */
@Api(tags = { "开票单位账号管理接口" })
@RestController
@RequestMapping("/api/base/unitAccount")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetBillingUnitAccountController extends BaseController<BudgetAgentExecuteView> {

	
	private final BudgetBillingUnitAccountService service;
	
	
	/**
     * 新增/修改（修改时需送id）
     */
    @ApiOperation(value = "新增/修改（修改时需送id）", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("addOrUpdate")
    public ResponseEntity<String> addOrUpdate(@Valid @RequestBody BudgetBillingUnitAccount bean, BindingResult bindingResult) {
        String retError = this.getResult(bindingResult);
        if (StringUtils.isNotBlank(retError)) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL, retError, null);
        }

        StringBuffer errMsg = new StringBuffer();
        if (this.service.checkData(bean, errMsg)) {
            if (null == bean.getId() || 0 == bean.getId().intValue()) {
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

//    /**
//     * 修改
//     */
//    @ApiOperation(value = "修改", httpMethod = "POST")
//    @ApiImplicitParams(value = {
//            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
//    })
//    @PostMapping("update")
//    public ResponseResult update(@Valid BudgetBillingUnit bean, BindingResult bindingResult) {
//        String retError = this.getResult(bindingResult);
//        if (StringUtils.isNotBlank(retError)) {
//            return ResponseResult.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL, retError);
//        }
//        bean.setFirstSpell(PinyinTools.getFirstspell(bean.getName()));
//        bean.setFullSpell(PinyinTools.getFullspell(bean.getName()));
//        return ResponseResult.ok(this.service.updateById(bean));
//    }

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
     * 分页查询单位账户信息
     */
    @ApiOperation(value = "分页查询单位账户", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "开票单位ID", name = "billingUnitId", dataType = "String"),
            @ApiImplicitParam(value = "停用标志 0：启用 1：停用", name = "stopFlag", dataType = "Integer"),
            @ApiImplicitParam(value = "单位名称（模糊查询）", name = "unitName", dataType = "String"),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("accountInfoPage")
    public ResponseEntity<Page<BillingUnitAccountVO>> accountInfoPage(
            @RequestParam(value = "billingUnitId") String billingUnitId, 
            @RequestParam(value = "stopFlag") Integer stopFlag,
            @RequestParam(value = "unitName") String unitName,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "rows", required = false, defaultValue = "20") Integer rows) throws Exception {
       
        
        Page<BillingUnitAccountVO> voList = this.service.getUnitAccountPageList(billingUnitId, stopFlag, unitName, page, rows);
        return ResponseEntity.ok(voList);
    }

    /**
     * 分页查询开户行信息
     */
    @ApiOperation(value = "分页查询开户行", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "银行名称", name = "bankName", dataType = "String"),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("bankInfoPage")
    public ResponseEntity<Page<WbBanks>> bankInfoPage(
            @RequestParam(value = "bankName") String bankName, 
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "rows", required = false, defaultValue = "20") Integer rows) throws Exception {
       
        
        Page<WbBanks> voList = this.service.getBankInfoPageList(bankName, page, rows);
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
