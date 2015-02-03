package org.littleshoot.proxy.impl.cache;

import java.util.concurrent.ConcurrentHashMap;

public class ResponseCache {
	
	
	private static final ConcurrentHashMap<Integer,Record>  cache=new ConcurrentHashMap<Integer, Record>();
	

	public static ConcurrentHashMap<Integer,Record> getCache() {
		
		return ResponseCache.cache;
	}



}
