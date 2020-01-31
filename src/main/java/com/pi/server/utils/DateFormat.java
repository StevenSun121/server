package com.pi.server.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

//时间格式化工具类
public class DateFormat {
	
	//默认时间格式
	/**
	 * yyyy：年
	 * MM：月
	 * dd：日
	 * hh：1~12小时制(1-12)
	 * HH：24小时制(0-23)
	 * mm：分
	 * ss：秒
	 * S：毫秒
	 * E：星期几
	 * D：一年中的第几天
	 * F：一月中的第几个星期(会把这个月总共过的天数除以7)
	 * w：一年中的第几个星期
	 * W：一月中的第几星期(会根据实际情况来算)
	 * a：上下午标识
	 * k：和HH差不多，表示一天24小时制(1-24)。
	 * K：和hh差不多，表示一天12小时制(0-11)。
	 * z：表示时区
	 */
	public static final String DEFAULT_FORMAT = "yyyy-MM-dd";
	
	private SimpleDateFormat simpleDateFormat;
	
	public DateFormat() {
		this.simpleDateFormat = new SimpleDateFormat(this.DEFAULT_FORMAT);
	}
	
	public DateFormat(String format) {
		this.simpleDateFormat = new SimpleDateFormat(format);
	}
	
	//当前系统时间格式化
	public String format() {
		Date date = new Date();
		return simpleDateFormat.format(date);
	}
	
	//时间格式化
	public String format(Date date) {
		return simpleDateFormat.format(date);
	}

	
	//字符串转时间
	public Date format(String date) {
		try {
			return simpleDateFormat.parse(date);
		} catch (ParseException e) {
			new Exception("时间转换异常！");
		}
		return null;
	}
}
