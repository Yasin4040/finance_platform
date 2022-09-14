package com.jtyjy.finance.manager.utils;


import com.iamxiongx.util.message.exception.BusinessException;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.util.ObjectUtils;

import java.util.*;


/**
 * bean和map互转 工具类
 * <p>
 * 使用到spring的cglib
 * Created by ZiYao Lee on 2022/09/14.
 */
public class BeanMapUtil {

    /**
     * 将Bean转为Map
     *
     * @param bean
     * @param <T>
     * @return
     */
    public static <T> Map<String, ?> beanToMap(T bean) {
        BeanMap beanMap = BeanMap.create(bean);
        Map<String, Object> map = new HashMap<>();
        beanMap.forEach((key, value) -> map.put(String.valueOf(key), value));
        return map;
    }

    /**
     * 将Map转为Bean
     *
     * @param map
     * @param beanClazz
     * @param <T>
     * @return
     */
    public static <T> T mapToBean(Map<String, ?> map, Class<T> beanClazz) {
        T bean;
        try {
            bean = beanClazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            throw new BusinessException("Map集合转换到Bean失败");
        }
        BeanMap beanMap = BeanMap.create(bean);
        beanMap.putAll(map);
        return bean;
    }

    /**
     * 将一组Beans转为List
     *
     * @param dataList
     * @param <T>
     * @return
     */
    public static <T> List<Map<String, ?>> beansToMaps(List<T> dataList) {
        List<Map<String, ?>> list = new ArrayList<>();
        if (ObjectUtils.isEmpty(dataList)) {
            return Collections.emptyList();
        }
        Map<String, ?> map;
        T bean;
        for (T t : dataList) {
            bean = t;
            map = beanToMap(bean);
            list.add(map);
        }
        return list;
    }

    /**
     * 将一组Map转为一组Beans
     *
     * @param dataMaps
     * @param beanClazz
     * @param <T>
     * @return
     */
    public static <T> List<T> mapsToBeans(List<Map<String, ?>> dataMaps, Class<T> beanClazz) {
        List<T> list = new ArrayList<>();
        if (ObjectUtils.isEmpty(dataMaps)) {
            return Collections.emptyList();
        }
        Map<String, ?> map;
        for (Map<String, ?> dataMap : dataMaps) {
            map = dataMap;
            T bean = mapToBean(map, beanClazz);
            list.add(bean);
        }
        return list;
    }
}
