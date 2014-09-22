package com.guodu.sms.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.ByteArrayOutputStream;
import java.io.*;

public class Util {

	private static String HexChar[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

	public static void sleep(long millisecond) {
		try {
			Thread.sleep(millisecond);
		} catch (InterruptedException interruptedexception) {

		}
	}

	public static String byteToHexString(byte b) {
		int n = b;
		if (n < 0)
			n = 256 + n;
		int d1 = n / 16;
		int d2 = n % 16;
		return String.valueOf(HexChar[d1]) + String.valueOf(HexChar[d2]);
	}

	public static String byteArrayToHexString(byte b[]) {
		String result = "";
		for (int i = 0; i < b.length; i++)
			result = String.valueOf(result)
					+ String.valueOf(byteToHexString(b[i]));

		return result;
	}

	public static byte HexStringTobyte(String S) {
		int i = (int)S.toUpperCase().charAt(0);
		int j = (int)S.toUpperCase().charAt(1);
		if (i > (int)'9') i = i - (int)'A' + 10; else i = i - (int)'0';
		if (j > (int)'9') j = j - (int)'A' + 10; else j = j - (int)'0';

		return (byte)(i*16+j);
	}

	public static byte[] HexStringTobyteArray(String S) {
		byte[] b = new byte[S.length()/2];

		for (int i=0; i < S.length(); i=i+2) {
			b[i/2] = HexStringTobyte(S.substring(i,i+2));
		}

		return b;
	}

	public static String FormatMobile(String Mobile) {

		String S = Mobile.trim();
		if ( S.substring(0,1).equals("+") ) S = S.substring(1);
		if ( (S.substring(0,1).equals("86")) && (S.length()==13) ) S = S.substring(2);

		return S;

	}

	public static String DateToString(Date dd) {

		return DateToString(dd,"yyyyMMddHHmmss");
	}

	public static String DateToString(Date dd, String fmt) {

		SimpleDateFormat tmp = new SimpleDateFormat(fmt);
		return tmp.format(dd);

	}

        /**
         * WarpLongSMS
         *
         * @param MsgCont String 传入的串
         * @param type int  表示传入的串的协议类型
         *                0--@@[SubNum/TotalNum]Content
         *                1--@$[SubNum/TotalNum]Content($为任意字符，代表一组长消息的标识)
         * @return byte[]
         */
        public static byte[] WarpLongSMS(String MsgCont, int type) {
            String RealContent = "";
            int TotalNum = 0;
            int SubNum = 0;
            SubNum = Integer.parseInt(MsgCont.trim().substring(3, 4));
            TotalNum = Integer.parseInt(MsgCont.trim().substring(5, 6));
            RealContent = MsgCont.trim().substring(7);
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            b.write(0x05);
            b.write(0x00);
            b.write(0x03);
            if (type == 0) {
                b.write(0x06);
            } else if (type == 1) {
                char c = MsgCont.trim().charAt(1);
                b.write((byte) c);
            }
            b.write((byte) TotalNum);
            b.write((byte) SubNum);
            try {
                byte[] SrcB = RealContent.getBytes("UTF-16");
                byte[] destB = new byte[SrcB.length - 2];
                System.arraycopy(SrcB, 2, destB, 0, SrcB.length - 2); //去掉标示utf-16表示汉字高低标示位的两个字节
                b.write(destB);
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }
            return b.toByteArray();
        }

        public static boolean IsNumber(String inputStr) {
            String str = "0123456789";
            for (int i = 0; i < inputStr.length(); i++) {
                int j = str.indexOf(inputStr.charAt(i));
                if (j == -1) {
                    return false;
                }
            }
            return true;
    }

        public static void main(String[] args){
	        System.out.println(new String(WarpLongSMS("test git")));
        }
}
