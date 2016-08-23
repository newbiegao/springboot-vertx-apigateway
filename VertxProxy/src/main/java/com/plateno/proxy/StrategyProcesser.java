package com.plateno.proxy;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.cyngn.vertx.async.promise.Promise;
import com.plateno.apigateway.domain.StrategyConfig;
import com.plateno.proxy.strategys.StrategyResult;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.ext.web.RoutingContext;

@Service
public class StrategyProcesser {

	@Autowired
	private ProxApplicationConfig proxyConfig ;
	
	@Autowired
	private ApplicationContext ctx ;
	
	@Autowired
    private Vertx vertx;
	
	public void beginProcess(RoutingContext event )
	{
		// 策略检查
		Map<String,StrategyConfig> strategys =  proxyConfig.getPathMappingStrategys(event) ;
		
		Promise promise = Promise.newInstance(this.vertx) ;
		
		for( String stName : strategys.keySet() )
		{
			Strategy st = (Strategy)ctx.getBean(stName) ;
			
			// 异步执行策略
			promise.then( (context, onResult) -> {
				st.process(event , strategys.get(stName) , context , onResult  ) ;
			}) ;
		}
			
		promise.then( (context, onResult) -> {
			onResult.accept(true);
			event.next() ;
		}) ;
		
		promise.eval() ;
		
	}
	
	public void endProcess( RoutingContext event  )
	{
		StrategyResult rest = event.get(Strategy.STRATEGY_RESULT) ;
		
		if( rest.isSuccess()) // 策略通过
		{
			event.next(); 
		}
		else // 策略不通过 , 返回错误信息
		{
			event.response().setStatusCode(rest.getCode()) ;
			event.response().end(rest.getMessage(), "UTF-8");
		}
	}
	
}
