package com.guodu.sms.util;

public class Mobile {

	public static String FormatMobile(String Mobile) {
		
		String S = Mobile.trim();
		if ( S.substring(0,1).equals("+") ) S = S.substring(1);
		if ( (S.substring(0,1).equals("86")) && (S.length()==13) ) S = S.substring(2);
		
		return S;

	}
	
	public static boolean isCMPPMobile(String Mobile) {

		String S = Mobile.substring(0,3);

		if ( S.equals("139") || S.equals("138") || S.equals("137") || S.equals("136") || S.equals("135") || S.equals("134") ) {
			
			return true;
			
		} else {
			
			return false;
		
		}

	}

	public static boolean isSGIPMobile(String Mobile) {

		String S = Mobile.substring(0,3);

		if ( S.equals("133") || S.equals("132") || S.equals("131") || S.equals("130") ) {
		
			return true;
			
		} else {
			
			return false;
			
		}

	}

}
