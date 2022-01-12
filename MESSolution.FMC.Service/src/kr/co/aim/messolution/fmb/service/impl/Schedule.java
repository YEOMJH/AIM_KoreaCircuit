package kr.co.aim.messolution.fmb.service.impl;

public class Schedule {
	private String cron;
	public Schedule(String cron, String bpel) {
		this.cron = cron;
		this.bpel = bpel;
	}
	private String bpel;
	public Schedule() {
	}
	
	public void setCron(String cron) {
		this.cron = cron;
	}
	public void setBpel(String bpel) {
		this.bpel = bpel;
	}
	public String getCron() {
		return cron;
	}
	public String getBpel() {
		return bpel;
	}
}
