package com.plateno.proxy.filters;

import io.vertx.core.http.HttpClient;
import io.vertx.ext.web.RoutingContext;

public abstract class AbstractFilter {

	public abstract void handle(RoutingContext event, HttpClient client);

	public  void enableProxy(RoutingContext event) {
		FiltersProcesser.enableProxy(event);
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
}
