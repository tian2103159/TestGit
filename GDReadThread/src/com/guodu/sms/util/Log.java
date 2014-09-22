package com.guodu.sms.util;

import java.util.Date;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;

public class Log {
	
	public static synchronized void println(String Text) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		System.out.println(sdf.format(new Date()) + " " +Text);
				
	}

	public static void writeToFile(String text){
		OutputStream os = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
		RandomAccessFile raf = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		try {
			os = new FileOutputStream("log.txt",true);
			osw = new OutputStreamWriter(os);
			bw = new BufferedWriter(osw);
			
			bw.write(sdf.format(new Date())+" "+text);
			bw.newLine();
			bw.flush();
		
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {os.close();} catch (IOException e) {e.printStackTrace();}
		}
	}
}
