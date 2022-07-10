/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package com.atguigu.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.BizCodeException;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class R2 extends HashMap<String, Object> {
	private static final long serialVersionUID = 1L;

	public <T> T getData(TypeReference<T> tTypeReference) {
		Object data = get("data");
		String s = JSON.toJSONString(data);
		T t = JSON.parseObject(s,tTypeReference);
		return t;
	}

	public R2 setData(Object data) {
		this.put("data",data);
		return this;
	}

	public R2(){

	}

	
	public R2 error() {
		return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
	}
	
	public R2 error(String msg) {
		return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
	}
	
	public R2 error(int code, String msg) {
		this.put("code", code);
		this.put("msg", msg);
		return this;
	}

	public R2 error(BizCodeException bizCodeException){
		return error(bizCodeException.getCode(),bizCodeException.getMsg());
	}

	public R2 ok() {
		this.put("code", 0);
		this.put("msg", "success");
		return this;
	}

	public R2 ok(String msg) {
		this.put("code", 0);
		this.put("msg", msg);
		return this;
	}

	public R2 ok(int code, String msg) {
		this.put("code", code);
		this.put("msg", msg);
		return this;
	}

	public R2 ok(Map<String, Object> map) {
		this.ok();
		this.putAll(map);
		return this;
	}


	@Override
	public R2 put(String key, Object value) {
		super.put(key, value);
		return this;
	}


	public Integer getCode(){
		return (Integer) this.get("code");
	}
}
