package com.plateno.proxy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

//@Component
@ConfigurationProperties(prefix="vertx.proxy.httpclient")
public class HttpClientOptionsConfig {
	
	public static final String MAX_POOL_SIZE="maxPoolSize" ;
	public static final String MAX_WAIT_QUEUE_SIZE ="maxWaitQueueSize" ;
	public static final String CONNECT_TIMEOOUT = "connectTimeout" ;
	
	private Integer maxPoolSize = 500 ;
	
	private Integer maxWaitQueueSize = 100 ;
	
	private Integer connectTimeout = 2000 ;

	public Integer getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(Integer maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public Integer getMaxWaitQueueSize() {
		return maxWaitQueueSize;
	}

	public void setMaxWaitQueueSize(Integer maxWaitQueueSize) {
		this.maxWaitQueueSize = maxWaitQueueSize;
	}

	public Integer getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(Integer connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
	
}
