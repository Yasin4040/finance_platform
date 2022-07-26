package com.jtyjy.finance.manager.utils;

import com.alibaba.fastjson.JSONArray;
import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.finance.manager.bean.BudgetYearAgent;
import com.jtyjy.finance.manager.vo.BudgetMonthAgentVO;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * @Author 袁前兼
 * @Date 2021/4/22 10:42
 */
@Slf4j
@Component
public class ResponseUtil {

    /**
     * 导出服务器本地excel文件
     *
     * @param response response
     * @param path     源文件路径
     * @param fileName 文件名称
     * @throws Exception 异常
     */
    public static void exportExcel(HttpServletResponse response, String path, String fileName) throws Exception {
        OutputStream outStream = new BufferedOutputStream(response.getOutputStream());
        try {
            File file = new File(path);
            InputStream inputStream = new FileInputStream(file);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel;application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            outStream.write(buffer);
            outStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            outStream.close();
        }
    }

    /**
     * 获取单个工作表导入内容
     *
     * @param multipartFile 上传的文件
     * @return 文件内容
     * @throws Exception 异常
     */
    public static List<List<String>> getSingleExcelContent(MultipartFile multipartFile) throws Exception {
        // 文件类型判断
        EasyExcelUtil.checkFile(multipartFile);

        return PoiExcelUtil.getSingleSheet(multipartFile, new SimpleDateFormat("yyyy-MM-dd"));
    }

    /**
     * 获取多个工作表导入内容
     *
     * @param multipartFile 上传的文件
     * @return 文件内容
     * @throws Exception 异常
     */
    public static Map<String, List<List<String>>> getMultipleExcelContent(MultipartFile multipartFile) throws Exception {
        // 文件类型判断
        EasyExcelUtil.checkFile(multipartFile);

        return PoiExcelUtil.getMultipleSheet(multipartFile, new SimpleDateFormat("yyyy-MM-dd"));
    }

    // ----------------------------------------------------------------------------------------------------

    /**
     * 年度动因文件导出
     *
     * @param exportMap    导出的数据
     * @param type         导出类型（1普通 2产品 3分解）
     * @param outputStream 输出流
     * @throws Exception 异常
     */
    public static <T> void exportYearAgentExcelFile(Map<String, List<T>> exportMap, Integer type, OutputStream outputStream) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            if (exportMap.isEmpty()) {
                exportMap.put("Sheet1", null);
            }
            for (Map.Entry<String, List<T>> entry : exportMap.entrySet()) {
                // 创建Sheet工作表
                XSSFSheet sheet = workbook.createSheet(entry.getKey());

                // 填充内容
                createYearAgentSheet(sheet, type, entry.getValue());

                // 设置表格内容样式
                PoiExcelUtil.setContentStyle(workbook, sheet, 2, false);

                // 设置表格头样式
                PoiExcelUtil.setHeaderStyle(workbook, sheet, 1);
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
     * 年度动因内容填充
     *
     * @param sheet         工作表
     * @param type          导出类型（1普通 2产品 3分解）
     * @param yearAgentList 导出数据
     */
    public static <T> void createYearAgentSheet(XSSFSheet sheet, Integer type, List<T> yearAgentList) {
        int totalColumn = 0;

        // 第一行内容
        XSSFRow row0 = sheet.createRow(0);
        row0.setHeight((short) 500);
        if (type == 1 || type == 2) {
            row0.createCell(totalColumn++, CellType.STRING).setCellValue(type == 1 ? "动因名称" : "产品名称");
            row0.createCell(totalColumn++, CellType.STRING).setCellValue("业务活动内容");
            row0.createCell(totalColumn++, CellType.STRING).setCellValue("次数");
            row0.createCell(totalColumn++, CellType.STRING).setCellValue("计算过程");
            row0.createCell(totalColumn++, CellType.STRING).setCellValue("上届预估");
            row0.createCell(totalColumn, CellType.STRING).setCellValue("分解");
        } else {
            row0.createCell(totalColumn++, CellType.STRING).setCellValue("预算单位");
            row0.createCell(totalColumn, CellType.STRING).setCellValue("分解");
        }

        // 第二行内容
        int costSplitColumn = totalColumn;
        XSSFRow row1 = sheet.createRow(1);
        row1.setHeight((short) 500);
        for (int i = 0; i < totalColumn; i++) {
            row1.createCell(i, CellType.STRING);
        }
        row1.createCell(totalColumn++, CellType.STRING).setCellValue("6月");
        row1.createCell(totalColumn++, CellType.STRING).setCellValue("7月");
        row1.createCell(totalColumn++, CellType.STRING).setCellValue("8月");
        row1.createCell(totalColumn++, CellType.STRING).setCellValue("9月");
        row1.createCell(totalColumn++, CellType.STRING).setCellValue("10月");
        row1.createCell(totalColumn++, CellType.STRING).setCellValue("11月");
        row1.createCell(totalColumn++, CellType.STRING).setCellValue("12月");
        row1.createCell(totalColumn++, CellType.STRING).setCellValue("1月");
        row1.createCell(totalColumn++, CellType.STRING).setCellValue("2月");
        row1.createCell(totalColumn++, CellType.STRING).setCellValue("3月");
        row1.createCell(totalColumn++, CellType.STRING).setCellValue("4月");
        row1.createCell(totalColumn, CellType.STRING).setCellValue("5月");
        for (int i = costSplitColumn + 1; i <= totalColumn; i++) {
            row0.createCell(i, CellType.STRING);
        }

        // 单元格合并
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
        if (type == 1 || type == 2) {
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 2, 2));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 3, 3));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 4, 4));
        }
        sheet.addMergedRegion(new CellRangeAddress(0, 0, costSplitColumn, totalColumn));

        // 设置单元格宽度
        sheet.setColumnWidth(0, 24 * 256);
        if (type == 1 || type == 2) {
            sheet.setColumnWidth(1, 24 * 256);
            sheet.setColumnWidth(2, 16 * 256);
            sheet.setColumnWidth(3, 24 * 256);
            sheet.setColumnWidth(4, 24 * 256);
        }
        for (int i = costSplitColumn; i <= totalColumn; i++) {
            sheet.setColumnWidth(i, 8 * 256);
        }

        if (yearAgentList != null && !yearAgentList.isEmpty()) {
            // 表格内容填充
            int count = 2;
            for (T t : yearAgentList) {
                XSSFRow row = sheet.createRow(count++);
                int columnIndex = 0;
                if (t instanceof BudgetYearAgent) {
                    BudgetYearAgent yearAgent = (BudgetYearAgent) t;
                    if (type == 1 || type == 2) {
                        row.createCell(columnIndex++, CellType.STRING).setCellValue(yearAgent.getName());
                        row.createCell(columnIndex++, CellType.STRING).setCellValue(yearAgent.getRemark());
                        row.createCell(columnIndex++, CellType.STRING).setCellValue(yearAgent.getHappencount() != null ? yearAgent.getHappencount() : "");
                        row.createCell(columnIndex++, CellType.STRING).setCellValue(yearAgent.getComputingprocess() != null ? yearAgent.getHappencount() : "");
                        row.createCell(columnIndex++, CellType.NUMERIC).setCellValue(yearAgent.getPreestimate().doubleValue());
                    } else {
                        row.createCell(columnIndex++, CellType.STRING).setCellValue(yearAgent.getUnitName());
                    }

                    row.createCell(columnIndex++, CellType.NUMERIC).setCellValue(yearAgent.getM6().doubleValue());
                    row.createCell(columnIndex++, CellType.NUMERIC).setCellValue(yearAgent.getM7().doubleValue());
                    row.createCell(columnIndex++, CellType.NUMERIC).setCellValue(yearAgent.getM8().doubleValue());
                    row.createCell(columnIndex++, CellType.NUMERIC).setCellValue(yearAgent.getM9().doubleValue());
                    row.createCell(columnIndex++, CellType.NUMERIC).setCellValue(yearAgent.getM10().doubleValue());
                    row.createCell(columnIndex++, CellType.NUMERIC).setCellValue(yearAgent.getM11().doubleValue());
                    row.createCell(columnIndex++, CellType.NUMERIC).setCellValue(yearAgent.getM12().doubleValue());
                    row.createCell(columnIndex++, CellType.NUMERIC).setCellValue(yearAgent.getM1().doubleValue());
                    row.createCell(columnIndex++, CellType.NUMERIC).setCellValue(yearAgent.getM2().doubleValue());
                    row.createCell(columnIndex++, CellType.NUMERIC).setCellValue(yearAgent.getM3().doubleValue());
                    row.createCell(columnIndex++, CellType.NUMERIC).setCellValue(yearAgent.getM4().doubleValue());
                    row.createCell(columnIndex, CellType.NUMERIC).setCellValue(yearAgent.getM5().doubleValue());
                } else if (t instanceof ArrayList) {
                    List<?> list = (ArrayList<?>) t;
                    for (Object value : list) {
                        row.createCell(columnIndex++, CellType.STRING).setCellValue(value != null ? value.toString() : "");
                    }
                } else if (t instanceof JSONArray) {
                    JSONArray list = (JSONArray) t;
                    for (Object value : list) {
                        row.createCell(columnIndex++, CellType.STRING).setCellValue(value != null ? value.toString() : "");
                    }
                }
            }
        }
    }

    // ----------------------------------------------------------------------------------------------------

    /**
     * 月度动因文件导出
     *
     * @param exportMap    导出的数据
     * @param type         导出类型（1普通 2产品 3分解）
     * @param outputStream 输出流
     * @throws Exception 异常
     */
    public static <T> void exportMonthAgentExcelFile(Map<String, List<T>> exportMap, Integer type, OutputStream outputStream) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            if (exportMap.isEmpty()) {
                exportMap.put("Sheet1", null);
            }
            createMonthAgentSheet(workbook, exportMap, type);

            // 生成文件
            workbook.write(outputStream);
            outputStream.flush();
        } catch (Exception e) {
            log.error("导出Excel表格异常:", e);
            throw e;
        }
    }

    /**
     * 月度动因汇总明细导出
     *
     * @param exportMap1   月度动因
     * @param exportMap2   月度产品
     * @param exportMap3   月度分解
     * @param outputStream 输出流
     * @throws Exception 异常
     */
    public static <T> void exportMonthAgentCollectExcelFile(Map<String, List<T>> exportMap1, Map<String, List<T>> exportMap2, Map<String, List<T>> exportMap3, OutputStream outputStream) throws Exception {
        if (exportMap1.isEmpty() && exportMap2.isEmpty() && exportMap3.isEmpty()) {
            return;
        }
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            createMonthAgentSheet(workbook, exportMap1, 1);
            createMonthAgentSheet(workbook, exportMap2, 2);
            createMonthAgentSheet(workbook, exportMap3, 3);

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
    private static <T> void createMonthAgentSheet(XSSFWorkbook workbook, Map<String, List<T>> exportMap, int type) {
        for (Map.Entry<String, List<T>> entry : exportMap.entrySet()) {
            // 创建Sheet工作表
            XSSFSheet sheet = workbook.createSheet(entry.getKey());

            // 填充内容
            createMonthAgentSheet(sheet, type, entry.getValue());

            // 设置表格样式
            PoiExcelUtil.setGeneralModelStyle(workbook, sheet);
        }
    }

    /**
     * 月度动因内容填充
     *
     * @param sheet          工作表
     * @param type           导出类型（1普通 2产品 3分解）
     * @param monthAgentList 导出数据
     */
    public static <T> void createMonthAgentSheet(XSSFSheet sheet, Integer type, List<T> monthAgentList) {
        int totalColumn = 0;

        // 第一行内容
        XSSFRow row0 = sheet.createRow(0);
        row0.setHeight((short) 500);
        if (type == 1) {
            row0.createCell(totalColumn++, CellType.STRING).setCellValue("动因名称");
        } else if (type == 2) {
            row0.createCell(totalColumn++, CellType.STRING).setCellValue("产品名称");
        } else if (type == 3) {
            row0.createCell(totalColumn++, CellType.STRING).setCellValue("预算单位");
        }
        row0.createCell(totalColumn++, CellType.STRING).setCellValue("月份");
        row0.createCell(totalColumn++, CellType.STRING).setCellValue("年度预算剩余可用余额");
        row0.createCell(totalColumn++, CellType.STRING).setCellValue("月度预算");
        row0.createCell(totalColumn, CellType.STRING).setCellValue("月度预算活动说明");

        // 设置单元格宽度
        sheet.setColumnWidth(0, 24 * 256);
        sheet.setColumnWidth(1, 10 * 256);
        sheet.setColumnWidth(2, 24 * 256);
        sheet.setColumnWidth(3, 16 * 256);
        sheet.setColumnWidth(4, 24 * 256);

        if (monthAgentList != null && !monthAgentList.isEmpty()) {
            // 表格内容填充
            int count = 1;
            for (T t : monthAgentList) {
                XSSFRow row = sheet.createRow(count++);
                int columnIndex = 0;
                if (t instanceof BudgetMonthAgentVO) {
                    BudgetMonthAgentVO monthAgentVO = (BudgetMonthAgentVO) t;
                    if (type == 1 || type == 2) {
                        row.createCell(columnIndex++, CellType.STRING).setCellValue(monthAgentVO.getName());
                    } else {
                        row.createCell(columnIndex++, CellType.STRING).setCellValue(monthAgentVO.getUnitName());
                    }
                    row.createCell(columnIndex++, CellType.STRING).setCellValue(monthAgentVO.getPeriod());
                    row.createCell(columnIndex++, CellType.NUMERIC).setCellValue(monthAgentVO.getUnExecute().doubleValue());
                    row.createCell(columnIndex++, CellType.NUMERIC).setCellValue(monthAgentVO.getTotal().doubleValue());
                    row.createCell(columnIndex, CellType.STRING).setCellValue(monthAgentVO.getMonthbusiness() != null ? monthAgentVO.getMonthbusiness() : "");
                } else if (t instanceof ArrayList) {
                    List<?> list = (ArrayList<?>) t;
                    for (Object value : list) {
                        row.createCell(columnIndex++, CellType.STRING).setCellValue(value != null ? value.toString() : "");
                    }
                } else if (t instanceof JSONArray) {
                    JSONArray list = (JSONArray) t;
                    for (Object value : list) {
                        row.createCell(columnIndex++, CellType.STRING).setCellValue(value != null ? value.toString() : "");
                    }
                }
            }
        }
    }

    // ----------------------------------------------------------------------------------------------------

    /**
     * 逾期及征信导入模板下载
     *
     * @param outputStream 输出流
     * @throws Exception 异常
     */
    public static void exportCreditExcelFile(List<List<String>> exportList, OutputStream outputStream) throws Exception {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("工号");
        columnNames.add("姓名");
        columnNames.add("逾期记录");
        columnNames.add("不良征信记录");

        List<Integer> columnWidths = new ArrayList<>();
        columnWidths.add(10 * 256);
        columnWidths.add(10 * 256);
        columnWidths.add(12 * 256);
        columnWidths.add(16 * 256);

        PoiExcelUtil.exportExcelFile("导入逾期及征信", columnNames, columnWidths, exportList, null, outputStream);
    }

    /**
     * 还款导入模板
     *
     * @param outputStream 输出流
     * @throws Exception 异常
     */
    public static void exportRepayMoneyExcelFile(List<List<String>> exportList, OutputStream outputStream) throws Exception {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("借款单号");
        columnNames.add("还款金额");

        List<Integer> columnWidths = new ArrayList<>();
        columnWidths.add(16 * 256);
        columnWidths.add(13 * 256);

        PoiExcelUtil.exportExcelFile("导入现金还款", columnNames, columnWidths, exportList, null, outputStream);
    }

    /**
     * 员工借款明细导出
     */
    public static void exportLendMoneyExcelFile(List<List<String>> exportList, OutputStream outputStream) throws Exception {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("借款单号");
        columnNames.add("工号");
        columnNames.add("姓名");
        columnNames.add("借款金额");
        columnNames.add("已还");
        columnNames.add("利息");
        columnNames.add("未还");
        columnNames.add("借款日期");
        columnNames.add("预计还款日期");
        columnNames.add("借款单类型");

        List<Integer> columnWidths = new ArrayList<>();
        columnWidths.add(16 * 256);
        columnWidths.add(15 * 256);
        columnWidths.add(15 * 256);
        columnWidths.add(18 * 256);
        columnWidths.add(18 * 256);
        columnWidths.add(18 * 256);
        columnWidths.add(18 * 256);
        columnWidths.add(18 * 256);
        columnWidths.add(18 * 256);
        columnWidths.add(20 * 256);

        PoiExcelUtil.exportExcelFile("员工借款明细", columnNames, columnWidths, exportList, null, outputStream);
    }

    /**
     * 项目借款达标验证导出
     */
    public static void exportProjectValidateExcelFile(Map<String, List<List<String>>> exportMap, OutputStream outputStream) throws Exception {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("项目编号(唯一标识)");
        columnNames.add("项目名称");
        columnNames.add("工号");
        columnNames.add("姓名");
        columnNames.add("借款金额");
        columnNames.add("借款日期(2020-12-13)");
        columnNames.add("计划还款日期(2021-02-01)");
        columnNames.add("借款事由");
        columnNames.add("项目借款类型(现金、转账、礼品)");
        columnNames.add("借款单号");
        columnNames.add("达标/完成状态(是/否)");

        List<Integer> columnWidths = new ArrayList<>();
        columnWidths.add(20 * 256);
        columnWidths.add(26 * 256);
        columnWidths.add(12 * 256);
        columnWidths.add(12 * 256);
        columnWidths.add(16 * 256);
        columnWidths.add(24 * 256);
        columnWidths.add(28 * 256);
        columnWidths.add(16 * 256);
        columnWidths.add(35 * 256);
        columnWidths.add(16 * 256);
        columnWidths.add(25 * 256);

        if (exportMap == null || exportMap.isEmpty()) {
            PoiExcelUtil.exportExcelFile("Sheet1", columnNames, columnWidths, null, null, outputStream);
        } else {
            PoiExcelUtil.exportExcelFile(columnNames, columnWidths, exportMap, null, outputStream);
        }
    }

    /**
     * 项目借款导出
     */
    public static void exportProjectLendExcelFile(List<List<String>> exportList, OutputStream outputStream) throws Exception {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("工号");
        columnNames.add("姓名");
        columnNames.add("借款金额");
        columnNames.add("借款日期(2020-12-13)");
        columnNames.add("计划还款日期(2021-02-01)");
        columnNames.add("借款事由");
        columnNames.add("项目借款类型(现金、转账、礼品)");

        List<Integer> columnWidths = new ArrayList<>();
        columnWidths.add(12 * 256);
        columnWidths.add(12 * 256);
        columnWidths.add(12 * 256);
        columnWidths.add(24 * 256);
        columnWidths.add(28 * 256);
        columnWidths.add(16 * 256);
        columnWidths.add(35 * 256);

        PoiExcelUtil.exportExcelFile("项目借款导入", columnNames, columnWidths, exportList, null, outputStream);
    }

    /**
     * 项目借款明细导出
     */
    public static void exportProjectLendDetailExcelFile(List<List<String>> exportList, OutputStream outputStream) throws Exception {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("项目单号");
        columnNames.add("工号");
        columnNames.add("姓名");
        columnNames.add("项目名称");
        columnNames.add("借款金额");
        columnNames.add("已还");
        columnNames.add("未还");
        columnNames.add("利息");
        columnNames.add("借款日期");
        columnNames.add("预计还款日期");
        columnNames.add("项目借款类型");

        List<Integer> columnWidths = new ArrayList<>();
        columnWidths.add(16 * 256);
        columnWidths.add(16 * 256);
        columnWidths.add(16 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(13 * 256);
        columnWidths.add(13 * 256);
        columnWidths.add(13 * 256);
        columnWidths.add(13 * 256);
        columnWidths.add(15 * 256);
        columnWidths.add(15 * 256);
        columnWidths.add(15 * 256);

        PoiExcelUtil.exportExcelFile("借款明细导出", columnNames, columnWidths, exportList, null, outputStream);
    }

    /**
     * 基础单位导出
     */
    public static void exportBaseUnit(List<List<String>> dataList, OutputStream outputStream, String filePath) throws Exception {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("单位名称");
        columnNames.add("排序号");
        columnNames.add("备注");

        List<Integer> columnWidths = new ArrayList<>();
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);

        if (dataList == null || dataList.isEmpty()) {
            PoiExcelUtil.exportExcelFile("基础单位", columnNames, columnWidths, null, null, outputStream);
        } else {
            columnNames.add("错误信息");
            columnWidths.add(20 * 256);
            PoiExcelUtil.exportExcelFile("基础单位", columnNames, columnWidths, dataList, null, filePath);
        }
    }

    /**
     * 基础科目导出
     */
    public static void exportBaseSubject(List<List<String>> dataList, OutputStream outputStream, String filePath) throws Exception {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("科目代码");
        columnNames.add("科目名称");
        columnNames.add("排序号");
        columnNames.add("备注");

        List<Integer> columnWidths = new ArrayList<>();
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        if (dataList == null || dataList.isEmpty()) {
            PoiExcelUtil.exportExcelFile("基础科目", columnNames, columnWidths, null, null, outputStream);
        } else {
            columnNames.add("错误信息");
            columnWidths.add(20 * 256);
            PoiExcelUtil.exportExcelFile("基础科目", columnNames, columnWidths, dataList, null, filePath);
        }
    }

    /**
     * 稿费作者导出
     */
    public static void exportAuthor(List<List<String>> dataList, OutputStream outputStream, String filePath) throws Exception {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("作者名字");
        columnNames.add("身份证号\r\n（个人作者必填）");
        columnNames.add("纳税人识别号\r\n（单位作者必填）");
        columnNames.add("是否公司员工\r\n（是或否）");
        columnNames.add("所在单位");
        columnNames.add("收款银行账号");
        columnNames.add("电子联行号");
        columnNames.add("银行");
        columnNames.add("省份");
        columnNames.add("城市");
        columnNames.add("支行");
        columnNames.add("备注");
        List<Integer> columnWidths = new ArrayList<>();
        columnWidths.add(12 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        if (dataList == null || dataList.isEmpty()) {
            PoiExcelUtil.exportExcelFile("稿费作者", columnNames, columnWidths, null, (short) 900, outputStream);
        } else {
            columnNames.add("错误信息");
            columnWidths.add(20 * 256);
            PoiExcelUtil.exportExcelFile("稿费作者", columnNames, columnWidths, dataList, (short) 900, filePath);
        }
    }

    /**
     * 银行账户导出
     */
    public static void exportBankAccount(List<List<String>> dataList, OutputStream outputStream, String filePath) throws Exception {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("编号");
        columnNames.add("名称");
        columnNames.add("账户类型");
        columnNames.add("户名");
        columnNames.add("银行账号");
        columnNames.add("开户行联行号");
        columnNames.add("工资卡");
        columnNames.add("备注");

        List<Integer> columnWidths = new ArrayList<>();
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(12 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(12 * 256);
        columnWidths.add(20 * 256);
        if (dataList == null || dataList.isEmpty()) {
            PoiExcelUtil.exportExcelFile("银行账户", columnNames, columnWidths, null, null, outputStream);
        } else {
            columnNames.add("错误信息");
            columnWidths.add(20 * 256);
            PoiExcelUtil.exportExcelFile("银行账户", columnNames, columnWidths, dataList, null, filePath);
        }
    }

    /**
     * 项目借款利息导出
     */
    public static void exportInterestExcelFile(Map<String, List<List<String>>> exportMap, OutputStream outputStream) throws Exception {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("工号");
        columnNames.add("姓名");
        columnNames.add("项目借款类型(现金、转账、礼品)");
        columnNames.add("借款金额");
        columnNames.add("利息");

        List<Integer> columnWidths = new ArrayList<>();
        columnWidths.add(12 * 256);
        columnWidths.add(12 * 256);
        columnWidths.add(35 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);

        if (exportMap == null || exportMap.isEmpty()) {
            PoiExcelUtil.exportExcelFile("Sheet1", columnNames, columnWidths, null, null, outputStream);
        } else {
            PoiExcelUtil.exportExcelFile(columnNames, columnWidths, exportMap, null, outputStream);
        }
    }

    /**
     * 项目还款记录明细导出
     */
    public static void exportProjectRepayMoneyExcelFile(Map<String, List<List<String>>> exportMap, OutputStream outputStream) throws Exception {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("项目名称");
        columnNames.add("借款单号");
        columnNames.add("工号");
        columnNames.add("姓名");
        columnNames.add("借款金额");
        columnNames.add("还款金额");
        columnNames.add("还款日期");

        List<Integer> columnWidths = new ArrayList<>();
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(12 * 256);
        columnWidths.add(12 * 256);
        columnWidths.add(16 * 256);
        columnWidths.add(16 * 256);
        columnWidths.add(16 * 256);

        PoiExcelUtil.exportExcelFile(columnNames, columnWidths, exportMap, null, outputStream);
    }

    /**
     * 其它借款导出
     */
    public static void exportOtherLendExcelFile(List<List<String>> exportList, OutputStream outputStream) throws Exception {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("工号");
        columnNames.add("姓名");
        columnNames.add("借款金额");
        columnNames.add("借款事由");
        columnNames.add("导入批次号(20201213)");
        columnNames.add("借款日期(2020-12-13)");
        columnNames.add("计划还款日期(2021-02-01)");
        columnNames.add("借款类型(个人借款、费用借款、销售政策支持借款申请、备用金借款、合同借款、非合同借款)");
        columnNames.add("合同编号\n（合同借款必填）");
        columnNames.add("合同名称\n（合同借款必填）");
        columnNames.add("届别\n（销售政策支持借款申请必填）");
        columnNames.add("预算单位\n（销售政策支持借款申请必填）");
        columnNames.add("项目名称\n（销售政策支持借款申请必填）");
        columnNames.add("项目类型（项目预领、项目借支）\n（销售政策支持借款申请必填）");
        columnNames.add("项目借款类型(现金、转账、礼品)\n（销售政策支持借款申请必填）");

        List<Integer> columnWidths = new ArrayList<>();
        columnWidths.add(12 * 256);
        columnWidths.add(12 * 256);
        columnWidths.add(13 * 256);
        columnWidths.add(16 * 256);
        columnWidths.add(24 * 256);
        columnWidths.add(24 * 256);
        columnWidths.add(28 * 256);
        columnWidths.add(90 * 256);
        columnWidths.add(18 * 256);
        columnWidths.add(18 * 256);
        columnWidths.add(30 * 256);
        columnWidths.add(30 * 256);
        columnWidths.add(30 * 256);
        columnWidths.add(33 * 256);
        columnWidths.add(33 * 256);

        PoiExcelUtil.exportExcelFile("其它借款导入", columnNames, columnWidths, exportList, (short) 800, outputStream);
    }

    /**
     * 报销明细导出
     */
    public static void exportBxDetailed(List<List<String>> dataList, OutputStream outputStream) throws Exception {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("报销状态");
        columnNames.add("报销单号");
        columnNames.add("报销单位");
        columnNames.add("届别");
        columnNames.add("月份");
        columnNames.add("预算单位");
        columnNames.add("报销人");
        columnNames.add("开票单位");
        columnNames.add("预算科目");
        columnNames.add("动因");
        columnNames.add("报销金额");
        columnNames.add("计入执行");
        columnNames.add("报销种类");
        columnNames.add("摘要");
        columnNames.add("付款单位");
        List<Integer> columnWidths = new ArrayList<>();
        columnWidths.add(12 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(12 * 256);
        columnWidths.add(12 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(12 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(12 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        PoiExcelUtil.exportExcelFile("报销明细", columnNames, columnWidths, dataList, null, outputStream);

    }

    /**
     * 报销退回原因汇总表导出
     */
    public static void exportBxReturn(List<List<String>> dataList, OutputStream outputStream) throws Exception {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("届别");
        columnNames.add("月份");
        columnNames.add("预算单位");
        columnNames.add("报销单号");
        columnNames.add("报销科目");
        columnNames.add("报销金额");
        columnNames.add("报销人");
        columnNames.add("退回原因");
        List<Integer> columnWidths = new ArrayList<>();
        columnWidths.add(12 * 256);
        columnWidths.add(12 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(30 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(30 * 256);
        PoiExcelUtil.exportExcelFile("退回原因汇总", columnNames, columnWidths, dataList, null, outputStream);

    }
    

    /**
     * 付款验证模板/错误信息导出
     */
    public static void exportPayVerify(List<List<String>> dataList, OutputStream outputStream) throws Exception {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("付款单号");
        columnNames.add("收款人账户");
        columnNames.add("收款人名称");
        columnNames.add("收方开户支行");
        columnNames.add("付款金额");
        columnNames.add("收方电子联行号");
        columnNames.add("收方开户银行类型");
        columnNames.add("付款失败原因");
        List<Integer> columnWidths = new ArrayList<>();
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(30 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        if (dataList == null || dataList.isEmpty()) {
            PoiExcelUtil.exportExcelFile("付款验证", columnNames, columnWidths, null, (short) 900, outputStream);
        } else {
            columnNames.add("错误信息");
            columnWidths.add(20 * 256);
            PoiExcelUtil.exportExcelFile("付款验证", columnNames, columnWidths, dataList, (short) 900, outputStream);
        }
    }    

    /**
     * 基础科目导出
     */
    public static void exportSubjectJindie(List<List<String>> dataList, OutputStream outputStream, String filePath) throws Exception {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("科目代码");
        columnNames.add("科目名称");
        columnNames.add("金蝶科目代码");
        columnNames.add("金蝶科目名称");
        List<Integer> columnWidths = new ArrayList<>();
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        if (StringUtils.isBlank(filePath)) {
            PoiExcelUtil.exportExcelFile("预算科目", columnNames, columnWidths, dataList, null, outputStream);
        } else {
            columnNames.add("错误信息");
            columnWidths.add(20 * 256);
            PoiExcelUtil.exportExcelFile("预算科目", columnNames, columnWidths, dataList, null, filePath);
        }
    }

    public static void exportContractLend(List<List<String>> dataList, OutputStream outputStream) throws Exception {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("是否付清");
        columnNames.add("借款单号");
        columnNames.add("付款状态");
        columnNames.add("员工工号");
        columnNames.add("借款人");
        columnNames.add("借款金额");
        columnNames.add("已还");
        columnNames.add("未还");
        columnNames.add("借款日期");
        columnNames.add("计划还款日期");
        columnNames.add("合同名称");
        columnNames.add("约定结算方式");
        columnNames.add("借款说明");
        List<Integer> columnWidths = new ArrayList<>();
        columnWidths.add(12 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(12 * 256);
        columnWidths.add(12 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(12 * 256);
        columnWidths.add(20 * 256);
        columnWidths.add(12 * 256);
        PoiExcelUtil.exportExcelFile("合同支出明细", columnNames, columnWidths, dataList, null, outputStream);

    }
}
