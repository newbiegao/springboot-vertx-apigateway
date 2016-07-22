package com.plateno.proxy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="vertx.proxy")
public class VertxProxyConfig {

	public static final String INSTANCES = "instances" ;
	public static final String PORT = "port" ;
	public static final String PROXY_NAME = "proxyName" ;
	
	private Integer instances = 1 ;

	private Integer port = 8080 ;
	
	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Integer getInstances() {
		return instances;
	}

	public void setInstances(Integer instances) {
		this.instances = instances;
	}
	
}
