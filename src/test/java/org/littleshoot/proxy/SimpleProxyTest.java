package org.littleshoot.proxy;

import java.net.InetSocketAddress;

import org.apache.log4j.PropertyConfigurator;

/**
 * Tests just a single basic proxy.
 */
public class SimpleProxyTest extends BaseProxyTest {
    @Override
    protected void setUp() {
    	PropertyConfigurator.configure("log4j.properties");
    	this.proxyServer = bootstrapProxy()
                .withPort(proxyServerPort)
//                .withAllowLocalOnly(false).withAddress(new InetSocketAddress("192.168.0.144", 8082))
                .start();
    }
}
