package com.plateno.proxy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

//@Component
@ConfigurationProperties(prefix="vertx.proxy")
public class VertxProxyConfig {

	private String configServiceId ;
	
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

	public String getConfigServiceId() {
		return configServiceId;
	}

	public void setConfigServiceId(String configServiceId) {
		this.configServiceId = configServiceId;
	}
	
}
