package com.jtyjy.finance.manager.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * @Author 袁前兼
 * @Date 2021/4/20 14:32
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateBudgetYearAgentDTO extends AddBudgetYearAgentDTO {

    @ApiModelProperty(value = "年度动因Id", required = true)
    private Long yearAgentId;

    @ApiModelProperty(value = "1月预算", required = true)
    private BigDecimal m1;

    @ApiModelProperty(value = "2月预算", required = true)
    private BigDecimal m2;

    @ApiModelProperty(value = "3月预算", required = true)
    private BigDecimal m3;

    @ApiModelProperty(value = "4月预算", required = true)
    private BigDecimal m4;

    @ApiModelProperty(value = "5月预算", required = true)
    private BigDecimal m5;

    @ApiModelProperty(value = "6月预算", required = true)
    private BigDecimal m6;

    @ApiModelProperty(value = "7月预算", required = true)
    private BigDecimal m7;

    @ApiModelProperty(value = "8月预算", required = true)
    private BigDecimal m8;

    @ApiModelProperty(value = "9月预算", required = true)
    private BigDecimal m9;

    @ApiModelProperty(value = "10月预算", required = true)
    private BigDecimal m10;

    @ApiModelProperty(value = "11月预算", required = true)
    private BigDecimal m11;

    @ApiModelProperty(value = "12月预算", required = true)
    private BigDecimal m12;

    public void setMonthMoney() {
        if (this.getM1() == null) {
            this.setM1(BigDecimal.ZERO);
        }
        if (this.getM2() == null) {
            this.setM2(BigDecimal.ZERO);
        }
        if (this.getM3() == null) {
            this.setM3(BigDecimal.ZERO);
        }
        if (this.getM4() == null) {
            this.setM4(BigDecimal.ZERO);
        }
        if (this.getM5() == null) {
            this.setM5(BigDecimal.ZERO);
        }
        if (this.getM6() == null) {
            this.setM6(BigDecimal.ZERO);
        }
        if (this.getM7() == null) {
            this.setM7(BigDecimal.ZERO);
        }
        if (this.getM8() == null) {
            this.setM8(BigDecimal.ZERO);
        }
        if (this.getM9() == null) {
            this.setM9(BigDecimal.ZERO);
        }
        if (this.getM10() == null) {
            this.setM10(BigDecimal.ZERO);
        }
        if (this.getM11() == null) {
            this.setM11(BigDecimal.ZERO);
        }
        if (this.getM12() == null) {
            this.setM12(BigDecimal.ZERO);
        }
    }
}
