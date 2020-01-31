package com.pi.server.service;

import com.pi.server.bean.MusicAllInOne.MusicData;
import com.pi.server.bean.MusicAllInOne.MusicResponse;
import com.pi.server.utils.HttpUtils;
import com.pi.server.utils.StringUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 歌曲信息获取服务
 *
 * 1.id
 * 2.详情
 * 3.链接
 * 4.歌词
 *
 */

@Service
public class MusicAllInOneService {
	
	//错误代码
	private static final Integer GET_MUSIC_SUCCESS = 200;
	private static final Integer GET_MUSIC_BY_NAME_ERROR = 201;
	private static final Integer GET_MUSIC_BY_ID_ERROR = 202;
	private static final Integer GET_MUSIC_URL_ERROR = 203;
	
	//请求头
	private Map<String, String> headers = new HashMap<>();
	//请求参数
	private Map<String, String> params = new HashMap<>();
	//返回信息
	private MusicResponse musicResponse = new MusicResponse(null, 200, "");
	
	//搜索
	public MusicResponse getMusic(String query, String type, String site, int page) {
		//TODO 数据校验 -> 空值校验/网站校验/查询内容校验
		setRequestHeaders(site);
		switch (type){
			case "name":
				getMusicByName(query, site, page);
				break;
			case "id":
				break;
			case "url":
				break;
		}
		return musicResponse;
	}

	//通过名字搜索
	private void getMusicByName(String query, String site, int page) {
		
		List<String> musicList = new ArrayList<>();
		
		JSONObject musicJsonData = getMusicIDData(query, site, page);
		
		//TODO 网站返回错误/查询无结果等情况返回错误信息
		
		switch (site) {
			case "netease":
				if(musicJsonData.getInt("code") != 200){
					buildMusicResponse(null, GET_MUSIC_BY_NAME_ERROR, "网易云音乐通过歌名请求歌曲列表失败,返回错误代码:" + musicJsonData.getInt("code"));
					return;
				}
				for(Object temp : musicJsonData.getJSONObject("result").getJSONArray("songs")) {
					musicList.add(((JSONObject)temp).get("id").toString());
				}
				break;
			case "qq":
				if(musicJsonData.getInt("code") != 0) {
					buildMusicResponse(null, GET_MUSIC_BY_NAME_ERROR, "QQ音乐通过歌名请求歌曲列表失败,返回错误代码:" + musicJsonData.getInt("code"));
					return;
				}
				for(Object temp : musicJsonData.getJSONObject("data").getJSONObject("song").getJSONArray("list")) {
					musicList.add(((JSONObject)temp).get("songmid").toString());
				}
				break;
		}
		
		getMusicByID(musicList, site);
	}
	
	//通过 URL 搜索
	
	private void getMusicByID(List<String> musicIDList, String site) {
	//通过 ID 搜索
		
		List<MusicData> musicList = new ArrayList<>();
		
		JSONObject musicJsonData = getMusicDetailData(musicIDList, site);
		
		switch (site) {
			case "netease":
				
				if(musicJsonData.getInt("code") != 200) {
					buildMusicResponse(null, GET_MUSIC_BY_ID_ERROR, "网易云音乐通过ID请求歌曲信息失败,返回错误代码:" + musicJsonData.getInt("code"));
					return;
				}
				
				JSONObject musicURLData = getMusicURLData_netease(musicIDList);
				
				if(musicURLData.getInt("code") != 200) {
					buildMusicResponse(null, GET_MUSIC_URL_ERROR, "网易云音乐获取歌曲链接失败,返回错误代码:" + musicURLData.getInt("code"));
					return;
				}
				
				Map<String, String> musicURLMap = new HashMap<>();
				
				for(Object temp : musicURLData.getJSONArray("data")) {
					JSONObject musicURL = (JSONObject)temp;
					musicURLMap.put(musicURL.getString("id"), musicURL.getString("url"));
				}
				
				for(Object temp : musicJsonData.getJSONArray("songs")) {
					JSONObject musicDetail = (JSONObject)temp;
					
					MusicData musicData = new MusicData();
					musicData.setSite(site);
					musicData.setId(musicDetail.getString("id"));
					musicData.setLink("http://music.163.com/#/song?id=" + musicDetail.getString("id"));
					musicData.setName(musicNameFormat(musicDetail.getString("name")));
					musicData.setAuthor(authorListToString(musicDetail.getJSONArray("artists"), "name"));
					musicData.setLrc(getMusicLrcData(musicDetail.getString("id"), site));
					if(musicURLMap.get(musicDetail.getString("id")).equals("null")) {
						musicData.setUrl("");
					}else {
						musicData.setUrl(musicURLMap.get(musicDetail.getString("id"))); // http://music.163.com/song/media/outer/url?id=1372060183.mp3
					}
					musicData.setMtype("mp3");
					musicData.setPic(musicDetail.getJSONObject("album").getString("picUrl") + "?param=300x300");
					musicData.setPtype("png");
					musicData.setTime(musicDetail.getInt("duration") / 1000);
					
					musicList.add(musicData);
				}
				buildMusicResponse(musicList, GET_MUSIC_SUCCESS, "");
				break;
			case "qq":
				
				JSONArray musicURLArray = getMusicURLData_qq(musicIDList).getJSONObject("req_0").getJSONObject("data").getJSONArray("midurlinfo");
				
				int index = 0;
				
				for(Object temp : musicJsonData.getJSONArray("data")) {
					JSONObject musicDetail = (JSONObject)temp;
					String musicID = musicDetail.getString("mid");
					
					String albumID = musicDetail.getJSONObject("album").getString("mid");
					
					MusicData musicData = new MusicData();
					musicData.setSite(site);
					musicData.setId(musicID);
					musicData.setLink("http://y.qq.com/n/yqq/song/" + musicID + ".html");
					musicData.setName(musicNameFormat(musicDetail.getString("title")));
					musicData.setAuthor(authorListToString(musicDetail.getJSONArray("singer"), "title"));
					musicData.setLrc(getMusicLrcData(musicID, site));
					musicData.setUrl("http://isure.stream.qqmusic.qq.com/" + musicURLArray.getJSONObject(index).getString("purl")); // http://isure.stream.qqmusic.qq.com/C400000fFXc24QGWSm.m4a?guid=2560684405&vkey=FA99ED8F2BB85012A43DBDB4229B9CD707C5E7979F7A584567544F032B169B07E8E4B3083BB40BBEE718D8D2DA9D648517BF9278495A4E21&uin=0&fromtag=66
					musicData.setMtype("m4a");
					if(StringUtils.isBlank(albumID)) {
						musicData.setPic("");
					}else {
						musicData.setPic("http://y.gtimg.cn/music/photo_new/T002R300x300M000" + albumID + ".jpg");
					}
					musicData.setPtype("jpg");
					musicData.setTime(musicDetail.getInt("interval"));
					
					musicList.add(musicData);
					//TODO  歌曲,图片 格式
				}
				buildMusicResponse(musicList, GET_MUSIC_SUCCESS, "");
				break;
		}
	}
	
	//通过名字搜索查询出一页 ID
	private JSONObject getMusicIDData(String query, String site, int page) {
		
//		'netease'            => [
//		'method'         => 'POST',
//				'url'            => 'http://music.163.com/api/linux/forward',
//				'referer'        => 'http://music.163.com/',
//				'proxy'          => false,
//				'body'           => encode_netease_data([
//				'method'     => 'POST',
//				'url'        => 'http://music.163.com/api/cloudsearch/pc',
//				'params'     => [
//		's'      => $query,
//				'type'   => 1,
//				'offset' => $page * 10 - 10,
//				'limit'  => 10
//                ]
//            ])
//        ],
//		'1ting'              => [
//		'method'         => 'GET',
//				'url'            => 'http://so.1ting.com/song/json',
//				'referer'        => 'http://h5.1ting.com/',
//				'proxy'          => false,
//				'body'           => [
//		'q'          => $query,
//				'page'       => $page,
//				'size'       => 10
//            ]
//        ],
//		'baidu'              => [
//		'method'         => 'GET',
//				'url'            => 'http://musicapi.qianqian.com/v1/restserver/ting',
//				'referer'        => 'http://music.baidu.com/',
//				'proxy'          => false,
//				'body'           => [
//		'method'    => 'baidu.ting.search.common',
//				'query'     => $query,
//				'format'    => 'json',
//				'page_no'   => $page,
//				'page_size' => 10
//            ]
//        ],
//		'kugou'              => [
//		'method'         => 'GET',
//				'url'            => MC_INTERNAL ?
//				'http://songsearch.kugou.com/song_search_v2' :
//				'http://mobilecdn.kugou.com/api/v3/search/song',
//				'referer'        => MC_INTERNAL ? 'http://www.kugou.com' : 'http://m.kugou.com',
//				'proxy'          => false,
//				'body'           => [
//		'keyword'    => $query,
//				'platform'   => 'WebFilter',
//				'format'     => 'json',
//				'page'       => $page,
//				'pagesize'   => 10
//            ]
//        ],
//		'kuwo'               => [
//		'method'         => 'GET',
//				'url'            => 'http://search.kuwo.cn/r.s',
//				'referer'        => 'http://player.kuwo.cn/webmusic/play',
//				'proxy'          => false,
//				'body'           => [
//		'all'        => $query,
//				'ft'         => 'music',
//				'itemset'    => 'web_2013',
//				'pn'         => $page - 1,
//				'rn'         => 10,
//				'rformat'    => 'json',
//				'encoding'   => 'utf8'
//            ]
//        ],
//		'qq'                 => [
//		'method'         => 'GET',
//				'url'            => 'http://c.y.qq.com/soso/fcgi-bin/search_for_qq_cp',
//				'referer'        => 'http://m.y.qq.com',
//				'proxy'          => false,
//				'body'           => [
//		'w'          => $query,
//				'p'          => $page,
//				'n'          => 10,
//				'format'     => 'json'
//            ],
//		'user-agent'     => 'Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1'
//        ],
//		'xiami'              => [
//		'method'         => 'GET',
//				'url'            => 'http://api.xiami.com/web',
//				'referer'        => 'http://m.xiami.com',
//				'proxy'          => false,
//				'body'           => [
//		'key'        => $query,
//				'v'          => '2.0',
//				'app_key'    => '1',
//				'r'          => 'search/songs',
//				'page'       => $page,
//				'limit'      => 10
//            ],
//		'user-agent'     => 'Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1'
//        ],
//		'5singyc'            => [
//		'method'         => 'GET',
//				'url'            => 'http://goapi.5sing.kugou.com/search/search',
//				'referer'        => 'http://5sing.kugou.com/',
//				'proxy'          => false,
//				'body'           => [
//		'k'          => $query,
//				't'          => '0',
//				'filterType' => '1',
//				'ps'         => 10,
//				'pn'         => $page
//            ]
//        ],
//		'5singfc'            => [
//		'method'         => 'GET',
//				'url'            => 'http://goapi.5sing.kugou.com/search/search',
//				'referer'        => 'http://5sing.kugou.com/',
//				'proxy'          => false,
//				'body'           => [
//		'k'          => $query,
//				't'          => '0',
//				'filterType' => '2',
//				'ps'         => 10,
//				'pn'         => 1
//            ]
//        ],
//		'migu'               => [
//		'method'         => 'GET',
//				'url'            => 'http://m.10086.cn/migu/remoting/scr_search_tag',
//				'referer'        => 'http://m.10086.cn',
//				'proxy'          => false,
//				'body'           => [
//		'keyword'    => $query,
//				'type'       => '2',
//				'pgc'        => $page,
//				'rows'       => 10
//            ],
//		'user-agent'    => 'Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1'
//        ],
//		'lizhi'              => [
//		'method'         => 'GET',
//				'url'            => 'http://m.lizhi.fm/api/search_audio/' . urlencode($query) . '/' . $page,
//				'referer'        => 'http://m.lizhi.fm',
//				'proxy'          => false,
//				'body'           => false,
//				'user-agent'     => 'Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1'
//        ],
//		'qingting'           => [
//		'method'         => 'GET',
//				'url'            => 'http://i.qingting.fm/wapi/search',
//				'referer'        => 'http://www.qingting.fm',
//				'proxy'          => false,
//				'body'           => [
//		'k'          => $query,
//				'page'       => $page,
//				'pagesize'   => 10,
//				'include'    => 'program_ondemand',
//				'groups'     => 'program_ondemand'
//            ]
//        ],
//		'ximalaya'           => [
//		'method'         => 'GET',
//				'url'            => 'http://search.ximalaya.com/front/v1',
//				'referer'        => 'http://www.ximalaya.com',
//				'proxy'          => false,
//				'body'           => [
//		'kw'         => $query,
//				'core'       => 'all',
//				'page'       => $page,
//				'rows'       => 10,
//				'is_paid'    => false
//            ]
//        ],
//		'kg'                 => [
//		'method'         => 'GET',
//				'url'            => 'http://kg.qq.com/cgi/kg_ugc_get_homepage',
//				'referer'        => 'http://kg.qq.com',
//				'proxy'          => false,
//				'body'           => [
//		'format'     => 'json',
//				'type'       => 'get_ugc',
//				'inCharset'  => 'utf8',
//				'outCharset' => 'utf-8',
//				'share_uid'  => $query,
//				'start'      => $page,
//				'num'        => 10
//            ]
//        ]
//    ];
		
		String httpResult = "";
		
		params.clear();
		
		switch (site){
			case "netease":
				
				params.put("s", query);
				params.put("type", "1");
				params.put("offset", String.valueOf((page - 1) * 10));
				params.put("limit", "10");
				
				httpResult = HttpUtils.post("http://music.163.com/api/cloudsearch/pc", params, headers);
				
				break;
			case "qq":
				
				params.put("w", query);
				params.put("p", String.valueOf(page));
				params.put("n", "10");
				params.put("format", "json");
				
				httpResult = HttpUtils.get("http://c.y.qq.com/soso/fcgi-bin/search_for_qq_cp", params, headers);
				
				break;
		}
		return JSONObject.fromObject(httpResult);
	}
	
	//通过 ID List 查询详情
	private JSONObject getMusicDetailData(List<String> musicIDList, String site) {
		
//		'netease'           => [
//		'method'        => 'POST',
//				'url'           => 'http://music.163.com/api/linux/forward',
//				'referer'       => 'http://music.163.com/',
//				'proxy'         => false,
//				'body'          => encode_netease_data([
//				'method'    => 'GET',
//				'url'       => 'http://music.163.com/api/song/detail',
//				'params'    => [
//		'id'      => $songid,
//				'ids'     => '[' . $songid . ']'
//                ]
//            ])
//        ],
//		'1ting'             => [
//		'method'        => 'GET',
//				'url'           => 'http://h5.1ting.com/touch/api/song',
//				'referer'       => 'http://h5.1ting.com/#/song/' . $songid,
//				'proxy'         => false,
//				'body'          => [
//		'ids'       => $songid
//            ]
//        ],
//		'baidu'             => [
//		'method'        => 'GET',
//				'url'           => 'http://music.baidu.com/data/music/links',
//				'referer'       => 'music.baidu.com/song/' . $songid,
//				'proxy'         => false,
//				'body'          => [
//		'songIds'   => $songid
//            ]
//        ],
//		'kugou'             => [
//		'method'        => 'GET',
//				'url'           => 'http://m.kugou.com/app/i/getSongInfo.php',
//				'referer'       => 'http://m.kugou.com/play/info/' . $songid,
//				'proxy'         => false,
//				'body'          => [
//		'cmd'       => 'playInfo',
//				'hash'      => $songid
//            ],
//		'user-agent'    => 'Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1'
//        ],
//		'kuwo'              => [
//		'method'        => 'GET',
//				'url'           => 'http://player.kuwo.cn/webmusic/st/getNewMuiseByRid',
//				'referer'       => 'http://player.kuwo.cn/webmusic/play',
//				'proxy'         => false,
//				'body'          => [
//		'rid'       => 'MUSIC_' . $songid
//            ]
//        ],
//		'qq'                => [
//		'method'        => 'GET',
//				'url'           => 'http://c.y.qq.com/v8/fcg-bin/fcg_play_single_song.fcg',
//				'referer'       => 'http://m.y.qq.com',
//				'proxy'         => false,
//				'body'          => [
//		'songmid'   => $songid,
//				'format'    => 'json'
//            ],
//		'user-agent'    => 'Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1'
//        ],
//		'xiami'             => [
//		'method'        => 'GET',
//				'url'           => 'http://www.xiami.com/song/playlist/id/' . $songid . '/type/0/cat/json',
//				'referer'       => 'http://www.xiami.com',
//				'proxy'         => false
//        ],
//		'5singyc'           => [
//		'method'        => 'GET',
//				'url'           => 'http://mobileapi.5sing.kugou.com/song/newget',
//				'referer'       => 'http://5sing.kugou.com/yc/' . $songid . '.html',
//				'proxy'         => false,
//				'body'          => [
//		'songid'    => $songid,
//				'songtype'  => 'yc'
//            ]
//        ],
//		'5singfc'           => [
//		'method'        => 'GET',
//				'url'           => 'http://mobileapi.5sing.kugou.com/song/newget',
//				'referer'       => 'http://5sing.kugou.com/fc/' . $songid . '.html',
//				'proxy'         => false,
//				'body'          => [
//		'songid'    => $songid,
//				'songtype'  => 'fc'
//            ]
//        ],
//		'migu'              => [
//		'method'        => 'GET',
//				'url'           => MC_INTERNAL ? 'http://music.migu.cn/v2/async/audioplayer/playurl/' . $songid : 'http://m.10086.cn/migu/remoting/cms_detail_tag',
//				'referer'       => 'http://m.10086.cn',
//				'proxy'         => false,
//				'body'          => MC_INTERNAL ? false : [
//		'cid'    => $songid
//            ],
//		'user-agent'    => 'Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1'
//        ],
//		'lizhi'             => [
//		'method'        => 'GET',
//				'url'           => 'http://m.lizhi.fm/api/audios_with_radio',
//				'referer'       => 'http://m.lizhi.fm',
//				'proxy'         => false,
//				'body'          => false,
//				'user-agent'    => 'Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1'
//        ],
//		'qingting'          => [
//		'method'        => 'GET',
//				'url'           => 'http://i.qingting.fm/wapi/channels/' . split_songid($songid, 0) . '/programs/' . split_songid($songid, 1),
//				'referer'       => 'http://www.qingting.fm',
//				'proxy'         => false,
//				'body'          => false
//        ],
//		'ximalaya'          => [
//		'method'        => 'GET',
//				'url'           => 'http://mobile.ximalaya.com/v1/track/ca/playpage/' . $songid,
//				'referer'       => 'http://www.ximalaya.com',
//				'proxy'         => false,
//				'body'          => false
//        ],
//		'kg'                => [
//		'method'        => 'GET',
//				'url'           => 'http://kg.qq.com/cgi/kg_ugc_getdetail',
//				'referer'       => 'http://kg.qq.com',
//				'proxy'         => false,
//				'body'          => [
//		'v'          => 4,
//				'format'     => 'json',
//				'inCharset'  => 'utf8',
//				'outCharset' => 'utf-8',
//				'shareid'    => $songid
//            ]
//        ]
		
		String httpResult = "";
		
		params.clear();
		
		switch (site) {
			case "netease":
				
				params.put("ids", songIDListToString(musicIDList));
				
				httpResult = HttpUtils.post("http://music.163.com/api/song/detail", params, headers);
				
				break;
			case "qq":

				musicIDList.add(0,"");
				
				params.put("songmid", songIDListToString(musicIDList));
				params.put("format", "json");
				
				musicIDList.remove(0);
				
				httpResult = HttpUtils.get("http://c.y.qq.com/v8/fcg-bin/fcg_play_single_song.fcg", params, headers);
				break;
			//TODO 不支持多个ID 同时查询的,在case内部进行多次查询,再返回统一处理
		}
		return JSONObject.fromObject(httpResult);
	}
	
	//根据 ID 查询歌词
	private String getMusicLrcData(String musicID, String site) {
		

//		'1ting'             => [
//		'method'        => 'GET',
//				'url'           => 'http://www.1ting.com/api/geci/lrc/' . $songid,
//				'referer'       => 'http://www.1ting.com/geci' . $songid . '.html',
//				'proxy'         => false,
//				'body'          => false
//        ],
//		'baidu'             => [
//		'method'        => 'GET',
//				'url'           => 'http://musicapi.qianqian.com/v1/restserver/ting',
//				'referer'       => 'http://music.baidu.com/song/' . $songid,
//				'proxy'         => false,
//				'body'          => [
//		'method' => 'baidu.ting.song.lry',
//				'songid' => $songid,
//				'format' => 'json'
//            ]
//        ],
//		'kugou'             => [
//		'method'        => 'GET',
//				'url'           => 'http://m.kugou.com/app/i/krc.php',
//				'referer'       => 'http://m.kugou.com/play/info/' . $songid,
//				'proxy'         => false,
//				'body'          => [
//		'cmd'        => 100,
//				'timelength' => 999999,
//				'hash'       => $songid
//            ],
//		'user-agent'    => 'Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X] AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1'
//        ],
//		'kuwo'              => [
//		'method'        => 'GET',
//				'url'           => 'http://m.kuwo.cn/newh5/singles/songinfoandlrc',
//				'referer'       => 'http://m.kuwo.cn/yinyue/' . $songid,
//				'proxy'         => false,
//				'body'          => [
//		'musicId' => $songid
//            ],
//		'user-agent'    => 'Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1'
//        ],
//		'xiami'             => [
//		'method'        => 'GET',
//				'url'           => $songid,
//				'referer'       => 'http://www.xiami.com',
//				'proxy'         => false
//        ],
//		'kg'                => [
//		'method'        => 'GET',
//				'url'           => 'http://kg.qq.com/cgi/fcg_lyric',
//				'referer'       => 'http://kg.qq.com',
//				'proxy'         => false,
//				'body'          => [
//		'format'     => 'json',
//				'inCharset'  => 'utf8',
//				'outCharset' => 'utf-8',
//				'ksongmid'   => $songid
//            ]
//        ]
		
		String httpResult;
		JSONObject musicLrcData;
		
		String returnStr = "";
		
		params.clear();
		
		switch (site) {
			case "netease":
				
				params.put("id", musicID);
				params.put("lv", "1");
				
				httpResult = HttpUtils.post("http://music.163.com/api/song/lyric", params, headers);
				
				musicLrcData = JSONObject.fromObject(httpResult);
				
				if(! musicLrcData.getJSONObject("lrc").isEmpty()) {
					returnStr = musicLrcData.getJSONObject("lrc").getString("lyric");
				}
				
				break;
			case "qq":
				
				params.clear();
				params.put("songmid", musicID);
				params.put("format", "json");
				params.put("nobase64", "1");
				params.put("songtype", "0");
				params.put("callback", "c");
				
				setRequestHeaders(site);
				
				httpResult = HttpUtils.get("http://c.y.qq.com/lyric/fcgi-bin/fcg_query_lyric.fcg", params, headers);
				
				httpResult = httpResult.substring(2,httpResult.length() - 1);
				
				musicLrcData = JSONObject.fromObject(httpResult);
				
				if(musicLrcData.has("lyric")) {
					String lyric = musicLrcData.getString("lyric");
					
					Pattern p = Pattern.compile("&#(\\d{2});");
					
					Matcher m = p.matcher(lyric);
					StringBuffer sb = new StringBuffer();
					while(m.find()){
						String text = m.group();
						char symbol = (char) Integer.parseInt(text.substring(2, 4));
						m.appendReplacement(sb, String.valueOf(symbol));
					}
					m.appendTail(sb);
					returnStr = sb.toString();
				}
				break;
		}
		return returnStr;
	}
	
	//网易云通过 ID List 查询 歌曲URL
	private JSONObject getMusicURLData_netease(List<String> musicIDList) {
		
		params.clear();
		params.put("ids", songIDListToString(musicIDList));
		params.put("br", "320000");
		
		String httpResult = HttpUtils.post("http://music.163.com/api/song/enhance/player/url", params, headers);

		return JSONObject.fromObject(httpResult);
	}
	
	//QQ音乐通过 ID 查询 歌曲URL
	private JSONObject getMusicURLData_qq(List<String> musicIDList) {

		params.clear();
		params.put("-", "getplaysongvkey4050488109222963");
		params.put("g_tk", "5381");
		params.put("loginUin", "0");
		params.put("hostUin", "0");
		params.put("format", "json");
		params.put("inCharset", "utf8");
		params.put("outCharset", "utf-8");
		params.put("notice", "0");
		params.put("platform", "yqq.json");
		params.put("needNewCode", "0");

		String reqStr = "{\"req_0\":{\"module\":\"vkey.GetVkeyServer\",\"method\":\"CgiGetVkey\",\"param\":{\"guid\":\"834463236\",\"songmid\":" + songIDListToStringWithSymbol(musicIDList) + ",\"songtype\":[0],\"uin\":\"0\",\"loginflag\":1,\"platform\":\"20\"}},\"comm\":{\"uin\":0,\"format\":\"json\",\"ct\":24,\"cv\":0}}";
//		params.put("data", reqStr);

//		JSONObject reqData = JSONObject.fromObject();
		try {
			params.put("data", URLEncoder.encode(reqStr, "UTF-8"));
//			params.put("data", URLEncoder.encode(reqData.toString(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
		}
		
		headers.put("Referer", "https://y.qq.com/portal/player.html");

		String httpResult = HttpUtils.get("https://u.y.qq.com/cgi-bin/musicu.fcg", params, headers);

		return JSONObject.fromObject(httpResult);
	}
	
	
	//构建 MusicResponse
	private void buildMusicResponse(List<MusicData> data, int code, String error) {
		musicResponse.setData(data);
		musicResponse.setCode(code);
		musicResponse.setError(error);
	}
	
	//根据网站构建请求头
	private void setRequestHeaders(String site) {
		switch (site) {
			case "netease":
				break;
			case "qq":
				headers.put("Referer", "http://m.y.qq.com");
				break;
		}
	}
	
	//songIDs List -> String
	private String songIDListToString(List<String> list) {
		return "[" + String.join(",", list) + "]";
	}
	
	//songIDs List -> String 带引号
	private String songIDListToStringWithSymbol(List<String> list) {
		return "[\"" + String.join("\",\"", list) + "\"]";
	}
	
	//替换歌曲名称中的特殊符号 不允许出现 / \ *
	private String musicNameFormat(String name) {
		name = name.replaceAll("/", "·");
		return name;
	}
	
	//author List -> String
	private String authorListToString(JSONArray list, String key) {
		List<String> artistsList = new ArrayList<>();
		for(Object temp : list) {
			JSONObject artists = (JSONObject)temp;
			artistsList.add(artists.getString(key));
		}
		return String.join("·", artistsList);
	}
	
}
