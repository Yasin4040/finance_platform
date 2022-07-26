package com.jtyjy.finance.manager.easyexcel;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.excel.annotation.ExcelProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.jtyjy.finance.manager.service.ImportBaseInterface;
import org.springframework.util.CollectionUtils;

/**
 * 通用导入监听器
 *
 * @author minzhq
 */

public class EasyExcelImportListener extends AnalysisEventListener<Map<Integer, String>> {
	//定义每多少条数据进行数据库保存
	private static final int BATCH_COUNT = 128000;

	private ImportBaseInterface service;

	private String importType;

	//表头有多少行(从0开始)
	private Integer headRows = 0;

	private Integer colNum;

	private Object[] params;

	//表头数据
	private Object head;
	private List<Object> details = new ArrayList<>();

	//所有数据map
	private Map<Integer, Map<Integer, String>> allDataMap;

	//表头错误信息
	private List<String> headErrorMsg = new ArrayList<>();

	private Map<Integer, Map<Integer, String>> successMap;

	private Map<Integer, Map<Integer, String>> errorMap;

	public Object getHead() {
		return head;
	}

	public void setHead(Object head) {
		this.head = head;
	}

	//重构，把传来的值赋给对应的属性
	public EasyExcelImportListener(ImportBaseInterface service, String importType, Integer headRows, Integer colNum, Object... params) {
		this.service = service;
		this.importType = importType;
		this.headRows = headRows;
		successMap = new HashMap<>();
		errorMap = new HashMap<>();
		allDataMap = new HashMap<>();
		this.colNum = colNum;
		this.params = params;
	}

	/**
	 * 重写invokeHeadMap方法，获去表头，如果有需要获取第一行表头就重写这个方法，不需要则不需要重写
	 *
	 * @param headMap Excel每行解析的数据为Map<Integer, String>类型，Integer是Excel的列索引,String为Excel的单元格值
	 * @param context context能获取一些东西，比如context.readRowHolder().getRowIndex()为Excel的行索引，表头的行索引为0，0之后的都解析成数据
	 */
	@Override
	public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
		//只能解析到第一行的表头
		logger.info("解析到一条头数据：{}, currentRowHolder: {}", headMap.toString(), context.readRowHolder().getRowIndex());
		//Map<Integer, Map<Integer, String>> map = new HashMap<>();
		//map.put(context.readRowHolder().getRowIndex(), headMap);
		//list.add(map);
	}


	/**
	 * 判断整行单元格数据是否均为空
	 */
	private boolean isLineNullValue(Map<Integer, String> data) {

		int size = data.size();
		Collection<String> list = data.values();

		if(size>10){
			return list.stream().limit(10).noneMatch(StringUtils::isNotBlank);
		}else{
			return CollectionUtils.isEmpty(list) || list.stream().noneMatch(StringUtils::isNotBlank);
		}
	}

	/**
	 * 重写invoke方法获得除Excel第一行表头之后的数据，
	 * 如果Excel第二行也是表头，那么也会解析到这里，如果不需要就通过判断context.readRowHolder().getRowIndex()跳过
	 *
	 * @param data    除了第一行表头外，数据都会解析到这个方法
	 * @param context 和上面解释一样
	 */
	@Override
	public void invoke(Map<Integer, String> data, AnalysisContext context) {
		Integer row = context.readRowHolder().getRowIndex();
		if (context != null) allDataMap.put(row, data);
		//如果表头行有报错，直接终止
		if (!headErrorMsg.isEmpty()) return;
		logger.info("解析到一条数据：{}, currentRowIndex: {}----", data.toString(), context.readRowHolder().getRowIndex());
		try {

			// 如果一行Excel数据均为空值，则不装载该行数据
			if (row>headRows && isLineNullValue(data)) {
				return;
			}
			Object head = service.validate(row, data, importType,this.head, params);
			if(row<=headRows){
				setHead(head);
			}else{

				if(head!=null){
					details.add(head);
				}
			}
			//从1开始。 因为0是表头，不在此方法执行
			successMap.put(context.readRowHolder().getRowIndex(), data);
			// 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
			if (successMap.size() >= BATCH_COUNT) {
				saveData();
				// 存储完成清理 list
				successMap.clear();
			}

		} catch (Exception e) {
			e.printStackTrace();
			if (context.readRowHolder().getRowIndex() <= headRows) {
				//表头行报错
				headErrorMsg.add(e.getMessage());
			} else {
				data.put(colNum, e.getMessage());
				errorMap.put(context.readRowHolder().getRowIndex(), data);
			}

		}
	}

	/**
	 * 解析到最后会进入这个方法，需要重写这个doAfterAllAnalysed方法，然后里面调用自己定义好保存方法
	 *
	 * @param context
	 */
	@Override
	public void doAfterAllAnalysed(AnalysisContext context) {
		// 这里也要保存数据，确保最后遗留的数据也存储到数据库
		if (CollectionUtils.isEmpty(headErrorMsg) && errorMap.isEmpty()) saveData();
		logger.info("所有数据解析完成！");
	}

	/**
	 * 加上存储数据库
	 */
	private void saveData() {
		logger.info("{}条数据，开始存储数据库！", successMap.size());
		service.saveData(successMap, importType, errorMap, headErrorMsg, head,details, params);
	}


	public Map<Integer, Map<Integer, String>> getSuccessMap() {
		return successMap;
	}

	public void setSuccessMap(Map<Integer, Map<Integer, String>> successMap) {
		this.successMap = successMap;
	}

	public Map<Integer, Map<Integer, String>> getErrorMap() {
		return errorMap;
	}

	public void setErrorMap(Map<Integer, Map<Integer, String>> errorMap) {
		this.errorMap = errorMap;
	}

	public List<String> getHeadErrorMsg() {
		return headErrorMsg;
	}

	public void setHeadErrorMsg(List<String> headErrorMsg) {
		this.headErrorMsg = headErrorMsg;
	}

	public Map<Integer, Map<Integer, String>> getAllDataMap() {
		return allDataMap;
	}

	public void setAllDataMap(Map<Integer, Map<Integer, String>> allDataMap) {
		this.allDataMap = allDataMap;
	}

	private final static Logger logger = LoggerFactory.getLogger(EasyExcelImportListener.class);


	public Object[] getParams() {
		return params;
	}

	public void setParms(Object[] params) {
		this.params = params;
	}


}
