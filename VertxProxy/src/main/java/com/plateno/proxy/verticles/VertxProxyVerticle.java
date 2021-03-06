package com.plateno.proxy.verticles ;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.cyngn.vertx.async.promise.Promise;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Applications;
import com.plateno.proxy.HttpUtilty;
import com.plateno.proxy.ProxApplicationConfig;
import com.plateno.proxy.StrategyProcesser;
import com.plateno.proxy.filters.FiltersProcesser;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.RedisClient;


/**
 * VERYX API GATEWAY代理
 * @author gaolk
 *
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VertxProxyVerticle extends AbstractVerticle implements IVertx {
	
	private static final Logger logger = LoggerFactory.getLogger(VertxProxyVerticle.class);
	
	@Autowired
	private FiltersProcesser filtersProcesser ;
	
	@Autowired
	private StrategyProcesser strategyProcesser ;
	
	@Autowired
	private ProxApplicationConfig appConfig ;
		
	@Autowired
    private Vertx vertx;
	
	@Autowired
	private RedisClient redis ;
	
	@PostConstruct
	public void start() throws Exception {

		// httpclient 对象
		HttpClientOptions hopt = new HttpClientOptions().setKeepAlive(true);
		
		hopt.setTcpKeepAlive(true);
		hopt.setUsePooledBuffers(true);
		hopt.setReuseAddress(true);
		hopt.setMaxWaitQueueSize( appConfig.getClientConfig().getMaxWaitQueueSize());
		hopt.setConnectTimeout(appConfig.getClientConfig().getConnectTimeout());
		hopt.setMaxPoolSize(appConfig.getClientConfig().getMaxPoolSize());
		
		HttpClient client = vertx.createHttpClient(hopt);

		Router proxyRouter = Router.router(vertx);
			
		// proxyRouter.route().handler(BodyHandler.create()) ;
		
		// proxyRouter.route("/*").handler(TimeoutHandler.create(10000)) ;
		
		// 所有请求前置处理
		proxyRouter.route("/*").handler( strategyProcesser :: beginProcess);
			
		// 策略检查
		proxyRouter.route("/*").handler( strategyProcesser :: endProcess);
		
			
		// 服务路由请求
		proxyRouter.route(proxyPath(appConfig.getAppName())).handler( requestHandler -> {
			
			InstanceInfo backServer = getRemoteService(requestHandler) ; 
			if( backServer == null ) return ;
			
			String path = getRemoteServicePath(requestHandler);
			
			// 执行远程请求
			HttpUtilty.requestHttpClientHander(requestHandler , client , backServer.getPort() , backServer.getHostName() , path , getTimeOut(backServer,appConfig.getClientConfig().getConnectTimeout()) );
			
		}) ;

		proxyRouter.route("/redis").handler(this::redisHander) ;
		
		// 错误处理
		proxyRouter.exceptionHandler(exceptionHandler -> {

			logger.error(" proxy inner error: " , exceptionHandler );

		});
		
		HttpServerOptions options = new HttpServerOptions();
		options.setTcpKeepAlive(true);
		options.setReuseAddress(true);
		
		vertx.createHttpServer(options).requestHandler(proxyRouter::accept).listen(appConfig.getVertxConfig().getPort());
	}
	
	private String getRemoteServicePath( RoutingContext context )
	{
		String prefix = "/" + appConfig.getAppName() + "/" + context.request().getParam("serviceName") ;
		String path = context.request().uri().replace(prefix, "") ;
		return path ;
	}

	private Integer getTimeOut( InstanceInfo serverInfo , Integer defaultTimeOut )
	{
		Integer timeout = defaultTimeOut ;
		
		if( serverInfo == null ) return timeout ;
		
		if ( StringUtils.isEmpty(serverInfo.getMetadata().get("timeout")) ) return timeout ;
		
		try
		{
			timeout = Integer.parseInt(serverInfo.getMetadata().get("timeout").trim()) ;
		}
		catch( Exception e )
		{
			logger.error(" service : {}  timeout : {} is not validate " , serverInfo.getInstanceId() , serverInfo.getMetadata().get("timeout") );
		}
		return timeout ;
	}
	
	private InstanceInfo getRemoteService( RoutingContext context )
	{
	
		InstanceInfo info = null;
		String serviceName = context.request().getParam("serviceName") ;
		if( StringUtils.isEmpty(serviceName) )
		{
			context.response().setStatusCode(404) ;
			context.response().end("service isEmpty");
			return info ;
		}
		
		try
		{
			// 从上下文获取zone
			String zone = FiltersProcesser.getZone(context) ;
			
			if ( StringUtils.isEmpty(zone))
			{
				info = getNextServerFromEureka(serviceName);
			}
			else
			{
				info = getNextServerFromEureka(serviceName , zone);
			}
		}
		catch( Exception exp )
		{
			context.response().setStatusCode(404) ;
			context.response().end("service:"+serviceName+" is not available");
			logger.error("service :" + serviceName + " is not available" , exp);
		}
		
		return info ;
	}
	
	/**
	 * 获取指定zone的节点 +表示增加 不带+表示特指
	 * @param virtualHostname
	 * @param zone
	 * @return
	 */
	private InstanceInfo getNextServerFromEureka(String virtualHostname , String zone ) {
       
		boolean isAdd = zone.charAt(0) == '+' ? true : false ;
		
		List<InstanceInfo> instanceInfoList = this.appConfig.getDiscoveryClient().getInstancesByVipAddress(virtualHostname, false);
          
		if (instanceInfoList == null || instanceInfoList.isEmpty()) {
	            throw new RuntimeException("No matches for the virtual host name :" + virtualHostname);
	    }
		
		// 根据zone过滤节点
		List<InstanceInfo> newInstanceList = new  ArrayList<>();
		
		for( InstanceInfo info : instanceInfoList )
		{
			if( zone.equals(info.getMetadata().get("zone")) )
			{
				newInstanceList.add(info);
				continue ;
			}
			
			if( isAdd ) newInstanceList.add(info);
		}
		
		if (newInstanceList == null || newInstanceList.isEmpty()) {
            throw new RuntimeException("No matches for the virtual host name :" + virtualHostname + " zone : " + zone );
		}
        
		// 负载均衡
		Applications apps =  this.appConfig.getDiscoveryClient().getApplications() ;
	
        int index = (int) (apps.getNextIndex(virtualHostname.toUpperCase(Locale.ROOT),
                false).incrementAndGet() % newInstanceList.size());
        
        return newInstanceList.get(index);
    }
	
	/**
	 * 获取所有未指定zone的节点
	 * @param virtualHostname
	 * @return
	 */
	private InstanceInfo getNextServerFromEureka(String virtualHostname ) {
	       
		List<InstanceInfo> instanceInfoList = this.appConfig.getDiscoveryClient().getInstancesByVipAddress(virtualHostname, false);
        
		if (instanceInfoList == null || instanceInfoList.isEmpty()) {
	            throw new RuntimeException("No matches for the virtual host name :" + virtualHostname);
	    }
		
		// 根据zone过滤节点
		List<InstanceInfo> newInstanceList = new  ArrayList<>();
		
		for( InstanceInfo info : instanceInfoList )
		{			
			if( StringUtils.isEmpty(info.getMetadata().get("zone")) )
			{
				newInstanceList.add(info);
			}
		}
		
		if (newInstanceList == null || newInstanceList.isEmpty()) {
            throw new RuntimeException("No matches for the virtual host name :" + virtualHostname );
		}
        
		// 负载均衡
		Applications apps =  this.appConfig.getDiscoveryClient().getApplications() ;
	
        int index = (int) (apps.getNextIndex(virtualHostname.toUpperCase(Locale.ROOT),
                false).incrementAndGet() % newInstanceList.size());
        
        return newInstanceList.get(index);
    }
	
	private String proxyPath( String appName )
	{
		return "/" + appName + "/:serviceName/*" ;
	}
	
	public void redisHander( RoutingContext requestHandler  )
	{
		
		Promise.newInstance(this.vertx).then((context, onResult) -> {
			
			this.redis.set("user", requestHandler.request().getParam("user") , rest ->{
				
				if( rest.succeeded() )
				{
					 // System.out.println("ok--------" + requestHandler.request().getParam("user") );
					// logger.info("ok--------" + requestHandler.request().getParam("user"));
					// requestHandler.response().end( requestHandler.request().getParam("user") );
					context.put("msg", requestHandler.request().getParam("user") ) ;
				}
				if( rest.failed() )
				{
					context.put("msg",rest.cause().toString());
//					requestHandler.response().setStatusCode(500);
//					requestHandler.response().end(" redis error");
				}
				onResult.accept(true);
					
			}) ;
			
		})
		.then( ( context , onResult ) -> {
			
			this.redis.get("user", rest ->{
				
				if( rest.succeeded() )
				{
					context.put("msg", rest.result() + "-ok") ;
					onResult.accept(true);
				}
				if( rest.failed() )
				{
					context.put("msg", rest.cause().toString()) ;
					onResult.accept(true);
				}
				
			}) ;
			
		})
		.then( (context, onResult) -> {
			
			requestHandler.response().end(context.getString("msg") );
			
		}).eval() ;
		
	}
	
}
