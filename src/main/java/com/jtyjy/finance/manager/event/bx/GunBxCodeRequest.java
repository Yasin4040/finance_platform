package com.jtyjy.finance.manager.event.bx;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

public class GunBxCodeRequest {
	
	public final static BxCodeRequest getRequest(HttpServletRequest request) throws Exception {
		String ps = request.getParameter("c");
		if(StringUtils.isBlank(ps)) {
			throw new Exception("获取请求参数失败，参数为空！");
		}
		String[] orderIdAndVersion = ps.split("-");
		if(orderIdAndVersion.length != 4) {
			throw new Exception("参数格式错误！");
		}
		return new BxCodeRequest(orderIdAndVersion[1], orderIdAndVersion[3], orderIdAndVersion[2]);
	}

}
