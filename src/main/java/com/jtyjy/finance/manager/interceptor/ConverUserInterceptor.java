package com.jtyjy.finance.manager.interceptor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jtyjy.core.interceptor.BaseUser;
import com.jtyjy.core.interceptor.LoginThreadLocal;
import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.finance.manager.bean.WbUser;
import com.jtyjy.finance.manager.service.WbUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * 普通用户与预算用户转换拦截器
 *
 * @author konglingcheng
 */
@Component
public class ConverUserInterceptor implements HandlerInterceptor, InitializingBean {

    @Autowired
    private WbUserService wbUserService;

    @Value("${token.prefix}")
    private String tokenPrefix;

    @Autowired
    private RedisClient redis;

    private ConcurrentHashMap<String, WbUser> userMap = new ConcurrentHashMap<String, WbUser>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            BaseUser baseUser = LoginThreadLocal.get();
            if (baseUser != null) {
                String token = request.getParameter("token");
                if (StringUtils.isNotBlank(token)) {
                    //从缓存中命中
                    WbUser wbUser = this.userMap.get(token);
                    if (wbUser == null) {
                        //查询财务系统用户,并缓存
                        wbUser = this.wbUserService.getOne(new QueryWrapper<WbUser>().eq("USER_NAME", baseUser.getEmpno()));
                        if (wbUser == null) {
                            return false;
                        }
                        userMap.put(token, wbUser);
                    }
                    UserThreadLocal.set(wbUser);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserThreadLocal.remove();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //不干啥
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
        pool.scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                //清除过期的缓存
                clearCache();
            }
        }, 0L, 10, TimeUnit.SECONDS);
    }

    /**
     * 清除过期的缓存
     */
    public void clearCache() {
        Enumeration<String> keys = this.userMap.keys();
        String key = null;
        String value = null;
        while (keys.hasMoreElements()) {
            key = keys.nextElement();
            value = this.redis.get(tokenPrefix + key);
            if (StringUtils.isEmpty(value)) {
                this.userMap.remove(key);
            }
        }
    }
}
