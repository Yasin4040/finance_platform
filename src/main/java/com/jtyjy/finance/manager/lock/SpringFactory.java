package com.jtyjy.finance.manager.lock;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Description:
 * Created by ZiYao Lee on 2022/09/21.
 * Time: 11:36
 */
@Component
public class SpringFactory implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    public SpringFactory() {
    }

    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        applicationContext = ctx;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static <T> T getBean(Class<T> beanClass) {
        return applicationContext.getBean(beanClass);
    }

    public static <T> T getBean(String beanName, Class<T> beanClass) {
        return applicationContext.getBean(beanName, beanClass);
    }

    public static <T> T getBean(String beanName) {
        return (T)applicationContext.getBean(beanName);
    }
}
