package com.plateno.proxy.filters.impl;

import org.springframework.stereotype.Service;

import com.plateno.proxy.filters.AbstractFilter;

import io.vertx.ext.web.RoutingContext;

@Service
public class BaseFilter extends AbstractFilter {

	@Override
	public void handle(RoutingContext event) {
		
		// System.out.println(event.request().uri());
		
		// this.disableProxy(event);
		
	}

}
