package com.jtyjy.finance.manager.controller;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.common.tools.PinyinTools;
import com.jtyjy.core.auth.anno.NoLoginAnno;
import com.jtyjy.core.local.JdbcSqlThreadLocal;
import com.jtyjy.core.result.ResponseResult;
import com.jtyjy.finance.manager.bean.TabDm;
import com.jtyjy.finance.manager.service.TabDmService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * <p></p>
 * <p>作者 konglingcheng</p>
 * <p>date 2020年4月29日</p>
 */
@Api(tags = { "属性管理" })
@RestController
@RequestMapping("/api/tabDm")
@CrossOrigin
@SuppressWarnings("all")

public class TabDmController extends BaseController<TabDm>{

	private static final Logger LOGGER = LoggerFactory.getLogger(TabDmController.class);
	
	@Autowired
	private TabDmService service;
	

	

	
	/**
	 * 新增
	 * 作者 konglingcheng
	 * date 2020年4月18日
	 * @param bean
	 * @param bindingResult
	 * @return
	 */
	@ApiOperation(value = "新增",httpMethod="POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "选项名称", name = "dmName", dataType = "String", required = true),
			@ApiImplicitParam(value = "排序", name = "dmOrder", dataType = "String", required = false),
			@ApiImplicitParam(value = "状态", name = "dmStatus", dataType = "String", required = false),
			@ApiImplicitParam(value = "属性类型", name = "dmType", dataType = "String", required = true),
			@ApiImplicitParam(value = "属性值【属性类型为录排系数时必填】", name = "dmValue", dataType = "String", required = false),
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@PostMapping("save")
	public ResponseResult save(@Valid  TabDm bean, BindingResult bindingResult){
		try {
			String retError = this.getResult(bindingResult);
			if(StringUtils.isNotBlank(retError)){
				return ResponseResult.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL,retError);
			}
			String dm = bean.getDm();
			
			bean.setDm(PinyinTools.getFullspell(bean.getDmName()));
			int count = this.service.validIsTheSame(bean.getDmType(),bean.getDm());
			if(count > 0) {
				return ResponseResult.apply(StatusCodeEnmus.DATA_IS_EXIST,"");
			}
			this.service.save2Mysql("tab_dm", bean, false, "");
			if(LOGGER.isInfoEnabled()){
				LOGGER.info("新增成功......");
			}
			return ResponseResult.apply(StatusCodeEnmus.REQUEST_SUCCESS,null,"新增成功");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseResult.error();
	}
	
	/**
	 * 分页模糊查询
	 * 作者 konglingcheng
	 * date 2020年4月18日
	 * @param bean
	 * @param page
	 * @param rows
	 * @return
	 */
	@ApiOperation(value = "分页模糊查询",httpMethod="POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "代码类型", name = "dmType", dataType = "String", required = false),
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@PostMapping("pageLike")
	@NoLoginAnno
	public ResponseResult page(@RequestBody TabDm bean,
							   @RequestParam(value = "page", required = true, defaultValue = "1") Integer page,
							   @RequestParam(value = "rows", required = true, defaultValue = "20")Integer rows){
		try {
			String sql = " and 1 = 1 and t.dm_type !='PARENT'";

			if (StringUtils.isNotEmpty(bean.getDmType())) {
				sql += " and t.dm_type = '" + bean.getDmType() + "'";
			}
			JdbcSqlThreadLocal.set(sql);
			Map<String, Object> map = this.service.pageLikeMysql(TabDm.class, bean, page, rows);
			return ResponseResult.ok(map);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseResult.error();
	}
	
	/**
	 * @param bean
	 * @param bindingResult
	 * @return
	 */
	@ApiOperation(value = "启用",httpMethod="POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "选项代码", name = "dm", dataType = "String", required = true),
			@ApiImplicitParam(value = "属性类型", name = "dmType", dataType = "String", required = true),
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@PostMapping("start")
	public ResponseResult start(@Valid TabDm bean, BindingResult bindingResult){
		try {
			this.service.start(bean);
			if(LOGGER.isInfoEnabled()){
				LOGGER.info("启用成功......");
			}
			return ResponseResult.apply(StatusCodeEnmus.REQUEST_SUCCESS,null,"启用成功");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseResult.error();
	}
	
	/**
	 * @param bean
	 * @param bindingResult
	 * @return
	 */
	@ApiOperation(value = "禁用",httpMethod="POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "选项代码", name = "dm", dataType = "String", required = true),
			@ApiImplicitParam(value = "属性类型", name = "dmType", dataType = "String", required = true),
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@PostMapping("stop")
	public ResponseResult stop(@Valid TabDm bean, BindingResult bindingResult){
		try {
				this.service.stop(bean);
			if(LOGGER.isInfoEnabled()){
				LOGGER.info("禁用成功......");
			}
			return ResponseResult.apply(StatusCodeEnmus.REQUEST_SUCCESS,null,"禁用成功");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseResult.error();
	}
	
	
	
	/**
	 * 查询所有属性
	 * @param id
	 * @return
	 */
	@ApiOperation(value = "查询所有属性",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@GetMapping("getAllType")
	public ResponseResult getAllType(){
		try {
			List<Map<String, Object>> data= this.service.selectAllType();
			if(LOGGER.isInfoEnabled()){
				LOGGER.info("查询成功......");
			}
			return ResponseResult.ok(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseResult.error();
	}
	
	@ApiOperation(value = "更新",httpMethod="POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "属性类型", name = "dmType", dataType = "String", required = true),
			@ApiImplicitParam(value = "属性代码", name = "dm", dataType = "String", required = true),
			@ApiImplicitParam(value = "属性值", name = "dmValue", dataType = "String", required = true),
			@ApiImplicitParam(value = "排序", name = "dmOrder", dataType = "String", required = false),
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@PostMapping("update")
	public ResponseResult update(TabDm dm){
		try {
			//if(PAYDEADLINE.equals(dm.getDm()) || FINEINSTER.equals(dm.getDm())) {
				UpdateWrapper<TabDm> wrapper = new UpdateWrapper<TabDm>();
				wrapper.eq("dm_type", dm.getDmType());
				wrapper.eq("dm", dm.getDm());
				wrapper.set("dm_value", dm.getDmValue());
				//wrapper.set("dm_order", dm.getDmOrder());
				this.service.update(wrapper);
				if(LOGGER.isInfoEnabled()){
					LOGGER.info("更新成功......");
				}
				return ResponseResult.ok();
			//}
			//return ResponseResult.apply(StatusCodeEnmus.FORBID_UPDATE,"只能更新画图系数或录排系数！");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseResult.error();
	}
	
	/**
	 *根据属性查选项
	 * @param id
	 * @return
	 */
	@ApiOperation(value = "根据属性查选项",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "属性类型", name = "type", dataType = "String", required = true),
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@GetMapping("getAllSubType")
	public ResponseResult getAllSubType(String type){
		try {
			List<TabDm> data= this.service.selectAllSubType(type);
			if(LOGGER.isInfoEnabled()){
				LOGGER.info("查询成功......");
			}
			return ResponseResult.ok(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseResult.error();
	}
	
	
	
}
