package com.pi.server.bean;

import lombok.Data;

import javax.websocket.Session;

@Data
public class WsChat {

	//用户昵称
	private String nickName;
	
	private Session session;

}
