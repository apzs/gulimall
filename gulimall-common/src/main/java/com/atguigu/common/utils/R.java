package com.atguigu.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.BizCodeException;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 返回数据
 */
public class R extends HashMap<String, Object> {
	private static final long serialVersionUID = 1L;
	
	public R() {
		put("code", 0);
		put("msg", "success");
	}
	
	public static R error() {
		return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
	}
	
	public static R error(String msg) {
		return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
	}
	
	public static R error(int code, String msg) {
		R r = new R();
		r.put("code", code);
		r.put("msg", msg);
		return r;
	}

	public static R error(BizCodeException bizCodeException){
		return error(bizCodeException.getCode(),bizCodeException.getMsg());
	}

	public static R ok(String msg) {
		R r = new R();
		r.put("msg", msg);
		return r;
	}
	
	public static R ok(Map<String, Object> map) {
		R r = new R();
		r.putAll(map);
		return r;
	}
	
	public static R ok() {
		return new R();
	}


	@Override
	public R put(String key, Object value) {
		super.put(key, value);
		return this;
	}

	public Integer getCode(){
		return (Integer) this.get("code");
	}

	public String getMsg(){
		return (String) this.get("msg");
	}

	private Object getData(){
		return this.get("data");
	}

	public R setData(Object obj){
		super.put("data",obj);
		return this;
	}

	public boolean isOk(){
		return this.getCode() == 0;
	}

	public boolean hasError(){
		return this.getCode() != 0;
	}

	private String getJSONString(Object obj){
		return JSON.toJSONString(obj);
	}

	public String getObjectStr(String key){
		return JSON.toJSONString(this.get(key));
	}

	private <T> T get(Object obj,Class<T> clazz){
		String s = getJSONString(obj);
		return JSON.parseObject(s,clazz);
	}

	private <T> List<T> getArray(Object obj,Class<T> clazz){
		String s = getJSONString(obj);
		return JSON.parseArray(s,clazz);
	}

	public <T> T get(String key,Class<T> clazz){
		return this.get(this.get(key),clazz);
	}

	public <T> List<T> getArray(String key,Class<T> clazz){
		return this.getArray(this.get(key),clazz);
	}

	public <T> T getData(Class<T> clazz){
		return this.get(this.getData(),clazz);
	}

	public <T> List<T> getDataArray(Class<T> clazz){
		return this.getArray(this.getData(),clazz);
	}

	public <T> T getData(TypeReference<T> tTypeReference) {
		String s = this.getJSONString(this.getData());
		return JSON.parseObject(s,tTypeReference);
	}
}
