package com.jtyjy.finance.manager.easyexcel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadStyle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 付款成功的汇总数据
 * @author shubo
 */
@Data
@HeadStyle()
@ContentStyle()
@NoArgsConstructor
@AllArgsConstructor
public class PayUnitBankSumExcelData {
    
    @ExcelIgnore()
    private List<Integer> indexList = new ArrayList<Integer>();//更新集合的下标数组
    
    @ExcelProperty("付款单位")
    @ColumnWidth(20)
	private String unitName;//付款单位
    
    @ExcelProperty("收款银行")
    @ColumnWidth(20)
	private String bankName;//收款银行
    
    @ExcelProperty("付款金额")
    @ColumnWidth(20)
	private BigDecimal payMoney;//付款金额

    @Override
    public boolean equals(Object obj) {//付款单位名称、收款银行名称相同则视为相同
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PayUnitBankSumExcelData other = (PayUnitBankSumExcelData) obj;
        if (bankName == null) {
            if (other.bankName != null)
                return false;
        } 
        if (unitName == null) {
            if (other.unitName != null)
                return false;
        } 
        if (bankName != null && unitName != null) {
            if (unitName.equals(other.unitName) && bankName.equals(other.bankName))
                return true;
        }
        
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bankName == null) ? 0 : bankName.hashCode());
        result = prime * result + ((unitName == null) ? 0 : unitName.hashCode());
        return result;
    }

    public PayUnitBankSumExcelData(String unitName, String bankName, BigDecimal payMoney) {
        super();
        this.unitName = unitName;
        this.bankName = bankName;
        this.payMoney = payMoney;
    }
	
	
}
