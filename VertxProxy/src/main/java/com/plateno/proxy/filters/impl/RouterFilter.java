package com.plateno.proxy.filters.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.plateno.proxy.FilterRule;
import com.plateno.proxy.HttpUtilty;
import com.plateno.proxy.ProxApplicationConfig;
import com.plateno.proxy.filters.AbstractFilter;

import io.vertx.core.http.HttpClient;
import io.vertx.ext.web.RoutingContext;

@Service
public class RouterFilter extends AbstractFilter {

	@Autowired
	private ProxApplicationConfig proxyConfig ;
	
	@Override
	public void handle(RoutingContext event , HttpClient client ) {

//		JsonObject router = getRout(event.request().path()) ;
//		
//		if( router != null )
//		{
//			this.disableFilter(event);
//			this.disableProxy(event);
//			
//			HttpUtilty.requestHttpClientHander(event, client, router.getInteger("port"), router.getString("hostName") , router.getString("uri"), router.getInteger("timeOut"));
//
//		}
		
		// this.forward(event, client, 9087, "localhost", event.request().uri() , 2000);
		
		// this.disableProxy(event);
		
//		event.response().setStatusCode(200) ;
//		event.response().end(config.getRemoteConfig().toString());
//		
//		this.disableProxy(event);
//		this.disableFilter(event);
		
		filter(event , client) ;
	
	}
	
	private void filter( RoutingContext event , HttpClient client )
	{
		FilterRule rule = this.matchRule(event.request().path()) ;
		
		if(rule == null) return ;
	
		if( StringUtils.isEmpty(rule.getAppName() ) ) 
		{
			// 基于 URL 路由
			this.disableFilter(event);
			this.disableProxy(event);
			
			HttpUtilty.requestHttpClientHander(event, client, rule.getPort() , rule.getHost() , rule.getUri() , rule.getTimeOut());
		}
		else
		{
			// 基于 appName 路由
			this.disableFilter(event);
			this.enableProxy(event, rule.getZone());
		}
		
	}
	
}
