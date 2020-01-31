package com.pi.server.api;

import com.pi.server.bean.BingImg;
import com.pi.server.service.BingImgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 获取bing图片
 */
@RestController
@RequestMapping("/bingImg")
public class BingImgApi {
	
	@Autowired
	private BingImgService bingImgService;
	
	//获取图片地址
	@RequestMapping("/url")
	public String bingImgUrl() {
		return bingImgService.selectByDate().getUrl();
	}
	
	//获取全部信息
	@RequestMapping("/all")
	public BingImg bingImg() {
		return bingImgService.selectByDate();
	}
	
}
