package com.jtyjy.finance.manager.config;


import com.jtyjy.core.interceptor.ApiNoLoginConfig;
import com.jtyjy.core.spring.SpringTools;
import com.jtyjy.finance.manager.filter.YkServerFilter;
import com.jtyjy.finance.manager.interceptor.ConverUserInterceptor;
import com.jtyjy.finance.manager.interceptor.NewApiDataAuthInterceptor;
import com.jtyjy.finance.manager.interceptor.NewLoginInterceptor;
import com.jtyjy.finance.manager.yk.YkArg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * <p>默认mvc配置</p>
 * <p>作者 konglingcheng</p>
 * <p>date 2020年6月22日</p>
 *
 * @author User
 */
@Configuration
public class SpringMVCConfig extends WebMvcConfigurationSupport {

    @Value("${spring.upload.tmp.path}")
    private String springUploadTmpPath;
    @Value("${app.id}")
    private Long appId;
    @Value("${auth.sql.url}")
    private String authSqlUrl;
    @Value("${hit.flush.uesr.info.url}")
    private String userInfoUrl;
    @Autowired
    private ApiNoLoginConfig config;
    @Autowired
    private ConverUserInterceptor converUserInterceptor;

    /**
     * 文件上传下载
     */
    @Bean
    public MultipartResolver multipartConfigElement() throws Exception {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setDefaultEncoding("utf-8");
        resolver.setMaxUploadSize(1048576000000L);
        resolver.setMaxInMemorySize(10240000);
        resolver.setUploadTempDir(new FileSystemResource(springUploadTmpPath));
        resolver.setResolveLazily(true);
        return resolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 判断用户是否登录的拦截器
        registry.addInterceptor(new NewLoginInterceptor(this.userInfoUrl, this.config)).addPathPatterns("/**");
        //registry.addInterceptor(new LoginInterceptor(this.userInfoUrl, this.config)).addPathPatterns("/**");
        //接口数据权限拦截器
        registry.addInterceptor(new NewApiDataAuthInterceptor(this.appId, this.authSqlUrl)).addPathPatterns("/**");
        //registry.addInterceptor(new ApiDataAuthInterceptor(this.appId, this.authSqlUrl)).addPathPatterns("/**");
        //普通用户转换财务系统用户拦截器
        registry.addInterceptor(this.converUserInterceptor).addPathPatterns("/**");
     }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/META-INF/resources/").setCachePeriod(0);
        super.addResourceHandlers(registry);
    }

    @Bean
    public FilterRegistrationBean<YkServerFilter> registFilter() {
        FilterRegistrationBean<YkServerFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new YkServerFilter(springTools(),YkArg.port));
        registration.addUrlPatterns("*.123");
        return registration;
    }

    @Bean
    public SpringTools springTools() {
        return new SpringTools();
    }
}
