package com.guodu.sms.structure;

public class DeliverMessage {

	public String SRCMOBILE;//��Ϣ��Դ�ֻ���
	public String LONGID;	//��ϢĿ��SP���պ���
	public int CONTENT_TYPE;//��Ϣ����, 0--ASCII, 4--������, 8--UCS, 15--GBK
	public String CONTENT;	//��Ϣ����, �Զ�����������16�����ַ�������ʽ��ʾ
	public String SENDID;	//SP�ύ���ţ�CMPP_SUBMIT������ʱ����SP������ISMG���������;
	public int ECODE;		//���Ͷ��ŵ�Ӧ����; 00--�ɹ�, ��0--ʧ��;

	public DeliverMessage() {
	    		    
	}

}
