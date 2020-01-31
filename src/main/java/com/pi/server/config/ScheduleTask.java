package com.pi.server.config;

import com.pi.server.service.CodeService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

//@Component
//@Configuration      // 标记配置类
//@EnableScheduling   // 开启定时任务
public class ScheduleTask {
	
	@Autowired
	private CodeService codeService;
	
	// 添加定时任务
//	@Scheduled(cron = "0 0/15 * * * ?")
//	@Scheduled(cron = "0/5 * * * * ?")
	private void configureTasks() {
		codeService.selectByKey("ip");
		//获取ip
		String result = "";
		BufferedReader in = null;
		try {
			URL url = new URL("http://ip-api.com/json/?fields=status,message,continent,continentCode,country,countryCode,region,regionName,city,district,isp,proxy,query&lang=zh-CN");
			// 打开和URL之间的连接
			URLConnection connection = url.openConnection();
			// 设置通用的请求属性
//			connection.setRequestProperty("accept", "*/*");
//			connection.setRequestProperty("connection", "Keep-Alive");
//			connection.setRequestProperty("user-agent",
//					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 建立实际的连接
			connection.connect();
			// 定义 BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			System.out.println("发送GET请求出现异常！" + e);
			e.printStackTrace();
		}
		// 使用finally块来关闭输入流
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		//获取数据库内ip
		//判断纯地址是否相同
	}
	
}