package com.jtyjy.finance.manager.test;

import com.klcwqy.easyexcel.anno.Location;

public class ZhuanZhang {

	@Location(column = 0)
	private String name;
	@Location(column = 1)
	private String account;
	@Location(column = 2)
	private Double money;
	@Location(column = 3)
	private String unit;
	public String getName() {
		return name;
	}
	public String getAccount() {
		return account;
	}
	public Double getMoney() {
		return money;
	}
	public String getUnit() {
		return unit;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public void setMoney(Double money) {
		this.money = money;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	@Override
	public String toString() {
		return "ZhuanZhang [name=" + name + ", account=" + account + ", money=" + money + ", unit=" + unit + "]";
	}
}
