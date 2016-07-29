package com.plateno.proxy.filters;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.plateno.proxy.FilterRule;
import com.plateno.proxy.ProxApplicationConfig;

import io.vertx.core.http.HttpClient;
import io.vertx.ext.web.RoutingContext;

public abstract class AbstractFilter {
	
	@Autowired
	private ProxApplicationConfig proxyConfig ;
	
	public abstract void handle(RoutingContext event, HttpClient client);

	public  void enableProxy(RoutingContext event) {
		FiltersProcesser.enableProxy(event);
	}
	
	public  void enableProxy(RoutingContext event , String zone ) {
		FiltersProcesser.enableProxy(event);
		FiltersProcesser.setZone(event, zone);
	}

	public  void disableProxy(RoutingContext event) {
		FiltersProcesser.disableProxy(event);
	}
	
	public void nextFilter(RoutingContext event)
	{
		event.put("nextFilter", true);
	}
	
	public void disableFilter(RoutingContext event)
	{
		event.put("nextFilter", false);
	}
	
	public boolean continueFilter( RoutingContext event )
	{
		if( event.get("nextFilter") == null ) return false ;
		return  (boolean) event.get("nextFilter");
	}
	
	public FilterRule matchRule( String path )
	{
		if(StringUtils.isEmpty(path)) return null ;
		
		List<FilterRule> rules = proxyConfig.getFilterRoules() ;
		
		for( FilterRule rule : rules )
		{
			if( path.startsWith( "/" + this.proxyConfig.getAppName() + rule.getPath() ))
			{
				return rule ;
			}
		}
		
		return null ;
	}
}
