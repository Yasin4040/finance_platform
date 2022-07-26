package com.jtyjy.finance.manager.vo;



import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author shubo
 */
@ApiModel(description = "期间信息VO")
@Data
public class YearPeriodVO {

    /**
     * id
     */
    @ApiModelProperty(value = "期间id")
    private Long id;

    @ApiModelProperty(value = "年月id")
    private String periodId;
    
    @ApiModelProperty(value = "届别名称-月份id")
    private String yearmonthname;
    
    /**
     * 届别名称，例如19届
     */
    @ApiModelProperty(value = "期间名称")
    private String period;

    /**
     * 开始日期
     */
    @ApiModelProperty(value = "开始日期")
    private String startdate;

    /**
     * 结束日期
     */
    @ApiModelProperty(value = "结束日期")
    private String enddate;

    /**
     * 当前期间
     */
    @ApiModelProperty(value = "当前期间 0：否 1：是")
    private String currentflag;

    /**
     * 编号如2019
     */
    @ApiModelProperty(value = "编号")
    private String code;

    /**
     * 启动预算
     */
    @ApiModelProperty(value = "启动预算编制 0：否 1：是")
    private String startbudgetflag;

    /**
     * 月结标志
     */
    @ApiModelProperty(value = "月结标志 0：否 1：是")
    private String endbudgeteditflag;
    
    private List<YearPeriodVO> children;
}
