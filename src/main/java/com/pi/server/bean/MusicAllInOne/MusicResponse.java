package com.pi.server.bean.MusicAllInOne;

import lombok.Data;

import java.util.List;

@Data
public class MusicResponse {
	
	private List<MusicData> data;
	private int code;
	private String error;

	public MusicResponse(List<MusicData> data, int code, String error) {
		this.data = data;
		this.code = code;
		this.error = error;
	}
}
