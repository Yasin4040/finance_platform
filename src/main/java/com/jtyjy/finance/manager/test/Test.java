package com.jtyjy.finance.manager.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.jtyjy.easy.excel.ExportHelper;
import com.jtyjy.easy.excel.ImportHelper;
import com.klcwqy.easyexcel.imported.ExcelImportHelper;
import com.klcwqy.easyexcel.processor.ImportPostProcessor;
import com.klcwqy.easyexcel.test.TestImportPostProcessor;



public class Test {

	public static void main(String[] args) throws Exception {
		ImportHelper helper = new ImportHelper("D:\\Users\\User\\Desktop\\1.xls");
		helper.addProcessors(new TestImportPostProcessor());
		List<Sheet> sheets = helper.loadSheet();
		Map<String, Object> map = helper.doImport(sheets, Order.class);
		Map<String, Object> zh_map = helper.doImport(sheets, ZhInfo.class);
		Map<String, Object> travel_map = helper.doImport(sheets, TravelInfo.class);
		helper.end(false);
		boolean error = helper.getErrorFile();
		
		
		Map<Class<?>, Map<String, Object>> export_map = new HashMap<Class<?>, Map<String,Object>>(3);
		export_map.put(Order.class, map);
		export_map.put(ZhInfo.class, zh_map);
		export_map.put(TravelInfo.class, travel_map);
		ExportHelper exportHelp = new ExportHelper("D:\\Users\\User\\Desktop\\3.xls");
		exportHelp.addPostProcessor(new TestExportPostProcessor());
		exportHelp.export(export_map,"D:\\Users\\User\\Desktop\\88.xls");
		exportHelp.end();
	}
}
