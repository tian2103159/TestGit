package com.guodu.sms.structure;

import java.sql.Date;
/**
 * 该类为：wait表的抽象类。
 * 
 */
public class Data {
	public String waitId;
	public int userId;
	public String createTime;
	public String sendTime;
	public String validTime;
	public String longId;
	public String desmobile;
	public String content;
	public int contentType;
	public int retry;
	public int gwkind;
	public int report;
	public int priority;
	
	public Data(){}
}
