package com.plateno.proxy.filters;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.plateno.proxy.ProxApplicationConfig;

import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

@Service
public  class FiltersProcesser implements InitializingBean {

	private List<AbstractFilter> filters = new ArrayList<AbstractFilter>();
	
	@Autowired
	private ProxApplicationConfig proxyConfig ;
	
	@Autowired
	private ApplicationContext ctx ;
	
	public void process(RoutingContext event ,  HttpClient client )
	{
		
		for( AbstractFilter filter : filters )
		{
			filter.handle(event , client);
			if( !filter.continueFilter(event) ) break ;

		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
		filters.addAll(ctx.getBeansOfType(AbstractFilter.class).values()) ;
	}
	
	public static void enableProxy(RoutingContext event) {
		event.put("enableProxy", true);
	}

	public static void disableProxy(RoutingContext event) {
		event.put("enableProxy", false);
	}

	public static boolean canProxy(RoutingContext event) {
		if( event.get("enableProxy") == null ) return true ;
		return (boolean) event.get("enableProxy");
	}

}
