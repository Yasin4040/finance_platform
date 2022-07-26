package com.jtyjy.finance.manager.test;

import com.klcwqy.easyexcel.anno.Location;
import com.klcwqy.easyexcel.anno.SheetInfo;

@SheetInfo(index = 2,startRow = 3,key = "travel")
public class TravelInfo {

	@Location(soleRow = 0, column = 2)
	private String name;
	@Location(soleRow = 0,column = 7)
	private String reason;
	private Travel instance;
}
