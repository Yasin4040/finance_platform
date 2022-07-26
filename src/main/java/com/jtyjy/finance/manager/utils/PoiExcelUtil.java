package com.jtyjy.finance.manager.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author User
 */
@Slf4j
public class PoiExcelUtil {

    /**
     * 设置工作簿单元格表头样式
     *
     * @param workbook workbook
     * @param sheet    sheet
     */
    public static void setGeneralModelStyle(XSSFWorkbook workbook, XSSFSheet sheet) {
        // 设置表格内容样式
        setContentStyle(workbook, sheet);

        // 设置表格头样式
        setHeaderStyle(workbook, sheet);
    }

    // ----------------------------------------------------------------------------------------------------

    /**
     * 设置工作簿单元格表头样式
     *
     * @param workbook workbook
     * @param sheet    sheet
     */
    public static void setHeaderStyle(XSSFWorkbook workbook, XSSFSheet sheet) {
        setHeaderStyle(workbook, sheet, 0);
    }

    /**
     * 设置工作簿单元格表头样式
     *
     * @param workbook workbook
     * @param sheet    sheet
     */
    public static void setHeaderStyle(XSSFWorkbook workbook, XSSFSheet sheet, int endRow) {
        XSSFFont font = workbook.createFont();
        font.setFontName("宋体");
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);

        // 单元格样式 居中对齐 宋体 粗体 12 (有边框)
        java.awt.Color backgroundColor = new java.awt.Color(191, 191, 191);
        XSSFCellStyle cellStyle = getCellStyle(workbook, font, backgroundColor, true);

        // 单元样式设置
        for (int i = 0; i <= endRow; i++) {
            styleSetting(sheet.getRow(i), cellStyle);
        }
    }

    // ----------------------------------------------------------------------------------------------------

    /**
     * 设置工作簿单元格样式
     *
     * @param workbook workbook
     * @param sheet    sheet
     */
    public static void setContentStyle(XSSFWorkbook workbook, XSSFSheet sheet) {
        setContentStyle(workbook, sheet, 1, false);
    }

    /**
     * 设置工作簿单元格样式
     *
     * @param workbook workbook
     * @param sheet    sheet
     */
    public static void setContentStyle(XSSFWorkbook workbook, XSSFSheet sheet, int startRow, boolean isBorder) {
        XSSFFont font = workbook.createFont();
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 12);

        // 单元格样式 居中对齐 宋体 12
        XSSFCellStyle cellStyle = getCellStyle(workbook, font, null, isBorder);

        // 单元样式设置
        for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
            styleSetting(sheet.getRow(i), cellStyle);
        }
    }

    private static void styleSetting(XSSFRow row, XSSFCellStyle cellStyleTitle) {
        if (row != null) {
            for (int j = 0; j < row.getLastCellNum(); j++) {
                XSSFCell cell = row.getCell(j);
                if (cell != null) {
                    cell.setCellStyle(cellStyleTitle);
                }
            }
        }
    }

    /**
     * 获取单元格样式
     *
     * @param workbook        workbook
     * @param font            字体
     * @param backgroundColor 背景色
     * @param isBorder        是否有边框
     * @return 样式对象
     */
    private static XSSFCellStyle getCellStyle(XSSFWorkbook workbook, XSSFFont font, java.awt.Color backgroundColor, boolean isBorder) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setWrapText(true);
        cellStyle.setFont(font);

        // 背景色
        if (backgroundColor != null) {
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cellStyle.setFillForegroundColor(new XSSFColor(backgroundColor));
        }

        // 设置边框
        if (isBorder) {
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
        }
        return cellStyle;
    }

    // ----------------------------------------------------------------------------------------------------

    /**
     * 获取Excel文件的多张工作表内容
     *
     * @param srcFile    上传的文件
     * @param dateFormat 时间转换格式（若表中不存在时间可传 null）
     * @return Excel文件的多张工作表内容
     * @throws Exception 异常
     */
    public static Map<String, List<List<String>>> getMultipleSheet(MultipartFile srcFile, SimpleDateFormat dateFormat) throws Exception {
        LinkedHashMap<String, List<List<String>>> linkedHashMap = new LinkedHashMap<>();
        try (InputStream inputStream = srcFile.getInputStream()) {
            // 创建Excel工作薄
            Workbook workbook = getWorkbook(inputStream, Objects.requireNonNull(srcFile.getOriginalFilename()));

            // 公式计算
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            // 循环读取工作表
            Iterator<Sheet> iterator = workbook.sheetIterator();
            while (iterator.hasNext()) {
                Sheet sheet = iterator.next();
                List<List<String>> sheetContent = getSheetContent(evaluator, dateFormat, sheet);

                linkedHashMap.put(sheet.getSheetName(), sheetContent);
            }
            return linkedHashMap;
        }
    }

    /**
     * 获取Excel文件的单张工作表内容
     *
     * @param multipartFile 上传的文件
     * @param dateFormat    时间转换格式（若表中不存在时间可传 null）
     * @return Excel文件的单张工作表内容
     * @throws Exception 异常
     */
    public static List<List<String>> getSingleSheet(MultipartFile multipartFile, SimpleDateFormat dateFormat) throws Exception {
        try (InputStream inputStream = multipartFile.getInputStream()) {
            // 创建Excel工作薄
            Workbook workbook = getWorkbook(inputStream, Objects.requireNonNull(multipartFile.getOriginalFilename()));

            // 公式计算
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            return getSheetContent(evaluator, dateFormat, workbook.getSheetAt(0));
        }
    }

    /**
     * 获取工作表内容
     *
     * @param evaluator  公式计算
     * @param dateFormat 时间转换格式
     * @param sheet      工作表
     * @return 单个工作表内容
     */
    private static List<List<String>> getSheetContent(FormulaEvaluator evaluator, SimpleDateFormat dateFormat, Sheet sheet) {
        List<List<String>> rowData = new ArrayList<>();
        if (sheet != null) {
            for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row != null) {
                    List<String> columnData = new ArrayList<>();
                    for (int columnNum = 0; columnNum < row.getLastCellNum(); columnNum++) {
                        // 单元格位置
                        Cell cell = row.getCell(columnNum);

                        // 获取单元格内容
                        String cellValue = getCellValue(evaluator, dateFormat, cell);
                        if (columnNum == 0 && StringUtils.isBlank(cellValue)) {
                            // 如果行记录的第一列单元格内容为空，则整行数据不予读取
                            break;
                        }
                        columnData.add(cellValue);
                    }
                    if (!columnData.isEmpty()) {
                        rowData.add(columnData);
                    }
                }
            }
        }
        return rowData;
    }

    /**
     * 获取单元格内容
     *
     * @param evaluator  公式计算
     * @param dateFormat 时间转换
     * @param cell       单元格
     * @return 单元格内容
     */
    private static String getCellValue(FormulaEvaluator evaluator, SimpleDateFormat dateFormat, Cell cell) {
        String value = "";
        if (cell != null) {
            switch (cell.getCellTypeEnum()) {
                case NUMERIC:
                    // 数值型
                    if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                        if (dateFormat != null) {
                            value = dateFormat.format(cell.getDateCellValue());
                        }
                    } else {
                        DecimalFormat df = new DecimalFormat("################.####");
                        value = df.format(cell.getNumericCellValue());
                    }
                    break;
                case STRING:
                    // 字符串型
                    value = cell.getStringCellValue();
                    if (StringUtils.isNotBlank(value.trim())) {
                        value = value.trim().replace("\n", " ");
                    }
                    break;
                case FORMULA:
                    // 公式
                    value = String.valueOf(evaluator.evaluate(cell).getNumberValue());
                    break;
                case BLANK:
                    // 空值
                    break;
                case BOOLEAN:
                    // 布尔型
                    value = String.valueOf(cell.getBooleanCellValue());
                    break;
                default:
            }
        }
        return value;
    }

    /**
     * 获取Excel工作薄对象
     *
     * @param inputStream 输入流
     * @param fileName    导入excel文件名（含后缀）
     * @return Excel文档对象
     * @throws Exception 异常
     */
    private static Workbook getWorkbook(InputStream inputStream, String fileName) throws Exception {
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        Workbook workbook;
        if (".xls".equals(fileType)) {
            workbook = new HSSFWorkbook(inputStream);
        } else if (".xlsx".equals(fileType)) {
            workbook = new XSSFWorkbook(inputStream);
        } else {
            throw new RuntimeException("请上传excel文件！");
        }
        return workbook;
    }

    // ----------------------------------------------------------------------------------------------------

    /**
     * Excel文件导出
     *
     * @param columnNames  列名称
     * @param columnWidths 列宽度
     * @param exportMap    导出内容
     * @param headHeight   表头高度，默认为500
     * @param outputStream 输出流
     * @throws Exception 异常
     */
    public static void exportExcelFile(List<String> columnNames, List<Integer> columnWidths, Map<String, List<List<String>>> exportMap, Short headHeight, OutputStream outputStream) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            for (Map.Entry<String, List<List<String>>> entry : exportMap.entrySet()) {
                // 创建Sheet工作表
                XSSFSheet sheet = workbook.createSheet(entry.getKey());

                // 填充内容
                createSheet(sheet, columnNames, columnWidths, entry.getValue(), headHeight);

                // 设置表格样式
                PoiExcelUtil.setGeneralModelStyle(workbook, sheet);
            }

            // 生成文件
            workbook.write(outputStream);
            outputStream.flush();
        } catch (Exception e) {
            log.error("导出Excel表格异常:", e);
            throw e;
        }
    }

    /**
     * Excel文件导出
     *
     * @param sheetName    工作表名称
     * @param columnNames  列名称
     * @param columnWidths 列宽度
     * @param exportList   导出内容
     * @param headHeight   表头高度，默认为500
     * @param outputStream 输出流
     * @throws Exception 异常
     */
    public static void exportExcelFile(String sheetName, List<String> columnNames, List<Integer> columnWidths, List<List<String>> exportList, Short headHeight, OutputStream outputStream) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            // 创建Sheet工作表
            XSSFSheet sheet = workbook.createSheet(sheetName);

            // 填充内容
            createSheet(sheet, columnNames, columnWidths, exportList, headHeight);

            // 设置样式
            PoiExcelUtil.setGeneralModelStyle(workbook, sheet);

            // 生成文件
            workbook.write(outputStream);
            outputStream.flush();
        } catch (Exception e) {
            log.error("导出Excel表格异常:", e);
            throw e;
        }
    }

    /**
     * Excel文件导出到服务器
     *
     * @param sheetName    工作表名称
     * @param columnNames  列名称
     * @param columnWidths 列宽度
     * @param exportList   导出内容
     * @param headHeight   表头高度，默认为500
     * @param filePath     输出文件路径
     * @throws Exception 异常
     */
    public static void exportExcelFile(String sheetName, List<String> columnNames, List<Integer> columnWidths, List<List<String>> exportList, Short headHeight, String filePath) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            if (StringUtils.isBlank(filePath)) {
                throw new Exception("输出文件路径不能为空");
            }
            // 创建Sheet工作表
            XSSFSheet sheet = workbook.createSheet(sheetName);

            // 填充内容
            createSheet(sheet, columnNames, columnWidths, exportList, headHeight);

            // 设置样式
            PoiExcelUtil.setGeneralModelStyle(workbook, sheet);
            FileOutputStream outputStream = new FileOutputStream(filePath);  
            // 生成文件
            workbook.write(outputStream);
            outputStream.flush();
        } catch (Exception e) {
            log.error("导出Excel表格异常:", e);
            throw e;
        }
    }
    
    /**
     * 创建Sheet工作表
     */
    private static void createSheet(XSSFSheet sheet, List<String> columnNames, List<Integer> columnWidths, List<List<String>> exportList, Short headHeight) {
        // 表头内容
        int totalColumn = 0;
        XSSFRow row0 = sheet.createRow(0);
        if (null == headHeight) {
            row0.setHeight((short) 500);
        } else {
            row0.setHeight(headHeight);
        }
        for (String columnName : columnNames) {
            row0.createCell(totalColumn++, CellType.STRING).setCellValue(columnName);
        }

        // 设置单元格宽度
        totalColumn = 0;
        for (Integer columnWidth : columnWidths) {
            sheet.setColumnWidth(totalColumn++, columnWidth);
        }

        // 表格内容
        if (exportList != null) {
            int count = 1;
            for (List<String> data : exportList) {
                XSSFRow row = sheet.createRow(count++);

                int columnIndex = 0;
                for (String value : data) {
                    row.createCell(columnIndex++, CellType.STRING).setCellValue(value != null ? value : "");
                }
            }
        }
    }
}