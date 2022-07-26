package com.jtyjy.finance.manager.test;

import java.util.Date;

import com.klcwqy.easyexcel.anno.Location;

public class Travel {

	@Location(column = 0)
	private String gj;
	@Location(column = 1,pattern = "yyyy/MM/dd")
	private Date start;
	@Location(column = 2,pattern = "yyyy/MM/dd")
	private Date end;
	@Location(column = 3)
	private String location;
	@Location(column = 4)
	private String mdd;
	@Location(column = 5)
	private Double ctf;
	@Location(column = 6)
	private Double snf;
	@Location(column = 7)
	private Double zsf;
	@Location(column = 8)
	private Double ts;
	@Location(column = 9)
	private Double bz;
	@Location(column = 10)
	private Double je;
	@Location(column = 11)
	private Double qt;
	@Location(column = 12)
	private Double xj;
	public String getGj() {
		return gj;
	}
	public Date getStart() {
		return start;
	}
	public Date getEnd() {
		return end;
	}
	public String getLocation() {
		return location;
	}
	public String getMdd() {
		return mdd;
	}
	public Double getCtf() {
		return ctf;
	}
	public Double getSnf() {
		return snf;
	}
	public Double getZsf() {
		return zsf;
	}
	public Double getTs() {
		return ts;
	}
	public Double getBz() {
		return bz;
	}
	public Double getJe() {
		return je;
	}
	public Double getQt() {
		return qt;
	}
	public Double getXj() {
		return xj;
	}
	public void setGj(String gj) {
		this.gj = gj;
	}
	public void setStart(Date start) {
		this.start = start;
	}
	public void setEnd(Date end) {
		this.end = end;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public void setMdd(String mdd) {
		this.mdd = mdd;
	}
	public void setCtf(Double ctf) {
		this.ctf = ctf;
	}
	public void setSnf(Double snf) {
		this.snf = snf;
	}
	public void setZsf(Double zsf) {
		this.zsf = zsf;
	}
	public void setTs(Double ts) {
		this.ts = ts;
	}
	public void setBz(Double bz) {
		this.bz = bz;
	}
	public void setJe(Double je) {
		this.je = je;
	}
	public void setQt(Double qt) {
		this.qt = qt;
	}
	public void setXj(Double xj) {
		this.xj = xj;
	}
	@Override
	public String toString() {
		return "Travel [gj=" + gj + ", start=" + start + ", end=" + end + ", location=" + location + ", mdd=" + mdd
				+ ", ctf=" + ctf + ", snf=" + snf + ", zsf=" + zsf + ", ts=" + ts + ", bz=" + bz + ", je=" + je
				+ ", qt=" + qt + ", xj=" + xj + "]";
	}
}
