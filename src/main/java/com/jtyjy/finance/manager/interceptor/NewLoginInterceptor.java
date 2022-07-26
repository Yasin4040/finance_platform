package com.jtyjy.finance.manager.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.core.auth.anno.AuthAnno;
import com.jtyjy.core.auth.anno.NoLoginAnno;
import com.jtyjy.core.constant.Constants;
import com.jtyjy.core.interceptor.ApiNoLoginConfig;
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

public class NewLoginInterceptor implements HandlerInterceptor {

    private final String userInfoUrl;
    private ApiNoLoginConfig config;

    public NewLoginInterceptor(String userInfoUrl, ApiNoLoginConfig config) {
        this.userInfoUrl = userInfoUrl;
        this.config = config;
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        ResponseTool.setSessionSame(request, response);
        String uri = request.getRequestURI();
        if (this.config.getApis() != null && this.config.getApis().size() > 0 && this.config.getApis().contains(uri)) {
            return true;
        } else if (!uri.contains("swagger") && !uri.contains("doc.html") && !uri.contains("webjars")) {
            if(uri.startsWith("/openapi")){
                //对外开放接口不需要进行登录验证
                return true;
            }
            AuthAnno authAnno = null;
            if (handler instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod)handler;
                Method method = handlerMethod.getMethod();
                NoLoginAnno notNeedLogin = (NoLoginAnno)method.getAnnotation(NoLoginAnno.class);
                authAnno = (AuthAnno)method.getAnnotation(AuthAnno.class);
                if (notNeedLogin != null) {
                    return true;
                }
            }

            String token = request.getParameter("token");
            String result = HttpUtil.doGet(this.userInfoUrl + token);
            JSONObject _json = JSONObject.parseObject(result);
            if (_json.getInteger("code") != StatusCodeEnmus.REQUEST_SUCCESS.getCode()) {
                ResponseResult _result = ResponseResult.apply(StatusCodeEnmus.OTHER, "获取用户信息失败");
                ResponseTool.setJsonMessage(response, _result, HttpStatus.OK.value());
                return false;
            } else {
                String redis_yh = _json.getString("data");
                BaseUser user = null;
                if (StringUtils.isNotBlank(redis_yh)) {
                    user = (BaseUser)Constants.MAPPER.readValue(redis_yh, BaseUser.class);
                }

                if (user == null) {
                    ResponseResult _result = ResponseResult.apply(StatusCodeEnmus.NO_LOGIN);
                    ResponseTool.setJsonMessage(response, _result, HttpStatus.OK.value());
                    return false;
                } else {
                    LoginThreadLocal.set(user);
                    return true;
                }
            }
        } else {
            return true;
        }
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        LoginThreadLocal.set((BaseUser)null);
        JdbcSqlThreadLocal.set((String)null);
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
