package com.plateno.proxy.filters.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.plateno.proxy.HttpUtilty;
import com.plateno.proxy.ProxApplicationConfig;
import com.plateno.proxy.filters.AbstractFilter;

import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

@Service
public class BaseFilter extends AbstractFilter {

	@Autowired
	private ProxApplicationConfig proxyConfig ;
	
	@Override
	public void handle(RoutingContext event , HttpClient client ) {

		JsonObject router = getRout(event.request().path()) ;
		
		if( router != null )
		{
			this.disableFilter(event);
			this.disableProxy(event);
			
			HttpUtilty.requestHttpClientHander(event, client, router.getInteger("port"), router.getString("hostName") , router.getString("uri"), router.getInteger("timeOut"));

		}
		
		// this.forward(event, client, 9087, "localhost", event.request().uri() , 2000);
		
		// this.disableProxy(event);
		
//		event.response().setStatusCode(200) ;
//		event.response().end(config.getRemoteConfig().toString());
//		
//		this.disableProxy(event);
//		this.disableFilter(event);
	
	}
	
	private JsonObject getRout( String path )
	{
		String routeJs = proxyConfig.getRemoteConfig().get("routers") ;
		if( StringUtils.isEmpty(routeJs) ) return null ;
		
		JsonArray json = new JsonArray(routeJs) ;
		for( int i=0 ; i<json.size() ; i++ )
		{
			JsonObject router =  json.getJsonObject(i) ;
			if( path.startsWith( "/" + this.proxyConfig.getAppName() + router.getString("path")))
			{
				return router ;
			}
		}
		
		return null ;

	}

}
