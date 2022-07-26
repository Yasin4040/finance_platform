package com.jtyjy.finance.manager.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import com.jtyjy.common.tools.ClassTools;

/**
 * 对象属性工具类
 * @author User
 *
 */
public class BeanFieldTool {
	
	private static final String CONCAT= "BFT";

	/**
	 * 属性重复校验
	 * @param fieldNames 要校验的属性数组
	 * @return
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public static final <T> boolean simpleDuplicateField(List<T> beans, String... fieldNames) throws Exception {
		if(beans.isEmpty()) {
			return Boolean.FALSE;
		}
		Class<?> clazz = beans.get(0).getClass();
		Map<String, Field> fieldMap = ClassTools.getAllFieldMap(clazz);
		if(fieldMap.isEmpty()) {
			return Boolean.FALSE;
		}
		//执行比较
		Set<String> values = new HashSet<String>();
		Field field = null;
		Object value = null;
		String _value = null;
		for (T bean : beans) {
			StringJoiner sj = new StringJoiner(CONCAT);
			for (String fieldName : fieldNames) {
				field = fieldMap.get(fieldName.toUpperCase());
				field.setAccessible(true);
				if(field != null) {
					value = field.get(bean);
					if(value != null) {
						sj.add(value.toString().trim());
					}
				}
			}
			//判断是否重复
			_value = sj.toString();
			if(values.contains(_value)) {
				return Boolean.TRUE;
			}
			values.add(_value);
		}
		return Boolean.FALSE;
	}
	
	static class Demo{
		private Integer age;
		private String name;
		public Integer getAge() {
			return age;
		}
		public void setAge(Integer age) {
			this.age = age;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Demo(Integer age, String name) {
			super();
			this.age = age;
			this.name = name;
		}
	}
	
	public static void main(String[] args) throws Exception {
		List<Demo> beans = new ArrayList<Demo>();
		beans.add(new Demo(1, "张三"));
		beans.add(new Demo(2, "张三"));
		System.out.println(BeanFieldTool.simpleDuplicateField(beans, "name","age"));
	}
}
