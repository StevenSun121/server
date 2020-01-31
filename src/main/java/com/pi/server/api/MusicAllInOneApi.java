package com.pi.server.api;

import com.pi.server.bean.MusicAllInOne.MusicResponse;
import com.pi.server.service.MusicAllInOneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 多合一平台音乐搜索试听下载
 * 在maicong的基础上修改 https://github.com/maicong/music
 * 目前支持
 * 		网易		netease
 *
 */
@RestController
@RequestMapping("/music")
public class MusicAllInOneApi {
	
	@Autowired
	private MusicAllInOneService musicAllInOneService;
	
	//搜索音乐
	@RequestMapping(method=RequestMethod.GET)
	public MusicResponse musicSearch(
			@RequestParam("query") String query,
			@RequestParam("type") String type,
			@RequestParam("site") String site,
			@RequestParam("page") int page) {
		
		return musicAllInOneService.getMusic(query, type, site, page);
	}
	
}