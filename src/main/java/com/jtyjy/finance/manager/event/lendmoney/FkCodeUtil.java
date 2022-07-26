package com.jtyjy.finance.manager.event.lendmoney;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.jtyjy.finance.manager.service.WeChatService;

/**
 * 企业微信扫描报销单请求参数
 * @author User
 *
 */
public class FkCodeUtil{

	public final static FkCodeRequest getWeChatRequest(HttpServletRequest request,WeChatService service) throws Exception {
		String code = request.getParameter("code");
		if(StringUtils.isBlank(code)) {
			throw new Exception("获取企业微信用户失败，code为空！");
		}
		String accessToken = service.getAccessToken();
		String empNo = service.getEmpNo(accessToken, code);
		if(StringUtils.isBlank(empNo)) {
			throw new Exception("获取员工工号失败！");
		}
		String requestId = request.getParameter("id");
		if(StringUtils.isBlank(requestId)) {
			throw new Exception("获取请求参数失败，参数为空！");
		}
		return new FkCodeRequest(requestId, empNo);
	}
	   
    public final static FkCodeRequest getGunRequest(HttpServletRequest request) throws Exception {
        String ps = request.getParameter("id");
        if(StringUtils.isBlank(ps)) {
            throw new Exception("获取请求参数失败，参数为空！");
        }
        String[] orderIdAndVersion = ps.split("-");
        if(orderIdAndVersion.length != 3) {
            throw new Exception("参数格式错误！");
        }
        return new FkCodeRequest(orderIdAndVersion[1], orderIdAndVersion[3]);
    }
}
