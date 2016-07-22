package com.plateno.proxy.filters;

import io.vertx.ext.web.RoutingContext;

public abstract class AbstractFilter {

	public abstract void handle(RoutingContext event) ;
	
	public void enableProxy( RoutingContext event )
	{
		event.put("enableProxy", true) ;
	}
	
	public void disableProxy( RoutingContext event )
	{
		event.put("enableProxy", false) ;
	}
	
	public boolean canProxy( RoutingContext event )
	{
		return (boolean)event.get("enableProxy") ;
	}
	
}
