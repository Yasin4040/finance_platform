package com.jtyjy.finance.manager.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.jtyjy.core.jdbc.JdbcTemplateService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "com.jtyjy.finance.manager.oadao",sqlSessionTemplateRef = "oracleSqlSessionTemplate")
public class OracleMyBatisConfig {

    
	@javax.annotation.Resource(name="oracleDataSource")
    private DataSource dataSource;
	
	 
	@Bean(name="oracleSqlSessionFactoryBean")
    public SqlSessionFactory oracleSqlSessionFactoryBean() throws Exception {
    	MybatisSqlSessionFactoryBean sqlSessionFactoryBean = null;
		try {
			sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
			// 设置数据源
			sqlSessionFactoryBean.setDataSource(dataSource);
			// 设置mybatis的主配置文件
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource mybatisConfigXml = resolver.getResource("classpath:mybatis/mybatis-config.xml");
			sqlSessionFactoryBean.setConfigLocation(mybatisConfigXml);
			// 设置别名包
			sqlSessionFactoryBean.setTypeAliasesPackage("com.jtyjy.finance.manager.oapojo");
			//设置分页插件
			PaginationInterceptor page = new PaginationInterceptor();
		    page.setDbType(DbType.ORACLE);
			sqlSessionFactoryBean.setPlugins(page);

			Resource[] resources = resolver.getResources("classpath:/mybatis/oamappers/*.xml");
			sqlSessionFactoryBean.setMapperLocations(resources);
		} catch (Exception e) {
			e.printStackTrace();
		}

        return sqlSessionFactoryBean.getObject();
    }
    
    @Bean("oracleSqlSessionTemplate")
    public SqlSessionTemplate defaultSqlSessionTemplate() throws Exception{
		SqlSessionTemplate template = new SqlSessionTemplate(oracleSqlSessionFactoryBean());
		return template;
    }
    
    /**
     * 事务管理器
     * 作者 konglingcheng
     * date 2020年4月23日
     * @return
     */
	@Bean("oracleTransactionManager")
	public PlatformTransactionManager primaryTransactionManager() {
	    return new DataSourceTransactionManager(this.dataSource);
	}
	
	@Bean(name = "oracleJdbcTemplateService")
    public JdbcTemplateService jdbcTemplate() {
    	return new JdbcTemplateService(this.dataSource);
    }
}
