package com.jtyjy.finance.manager.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.jtyjy.core.jdbc.JdbcTemplateService;
import com.jtyjy.core.spring.SpringTools;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Objects;

@Configuration
@MapperScan(basePackages = "com.jtyjy.finance.manager.mapper", sqlSessionTemplateRef = "defaultSqlSessionTemplate")
public class MyBatisConfig {

    @javax.annotation.Resource(name = "defaultDataSource")
    private DataSource dataSource;

    @Bean(name = "defaultSqlSessionFactoryBean")
    @Primary
    public SqlSessionFactory defaultSqlSessionFactoryBean() throws Exception {
        MybatisSqlSessionFactoryBean sqlSessionFactoryBean = null;
        try {
            sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
            // 设置数据源
            sqlSessionFactoryBean.setDataSource(dataSource);
            // 设置mybatis的主配置文件
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            //Resource mybatisConfigXml = resolver.getResource("classpath:mybatis/mybatis-config.xml");
            //sqlSessionFactoryBean.setConfigLocation(mybatisConfigXml);

            MybatisConfiguration configuration = new MybatisConfiguration();
            configuration.setJdbcTypeForNull(JdbcType.NULL);
            configuration.setMapUnderscoreToCamelCase(true);
            //清除一级缓存
            configuration.setLocalCacheScope(LocalCacheScope.STATEMENT);
            //清除二级缓存
            configuration.setCacheEnabled(false);
            // 配置打印sql语句
            //configuration.setLogImpl(StdOutImpl.class);
            //configuration.setMapUnderscoreToCamelCase(true);
            sqlSessionFactoryBean.setConfiguration(configuration);

            // 设置别名包
            sqlSessionFactoryBean.setTypeAliasesPackage("com.jtyjy.finance.manager.bean,com.jtyjy.finance.manager.mapper.response");
            //设置分页插件
            PaginationInterceptor page = new PaginationInterceptor();
            page.setDbType(DbType.MYSQL);
            sqlSessionFactoryBean.setPlugins(page);
            //配置mapper的扫描，找到所有的mapper.xml映射文件
            Resource[] resources = resolver.getResources("classpath:/mybatis/mappers/*.xml");
            sqlSessionFactoryBean.setMapperLocations(resources);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Objects.requireNonNull(sqlSessionFactoryBean).getObject();
    }

    @Bean("defaultSqlSessionTemplate")
    public SqlSessionTemplate defaultSqlSessionTemplate() throws Exception {
        return new SqlSessionTemplate(defaultSqlSessionFactoryBean());
    }

    /**
     * 事务管理器
     */
    @Bean("defaultTransactionManager")
    @Primary
    public PlatformTransactionManager primaryTransactionManager() {
        return new DataSourceTransactionManager(this.dataSource);
    }

    @Bean(name = "defaultJdbcTemplateService")
    public JdbcTemplateService jdbcTemplate() {
        return new JdbcTemplateService(this.dataSource);
    }

}
