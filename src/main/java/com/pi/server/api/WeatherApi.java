package com.pi.server.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 1. 和风天气
 * 13311239236@163.com
 * https://dev.heweather.com/docs/api/weather
 * key: 06bf349f8fea4d35a87b9304d2d903be
 *
 * 2. json在线
 * https://www.sojson.com/api/weather.html
 * http://t.weather.sojson.com/api/weather/city/101060101
 * https://gitee.com/wangjins/weather_api
 *
 * 3. 天气
 * https://www.tianqiapi.com/?action=day
 *
 * 4. 彩云
 * https://caiyunapp.com/api/pricing.html
 *
 * 5.中国天气
 * http://m.weather.com.cn/mweather/101010100.shtml
 * http://m.weather.com.cn/d/town/index?lat=39.915055&lon=116.403982
 */
@RestController
@RequestMapping("/weather")
public class WeatherApi {
	
	//获取图片地址
	@RequestMapping("/now")
	public void now(){
		//https://free-api.heweather.net/s6/weather/now?location=%E9%95%BF%E6%98%A5&key=06bf349f8fea4d35a87b9304d2d903be
	}
	
}
