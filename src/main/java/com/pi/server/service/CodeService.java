package com.pi.server.service;

import com.pi.server.bean.Code;
import com.pi.server.mapper.CodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CodeService {
	
	@Autowired
	private CodeMapper codeMapper;
	
	// code表查询方法
	public Code selectByKey(String key){
		Code code = codeMapper.selectByKey(key);
		return code;
	}
	
}
