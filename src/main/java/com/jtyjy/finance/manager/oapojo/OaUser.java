package com.jtyjy.finance.manager.oapojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * OA人员信息
 * 作者 konglingcheng
 * 日期 2020年8月6日
 */
@TableName(value = "hrmresource")
@Data
public class OaUser implements Serializable{

	private static final long serialVersionUID = 1L;
	@TableId(type = IdType.AUTO)
	private String id                  ;
	private String lastname               ;
	private String telephone              ;

}
