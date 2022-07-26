package com.jtyjy.finance.manager.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.bean.Test;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.mapper.TestMapper;

import lombok.RequiredArgsConstructor;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TestService extends DefaultBaseService<TestMapper, Test> {

	private final TabChangeLogMapper loggerMapper;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("test"));
	}

	public void test() {
		Test test = new Test();
		test.setStr("12345678");
		this.save(test);
		List<Test> list = this.list();
		for (Test test2 : list) {
			System.out.println(test2);
		}
	}
	public void test2() {
		List<Test> list = this.list();
		for (Test test2 : list) {
			System.out.println(test2+".................................");
		}
	}
	
	public static void main(String[] args) {
        Boolean a = true;
        Boolean b = false;
        Boolean c = true;
        Boolean d = false;
        Boolean ab = a == b;
        Boolean ac = a == c;
        Boolean bc = b == c;
        Boolean bd = b == d;
        Boolean cd = c == d;
        if (ab || cd) {
           System.out.println(666); 
        }
        if (ab && cd) {
            System.err.print(777);; 
        }
    }
	
}
