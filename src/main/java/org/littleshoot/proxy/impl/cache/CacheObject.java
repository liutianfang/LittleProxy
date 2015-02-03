package org.littleshoot.proxy.impl.cache;

import io.netty.handler.codec.http.HttpObject;

public class CacheObject {
	

	/**
	 * create time in nano second
	 */
	Long nano;
	/**
	 * httpObject name
	 */
	String objName = "";
	/**
	 * is response
	 */
	boolean isResponse;
	/**
	 * cached HttpObject
	 */
	HttpObject httpObject;

	public CacheObject(Long nano, String objName, boolean isResponse, HttpObject httpObject) {
		this.nano = nano;
		this.objName = objName;
		this.isResponse = isResponse;
		this.httpObject = httpObject;

	}
	
	public boolean isResponse() {
		return isResponse;
	}

	public void setResponse(boolean isResponse) {
		this.isResponse = isResponse;
	}

	public HttpObject getHttpObject() {
		return httpObject;
	}

	public void setHttpObject(HttpObject httpObject) {
		this.httpObject = httpObject;
	}

}
