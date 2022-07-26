package com.jtyjy.finance.manager.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.core.auth.anno.ApiDataAuthAnno;
import com.jtyjy.core.interceptor.BaseUser;
import com.jtyjy.core.interceptor.LoginThreadLocal;
import com.jtyjy.core.local.JdbcSqlThreadLocal;
import com.jtyjy.core.result.ResponseResult;
import com.jtyjy.core.tools.HttpClientTool;
import com.jtyjy.core.tools.ResponseTool;
import com.jtyjy.finance.manager.utils.HttpUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @description:
 * @author: gll
 * @create: 2021-12-13 13:23
 */
public class NewApiDataAuthInterceptor implements HandlerInterceptor {

    private final Long appId;
    private final String auth_sql_url;

    public NewApiDataAuthInterceptor(Long appId, String auth_sql_url) {
        this.appId = appId;
        this.auth_sql_url = auth_sql_url;
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        ApiDataAuthAnno authAnno = null;
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod)handler;
            Method method = handlerMethod.getMethod();
            authAnno = (ApiDataAuthAnno)method.getAnnotation(ApiDataAuthAnno.class);
            if (authAnno == null) {
                return true;
            }
        }

        String uri = request.getRequestURI();
        if (authAnno != null) {
            BaseUser user = LoginThreadLocal.get();
            if (user == null) {
                ResponseResult _result = ResponseResult.apply(StatusCodeEnmus.NO_LOGIN);
                ResponseTool.setJsonMessage(response, _result, HttpStatus.OK.value());
                return false;
            }

            String url = this.auth_sql_url + "?serverId=" + this.appId + "&api=" + uri + "&userId=" + user.getEmpid();
            String json = HttpUtil.doGet(url);
            //String json = HttpClientTool.getRequest(url);
            if (StringUtils.isEmpty(json)) {
                ResponseResult _result = ResponseResult.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL, "获取权限失败");
                ResponseTool.setJsonMessage(response, _result, HttpStatus.OK.value());
                return false;
            }

            JSONObject obj = JSONObject.parseObject(json);
            if (obj.getInteger("code") != 0) {
                ResponseResult _result = ResponseResult.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL, "获取权限失败");
                ResponseTool.setJsonMessage(response, _result, HttpStatus.OK.value());
                return false;
            }

            String sql = obj.getString("data");
            JdbcSqlThreadLocal.set(sql);
        }

        return true;
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        JdbcSqlThreadLocal.set((String)null);
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
