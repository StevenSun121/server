package com.pi.server.mapper;

import com.pi.server.bean.BingImg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BingImgMapper {
	
	BingImg selectByDate(@Param("date") String date);
	
	int add(BingImg bingImg);
}
