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
  
public class XLTDataThread extends Thread{
	private static Logger logger = Logger.getLogger(OtherDataThread.class);

	private String cfgFileName;

	private String oraUrl;

	private String oraUserName;

	private String oraPassword;

	private Connection con;

	private String oraReadWaitData;

	private String oraWriteData; // 向 小灵通表 写数据的 存储过程

	private String oraNumQuery; // 获取 小灵通表中 当前数据总数的 存储过程
	
	private String gwkind;      //从配置文件中读取网关，应该是多个，以","分隔

	private int oraBatchNum;

	private long sleepTime;

	private int xltmax; // 小灵通表中最多存放的数据条数--阀值

	private int sendInterval; // 限定 线程提取速度

	public XLTDataThread() {
	}

	/**
	 * 构造方法中，从配置文件中读取相关数据，如连接数据库、移动表的阀值。 并启动该对象的线程。
	 * 
	 * @param cfgFileName
	 *            配置文件的名字
	 */
	public XLTDataThread(ThreadGroup grp, String cfgFileName) {
		super(grp, "xltThread");

		this.cfgFileName = cfgFileName;

		try {
			Args oraArgs = new Cfg(cfgFileName).getArgs("oracle");

			oraUrl = oraArgs.get("url", "");
			oraUserName = oraArgs.get("username", "");
			oraPassword = oraArgs.get("password", "");

			oraReadWaitData = oraArgs.get("readwaitdata", "");
			oraWriteData = oraArgs.get("writedata", "");
			oraNumQuery = oraArgs.get("numquery", "");

			gwkind = oraArgs.get("xltGwkind", "");
				
			oraBatchNum = oraArgs.get("batchnum", 20);
			sleepTime = oraArgs.get("sleeptime", 500);

			xltmax = oraArgs.get("xltmax", 1000);

			sendInterval = oraArgs.get("sendinterval", 5);

			start();

		} catch (Exception e) {
			logger.debug("发生内部异常，原因：[" + e.getMessage() + "]");
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			showMessage("小灵通读取线程启动，数据库：" + oraUrl);

			// 连接数据库
			OracleDriver od = new OracleDriver();
			Properties pro = new Properties();
			pro.setProperty("user", oraUserName);
			pro.setProperty("password", oraPassword);

			con = od.connect(oraUrl, pro);
			con.setAutoCommit(false);

			while (true) {

				sleep(sendInterval);

				CallableStatement cst = null;

				int xltnum = getXLTDataNum(con); // 取得小灵通表的阀值
				if (xltnum >= xltmax) { // 如果小灵通表中的数据总数大于阀值，取数据线程等待
					sleep(sleepTime);
					continue;
				}
				
				String[] sn = gwkind.split(","); //取得网关
				int[] oraGwkind = new int[sn.length];
				for(int i=0;i<sn.length;i++){
					oraGwkind[i]=Integer.parseInt(sn[i]);					
				}
				
				String data="";
				
				cst = con.prepareCall("{? = call " + oraReadWaitData
							+ "(?,?) }");
				if(oraGwkind.length==1){
					cst.registerOutParameter(1, Types.CLOB);
					cst.setInt(2, oraBatchNum);
					cst.setInt(3, oraGwkind[0]);
					cst.execute();
					
					data = cst.getString(1);// 获得数据
				}else{
					for(int i=0;i<oraGwkind.length;i++){
						cst.registerOutParameter(1, Types.CLOB);
						cst.setInt(2, oraBatchNum);
						cst.setInt(3, oraGwkind[i]);
						cst.execute();
						String dataTemp = cst.getString(1);// 获得数据
						
						if(dataTemp!=null){
							data = data + dataTemp;
						}
						sleep(1);
					}
				}
				
				if (data == null || data.length() == 0) {
					cst.close();
					sleep(500); // wait表中无数据，睡眠等待。
					continue;
				}
				
				Data[] writeData = getData(data);
				Map map = count(writeData);
				con.commit();
				cst.close();
				writeLog(map); // 向日志文件中写入每个用户读取出的总条数

				// 以下是将取出的数据写回到小灵通表中
				cst = con.prepareCall("{ call " + oraWriteData
						+ "(?,?,?,?,?,?,?,?,?,?,?,?,?)}");
				for (int i = 0; i < writeData.length; i++) {
					cst.setString(1, writeData[i].waitId);
					cst.setInt(2, writeData[i].userId);
					cst.setString(3, writeData[i].createTime);
					cst.setString(4, writeData[i].sendTime);
					cst.setString(5, writeData[i].validTime);
					cst.setString(6, writeData[i].longId);
					cst.setString(7, writeData[i].desmobile);
					cst.setString(8, writeData[i].content);
					cst.setInt(9, writeData[i].contentType);
					cst.setInt(10, writeData[i].retry);
					cst.setInt(11, writeData[i].gwkind);
					cst.setInt(12, writeData[i].report);
					cst.setInt(13, writeData[i].priority);
					cst.addBatch();
					sleep(1);
				}
				cst.executeBatch();
				con.commit();
				cst.close();
				map.clear(); // 一次取数据结束，清空MAP
			} // while end;
		} catch (Exception e) {
			try {
				con.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			logger.debug("发生内部异常，原因：[" + e.getMessage() + "]");
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		showMessage("小灵通读取数据线程运行结束");
	}

	/**
	 * 将取出的数据，抽象为类。
	 * 
	 * @param data
	 *            从wait表中取出的数据
	 * @return 返回封装的类对象
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
	 * 该方法用于统计：在读取一次数据时，每个用户的数据被读取了多少条，写入日志文件中，供业务人员参考。
	 * 
	 * @param writeData
	 *            一次读出来的数据
	 * @return 返回一个Map,Key--用户的userId,Value--当前用户发送的总条数。
	 */
	public Map count(Data[] writeData) {
		Map map = new HashMap();
		for (int i = 0; i < writeData.length; i++) {
			int id = writeData[i].userId;
			if (map.containsKey(id)) {
				Integer n = (Integer) map.get(id);
				map.put(id, n + 1);
			} else {
				map.put(id, 1);
			}
		}
		return map;
	}

	/**
	 * 读取MAP，将用户数据读取的总条数，写入到日志文件中。
	 * 
	 * @param map
	 */
	public void writeLog(Map map) {
		Set set = map.keySet();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			Integer userId = (Integer) it.next();
			showMessage("此次" + userId + "用户共读取了" + map.get(userId) + "条记录.");
		}
	}

	/**
	 * 该方法用于通过读取 小灵通表中数据总数的 存储过程 ，得到小灵通表中的数据总数。
	 * 
	 * @param con
	 *            数据库连接
	 * @return 移动表中的数据总数
	 */
	private synchronized int getXLTDataNum(Connection con) {
		CallableStatement cst = null;

		try {
			cst = con.prepareCall("{? = call " + oraNumQuery + "(?)}");
			cst.registerOutParameter(1, Types.INTEGER);
			cst.setInt(2,2);//2和3都是小灵通
			cst.execute();

			int num = cst.getInt(1);

			return num;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			try {
				cst.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void showMessage(String str) {
		Log.println("[小灵通数据读取线程-" + this.getId() + "] " + str);
	}
}
