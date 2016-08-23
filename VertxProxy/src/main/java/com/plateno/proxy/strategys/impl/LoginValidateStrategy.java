package com.plateno.proxy.strategys.impl;

import java.util.function.Consumer;
import org.springframework.stereotype.Service;
import com.plateno.apigateway.domain.StrategyConfig;
import com.plateno.proxy.strategys.AbstractStrategy;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

@Service
public class LoginValidateStrategy extends AbstractStrategy {

	@Override
	protected void doProcess(RoutingContext event , StrategyConfig config , JsonObject context, Consumer<Boolean> onResult ) {
		// TODO Auto-generated method stub
		
		// System.out.println("---------------------");
		
		// this.pass(event, context, onResult);
		
		this.unpass(event, context, onResult, config); 
	}

}
