package com.plateno.proxy;

import java.util.function.Consumer;

import com.plateno.apigateway.domain.StrategyConfig;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public interface Strategy {

	public static final String STRATEGY_RESULT = "stgRest" ;
	
	public void process( RoutingContext event ,  StrategyConfig config , JsonObject context, Consumer<Boolean> onResult ) ; 
	
}
