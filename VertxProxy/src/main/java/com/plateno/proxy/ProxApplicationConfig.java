package com.plateno.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.plateno.proxy.config.HttpClientOptionsConfig;
import com.plateno.proxy.config.VertxProxyConfig;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@Configuration
@EnableConfigurationProperties({HttpClientOptionsConfig.class , VertxProxyConfig.class})
public class ProxApplicationConfig {

	private static final Logger logger = LoggerFactory.getLogger(ProxApplicationConfig.class) ;
	
	private  Vertx vertx ;
		
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
	
	/**
	 * 获取远程配置中心配置信息
	 * @return
	 */
	public Map<String,String> getRemoteConfig()
	{
		InstanceInfo configServerInfo ;
		try
		{
			configServerInfo = this.discoveryClient.getNextServerFromEureka(vertxConfig.getConfigServiceId(), false) ;
			return configServerInfo.getMetadata() ;
		}
		catch( Exception exp )
		{
			logger.error(" can't find config server : " + vertxConfig.getConfigServiceId() , exp);
			return new HashMap<String, String>() ;
		}
	}
	
	/**
	 * 获取路由配置信息
	 * @return
	 */
	public List<FilterRule> getFilterRoules()
	{
		List<FilterRule> rules = new ArrayList<FilterRule>() ;
		
		String routeJs = getRemoteConfig().get("routers") ;
		if( StringUtils.isEmpty(routeJs) ) return rules ;
		
		JsonArray jsonArry = new JsonArray(routeJs) ;
		
		for( int i=0 ; i<jsonArry.size() ; i++ )
		{
			FilterRule rule = new FilterRule() ;
			JsonObject ruleJson = jsonArry.getJsonObject(i) ;
			rule.setAppName(ruleJson.getString("appName"));
			rule.setHost(ruleJson.getString("host"));
			rule.setPath(ruleJson.getString("path"));
			rule.setPort(ruleJson.getInteger("port"));
			rule.setUri(ruleJson.getString("uri"));
			rule.setZone(ruleJson.getString("zone"));
			rule.setTimeOut(ruleJson.getInteger("timeOut"));
			
			rules.add(rule) ;
		}
		
		return rules ;
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
