package com.jtyjy.finance.manager.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class EasyExcelUtil {

    /**
     * 判断文件类型
     */
    public static void checkFile(MultipartFile srcFile) {
        // 文件后缀名判断
        String fileName = Objects.requireNonNull(srcFile.getOriginalFilename());
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (!"xls".equals(fileExtension) && !"xlsx".equals(fileExtension)) {
            throw new RuntimeException("导入失败!只支持导入excel文件!");
        }
    }

    /**
     * 导出
     *
     * @param response
     * @param data
     * @param fileName
     * @param sheetName
     * @param clazz
     * @throws Exception
     */
    public static void writeExcel(HttpServletResponse response, List<? extends Object> data, String fileName, String sheetName, Class clazz) throws Exception {
        //表头样式
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        //设置表头居中对齐
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        //内容样式
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        //设置内容靠左对齐
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
        HorizontalCellStyleStrategy horizontalCellStyleStrategy = new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
        EasyExcel.write(getOutputStream(fileName, response), clazz).excelType(ExcelTypeEnum.XLSX).sheet(sheetName).registerWriteHandler(horizontalCellStyleStrategy)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .doWrite(data);
    }

    public static InputStream getTemplateInputStream(String templateName) {
        return EasyExcelUtil.class.getClassLoader().getResourceAsStream("template/" + templateName);
    }

    public static ExcelWriter getExcelWriter(HttpServletResponse response, String fileName, InputStream inputStream, Class<?> clazz) throws Exception {
        return EasyExcel.write(getOutputStream(fileName, response), clazz).withTemplate(inputStream).build();
    }

    public static OutputStream getOutputStream(String fileName, HttpServletResponse response) throws Exception {
        fileName = URLEncoder.encode(fileName, "UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("content-Type", "application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        return response.getOutputStream();
    }

    public static OutputStream getXlsOutputStream(String fileName, HttpServletResponse response) throws Exception {
        fileName = URLEncoder.encode(fileName, "UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("content-Type", "application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xls");
        return response.getOutputStream();
    }
    
//    /**
//     * @param in            文件输入流
//     * @param customContent 自定义模型可以在 AnalysisContext中获取用于监听者回调使用
//     * @param eventListener 用户监听
//     * @throws IOException
//     * @throws EmptyFileException
//     * @throws InvalidFormatException
//     */
//    public static ExcelReader getExcelReader(InputStream in, Object customContent,
//                                             AnalysisEventListener<?> eventListener) throws EmptyFileException, IOException, InvalidFormatException {
//        // 如果输入流不支持mark/reset，需要对其进行包裹
//        if (!in.markSupported()) {
//            in = new PushbackInputStream(in, 8);
//        }
//        // 确保至少有一些数据
//        byte[] header8 = IOUtils.peekFirst8Bytes(in);
//        ExcelTypeEnum excelTypeEnum = null;
//        if (NPOIFSFileSystem.hasPOIFSHeader(header8)) {
//            excelTypeEnum = ExcelTypeEnum.XLS;
//        }
//        if (DocumentFactoryHelper.hasOOXMLHeader(in)) {
//            excelTypeEnum = ExcelTypeEnum.XLSX;
//        }
//        if (excelTypeEnum != null) {
//            return new ExcelReader(in, excelTypeEnum, customContent, eventListener);
//        }
//        throw new InvalidFormatException("Your InputStream was neither an OLE2 stream, nor an OOXML stream");
//
//    }

//    /**
//     * @param in            文件输入流
//     * @param customContent 自定义模型可以在 AnalysisContext中获取用于监听者回调使用
//     * @param eventListener 用户监听
//     * @param trim          是否对解析的String做trim()默认true,用于防止 excel中空格引起的装换报错。
//     * @throws IOException
//     * @throws EmptyFileException
//     * @throws InvalidFormatException
//     */
//    public static ExcelReader getExcelReader(InputStream in, Object customContent,
//                                             AnalysisEventListener<?> eventListener, boolean trim)
//            throws EmptyFileException, IOException, InvalidFormatException {
//        // 如果输入流不支持mark/reset，需要对其进行包裹
//        if (!in.markSupported()) {
//            in = new PushbackInputStream(in, 8);
//        }
//
//        // 确保至少有一些数据
//        byte[] header8 = IOUtils.peekFirst8Bytes(in);
//        ExcelTypeEnum excelTypeEnum = null;
//        if (NPOIFSFileSystem.hasPOIFSHeader(header8)) {
//            excelTypeEnum = ExcelTypeEnum.XLS;
//        }
//        if (DocumentFactoryHelper.hasOOXMLHeader(in)) {
//            excelTypeEnum = ExcelTypeEnum.XLSX;
//        }
//        if (excelTypeEnum != null) {
//            return new ExcelReader(in, excelTypeEnum, customContent, eventListener, trim);
//        }
//        throw new InvalidFormatException("Your InputStream was neither an OLE2 stream, nor an OOXML stream");
//    }

    /**
     * 读取Excel文件内容
     *
     * @param in
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T> List<T> getExcelContent(InputStream in, Class<T> tClass) {
        List<T> excelPropertyIndexModelList = new ArrayList<>();
        try {
//            AnalysisEventListener<T> listener = new AnalysisEventListener<T>() {
//                @Override
//                public void invoke(T excelPropertyIndexModel, AnalysisContext analysisContext) {
//                    excelPropertyIndexModelList.add(excelPropertyIndexModel);
//                }
//
//                @Override
//                public void doAfterAllAnalysed(AnalysisContext analysisContext) {
//                    // 读取之后的操作
//                }
//            };
////            ExcelReader excelReader = EasyExcelUtil.getExcelReader(in, null, listener);
//            // 第二个参数为表头行数，按照实际设置
//            excelReader.read(new Sheet(1, 1, (Class<? extends BaseRowModel>) tClass));
            EasyExcel.read(in,tClass,new PageReadListener(x->excelPropertyIndexModelList.add((T) x))).sheet().doRead();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return excelPropertyIndexModelList;
    }

    /**
     * 获取文件扩展名
     *
     * @param fileName
     * @return
     */
    public static String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * 获取文件名
     *
     * @param fileName
     * @return
     */
    public static String getFileNameNotExtension(String fileName) {
        int index = fileName.lastIndexOf(".");
        return fileName.substring(0, index);
    }
}
