package com.jtyjy.finance.manager.dto.commission;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description:
 * @author: gll
 * @create: 2021-12-22 09:46
 */
@Data
public class EntryCompletedDTO {

    @ApiModelProperty(value = "凭证号",required = true)
    private String voucherNo;

    @ApiModelProperty(value = "id，任务id",required = true)
    private Long id;
}
