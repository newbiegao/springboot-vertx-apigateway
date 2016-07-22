package com.plateno.proxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.plateno.proxy.config.HttpClientOptionsConfig;
import com.plateno.proxy.config.VertxProxyConfig;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

@Configuration
public class ProxApplicationConfig {

	private  Vertx vertx ;
	
	@Autowired
	private VertxProxyConfig vertxConfig ;
	
	@Autowired
	private HttpClientOptionsConfig clientConfig ;
	
	@Value("${spring.application.name}")
	private String appName;
	

	public VertxProxyConfig getVertxConfig() {
		return vertxConfig;
	}



	public void setVertxConfig(VertxProxyConfig vertxConfig) {
		this.vertxConfig = vertxConfig;
	}



	public HttpClientOptionsConfig getClientConfig() {
		return clientConfig;
	}



	public void setClientConfig(HttpClientOptionsConfig clientConfig) {
		this.clientConfig = clientConfig;
	}



	public String getAppName() {
		return appName;
	}



	public void setAppName(String appName) {
		this.appName = appName;
	}

	
	@Bean
    public Vertx getVertxInstance() {
        if (this.vertx==null) {
        	VertxOptions vopt = new VertxOptions() ;
        	vopt.setEventLoopPoolSize(40);
            this.vertx = Vertx.vertx(vopt);
        }
        return this.vertx;
    }
	
}
