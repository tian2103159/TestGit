package com.guodu.dataservice;

import com.guodu.sms.util.Log;
import com.huawei.insa2.util.Args;
import com.huawei.insa2.util.Cfg;
/**
 * ���ӹ��ܣ�Logger,���쳣��Ϣд���ļ��У��Ա��ڳ���ʱ
 * �ҳ����ںδ���
 * 
 * @author Wangweiwei  2008-2-25
 *
 */  
public class Start {
	public static void main(String[] args){
		
		String cfg = "configcmpp.xml";
		
		if(args.length == 1){
			cfg = args[0];  		//��������ļ���
		}
		
		try{
			//��ʼ��LOG��־
			String path="./log.properties";
			org.apache.log4j.PropertyConfigurator.configure(path);
			
			Args optArgs = new Cfg(cfg).getArgs("option");
			
			int cmppThread = optArgs.get("cmppthread",1);
			int sgipThread = optArgs.get("sgipthread",1);
			int xltThread = optArgs.get("xltthread",1);
			int otherThread = optArgs.get("othersthread", 1);
			
			ThreadGroup cmppTg = new ThreadGroup("cmppthread");
			ThreadGroup sgipTg = new ThreadGroup("sgipthread");
			ThreadGroup xltTg = new ThreadGroup("xltthread");
			ThreadGroup otherTg = new ThreadGroup("othersthread");
			
			while(true){
				int iFlag = 0;
				
				if(cmppThread > cmppTg.activeCount()){
					new CMPPDataThread(cmppTg,cfg);
					iFlag = 1;
				}
				
				if(sgipThread > sgipTg.activeCount()){
					new SGIPDataThread(sgipTg,cfg);
					iFlag = 1;
				}
				
				if(xltThread > xltTg.activeCount()){
					new XLTDataThread(xltTg,cfg);
					iFlag = 1;
				}
				
				if(otherThread>otherTg.activeCount()){
					new OtherDataThread(otherTg,cfg);
					iFlag = 1;
				}
				if(iFlag ==1){
					iFlag = 0;
					showMessage("��ǰ���� " + cmppTg.activeCount() 
						+ "���ƶ�ȡ�����߳�, "+ sgipTg.activeCount()
						+ " ����ͨȡ�����߳�,"+ xltTg.activeCount()
						+ " ��С��ͨȡ�����߳�,"+ otherTg.activeCount()
						+ " ��Otherȡ�����߳�.");
				}
				
				Thread.sleep(1000);
			}
		}catch(Exception e){
			e.printStackTrace();
			showMessage("�����ڲ�ԭ��"+e.getMessage());
		}
	}   
	
	private static void showMessage(String str){
		Log.println("��ȡ�߳̿���̨��"+str);
	} 
}
