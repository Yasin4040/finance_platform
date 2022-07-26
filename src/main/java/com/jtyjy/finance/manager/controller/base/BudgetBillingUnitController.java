package com.jtyjy.finance.manager.controller.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.finance.manager.bean.BudgetAgentExecuteView;
import com.jtyjy.finance.manager.bean.BudgetBillingUnit;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.service.BudgetBillingUnitService;
import com.jtyjy.finance.manager.vo.BillingUnitVO;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Admin
 */
@Api(tags = { "开票单位管理接口" })
@RestController
@RequestMapping("/api/base/billUnit")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetBillingUnitController extends BaseController<BudgetAgentExecuteView> {
	
	private final BudgetBillingUnitService service;
	
	/**
     * 新增/修改（修改时需送id）
     */
    @ApiOperation(value = "新增/修改（修改时需送id）", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "开票单位id（修改必送）", name = "id", dataType = "Integer"),
            @ApiImplicitParam(value = "单位名称", name = "name", dataType = "String", required = true),
            @ApiImplicitParam(value = "单位类型 0：无票 1：公司发票", name = "billingUnitType", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "是否法人单位 0：否【默认】 1：是", name = "corporation", dataType = "Integer"),
            @ApiImplicitParam(value = "内部单位标志 0：内部 1：外部【默认】", name = "ownFlag", dataType = "Integer"),
            @ApiImplicitParam(value = "停用标志 0：启用【默认】 1：停用", name = "stopFlag", dataType = "Integer"),
            @ApiImplicitParam(value = "排序号", name = "orderNo", dataType = "Integer"),
            @ApiImplicitParam(value = "预算员（多个,隔开）ids", name = "budgeters", dataType = "String"),
            @ApiImplicitParam(value = "会计（多个,隔开）ids", name = "accountants", dataType = "String"),
            @ApiImplicitParam(value = "备注", name = "remark", dataType = "String"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("addOrUpdate")
    public ResponseEntity<String> addOrUpdate(BudgetBillingUnit bean, BindingResult bindingResult) {
        String retError = this.getResult(bindingResult);
        if (StringUtils.isNotBlank(retError)) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL, retError, null);
        }
        if (null == bean.getOrderNo()) {
            bean.setOrderNo(0);
        }

        StringBuffer errMsg = new StringBuffer();
        if (this.service.checkData(bean, errMsg)) {
            if (null == bean.getId() || 0 == bean.getId().intValue()) {
                this.service.add(bean);
                return ResponseEntity.ok();
            }else {
                this.service.modify(bean);
                return ResponseEntity.ok();
            }
        }else {
            return ResponseEntity.apply(StatusCodeEnmus.DATA_IS_EXIST, errMsg.toString());
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
     * 分页条件查询
     */
    @ApiOperation(value = "分页条件查询", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "单位名称（模糊查询）", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "单位类型 0：无票 1：公司发票", name = "billingUnitType", dataType = "Integer"),
            @ApiImplicitParam(value = "是否法人单位 0：否 1：是", name = "corporation", dataType = "Integer"),
            @ApiImplicitParam(value = "内部单位标志 0：内部 1：外部", name = "ownFlag", dataType = "Integer"),
            @ApiImplicitParam(value = "停用标志 0：启用 1：停用", name = "stopFlag", dataType = "Integer"),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("page")
    public ResponseEntity<Page<BillingUnitVO>> page(
            BudgetBillingUnit params,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer rows) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("name", params.getName());
        map.put("billingUnitType", params.getBillingUnitType());       
        map.put("corporation", params.getCorporation());      
        map.put("ownflag", params.getOwnFlag());       
        map.put("stopflag", params.getStopFlag());     

        Page<BillingUnitVO> voList = this.service.getBillUnitPageList(map, page, rows);
//        Map<String, Object> dataMap = new HashMap<>();
//        dataMap.put("total", voList.getTotal());
//        dataMap.put("list", voList.getRecords());
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
