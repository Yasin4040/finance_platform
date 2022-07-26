package com.jtyjy.finance.manager.controller;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

public class BaseController<T> {

	private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	/**
	 * 获取绑定结果
	 * 作者 konglingcheng
	 * date 2020年4月2日
	 * @param bindingResult
	 * @return
	 */
	public String getResult(BindingResult bindingResult) {
		StringBuilder sb = new StringBuilder();
		if(bindingResult.hasErrors()) {
			List<ObjectError> allErrors = bindingResult.getAllErrors();
			for (ObjectError objectError : allErrors) {
				sb.append(objectError.getDefaultMessage()).append("；");
			}
		}
		String result = sb.toString();
		return result;
	}
	
	/**
	 * <p>手动校验单个对象</p>
	 * 作者 konglingcheng
	 * date 2020年6月1日
	 * <p>@param <Q>
	 * <p>@param t
	 * <p>@return</p>
	 */
	public static <Q> String validate(Q t) {
		Set<ConstraintViolation<Q>> set = validator.validate(t, Default.class);
		if(set == null || set.size() == 0) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (ConstraintViolation<Q> constraintViolation : set) {
			sb.append(constraintViolation.getMessage()).append("；");
		}
		return sb.toString();
	}
		   
    /**
     * 手动校验单个对象
     * @param <Q>
     * @param t
     * @param errMsg 错误信息
     * @return 校验错误字段个数
     */
    public static <Q> int validate(Q t, StringBuilder errMsg) {
        Set<ConstraintViolation<Q>> set = validator.validate(t, Default.class);
        if(set == null || set.size() == 0) {
            return 0;
        }
        for (ConstraintViolation<Q> constraintViolation : set) {
            errMsg.append(constraintViolation.getMessage()).append("；");
        }
        return set.size();
    }
    
	/**
	 * <p>手动校验集合</p>
	 * 作者 konglingcheng
	 * date 2020年6月1日
	 * <p>@param <Q>
	 * <p>@param list
	 * <p>@return</p>
	 */
	public static <Q> String validateList(List<Q> list) {
		if(list == null || list.size() == 0) {
			return "所有对象均为空";
		}
		StringBuilder sb = new StringBuilder();
		String result = null;
		int number = 1;
		for (Q q : list) {
			result = validate(q);
			if(StringUtils.isNotEmpty(result)) {
				sb.append("第"+number+"条数据校验不通过：【"+result+"】").append("<br>");
			}
			number++;
		}
		return sb.toString();
	}
}
