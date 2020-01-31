package com.pi.server.utils;

public class StringUtils {
	
	//判断字符串是否为空 字符串都为空格、制表符、tab 的情况
	public static boolean isBlank(String str) {
		int strLen;
		if (str != null && (strLen = str.length()) != 0) {
			for (int i = 0; i < strLen; ++i) {
				// 判断字符是否为空格、制表符、tab
				if (!Character.isWhitespace(str.charAt(i))) {
					return false;
				}
			}
			return true;
		} else {
			return true;
		}
	}

}
