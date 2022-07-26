package com.jtyjy.finance.manager.utils;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.util.UrlPathHelper;

/**
 * 请求响应工具
 * @author User
 *
 */
public class RequestAnswerTool {
	
	/**
	 * <p>错误提示页面html</p>
	 * 作者 konglingcheng
	 * date 2020年6月19日
	 * <p>@param message
	 * <p>@return</p>
	 */
	private static String getForm(String message, String title) {
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html>").append("\r\n");
		sb.append("<html>").append("\r\n");
		sb.append("<head>").append("\r\n");
		sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">").append("\r\n");
		sb.append("<title>"+title+"</title>").append("\r\n");
		sb.append("<script type=\"text/javascript\">").append("\r\n");
		sb.append("alert('"+message+"');").append("\r\n");
		sb.append("window.close();").append("\r\n");
		sb.append("</script>").append("\r\n");
		sb.append("</head>").append("\r\n");
		sb.append("<body>").append("\r\n");
		sb.append("</body>").append("\r\n");
		sb.append("</html>").append("\r\n");
		return sb.toString();
	}
	
	
	/**
	 * <p>直接输出页面</p>
	 * 作者 konglingcheng
	 * date 2020年6月19日
	 * <p>@param message
	 * <p>@param response
	 * <p>@throws Exception</p>
	 */
	public static void page(String message, String title,HttpServletResponse response) throws Exception {
		String body = getForm(message,title);
		response.setContentType("text/html;charset=UTF-8");
		response.getWriter().write(body);// 直接将完整的表单html输出到页面
		response.getWriter().flush();
		response.getWriter().close();
	}
	
	/**
     * 获得当前访问的URL路径
     * @param request
     * @return
     */
    public static String getUrlSAndParameter(HttpServletRequest request) {
        UrlPathHelper helper = new UrlPathHelper();
        StringBuilder buff = new StringBuilder();
        buff.append(request.getRequestURI());
        String queryString = helper.getOriginatingQueryString(request);
        if (queryString != null) {
            buff.append("?").append(queryString);
        }
        try {
            return new String(buff.toString().getBytes(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return buff.toString();
        }
    }

}
