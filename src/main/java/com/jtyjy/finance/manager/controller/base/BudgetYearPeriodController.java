package com.jtyjy.finance.manager.controller.base;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.common.tools.PinyinTools;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.bean.BudgetAgentExecuteView;
import com.jtyjy.finance.manager.bean.BudgetMonthPeriod;
import com.jtyjy.finance.manager.bean.BudgetYearPeriod;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.service.BudgetMonthEndUnitService;
import com.jtyjy.finance.manager.service.BudgetYearPeriodService;
import com.jtyjy.finance.manager.vo.BudgetUnitVO;
import com.jtyjy.finance.manager.vo.YearPeriodVO;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shubo
 */
@Api(tags = { "期间信息管理接口" })
@RestController
@RequestMapping("/api/base/period")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetYearPeriodController extends BaseController<BudgetAgentExecuteView> {	
	
    private final BudgetYearPeriodService service;
    private final BudgetMonthEndUnitService monthEndUnitService;
	/**
     * 新增/修改（修改时需送id）
	 * @throws Exception 
     */
    @ApiOperation(value = "新增/修改（修改时需送id）", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "期间id（修改必送）", name = "id", dataType = "Integer"),
            @ApiImplicitParam(value = "期间名称", name = "period", dataType = "String", required = true),
            @ApiImplicitParam(value = "当前期间 0：否 1：是", name = "currentflag", dataType = "String", required = true),
            @ApiImplicitParam(value = "开始日期(yyyy-mm-dd)", name = "startdate", dataType = "String", required = true),
            @ApiImplicitParam(value = "结束日期(yyyy-mm-dd)", name = "enddate", dataType = "String", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("addOrUpdate")
    public ResponseEntity<String> addOrUpdate(
            @RequestParam(value = "id") Integer id,
            @RequestParam(value = "period") String period,
            @RequestParam(value = "currentflag") String currentflag,
            @RequestParam(value = "startdate") String startdate,
            @RequestParam(value = "enddate") String enddate) throws Exception {
        
        BudgetYearPeriod bean = new BudgetYearPeriod();
        if (StringUtils.isBlank(period)) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL, "期间名称无效");
        }
        if (StringUtils.isBlank(currentflag)) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL, "当前期间标志无效");
        }
        if (StringUtils.isBlank(startdate)) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL, "开始日期无效");
        }
        if (StringUtils.isBlank(enddate)) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL, "结束日期无效");
        }
        if (null != id) {
            bean.setId(Long.valueOf(id));
        }
        bean.setPeriod(period);
        bean.setCurrentflag("1".equals(currentflag));
        bean.setStartdate(Constants.FORMAT_10.parse(startdate));
        bean.setEnddate(Constants.FORMAT_10.parse(enddate));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
        bean.setCode(formatter.format(bean.getEnddate()));
        StringBuffer errMsg = new StringBuffer();
        if (this.service.checkData(bean , errMsg)) {
            if (null == bean.getId() || 0 == bean.getId().intValue()) {
                bean.setCreatetime(new Date());
                if (this.service.addPeriod(bean, errMsg)) {
                    return ResponseEntity.ok();
                }else {
                    return ResponseEntity.apply(StatusCodeEnmus.OTHER, errMsg.toString());
                }
            }else {
                if (this.service.updatePeriod(bean, errMsg)){
                    return ResponseEntity.ok();
                }else {
                    return ResponseEntity.apply(StatusCodeEnmus.OTHER, errMsg.toString());
                }
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
        this.service.removeByIds(Arrays.asList(ids.split(",")));
        return ResponseResult.ok();
    }

    /**
     * 获取月份信息
     */
    @ApiOperation(value = "获取月份信息", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("getMonth")
    public ResponseEntity<List<BudgetMonthPeriod>> getMonth() throws Exception {

        List<BudgetMonthPeriod> voList = this.service.getMonthPeriod();
        return ResponseEntity.ok(voList);
    }

    /**
     * 获取当前届别
     */
    @ApiOperation(value = "获取当前届别", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("getNowYear")
    public ResponseEntity<BudgetYearPeriod> getNowYear() throws Exception {

        BudgetYearPeriod budgetYearPeriod = this.service.getNowYearPeriod();
        return ResponseEntity.ok(budgetYearPeriod);
    }
    
    /**
     * 查询月结单位
     */
    @ApiOperation(value = "查询月结单位", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "届别id", name = "yearId", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "月份启动id", name = "monthStartId", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("getEndMonthUnit")
    public ResponseEntity<List<BudgetUnitVO>> getMonthUnitEnd(Integer yearId, Integer monthStartId) throws Exception {
        List<BudgetUnitVO> voList = this.monthEndUnitService.getEndMonthUnit(Long.valueOf(yearId), Long.valueOf(monthStartId));
        return ResponseEntity.ok(voList);
    }
       
    
    /**
     * 设置月结单位
     */
    @ApiOperation(value = "设置月结单位", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "年月id（届别id-月份id）", name = "periodId", dataType = "String", required = true),
            @ApiImplicitParam(value = "预算单位ids（多个用,隔开）", name = "unitIds", dataType = "String", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("setEndMonthUnit")
    public ResponseEntity<List<BudgetUnitVO>> setEndMonthUnit(String periodId, String unitIds) throws Exception {
        this.monthEndUnitService.setEndMonthUnit(periodId, unitIds);
        return ResponseEntity.ok();
    }
    
    /**
     * 查询期间信息
     */
    @ApiOperation(value = "查询期间信息", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("query")
    public ResponseEntity<List<YearPeriodVO>> query() throws Exception {

        List<YearPeriodVO> voList = this.service.getYearMonthPeriod();
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
