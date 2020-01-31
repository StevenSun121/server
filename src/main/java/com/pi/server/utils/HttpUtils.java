package com.pi.server.utils;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 类名称: HTTPUtils<br>
 * 类描述: Http请求<br>
 * 版权所有: Copyright (c) 2018/7/30 Zain Zhang, LTD.CO <br>
 * 创建时间: 2018/7/30 14:19 <br>
 *
 * @author zzy <br>
 * @version V1.0.0 <br>
 */
public class HttpUtils {
	private static final String ENCODING = "UTF-8";
	
	public static String get(final String url, Map<String, String> params, Map<String, String> headers) {
		String result = "";
		CloseableHttpClient httpClient = HttpClients.createDefault();
		String paramStr = getRequestParams(params);
		HttpGet httpGet = new HttpGet(url + paramStr);
//        //返回的数据包不进行压缩，解决content length为-1的问题
//        httpGet.setHeader("Accept-Encoding", "identity");
//		httpGet.setHeader("Referer", "http://m.y.qq.com");
		setHttpHeaders(httpGet, headers);
		System.out.println("executing get request " + httpGet.getURI());
		try {
			//执行get请求
			try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
				//获取响应实体
				HttpEntity entity = response.getEntity();
				System.out.println("--------------------------------------");
				// 打印响应状态
				System.out.println(response.getStatusLine());
				if (entity != null) {
					// 打印响应内容长度
					System.out.println("Response content length: " + entity.getContentLength());
					// 打印响应内容
					result = EntityUtils.toString(entity, ENCODING);
//                    System.out.println("Response content: " + result);
				}
				System.out.println("------------------------------------");
			}
			
		}catch (ParseException | IOException e) {
			e.printStackTrace();
		} finally {
			// 关闭连接,释放资源
			try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static String post(final String url, Map<String, String> params, Map<String, String> headers) {
		String result = "";
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);
		
		try {
			//获取请求的参数
			HttpEntity requestEntity = getRequestEntity(params);
			httpPost.setEntity(requestEntity);
			setHttpHeaders(httpPost, headers);
			System.out.println("executing post request " + httpPost.getURI());
			//post请求
			try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
				//获取响应实体
				HttpEntity entity = response.getEntity();
				System.out.println("--------------------------------------");
				// 打印响应状态
				System.out.println(response.getStatusLine());
				if (entity != null) {
					// 打印响应内容长度
					System.out.println("Response content length: " + entity.getContentLength());
					// 打印响应内容
					result = EntityUtils.toString(entity, ENCODING);
					System.out.println("Response content: " + result);
				}
				System.out.println("------------------------------------");
			}
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		} finally {
			// 关闭连接,释放资源
			try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	/**
	 * 组装HttpGet请求需要的参数
	 */
	private static String getRequestParams(Map<String, String> params) {
		Iterator<String> it = params.keySet().iterator();
		StringBuilder paramStr = new StringBuilder();
		paramStr.append("?");
		while (it.hasNext()){
			String key = it.next();
			paramStr.append(key).append("=").append(params.get(key)).append("&");
		}
		// 去掉最后一个&
		return paramStr.substring(0, paramStr.length() - 1);
	}
	
	/**
	 * 组装HttpPost请求需要的参数
	 */
	private static UrlEncodedFormEntity getRequestEntity(Map<String, String> params) {
		
		// 声明存放参数的List集合
		List<NameValuePair> paramsList = new ArrayList<>();
		
		// 遍历map，设置参数到list中
		for (Map.Entry<String, String> entry : params.entrySet()) {
			paramsList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		
		// 创建form表单对象
		UrlEncodedFormEntity formEntity = null;
		try {
			formEntity = new UrlEncodedFormEntity(paramsList, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return formEntity;
	}
	
	/**
	 * 组装Http请求需要的请求头
	 *
	 */
	private static void setHttpHeaders(HttpRequestBase httpMethod, Map<String, String> headers) {
		// 遍历map，设置请求头
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			httpMethod.setHeader(entry.getKey(), entry.getValue());
		}
	}
	
}