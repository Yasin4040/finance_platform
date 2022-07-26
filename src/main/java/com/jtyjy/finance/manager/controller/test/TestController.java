package com.jtyjy.finance.manager.controller.test;

import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.jtyjy.core.auth.anno.NoLoginAnno;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.utils.HtmlUtil;
import com.klcwqy.easy.lock.impl.ZookeeperShareLock;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(tags = {"测试接口"})
@RestController
@RequestMapping("/api/test")
@CrossOrigin
public class TestController {
	
	@Autowired
	private CuratorFramework client;

	@ApiOperation(value = "测试新版用户和接口权限", httpMethod = "GET")
    @ApiImplicitParams(value = {
    		@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = false),
    		@ApiImplicitParam(value = "a", name = "a", dataType = "String", required = false),
    		@ApiImplicitParam(value = "b", name = "b", dataType = "String", required = false),
    		@ApiImplicitParam(value = "c", name = "c", dataType = "Integer", required = false),
    		@ApiImplicitParam(value = "d", name = "d", dataType = "String", required = false)
    })
	@GetMapping("/a")
//	@ApiDataAuthAnno
	@NoLoginAnno
	public void test(HttpServletResponse response) throws Exception{
		HtmlUtil.draw(HtmlUtil.html("报销审核", "扫码", "成功", "Hello World", Constants.FULL_FORMAT.format(new Date())), response);
	}
	
	
	@ApiOperation(value = "测试新版用户和接口权限", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = false),
			@ApiImplicitParam(value = "a", name = "a", dataType = "String", required = false),
			@ApiImplicitParam(value = "b", name = "b", dataType = "String", required = false),
			@ApiImplicitParam(value = "c", name = "c", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "d", name = "d", dataType = "String", required = false)
	})
	@PostMapping("/aa")
//	@ApiDataAuthAnno
	@NoLoginAnno
	public ResponseEntity<Object> aa(String a,String b, Integer c, @DateTimeFormat(pattern = "yyyyMMdd")Date d){
		System.out.println(a);
		System.out.println(b);
		System.out.println(c);
		System.out.println(d);
		return ResponseEntity.ok(new Test(a,b,c,d));
	}
	
	@ApiOperation(value = "测试新版用户和接口权限", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = false),
			@ApiImplicitParam(value = "a", name = "a", dataType = "String", required = false),
			@ApiImplicitParam(value = "b", name = "b", dataType = "String", required = false),
			@ApiImplicitParam(value = "c", name = "c", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "d", name = "d", dataType = "String", required = false)
	})
	@PostMapping("/voided")
//	@ApiDataAuthAnno
	@NoLoginAnno
	public void voided(String a,String b, Integer c, @DateTimeFormat(pattern = "yyyyMMdd")Date d){
		System.out.println(a);
		System.out.println(b);
		System.out.println(c);
		System.out.println(d);
	}
	
	@ApiOperation(value = "测试新版用户和接口权限", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = false),
			@ApiImplicitParam(value = "a", name = "a", dataType = "String", required = false),
			@ApiImplicitParam(value = "b", name = "b", dataType = "String", required = false),
			@ApiImplicitParam(value = "c", name = "c", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "d", name = "d", dataType = "String", required = false)
	})
	
	@PostMapping("/b")
//	@ApiDataAuthAnno
	@NoLoginAnno
	public ResponseEntity<Object> b(Test test){
		System.out.println(test.toString());
		return ResponseEntity.ok(test);
	}
	
	@ApiOperation(value = "测试新版用户和接口权限", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = false),
			@ApiImplicitParam(value = "a", name = "a", dataType = "String", required = false),
			@ApiImplicitParam(value = "b", name = "b", dataType = "String", required = false),
			@ApiImplicitParam(value = "c", name = "c", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "d", name = "d", dataType = "String", required = false)
	})
	
	@PostMapping("/d")
//	@ApiDataAuthAnno
	@NoLoginAnno
	public ResponseEntity<Object> d(@RequestBody Test test){
		System.out.println(test.toString());
		return ResponseEntity.ok(test);
	}
	
	/**
	 * 上传附件
	 * 作者 konglingcheng
	 * date 2020年4月7日
	 * @param file
	 * @return
	 */
	@PostMapping(value = "/upload")
	@ApiOperation(value = "上传文件",httpMethod="POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "文件", name = "file", dataType = "MultipartFile", required = true)
	})
	@NoLoginAnno
	public ResponseEntity<Object> upload(@RequestParam(value = "file",required = true)CommonsMultipartFile file){
		System.out.println(file);
		return ResponseEntity.ok();
	}
	
	/**
	 * 上传附件
	 * 作者 konglingcheng
	 * date 2020年4月7日
	 * @param file
	 * @return
	 */
	@PostMapping(value = "/upload1")
	@ApiOperation(value = "上传文件",httpMethod="POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "文件", name = "file", dataType = "MultipartFile", required = true),
			@ApiImplicitParam(value = "b", name = "b", dataType = "String", required = false),
			@ApiImplicitParam(value = "c", name = "c", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "d", name = "d", dataType = "String", required = false)
	})
	@NoLoginAnno
	public ResponseEntity<Object> upload1(@RequestParam(value = "file",required = true)CommonsMultipartFile file, String b, Integer c, @DateTimeFormat(pattern = "yyyyMMdd")Date d){
		System.out.println(b);
		System.out.println(c);
		System.out.println(d);
		return ResponseEntity.ok();
	}
	
	/**
	 * 上传附件
	 * 作者 konglingcheng
	 * date 2020年4月7日
	 * @param file
	 * @return
	 */
	@PostMapping(value = "/upload2")
	@ApiOperation(value = "上传文件",httpMethod="POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "文件", name = "file", dataType = "MultipartFile", required = true),
			@ApiImplicitParam(value = "b", name = "b", dataType = "String", required = false),
			@ApiImplicitParam(value = "c", name = "c", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "d", name = "d", dataType = "String", required = false)
	})
	@NoLoginAnno
	public ResponseEntity<Object> upload2(@RequestParam(value = "file",required = true)CommonsMultipartFile file, Test test){
		System.out.println(test.toString());
		return ResponseEntity.ok(test);
	}
	
	/**
	 * 上传附件
	 * 作者 konglingcheng
	 * date 2020年4月7日
	 * @param file
	 * @return
	 */
	@PostMapping(value = "/upload3")
	@ApiOperation(value = "上传文件",httpMethod="POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "文件", name = "file", dataType = "MultipartFile", required = true),
			@ApiImplicitParam(value = "b", name = "b", dataType = "String", required = false),
			@ApiImplicitParam(value = "c", name = "c", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "d", name = "d", dataType = "String", required = false)
	})
	@NoLoginAnno
	public ResponseEntity<Object> upload3(@RequestParam(value = "file",required = true)CommonsMultipartFile file,@RequestBody Test test){
		System.out.println(test.toString());
		return ResponseEntity.ok(test);
	}
	
	@GetMapping(value = "/lock")
	@ApiOperation(value = "锁测试",httpMethod="GET")
	@NoLoginAnno
	public ResponseEntity<Object> lock() throws Exception{
		ZookeeperShareLock lock = new ZookeeperShareLock(this.client, "/test/lock", null);
		long start = System.currentTimeMillis();
		lock.tryLock();
		lock.unLock();
		long end = System.currentTimeMillis();
		return ResponseEntity.ok((end - start) / 1000);
	}
}
