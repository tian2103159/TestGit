package com.guodu.dataservice;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import oracle.jdbc.driver.OracleDriver;

import org.apache.log4j.Logger;   

import com.guodu.sms.structure.Data;
import com.guodu.sms.util.Log;
import com.huawei.insa2.util.Args;
import com.huawei.insa2.util.Cfg;   
/**
 * �ƶ����ݶ�ȡ�߳��ࣨ��wait���У����������ݴ浽 �ƶ����С�
 *
 */
public class CMPPDataThread extends Thread {
	private static Logger logger = Logger.getLogger(CMPPDataThread.class);
	
	private String cfgFileName;
	private String oraUrl;
	private String oraUserName;
	private String oraPassword;
	
	private Connection con;
	
	private String oraReadWaitData;
	private String oraWriteData;  //�� �ƶ��� д���ݵ� �洢���� 
	private String oraNumQuery;   //��ȡ �ƶ����� ��ǰ���������� �洢����
	
	private int oraGwkind = 0; 
	private int oraBatchNum;
	
	private long sleepTime;
	
	private int ydmax; 			    //�ƶ���������ŵ���������--��ֵ 
	
	private int sendInterval;       //�޶� �߳���ȡ�ٶ�
	
	public CMPPDataThread(){}
	
	/**
	 * ���췽���У��������ļ��ж�ȡ������ݣ����������ݿ⡢�ƶ����ķ�ֵ��
	 * �������ö�����̡߳�
	 * @param cfgFileName �����ļ�������
	 */
	public CMPPDataThread(ThreadGroup grp,String cfgFileName){
		super(grp,"cmppThread");
		
		this.cfgFileName = cfgFileName;
		
		try{
			Args oraArgs = new Cfg(cfgFileName).getArgs("oracle");
			
			oraUrl = oraArgs.get("url","");
			oraUserName = oraArgs.get("username","");
			oraPassword = oraArgs.get("password","");
			
			oraReadWaitData = oraArgs.get("readwaitdata","");
			oraWriteData = oraArgs.get("writedata","");
			oraNumQuery = oraArgs.get("numquery","");
			
			oraBatchNum = oraArgs.get("batchnum",20);
			sleepTime = oraArgs.get("sleeptime",500);
			
			ydmax = oraArgs.get("yidongmax",1000);
			
			sendInterval = oraArgs.get("sendinterval", 5);
			
			start();
			
		}catch(Exception e){
			logger.debug("�����ڲ��쳣��ԭ��["+ e.getMessage()+"]");
			e.printStackTrace();
		}
	}
	
	public void run(){
		try{
			showMessage("�ƶ���ȡ�߳����������ݿ⣺" + oraUrl );
			
			//�������ݿ�
			OracleDriver od = new OracleDriver();
			Properties pro = new Properties();
			pro.setProperty("user",oraUserName);
			pro.setProperty("password",oraPassword); 
			
			con = od.connect(oraUrl,pro);
			con.setAutoCommit(false);
			
			while(true){
				
				sleep(sendInterval);
				
				CallableStatement cst = null;
				
				int ydnum = getYDDataNum(con); //ȡ���ƶ����ķ�ֵ
				if(ydnum >= ydmax ){           //����ƶ����е������������ڷ�ֵ��ȡ�����̵߳ȴ�
					sleep(sleepTime);
					continue;
				}
				
				cst = con.prepareCall("{? = call " + oraReadWaitData + "(?,?) }");
				cst.registerOutParameter(1,Types.CLOB);
				cst.setInt(2,oraBatchNum);
				cst.setInt(3,oraGwkind);
				cst.execute();
				
				
				String data = cst.getString(1);//�������
				
				if(data==null || data.length()==0){
					cst.close();
					sleep(500); //wait���������ݣ�˯�ߵȴ���
					continue;
				}
				
				Data[] writeData = getData(data);
				Map map = count(writeData);
		        con.commit();
				cst.close();
				writeLog(map); //����־�ļ���д��ÿ���û���ȡ����������
				
				//�����ǽ�ȡ��������д�ص��ƶ�����
				cst = con.prepareCall("{ call "+oraWriteData+"(?,?,?,?,?,?,?,?,?,?,?,?,?)}");
				for(int i=0;i<writeData.length;i++){
					cst.setString(1,writeData[i].waitId);
					cst.setInt(2,writeData[i].userId);
					cst.setString(3,writeData[i].createTime);
					cst.setString(4,writeData[i].sendTime);
					cst.setString(5,writeData[i].validTime);
					cst.setString(6,writeData[i].longId);
					cst.setString(7,writeData[i].desmobile);
					cst.setString(8,writeData[i].content);
					cst.setInt(9,writeData[i].contentType);
					cst.setInt(10,writeData[i].retry);
					cst.setInt(11,writeData[i].gwkind);
					cst.setInt(12,writeData[i].report);
					cst.setInt(13,writeData[i].priority);
					
					cst.addBatch();
					sleep(1);
				}
				cst.executeBatch();
				con.commit();
				cst.close();
				map.clear(); //һ��ȡ���ݽ��������MAP
			} //while end;
		}catch(Exception e){
			try {con.rollback();} catch (SQLException e1) {e1.printStackTrace();}
			e.printStackTrace();
			logger.debug("�����ڲ��쳣��ԭ��["+ e.getMessage()+"]");
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		showMessage("�ƶ�ȡ�����߳����н���");
	}
	
	/**
	 * ��ȡ�������ݣ�����Ϊ�ࡣ
	 * @param data ��wait����ȡ��������	
	 * @return ���ط�װ�������
	 */
	public Data[]  getData(String data){
		String row[] = data.split("\1");
		int num = row.length;
		Data writeData[] = new Data[num];
		
		for(int i=0;i<num;i++){
			String col[] = row[i].split("\2");
			
			writeData[i] = new Data();
			writeData[i].waitId = col[0];
			writeData[i].userId = Integer.parseInt(col[1]);
			writeData[i].createTime = col[2];
			writeData[i].sendTime = col[3];
			writeData[i].validTime = col[4];
			writeData[i].longId = col[5];
			writeData[i].desmobile = col[6];
			writeData[i].content = col[7];
			writeData[i].contentType = Integer.parseInt(col[8]);
			writeData[i].retry = Integer.parseInt(col[9]);
			writeData[i].gwkind = Integer.parseInt(col[10]);
			writeData[i].report = Integer.parseInt(col[11]);
			writeData[i].priority = Integer.parseInt(col[12]);
		}	
		
		return writeData;
	}
	
	/**
	 * �÷�������ͳ�ƣ��ڶ�ȡһ������ʱ��ÿ���û������ݱ���ȡ�˶�������д����־�ļ��У���ҵ����Ա�ο���
	 * @param writeData һ�ζ�����������
	 * @return ����һ��Map,Key--�û���userId,Value--��ǰ�û����͵���������
	 */
	public Map count(Data[] writeData){
		Map map = new HashMap();
		for(int i=0;i<writeData.length;i++){
			int id = writeData[i].userId;
			if(map.containsKey(id)){
				Integer n = (Integer) map.get(id);
				map.put(id,n+1);
			}else{
				map.put(id,1);
			}
		}
		return map;
	}
	
	/**
	 * ��ȡMAP�����û����ݶ�ȡ����������д�뵽��־�ļ��С�
	 * @param map 
	 */
	public void writeLog(Map map){
		Set set = map.keySet();
		Iterator it = set.iterator();
		while(it.hasNext()){
			Integer userId = (Integer) it.next();
			showMessage("�˴�"+userId+"�û�����ȡ��"+map.get(userId)+"����¼.");
		}
	}
	
	/**
	 * �÷�������ͨ����ȡ �ƶ��������������� �洢���� ���õ��ƶ����е�����������
	 * @param con ���ݿ�����
	 * @return �ƶ����е���������
	 */
	private synchronized int getYDDataNum(Connection con){
		CallableStatement cst = null;
		
		try{
			cst = con.prepareCall("{? = call " + oraNumQuery +"(?)}");
			cst.registerOutParameter(1,Types.INTEGER);
			cst.setInt(2, oraGwkind);
			cst.execute();
			
			int num = cst.getInt(1);
			
			return num;
		}catch(Exception e){
			e.printStackTrace();
			return 0;
		}finally{
			try {
				cst.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void showMessage(String str){
		Log.println("[�ƶ����ݶ�ȡ�߳�-"+ this.getId()+"] "+str);
	}
}