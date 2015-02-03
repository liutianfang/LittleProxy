package org.littleshoot.proxy;

import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

/**
 * Tests just a single basic proxy.
 */
public class SimpleProxy  {
    public static void main(String[] args){
    	HttpProxyServer server = DefaultHttpProxyServer.bootstrap()
				.withAllowLocalOnly(false)
//				.withAddress(localServerIP)
				.withPort(8081)
				.start();
    }
}
