package com.plateno.proxy.strategys;

import java.util.function.Consumer;

import com.plateno.apigateway.domain.StrategyConfig;
import com.plateno.proxy.Strategy;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public abstract class AbstractStrategy implements Strategy {

	protected abstract void doProcess( RoutingContext event , StrategyConfig config , JsonObject context, Consumer<Boolean> onResult ) ;
	
	protected void pass( RoutingContext event , JsonObject context , Consumer<Boolean> onResult )
	{
		context.put("success", true) ;
		onResult.accept(true);
		StrategyResult rest = new StrategyResult() ;
		rest.setSuccess(true);
		event.put(Strategy.STRATEGY_RESULT, rest);
	}
	
	protected void unpass( RoutingContext event , JsonObject context , Consumer<Boolean> onResult , StrategyConfig config )
	{
		context.put("success", false) ;
		onResult.accept(true);
		// 设置错误信息
		StrategyResult rest = new StrategyResult() ;
		rest.setCode(config.getErrorCode());
		rest.setMessage(config.getErrorMessage());
		rest.setSuccess(false);
		event.put(Strategy.STRATEGY_RESULT, rest);
	}
	
	public void process( RoutingContext event , StrategyConfig config , JsonObject context, Consumer<Boolean> onResult ) 
	{
		// 执行策略
		Integer index = context.getInteger("index",0) ;
		if( index == 0 ) // 没有执行过
		{
			context.put("index", 1);
			doProcess(event , config , context , onResult );
		}
		else
		{
			context.put("index", index+1);
			boolean success = context.getBoolean("success") ;
			if( success )
			{
				doProcess(event , config , context , onResult );
			}
		}
		
		onResult.accept(true);
	}
	
}
