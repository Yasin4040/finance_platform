package com.jtyjy.finance.manager.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @description:
 * @author: gll
 * @create: 2021-12-16 10:44
 */
@Data
public class ExpenseInfoVO {



    @ExcelProperty(value="报销单号")
    private String reimcode;


    @ExcelProperty(value="报销状态")
    private String reuqeststatus_dictname;

    @ExcelProperty(value="最新进度")
    private String curscanstatusname;

    @ExcelProperty(value="接收人")
    private String curscanername;

    @ExcelProperty(value="报销类型名称")
    private String bxtype_dictname;

    @ExcelProperty(value="附件张数")
    private Integer fjzs;

    @ExcelProperty(value="届别")
    private String yearname;

    @ExcelProperty(value="月份")
    private String monthname;

    @ExcelProperty(value="预算单位")
    private String ysdw;

    @ExcelProperty(value="报销人")
    private String bxr;

    @ExcelProperty(value="报销日期")
    private String bxrq;

    @ExcelProperty(value="报销金额")
    private Double bxje;

    @ExcelProperty(value="转账金额")
    private Double zzje;

    @ExcelProperty(value="冲账金额")
    private Double czje;

    @ExcelProperty(value="现金金额")
    private Double xjje;

    @ExcelProperty(value="其他金额")
    private Double othermoney;

    @ExcelProperty(value="划拨金额")
    private Double hbje;

    @ExcelProperty(value="申请人名称")
    private String applicantame;

    @ExcelProperty(value="申请时间")
    private String applicanttime;

    @ExcelProperty(value="提交时间")
    private String submittime;

    @ExcelProperty(value="审核通过时间")
    private String verifytime;

}
