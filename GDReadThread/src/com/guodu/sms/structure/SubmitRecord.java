package com.guodu.sms.structure;


public class SubmitRecord {
	
  public String SMSID;						//消息编号
  public int USER_ID;						//用户标识
  public String LONGID;						//消息长号码
  public String DESMOBILE;					//目的手机号
  public String CONTENT;					//消息内容
  public int CONTENT_TYPE;					//待发消息编码,0-ASCII,4-二进制消息,8-UCS2编码,15-GB编码
  public String CREATEDATE;					//消息提交时间  
  public String VALIDDATE;					//发送有效期(截止时间), 默认为:null
  //public int TP_PID;						//GSM协议类型。 默认为: 0
  //public int TP_UDHI;						//GSM协议类型。 默认为: 0
  public int RETRY;						//失败重试次数
  public int GWKIND;						//网关标识 0=CMPP, 1=SGIP, 2=CNGP, 9=未知
  public int REPORT;						//是否要求回复报告,0-无,1-提交,2-接收,3-提交+接收

  public String SENDID;						//发送网关标识
  public int ECODE;	  					//发送结果码
  	
  public SubmitRecord() {
    	
  }


}
