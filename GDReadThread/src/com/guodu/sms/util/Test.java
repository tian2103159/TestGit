package com.guodu.sms.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import oracle.jdbc.driver.OracleDriver;

public class Test {
	public static void main(String[] args){
		Connection con = null;
		
		try {
//			int i=0/0;
			String oraUserName=null ;
			String oraPassword=null ;
			String oraUrl = null;
			OracleDriver od = new OracleDriver();
			Properties pro = new Properties();
			pro.setProperty("user",oraUserName);
			pro.setProperty("password",oraPassword);
			con = od.connect(oraUrl,pro);
			con.setAutoCommit(false);
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.writeToFile(e.getMessage());
		}
	}
}
