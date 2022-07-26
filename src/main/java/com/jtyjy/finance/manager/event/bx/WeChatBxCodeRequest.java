package com.jtyjy.finance.manager.event.bx;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.jtyjy.finance.manager.service.WeChatService;

/**
 * 企业微信扫描报销单请求参数
 * @author User
 *
 */
public class WeChatBxCodeRequest{

	public final static BxCodeRequest getRequest(HttpServletRequest request,WeChatService service) throws Exception {
		String code = request.getParameter("code");
		if(StringUtils.isBlank(code)) {
			throw new Exception("获取企业微信用户失败，code为空！");
		}
		String accessToken = service.getAccessToken();
		String empNo = service.getEmpNo(accessToken, code);
		if(StringUtils.isBlank(empNo)) {
			throw new Exception("获取员工工号失败！");
		}
		String ps = request.getParameter("c");
		if(StringUtils.isBlank(ps)) {
			throw new Exception("获取请求参数失败，参数为空！");
		}
		String[] orderIdAndVersion = ps.split("-");
		if(orderIdAndVersion.length != 2) {
			throw new Exception("参数格式错误！");
		}
		return new BxCodeRequest(orderIdAndVersion[0], empNo, orderIdAndVersion[1]);
	}
}
