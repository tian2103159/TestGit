package com.guodu.sms.structure;


public class SubmitRecord {
	
  public String SMSID;						//��Ϣ���
  public int USER_ID;						//�û���ʶ
  public String LONGID;						//��Ϣ������
  public String DESMOBILE;					//Ŀ���ֻ���
  public String CONTENT;					//��Ϣ����
  public int CONTENT_TYPE;					//������Ϣ����,0-ASCII,4-��������Ϣ,8-UCS2����,15-GB����
  public String CREATEDATE;					//��Ϣ�ύʱ��  
  public String VALIDDATE;					//������Ч��(��ֹʱ��), Ĭ��Ϊ:null
  //public int TP_PID;						//GSMЭ�����͡� Ĭ��Ϊ: 0
  //public int TP_UDHI;						//GSMЭ�����͡� Ĭ��Ϊ: 0
  public int RETRY;						//ʧ�����Դ���
  public int GWKIND;						//���ر�ʶ 0=CMPP, 1=SGIP, 2=CNGP, 9=δ֪
  public int REPORT;						//�Ƿ�Ҫ��ظ�����,0-��,1-�ύ,2-����,3-�ύ+����

  public String SENDID;						//�������ر�ʶ
  public int ECODE;	  					//���ͽ����
  	
  public SubmitRecord() {
    	
  }


}
