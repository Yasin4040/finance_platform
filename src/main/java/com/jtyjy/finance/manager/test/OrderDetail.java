package com.jtyjy.finance.manager.test;

import com.klcwqy.easyexcel.anno.Location;

public class OrderDetail {

	@Location(column = 0)
	private String a;
	@Location(column = 1)
	private String b;
	@Location(column = 2)
	private String c;
	@Location(column = 3)
	private String d;
	@Location(column = 4)
	private String e;
	@Location(column = 5)
	private String f;
	public String getA() {
		return a;
	}

	public String getB() {
		return b;
	}

	public String getC() {
		return c;
	}

	public String getD() {
		return d;
	}

	public String getE() {
		return e;
	}

	public String getF() {
		return f;
	}

	public void setA(String a) {
		this.a = a;
	}

	public void setB(String b) {
		this.b = b;
	}

	public void setC(String c) {
		this.c = c;
	}

	public void setD(String d) {
		this.d = d;
	}

	public void setE(String e) {
		this.e = e;
	}

	public void setF(String f) {
		this.f = f;
	}

	@Override
	public String toString() {
		return "OrderDetail [a=" + a + ", b=" + b + ", c=" + c + ", d=" + d + ", e=" + e + ", f=" + f + "]";
	}
	
}
