package com.jtyjy.finance.manager.controller.base;

import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.core.auth.anno.ApiDataAuthAnno;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.core.result.ResponseResult;
import com.jtyjy.finance.manager.bean.BudgetAgentExecuteView;
import com.jtyjy.finance.manager.bean.BudgetUnit;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.service.BudgetUnitService;
import com.jtyjy.finance.manager.vo.BudgetUnitSubjectVO;
import com.jtyjy.finance.manager.vo.BudgetUnitVO;
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
import java.util.List;
import java.util.Map;

/**
 * @author shubo
 */
@Api(tags = { "预算单位管理接口" })
@RestController
@RequestMapping("/api/base/budgetUnit")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetUnitController extends BaseController<BudgetAgentExecuteView> {	
	
    
    private final BudgetUnitService service;

	/**
     * 新增/修改（修改时需送id）
     */
    @ApiOperation(value = "新增/修改（修改时需送id）", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位id（修改必送）", name = "id", dataType = "Integer"),
            @ApiImplicitParam(value = "预算单位名称", name = "name", dataType = "String", required = true),
            @ApiImplicitParam(value = "基础单位id", name = "baseUnitId", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "单位类型", name = "unitType", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "预算体系0：否，1：是", name = "budgetFlag", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "届别id", name = "yearId", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "预算员id（多个以,隔开）", name = "managers", dataType = "String", required = true),
            @ApiImplicitParam(value = "收入会计id（多个以,隔开）", name = "accounting", dataType = "String", required = true),
            @ApiImplicitParam(value = "部门id（多个以,隔开）", name = "budgetDepts", dataType = "String"),
            @ApiImplicitParam(value = "人员id（多个以,隔开）", name = "budgetUsers", dataType = "String"),
            @ApiImplicitParam(value = "本届码洋占比公式", name = "ccratioFormula", dataType = "String"),
            @ApiImplicitParam(value = "本届收入占比公式", name = "revenueFormula", dataType = "String"),
            @ApiImplicitParam(value = "排序号", name = "orderNo", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "备注", name = "remark", dataType = "String"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("addOrUpdate")
    public ResponseEntity<String> addOrUpdate(@Valid BudgetUnitVO bean, BindingResult bindingResult) {
        String retError = this.getResult(bindingResult);
        if (StringUtils.isNotBlank(retError)) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL, retError, null);
        }
        BudgetUnit BudgetUnit = new BudgetUnit(bean);
        StringBuffer errMsg = new StringBuffer();
        if (this.service.checkData(BudgetUnit, errMsg)) {
            if (null == bean.getId() || 0 == bean.getId().intValue()) {
                this.service.addUnit(BudgetUnit);
                return ResponseEntity.ok();
            }else {
                this.service.updateUnit(BudgetUnit);
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
    public ResponseEntity deleteByIds(String ids) {
        StringBuffer errMsg = new StringBuffer();
        if(this.service.deleteUnit(ids, errMsg)) {
            return ResponseEntity.ok();
        }else {
            return ResponseEntity.apply(StatusCodeEnmus.OTHER, errMsg.toString());
        }
    }

    /**
     * 查询预算单位的预算科目
     */
    @ApiOperation(value = "查询预算单位的预算科目", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位id", name = "unitId", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("queryUnitSubject")
    public ResponseEntity<List<BudgetUnitSubjectVO>> queryUnitSubject(Integer unitId) throws Exception {

        List<BudgetUnitSubjectVO> voList = this.service.queryUnitSubject(Long.valueOf(unitId));
        return ResponseEntity.ok(voList);
    }

    /**
     * 保存预算单位的预算科目
     */
    @ApiOperation(value = "保存预算单位的预算科目", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位id", name = "unitId", dataType = "String", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("saveUnitSubject")
    public ResponseEntity<String> saveUnitSubject(String unitId, @RequestBody @Valid List<BudgetUnitSubjectVO> list, BindingResult bindingResult) {
        String retError = this.getResult(bindingResult);
        if (StringUtils.isNotBlank(retError)) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL, retError, null);
        }
        this.service.updateunitsubject(unitId, list);
        return ResponseEntity.ok();
        
    }

    /**
     * 查询预算单位的预算科目
     */
    @ApiOperation(value = "查询预算单位的产品", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位id", name = "unitId", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "产品名称（模糊查询）", name = "name", dataType = "String", required = false),
            @ApiImplicitParam(value = "产品分类id", name = "cid", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("getProductByUnit")
    public ResponseEntity<List<Map<String, Object>>> getProductByUnit(Long unitId, String name, Long cid) throws Exception {

        List<Map<String, Object>> voList = this.service.getProductByUnit(unitId, name, cid);
        return ResponseEntity.ok(voList);
    }
    
    /**
     * 设置预算单位的预算科目
     */
    @ApiOperation(value = "设置预算单位的产品", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位id", name = "unitId", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "选中产品ids（多个,隔开）", name = "proIds", dataType = "String", required = true),
            @ApiImplicitParam(value = "产品分类树id", name = "pid", dataType = "String", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("setUnitProduct")
    public ResponseEntity setUnitProduct(Long unitId, String proIds, String pid) throws Exception {

        this.service.setUnitProduct(unitId, pid, proIds);
        return ResponseEntity.ok();
    }
    
    /**
     * 查询预算单位
     */
    @ApiOperation(value = "查询预算单位（权限控制）", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "届别id", name = "yearId", dataType = "int"),
            @ApiImplicitParam(value = "单位名称（模糊查询）", name = "unitName", dataType = "int"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("info")
    @ApiDataAuthAnno
    public ResponseEntity<List<BudgetUnitVO>> info(@RequestParam(value = "yearId", required = true) Integer yearId, String unitName) throws Exception {
        List<BudgetUnitVO> voList = this.service.getBudgetUnit(yearId, unitName,true);
        return ResponseEntity.ok(voList);
    }
    
    @ApiOperation(value = "查询预算单位(期间管理)", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "届别id", name = "yearId", dataType = "int"),
            @ApiImplicitParam(value = "单位名称（模糊查询）", name = "unitName", dataType = "int"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("infoNoAuth")
    public ResponseEntity<List<BudgetUnitVO>> infoNoAuth(@RequestParam(value = "yearId", required = true) Integer yearId, String unitName) throws Exception {
        List<BudgetUnitVO> voList = this.service.getBudgetUnit(yearId, unitName,false);
        return ResponseEntity.ok(voList);
    }
    
    /**
     * 预算单位移动
     */
    @ApiOperation(value = "预算单位移动", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "要移动的id", name = "id", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "新的父级id（最外层为0）", name = "pid", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("move")
    public ResponseResult move(@RequestParam(value = "id") Integer id, @RequestParam(value = "pid") Integer pid) {
        String errMsg = this.service.moveUnit(Long.valueOf(id), Long.valueOf(pid));
        if (StringUtils.isBlank(errMsg)) {
            return ResponseResult.ok();
        }else {
            return ResponseResult.error(errMsg);
        }
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
       
    /**
     * 初始化预算单位
     * @throws Exception 
     */
    @ApiOperation(value = "初始化预算单位", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "来源届别id", name = "sourceYearId", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "目标届别id", name = "targetYearId", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("init")
    public ResponseResult init(@RequestParam(value = "sourceYearId") Long sourceYearId, @RequestParam(value = "targetYearId") Long targetYearId) throws Exception {
        this.service.initUnit(sourceYearId, targetYearId);;
        return ResponseResult.ok();
    }
}
