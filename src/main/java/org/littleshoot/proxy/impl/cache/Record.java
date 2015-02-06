package org.littleshoot.proxy.impl.cache;

import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.LastHttpContent;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.littleshoot.proxy.impl.ProxyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Record {

	private static final Logger LOG = LoggerFactory.getLogger(Record.class);

	/**
	 * ctx hash
	 */
	int ctxHash;

	/**
	 * create time, in nano-second
	 */
	Long createNano = 0L;

	/**
	 * URL
	 */
	String requestURL = "";

	int ruleID = -1;

	/**
	 * 
	 */
	ConcurrentHashMap<Long, CacheObject> cacheMap = null;

	/**
	 * 
	 */
	boolean delayFlag = false;

	private HashSet<Long> sendout = new HashSet<Long>();

	/**
	 * 
	 */
	int delay = 1;

	boolean chunked = false;

	public void createResponseMap() {
		if (cacheMap == null) {
			cacheMap = new ConcurrentHashMap<Long, CacheObject>();
		}
	}

	public StringBuilder responsePreBody = new StringBuilder();
	public StringBuilder responsePostBody = new StringBuilder();

	public byte[] allContent = null;

	public DefaultHttpContent fullHttpContent = null;
	
	public DefaultLastHttpContent lastChunk = null;
	
	public HttpHeaders  httpRequestHeaders=null;
	
	

	/**
	 * insert
	 * 
	 * @param httpObject
	 */
	public void insert(HttpObject httpObject) {

		Long nano = System.nanoTime();
		CacheObject cobj = null;

		if (httpObject instanceof DefaultHttpResponse) {

			cobj = new CacheObject(nano, "DefaultHttpResponse", true, httpObject);

		} else if (httpObject instanceof DefaultHttpContent) {

			cobj = new CacheObject(nano, "DefaultHttpContent", false, httpObject);

		} else if (httpObject instanceof LastHttpContent) {

			cobj = new CacheObject(nano, "LastHttpContent", false, httpObject);

		}

		if (httpObject instanceof HttpContent) {

			LOG.debug("add content to responsePreBody,  createNano " + createNano);
			this.responsePreBody.append(((HttpContent) httpObject).content().retain().toString(Charset.forName("UTF-8")));

			byte[] tmp = new byte[((HttpContent) httpObject).content().retain().resetReaderIndex().readableBytes()];
			((HttpContent) httpObject).content().readBytes(tmp);

			if (allContent == null) {
				allContent = new byte[tmp.length];
				System.arraycopy(tmp, 0, allContent, 0, tmp.length);

			} else {
				byte[] copy = new byte[allContent.length];
				System.arraycopy(allContent, 0, copy, 0, allContent.length);
				allContent = new byte[tmp.length + copy.length];
				System.arraycopy(copy, 0, allContent, 0, copy.length);
				System.arraycopy(tmp, 0, allContent, copy.length, tmp.length);

			}

		}
		// else if (httpObject instanceof DefaultLastHttpContent) {
		//
		// LOG.debug("add content to responsePreBody,  createNano " +
		// createNano);
		// this.responsePreBody.append(((DefaultLastHttpContent)
		// httpObject).content().retain().toString(Charset.forName("UTF-8")));
		//
		// byte[] tmp = new byte[((DefaultLastHttpContent)
		// httpObject).content().retain().resetReaderIndex().readableBytes()];
		// ((DefaultLastHttpContent) httpObject).content().readBytes(tmp);
		//
		// byte[] copy = new byte[allContent.length];
		// System.arraycopy(allContent, 0, copy, 0, allContent.length);
		// allContent = new byte[tmp.length + copy.length];
		// System.arraycopy(copy, 0, allContent, 0, copy.length);
		// System.arraycopy(tmp, 0, allContent, copy.length, tmp.length);
		//
		// }

		if (cacheMap == null) {
			cacheMap = new ConcurrentHashMap<Long, CacheObject>();
		}

		if (cobj != null) {
			cacheMap.put(nano, cobj);
		} else {
			LOG.error("insert httpObject error, nanotime is " + nano + " httpObject is : " + httpObject);
		}
		LOG.debug("insert httpObject, nanotime is " + nano + " httpObject is : " + httpObject);
	}

	/**
	 * return the chacheMap is initial
	 */
	public boolean isFirst() {
		if (cacheMap == null || cacheMap.size() == 0) {
			return true;
		}
		return false;
	}

	public boolean isLast(HttpObject httpObject) {

		if (httpObject.toString().equalsIgnoreCase("EmptyLastHttpContent")) {
			return true;
		}

		return ProxyUtils.isLastChunk(httpObject);
	}

	public int getCtxHash() {
		return ctxHash;
	}

	public void setCtxHash(int ctxHash) {
		this.ctxHash = ctxHash;
	}

	public Long getCreateNano() {
		return createNano;
	}

	public void setCreateNano(Long createNano) {
		this.createNano = createNano;
	}

	public String getRequestURL() {
		return requestURL;
	}

	public void setRequestURL(String requestURL) {
		this.requestURL = requestURL;
	}

	public ConcurrentHashMap<Long, CacheObject> getCacheMap() {
		return cacheMap;
	}

	public void setCacheMap(ConcurrentHashMap<Long, CacheObject> cacheMap) {
		this.cacheMap = cacheMap;
	}

	/**
	 * return sorted nanotimes array, from small to big
	 * 
	 * @return
	 */
	public long[] getSortedNanoTimes() {

		if (cacheMap == null || cacheMap.size() == 0) {
			return null;
		} else {

			long[] ret = new long[cacheMap.size()];
			int i = 0;

			for (Map.Entry<Long, CacheObject> entry : cacheMap.entrySet()) {
				// for (long key : cacheMap.keySet()) {

				ret[i] = entry.getKey();
				i++;
			}

			Arrays.sort(ret);
			return ret;

		}
	}

	/*
	 * for (Map.Entry<String, String> entry : map.entrySet()) {
	 * System.out.println("key= " + entry.getKey() + "  and  value= " +
	 * entry.getValue()); } (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer().append("cache recrode:\tctxHash is ").append(ctxHash).append("\r\n\t\tcreateNano  ").append(createNano)
				.append("\r\n\t\t requestURL  ").append(requestURL).append("\r\n\t\t cache size  ").append(cacheMap.size());
		for (Map.Entry<Long, CacheObject> entry : cacheMap.entrySet()) {
			sb.append("\r\n\t objname  ").append(cacheMap.get(entry.getKey()).objName).append("   time is ").append(cacheMap.get(entry.getKey()).nano)
					.append("\r\n\t isresponse  ").append(cacheMap.get(entry.getKey()).isResponse);
		}

		return sb.append("\r\n").toString();
	}

	/**
	 * return key for Response
	 * 
	 * @return
	 */
	public long getResponseKey() {

		for (Map.Entry<Long, CacheObject> entry : cacheMap.entrySet()) {
			if (entry.getValue().isResponse()) {
				return entry.getKey();
			}
		}
		return 0;
	}

	/**
	 * return sorted all content cacheObj's nanotimes
	 * 
	 * @return
	 */
	public long[] getSortedContentKeys() {

		long[] ret = new long[cacheMap.size() - 1];
		int i = 0;

		for (Map.Entry<Long, CacheObject> entry : cacheMap.entrySet()) {
			if (entry.getValue().isResponse()) {
				continue;
			}
			ret[i] = entry.getKey();
			i++;
		}

		Arrays.sort(ret);
		return ret;

	}

	public byte[] getUnsendChunkedContent() {

		long[] keys = getSortedContentKeys();
		
		byte[] ret={};

		for (int i = 0; i < keys.length; i++) {

			if (!this.sendout.contains(keys[i])) {

				if (this.cacheMap.get(keys[i]).getHttpObject() instanceof HttpContent) {
					
					byte[] tmp = new byte[((HttpContent) this.cacheMap.get(keys[i]).getHttpObject()).content().retain().resetReaderIndex().readableBytes()];
					((HttpContent) this.cacheMap.get(keys[i]).getHttpObject()).content().readBytes(tmp);
					LOG.debug("Httpchunk   content length " +tmp.length);
					
					if (ret == null) {
						ret = new byte[tmp.length];
						System.arraycopy(tmp, 0, ret, 0, tmp.length);

					} else {
						byte[] copy = new byte[ret.length];
						System.arraycopy(ret, 0, copy, 0, ret.length);
						ret = new byte[tmp.length + copy.length];
						System.arraycopy(copy, 0, ret, 0, copy.length);
						System.arraycopy(tmp, 0, ret, copy.length, tmp.length);

					}
					
				}

			}
		}
		
		return ret;

	}
	
	/*
	 * byte[] tmp = new byte[((HttpContent) httpObject).content().retain().resetReaderIndex().readableBytes()];
			((HttpContent) httpObject).content().readBytes(tmp);

			
	 */

	public void setRuleID(int ruleID) {
		this.ruleID = ruleID;
	}

	public int getRuleID() {
		return ruleID;
	}

	public boolean isDelayFlag() {
		return delayFlag;
	}

	public void setDelayFlag(boolean delayFlag) {
		this.delayFlag = delayFlag;
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}



	public HashSet<Long> getSendout() {
		return sendout;
	}

	public void setSendout(HashSet<Long> sendout) {
		this.sendout = sendout;
	}

	public boolean isChunked() {
		return chunked;
	}

	public void setChunked(boolean chunked) {
		this.chunked = chunked;
	}

	public HttpHeaders getHttpRequestHeaders() {
		return httpRequestHeaders;
	}

	public void setHttpRequestHeaders(HttpHeaders httpRequestHeaders) {
		this.httpRequestHeaders = httpRequestHeaders;
	}



}
