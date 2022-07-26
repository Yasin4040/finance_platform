package com.jtyjy.finance.manager.cache;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * 缓存基类
 * 作者 konglingcheng
 * 日期 2020年7月9日
 */
public abstract class BaseCache {

	/**
	 * 缓存
	 * 作者:konglingcheng
	 * 日期:2020年7月9日
	 */
	public abstract void cache() throws Exception;
	
	/**
	 * 重新缓存
	 * 作者:konglingcheng
	 * 日期:2020年7月9日
	 */
	public abstract void recache() throws Exception;
	
	/**
	 * 根据list设置map缓存
	 * 作者:konglingcheng
	 * 日期:2020年7月9日
	 * @param <T>
	 * @param list
	 * @param map
	 * @param propName 作为key的属性名
	 * @param clazz map值的类型
	 * @throws Exception 
	 */
	public static <T> void setMapByList(List<T> list, Map<String, T> map, String propName, Class<T> clazz) throws Exception {
		if (list != null && list.size() > 0) {
			if(StringUtils.isBlank(propName)) {
				throw new Exception("属性名不能为空");
			}
			Field field = clazz.getDeclaredField(propName);
			if(field == null) {
				throw new Exception("属性名在类中不存在");
			}
			field.setAccessible(true);
			for (T t : list) {
				map.put(field.get(t).toString(), t);
			}
		}
	}
}
