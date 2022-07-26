package com.jtyjy.finance.manager.service;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author minzhq
 *
 */
public interface ImportBaseInterface {
	/**
	 * 
	 * @param row 最小为1
	 * @param data
	 * @param importType
	 */
	Object validate(Integer row,Map<Integer, String> data,String importType,Object head,Object... params);
	
	/**
	 * 批量保存数据
	 * @param successMap
	 * @param importType
	 * @param errorMap
	 * @param headErrorMsg 
	 */
	void saveData(Map<Integer, Map<Integer, String>> successMap,String importType
			,Map<Integer, Map<Integer, String>> errorMap, List<String> headErrorMsg,Object head,List<Object> details,Object... params);
}
