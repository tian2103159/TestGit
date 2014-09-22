package com.guodu.sms.structure;

public class DeliverMessage {

	public String SRCMOBILE;//消息来源手机号
	public String LONGID;	//消息目的SP接收号码
	public int CONTENT_TYPE;//消息编码, 0--ASCII, 4--二进制, 8--UCS, 15--GBK
	public String CONTENT;	//消息内容, 对二进制码是以16进制字符串的形式表示
	public String SENDID;	//SP提交短信（CMPP_SUBMIT）操作时，与SP相连的ISMG产生的序号;
	public int ECODE;		//发送短信的应答结果; 00--成功, 非0--失败;

	public DeliverMessage() {
	    		    
	}

}
