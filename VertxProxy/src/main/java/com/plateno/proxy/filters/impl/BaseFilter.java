package com.plateno.proxy.filters.impl;

import org.springframework.stereotype.Service;

import com.plateno.proxy.filters.AbstractFilter;

import io.vertx.core.http.HttpClient;
import io.vertx.ext.web.RoutingContext;

@Service
public class BaseFilter extends AbstractFilter {

	@Override
	public void handle(RoutingContext event , HttpClient client ) {
		
		
		// this.forward(event, client, 9087, "localhost", event.request().uri() , 2000);
		
		// this.disableProxy(event);
		
	}

}
