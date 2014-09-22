package com.guodu.dataservice;

import com.guodu.sms.util.Log;
import com.huawei.insa2.util.Args;
import com.huawei.insa2.util.Cfg;
/**
 * 增加功能：Logger,将异常信息写入文件中，以便于出错时
 * 找出错在何处。
 * 
 * @author Wangweiwei  2008-2-25
 *
 */  
public class Start {
	public static void main(String[] args){
		
		String cfg = "configcmpp.xml";
		
		if(args.length == 1){
			cfg = args[0];  		//获得配置文件名
		}
		
		try{
			//初始化LOG日志
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
					showMessage("当前共有 " + cmppTg.activeCount() 
						+ "个移动取数据线程, "+ sgipTg.activeCount()
						+ " 个联通取数据线程,"+ xltTg.activeCount()
						+ " 个小灵通取数据线程,"+ otherTg.activeCount()
						+ " 个Other取数据线程.");
				}
				
				Thread.sleep(1000);
			}
		}catch(Exception e){
			e.printStackTrace();
			showMessage("发生内部原因："+e.getMessage());
		}
	}   
	
	private static void showMessage(String str){
		Log.println("读取线程控制台："+str);
	} 
}
