package com.jtyjy.finance.manager.test;

import javax.annotation.PostConstruct;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;

import com.jtyjy.core.redis.RedisClient;
import com.klcwqy.easy.lock.impl.ZookeeperShareLock;

//@Component
public class LockTest {

	@Autowired
	private RedisClient redis;
	
	@Autowired
	private CuratorFramework client;
	
	private ZookeeperShareLock lock;
	
	@PostConstruct
	public void init(){
		this.lock = new ZookeeperShareLock(this.client, "/finance-platform/test1", null);
		this.run();
	}

	private void run(){
		try {
			while(true) {
				this.lock.tryLock();
				String count = this.redis.get("lock");
				System.out.println(count);
				if(Integer.parseInt(count) <= 0) {
					this.lock.unLock();
					return;
				}
//				Thread.sleep(1000);
				this.redis.set("lock", (Integer.parseInt(count)-1)+"");
				this.lock.unLock();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
