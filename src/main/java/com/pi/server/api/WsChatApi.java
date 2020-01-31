package com.pi.server.api;

import com.pi.server.service.WsChatService;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 聊天室
 */
@ServerEndpoint("/wsChat")
@Component
public class WsChatApi {
	
	//保存房间及人员信息集合
	private static final ConcurrentHashMap<String, CopyOnWriteArraySet<WsChatApi>> chatMap = new ConcurrentHashMap<>();
	
	//与某个客户端的连接会话，需要通过它来给客户端发送数据
	public Session session;
	
	//用户昵称
	public String nickName;
	
	//房间号
	public String channel;
	
	WsChatService wsChatService = new WsChatService();
	
	//打开ws链接
	@OnOpen
	public void onOpen(Session session){
		this.session = session;
	}
	
	@OnClose
	public void onClose(){
		wsChatService.closeHandle(this, chatMap);
	}

	@OnMessage
	public void onMessage(String message) {
		JSONObject jsonObject = JSONObject.fromObject(message);
		wsChatService.messageHandle(jsonObject, this,  chatMap);
	}

	@OnError
	public void onError(Throwable error){
		wsChatService.closeHandle(this, chatMap);
	}

}