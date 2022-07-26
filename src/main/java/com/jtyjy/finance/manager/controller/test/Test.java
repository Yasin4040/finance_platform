package com.jtyjy.finance.manager.controller.test;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jtyjy.core.constant.Constants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Test {

	private String a;
	private String b;
	private Integer c;
	@DateTimeFormat(pattern = "yyyyMMdd")
	private Date d;
	
	public static void main(String[] args) throws JsonProcessingException {
		Test t = new Test();
		t.setA("--");
		t.setB("$$");
		t.setC(456);
		t.setD(new Date());
		System.out.println(Constants.MAPPER.writeValueAsString(t));
	}
}
