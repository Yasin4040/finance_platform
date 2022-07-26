package com.jtyjy.finance.manager.config;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置分布式锁
 * @author User
 *
 */
@Configuration
public class LockConfig {
	
	@Value("${zk.url}")
    private String zk_url;
	
	@Bean
	public CuratorFramework curatorFramework() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(this.zk_url,5000, 5000, retryPolicy);
        client.start();
        return client;
	}

}
