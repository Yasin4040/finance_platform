package com.jtyjy.finance.manager.test;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.klcwqy.easyexcel.processor.ExportPostProcessor;

public class TestExportPostProcessor implements ExportPostProcessor{

	public void onOpenRow(Row row) {
		System.out.println("TestExportPostProcessor.onOpenRow="+row.getFirstCellNum());
		
	}

	public void onLeaveCell(Row row, Cell cell) {
		int num = row.getRowNum();
		if(num == 1) {
			Workbook workbook = row.getSheet().getWorkbook();
			CellStyle style = workbook.createCellStyle();
			style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			cell.setCellStyle(style);
		}
		System.out.println("TestExportPostProcessor.onLeaveCell="+cell.toString());
	}

}
