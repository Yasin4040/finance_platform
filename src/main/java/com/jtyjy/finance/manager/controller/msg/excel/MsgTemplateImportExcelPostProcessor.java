package com.jtyjy.finance.manager.controller.msg.excel;

import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Component;

import com.klcwqy.easyexcel.imported.ExcelImportHelper;
import com.klcwqy.easyexcel.processor.ImportPostProcessor;

@Component
public class MsgTemplateImportExcelPostProcessor implements ImportPostProcessor{

	@Override
	public void instanceProcess(ExcelImportHelper arg0, Class<?> arg1, Row arg2, Map<String, Object> arg3, Object arg4)
			throws Exception {
		
		
	}

	@Override
	public void process(ExcelImportHelper arg0, Class<?> arg1, Map<String, Object> arg2, Sheet arg3, int arg4)
			throws Exception {
		
		
	}

}
