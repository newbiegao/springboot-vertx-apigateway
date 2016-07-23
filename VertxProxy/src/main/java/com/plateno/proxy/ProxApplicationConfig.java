package com.plateno.proxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.discovery.EurekaClient;
import com.plateno.proxy.config.HttpClientOptionsConfig;
import com.plateno.proxy.config.VertxProxyConfig;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

@Configuration
@EnableConfigurationProperties({HttpClientOptionsConfig.class , VertxProxyConfig.class})
public class ProxApplicationConfig {

	private  Vertx vertx ;
	
//	public static ApplicationInfoManager applicationInfoManager ;
//	public static EurekaClient eurekaClient;
	
	@Autowired
	public ApplicationInfoManager applicationInfoManager ;
	
	@Autowired
	private EurekaClient discoveryClient ;
	
	@Autowired
	private VertxProxyConfig vertxConfig ;
	
	@Autowired
	private HttpClientOptionsConfig clientConfig ;
	
	@Value("${spring.application.name}")
	private String appName;
	
	
	public EurekaClient getDiscoveryClient() {
		return discoveryClient;
	}

	public void setDiscoveryClient(EurekaClient discoveryClient) {
		this.discoveryClient = discoveryClient;
	}

	public ApplicationInfoManager getApplicationInfoManager() {
	
		return applicationInfoManager;
	}

	public void setApplicationInfoManager(ApplicationInfoManager applicationInfoManager) {
		this.applicationInfoManager = applicationInfoManager;
	}

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
	
//	@PostConstruct
//	public void eurekaClientInit() {
//
//		// 构造eureka客户端
//		if( applicationInfoManager == null )
//		{
//			MyDataCenterInstanceConfig instanceConfig = new MyDataCenterInstanceConfig();
//			
//			InstanceInfo instanceInfo = new EurekaConfigBasedInstanceInfoProvider(instanceConfig).get();
//			
//			applicationInfoManager = new ApplicationInfoManager(instanceConfig, instanceInfo);
//		}
//		
//		if( eurekaClient == null )
//		{
//			DefaultEurekaClientConfig clientConfig = new DefaultEurekaClientConfig();
//			
//			eurekaClient = new DiscoveryClient(applicationInfoManager, clientConfig);
//			
//		}
//		
//	}
	
}
