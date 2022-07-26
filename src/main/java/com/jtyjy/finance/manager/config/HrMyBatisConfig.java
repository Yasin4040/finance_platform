package com.jtyjy.finance.manager.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
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

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.jtyjy.core.jdbc.JdbcTemplateService;
import com.jtyjy.core.spring.SpringTools;

@Configuration
@MapperScan(basePackages = "com.jtyjy.finance.manager.hrmapper",sqlSessionTemplateRef = "hrSqlSessionTemplate")
public class HrMyBatisConfig {

	@javax.annotation.Resource(name="hrDataSource")
    private DataSource hrDataSource;
    
    @Bean(name="hrSqlSessionFactoryBean")
    @Primary
    public SqlSessionFactory hrSqlSessionFactoryBean() throws Exception {
    	MybatisSqlSessionFactoryBean sqlSessionFactoryBean = null;
		try {
			sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
			// 设置数据源
			sqlSessionFactoryBean.setDataSource(hrDataSource);
			// 设置mybatis的主配置文件
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource mybatisConfigXml = resolver.getResource("classpath:mybatis/mybatis-config.xml");
			sqlSessionFactoryBean.setConfigLocation(mybatisConfigXml);
			// 设置别名包
			sqlSessionFactoryBean.setTypeAliasesPackage("com.jtyjy.finance.manager.hrbean");
			//设置分页插件
			PaginationInterceptor page = new PaginationInterceptor();
		    page.setDbType(DbType.MYSQL);
			sqlSessionFactoryBean.setPlugins(page);
			//配置mapper的扫描，找到所有的mapper.xml映射文件
			Resource[] resources = resolver.getResources("classpath:/mybatis/hrmappers/*.xml");
			sqlSessionFactoryBean.setMapperLocations(resources);
		} catch (Exception e) {
			e.printStackTrace();
		}

        return sqlSessionFactoryBean.getObject();
    }
    
    @Bean("hrSqlSessionTemplate")
    public SqlSessionTemplate hrSqlSessionTemplate() throws Exception{
		SqlSessionTemplate template = new SqlSessionTemplate(hrSqlSessionFactoryBean());
		return template;
    }
    
    /**
     * 事务管理器
     * 作者 konglingcheng
     * date 2020年4月23日
     * @return
     */
	@Bean("hrTransactionManager")
	public PlatformTransactionManager primaryTransactionManager() {
	    return new DataSourceTransactionManager(this.hrDataSource);
	}
	
	@Bean(name = "hrJdbcTemplateService")
    public JdbcTemplateService jdbcTemplate() {
    	return new JdbcTemplateService(this.hrDataSource);
    }
	
}
