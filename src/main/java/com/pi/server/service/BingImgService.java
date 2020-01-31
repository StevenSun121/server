package com.pi.server.service;

import com.pi.server.bean.BingImg;
import com.pi.server.mapper.BingImgMapper;
import com.pi.server.utils.DateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class BingImgService {
	
	@Autowired
	private BingImgMapper bingImgMapper;
	
	// bing 壁纸查询
	public BingImg selectByDate(){
		String today = new DateFormat().format();
		BingImg bingImg = bingImgMapper.selectByDate(today);
		if(bingImg == null){
			RestTemplate restTemplate = new RestTemplate();
			Map responseMap = restTemplate.getForObject("https://www.bing.com/hpimagearchive.aspx?format=js&idx=0&n=1&mkt=EN", Map.class);
			Map<String, String> dataMap = (Map<String, String>) ((List) responseMap.get("images")).get(0);
			bingImg = new BingImg();
			bingImg.setTitle(dataMap.get("copyright"));
			bingImg.setUrl("https://www.bing.com/" + dataMap.get("url"));
			bingImg.setDate(today);
			bingImgMapper.add(bingImg);
		}
		return bingImg;
	}
	
}
