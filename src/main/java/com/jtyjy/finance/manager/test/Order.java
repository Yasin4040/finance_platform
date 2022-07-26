package com.jtyjy.finance.manager.test;

import java.util.Date;

import com.klcwqy.easyexcel.anno.Location;
import com.klcwqy.easyexcel.anno.SheetInfo;

@SheetInfo(index = 0,startRow = 5,key = "detail")
public class Order {
	@Location(soleRow = 1, column = 1)
	private String a;
	@Location(soleRow = 1, column = 3)
	private String b;
	@Location(soleRow = 1, column = 5)
	private String c;
	@Location(soleRow = 2, column = 1)
	private String d;
	@Location(soleRow = 2, column = 3, pattern = "yyyy/MM/dd")
	private Date e;
	@Location(soleRow = 2, column = 5)
	private String f;
	@Location(soleRow = 3, column = 1)
	private Integer g;
	@Location(soleRow = 3, column = 3)
	private String h;
	@Location(soleRow = 3, column = 5)
	private String i;
	private OrderDetail instance;
}
