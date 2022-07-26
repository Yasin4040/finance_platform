package com.jtyjy.finance.manager.controller.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.finance.manager.bean.BudgetAgentExecuteView;
import com.jtyjy.finance.manager.bean.WbUser;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.service.WbUserService;
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

/**
 * @author shubo
 */
@Api(tags = { "用户信息管理接口" })
@RestController
@RequestMapping("/api/base/wbUser")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WbUserController extends BaseController<BudgetAgentExecuteView> {	
	
    private final WbUserService service;

	/**
     * 新增/修改（修改时需送id）
     */
    @ApiOperation(value = "新增/修改（修改时需送id）", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "用户id", name = "id", dataType = "Integer"),
            @ApiImplicitParam(value = "工号", name = "code", dataType = "String", required = true),
            @ApiImplicitParam(value = "名称", name = "pname", dataType = "String", required = true),
            @ApiImplicitParam(value = "户名", name = "accountName", dataType = "String"),
            @ApiImplicitParam(value = "账户类型 1：对内，2：对外", name = "accountType", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "账号", name = "bankAccount", dataType = "String", required = true),
            @ApiImplicitParam(value = "工资账户 0：否 1：是", name = "wagesFlag", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "开户行电子联行号", name = "branchCode", dataType = "String", required = true),
            @ApiImplicitParam(value = "停用标志 0：启用 1：停用", name = "stopFlag", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "排序号", name = "orderNo", dataType = "Integer"),
            @ApiImplicitParam(value = "备注", name = "remark", dataType = "String"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("addOrUpdate")
    public ResponseEntity<String> addOrUpdate(@Valid WbUser bean, BindingResult bindingResult) {
        String retError = this.getResult(bindingResult);
        if (StringUtils.isNotBlank(retError)) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL, retError, null);
        }
        if(StringUtils.isBlank(bean.getUserId())) {
            this.service.save(bean);
            return ResponseEntity.apply(StatusCodeEnmus.DATA_IS_EXIST, retError);
            
        }else {
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
     * 分页查询用户
     */
    @ApiOperation(value = "分页查询用户", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "用户名称（模糊查询）", name = "displayName", dataType = "String"),
            @ApiImplicitParam(value = "预算单位id（报销人查询需送）", name = "unitId", dataType = "Integer"),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("page")
    public ResponseEntity<Page<WbUser>> page(
            @RequestParam(value = "displayName", required = false) String displayName,
            @RequestParam(value = "unitId", required = false) Long unitId,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "rows", required = false, defaultValue = "20") Integer rows) throws Exception {
        Page<WbUser> voList = new Page<WbUser>();
        voList = this.service.getUserPageInfo(displayName, unitId, page, rows);

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
