package com.pi.server.service;

import com.pi.server.api.WsChatApi;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.websocket.Session;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class WsChatService {
	
	//消息处理
	public void messageHandle(JSONObject jsonObject, WsChatApi wsChatApi,
							  ConcurrentHashMap<String, CopyOnWriteArraySet<WsChatApi>> chatMap) {
		
		String cmd = jsonObject.get("cmd").toString();
		if(cmd != null && !cmd.equals("")){
			
			//加入频道
			if(cmd.equals("join")){
				joinCommand(jsonObject, wsChatApi, chatMap);
			//聊天
			}else if(cmd.equals("chat")){
				String message = jsonObject.get("text").toString();
				chatMap.get(wsChatApi.channel).stream().forEach(api -> {
					sendMessage(chatJson(wsChatApi.nickName, message), api.session);
				});
			}
			/**
			 * 修改昵称 	rename
			 * 切换频道	move
			 * 私聊		invite
			 * 服务器	stats
			 */
		}else {
			sendMessage(warnJson("未知指令!"), wsChatApi.session);
		}
	}
	
	//客户端关闭连接或发生错误
	public void closeHandle(WsChatApi wsChatApi, ConcurrentHashMap<String, CopyOnWriteArraySet<WsChatApi>> chatMap){
		if(wsChatApi.channel != null && !wsChatApi.channel.equals(""))
			chatMap.get(wsChatApi.channel).remove(wsChatApi);
	}
	
	//加入频道处理
	public void joinCommand(JSONObject jsonObject, WsChatApi wsChatApi,
						   ConcurrentHashMap<String, CopyOnWriteArraySet<WsChatApi>> chatMap) {
		String channel = jsonObject.get("channel").toString();
		String nickName = jsonObject.get("nick").toString();
		
		if(channel.equals("")) {
			sendMessage(warnJson("频道不能为空!"), wsChatApi.session);
		}else if(nickName.equals("")) {
			sendMessage(warnJson("昵称不能为空!"), wsChatApi.session);
		}else if(wsChatApi.channel != null){
			sendMessage(warnJson("无法重复加入频道!"), wsChatApi.session);
		}else {
			CopyOnWriteArraySet<WsChatApi> chatChannel = chatMap.get(channel);
			//频道已创建
			if(chatChannel != null){
				
				//当前频道内无重名
				Stream stream = chatChannel.stream().filter(api -> api.nickName.equals(nickName));
				if(stream.count() == 0){
					
					wsChatApi.channel = channel;
					wsChatApi.nickName = nickName;
					
					if(chatChannel.add(wsChatApi))
						
						//发送广播 通知频道内其他用户
						chatChannel.stream().filter(api -> !api.nickName.equals(nickName)).forEach(api -> {
							sendMessage(onlineAddJson(nickName), api.session);
						});
					
					//通知用户加入频道成功
					sendMessage(onlineJson(chatChannel.stream().map(api -> api.nickName)
							.collect(Collectors.toList())), wsChatApi.session);
					
					//重名心跳
				}else {
					sendMessage(warnJson("昵称:" + nickName + ",已被使用!"), wsChatApi.session);
				}
				
				//创建频道
			}else {
				chatChannel = new CopyOnWriteArraySet<>();
				
				wsChatApi.channel = channel;
				wsChatApi.nickName = nickName;
				
				chatChannel.add(wsChatApi);
				
				chatMap.put(channel, chatChannel);
				
				sendMessage(onlineJson(new String[]{nickName}), wsChatApi.session);
			}
		}
	}
	
	//加入频道成功消息拼装
	public String onlineJson(Object nicks) {
		
		JSONObject jsonObject = new JSONObject();
		
		jsonObject.put("cmd", "online");
		jsonObject.put("nicks", nicks);
		jsonObject.put("time", System.currentTimeMillis());
		
		return jsonObject.toString();
	}
	
	//其他用户加入频道通知
	public String onlineAddJson(String nick) {
		
		JSONObject jsonObject = new JSONObject();
		
		jsonObject.put("cmd", "onlineAdd");
		jsonObject.put("nick", nick);
		jsonObject.put("time", System.currentTimeMillis());
		
		return jsonObject.toString();
	}
	
	//用户消息广播
	public String chatJson(String nick, String message) {
		
		JSONObject jsonObject = new JSONObject();
		
		jsonObject.put("cmd", "chat");
		jsonObject.put("nick", nick);
		jsonObject.put("text", message);
		jsonObject.put("time", System.currentTimeMillis());
		
		return jsonObject.toString();
	}
	
	//错误消息拼装
	public String warnJson(String warn) {
		
		JSONObject jsonObject = new JSONObject();
		
		jsonObject.put("cmd", "warn");
		jsonObject.put("text", warn);
		jsonObject.put("time", System.currentTimeMillis());
		
		return jsonObject.toString();
	}
	
	//发送消息
	public void sendMessage(String message, Session session) {
		
		try {
			session.getBasicRemote().sendText(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
