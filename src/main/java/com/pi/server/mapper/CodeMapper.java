package com.pi.server.mapper;

import com.pi.server.bean.Code;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CodeMapper {
	
	Code selectByKey(@Param("codeKey") String codeKey);
	
}
