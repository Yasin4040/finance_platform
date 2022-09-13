package com.jtyjy.finance.manager.listener.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.alibaba.excel.exception.ExcelCommonException;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.CellExtra;
import com.alibaba.excel.read.listener.ReadListener;
import com.jtyjy.finance.manager.dto.individual.ImportErrorDTO;
import com.jtyjy.finance.manager.utils.DynamicBeanUtil;
import com.jtyjy.finance.manager.utils.ExcelImportValid;
import com.jtyjy.finance.manager.utils.ListUtils;
import io.netty.util.internal.ObjectUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.util.PropertiesUtil;


import javax.validation.ValidationException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Description:
 * Created by ZiYao Lee on 2022/08/30.
 * Time: 08:45
 */
@Slf4j
public class PageReadListener<T> implements ReadListener<T> {
    /**
     * Single handle the amount of data
     */
    public static int BATCH_COUNT = 100;
    /**
     * Temporary storage of data
     */
    private List<T> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
    private List<Map> errorList = new ArrayList<>();
    /**
     * consumer
     */
    private final Consumer<List<T>> consumer;

    public PageReadListener(Consumer<List<T>> consumer,List<Map> errorList) {
        this.consumer = consumer;
        this.errorList = errorList;
    }

    public PageReadListener(Consumer<List<T>> consumer) {
        this.consumer = consumer;
    }


    @Override
    public void onException(Exception exception, AnalysisContext analysisContext) throws Exception {
//        String error = String.format("解析失败，但是继续解析下一行:{}", exception.getMessage());
//        log.error(error);
//        // 如果是某一个单元格的转换异常 能获取到具体行号
//        // 如果要获取头的信息 配合invokeHeadMap使用
//        if (exception instanceof ExcelDataConvertException) {
//            ExcelDataConvertException excelDataConvertException = (ExcelDataConvertException)exception;
//            error = String.format("第{}行，第{}列解析异常，数据为:{}", excelDataConvertException.getRowIndex(),
//                    excelDataConvertException.getColumnIndex(), excelDataConvertException.getCellData());
//            log.error(error);
//        }
//        errorList.add(error);
//        throw  exception;
    }

    @Override
    public void invokeHead(Map<Integer, CellData> map, AnalysisContext analysisContext) {

    }

    @SneakyThrows
    @Override
    public void invoke(T data, AnalysisContext context) {
        try {
            if (isLineNullValue(data)) {
                return;
            }
            ExcelImportValid.valid(data);
        } catch (Exception e) {

            //validationFormatError
//            Object object = new Object();

            Map<String,Object> errorMap= BeanUtils.describe(data);
            errorMap.put("validationFormatError",e.getMessage());
//            BeanUtils.populate(object,dataMap);
            errorList.add(errorMap);
            return;
        }
        cachedDataList.add(data);
        if (cachedDataList.size() >= BATCH_COUNT) {
            consumer.accept(cachedDataList);
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    @Override
    public void extra(CellExtra cellExtra, AnalysisContext analysisContext) {

    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (CollectionUtils.isNotEmpty(cachedDataList)) {
            consumer.accept(cachedDataList);
        }
    }

    @Override
    public boolean hasNext(AnalysisContext analysisContext) {
        return true;
    }

    /**
     * 判断整行单元格数据是否均为空
     */
    private boolean isLineNullValue(T data) {
        if (data instanceof String) {
            return ObjectUtils.isEmpty(data);
        }
        try {
            List<Field> fields = Arrays.stream(data.getClass().getDeclaredFields())
                    .filter(f -> f.isAnnotationPresent(ExcelProperty.class))
                    .collect(Collectors.toList());
            List<Boolean> lineNullList = new ArrayList<>(fields.size());
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(data);
                if (ObjectUtils.isEmpty(value)) {
                    lineNullList.add(Boolean.TRUE);
                } else {
                    lineNullList.add(Boolean.FALSE);
                }
            }
            return lineNullList.stream().allMatch(Boolean.TRUE::equals);
        } catch (Exception e) {
            log.error("读取数据行[{}]解析失败: {}", data, e.getMessage());
        }
        return true;
    }

}
