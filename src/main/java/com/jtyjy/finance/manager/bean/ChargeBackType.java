package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @description:
 * @author: gll
 * @create: 2021-12-13 11:42
 */
@TableName(value = "charge_back_type")
@Data
public class ChargeBackType {

    private Integer id;

    private String backType;
}
