package com.jtyjy.finance.manager.config;

import com.jtyjy.core.startup.anno.EnableComponent;
import com.jtyjy.core.tools.DBTools;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = {"com.jtyjy.finance.manager", "com.jtyjy.core.api"})
@PropertySource(value = {"classpath:application.properties"})
@SpringBootApplication(exclude = {MultipartAutoConfiguration.class})
@EnableComponent(fastDfs = true, xxlJob = true, redis = true)
@EnableAsync
public class MainApplication extends SpringBootServletInitializer {

    /************mysql配置*******************/
    @Value("${db.username}")
    private String username;
    @Value("${db.password}")
    private String passwd;
    @Value("${url}")
    private String jdbcUrl;
    @Value("${driver}")
    private String driverClassName;

    @Value("${hr.db.username}")
    private String hrusername;
    @Value("${hr.db.password}")
    private String hrpasswd;
    @Value("${hr.url}")
    private String hrjdbcUrl;
    @Value("${hr.driver}")
    private String hrdriverClassName;

    @Value("${maxPoolSize}")
    private Integer maxPoolSize;
    @Value("${minIdle}")
    private Integer minIdle;
    @Value("${validationTimeout}")
    private Integer validationTimeout;
    @Value("${idleTimeout}")
    private Integer idleTimeout;

    /**
     * oa的配置
     */
    @Value("${oracle.username}")
    private String oracle_username;
    @Value("${oracle.password}")
    private String oracle_password;
    @Value("${oracle.url}")
    private String oracle_url;
    @Value("${oracle.driver}")
    private String oracle_driver;

    @Bean(destroyMethod = "close", name = "defaultDataSource")
    public DataSource defaultDataSource() throws Exception {
        String query = "SELECT 1 FROM DUAL";
        return DBTools.dataSource(driverClassName, jdbcUrl, username, passwd, query, maxPoolSize, minIdle, validationTimeout, idleTimeout);
    }

    @Bean(destroyMethod = "close", name = "hrDataSource")
    public DataSource hrDataSource() throws Exception {
        String query = "SELECT 1 FROM DUAL";
        return DBTools.dataSource(hrdriverClassName, hrjdbcUrl, hrusername, hrpasswd, query, maxPoolSize, minIdle, validationTimeout, idleTimeout);
    }

    @Bean(destroyMethod = "close",name = "oracleDataSource")
    public DataSource oracleDataSource() throws Exception {
        String query = "SELECT 1 FROM DUAL";
        return DBTools.dataSource(oracle_driver, oracle_url, oracle_username, oracle_password, query, maxPoolSize, minIdle, validationTimeout, idleTimeout);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(MainApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

}
