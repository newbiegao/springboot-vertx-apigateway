package com.plateno.proxy.filters;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import io.vertx.ext.web.RoutingContext;

@Service
public  class FiltersProcesser implements InitializingBean {

	private List<AbstractFilter> filters = new ArrayList<AbstractFilter>();
	
	@Autowired
	private ApplicationContext ctx ;
	
	public void process(RoutingContext event)
	{
		for( AbstractFilter filter : filters )
		{
			filter.handle(event);
		}
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		
		filters.addAll(ctx.getBeansOfType(AbstractFilter.class).values()) ;
	}
	
}
