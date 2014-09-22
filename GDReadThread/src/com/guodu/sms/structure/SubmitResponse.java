package com.guodu.sms.structure;

public class SubmitResponse {
	/**
	 * SP提交短信（CMPP_SUBMIT）操作时，与SP相连的ISMG产生的序号;
	 */
	public String MsgID;
	
	/**
	 * 发送短信的应答结果; 00--成功, 非0--失败;
	 */
	public int Stat;					

    public SubmitResponse() {
    	
    }

}
