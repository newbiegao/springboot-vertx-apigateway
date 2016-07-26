package com.plateno.proxy.verticles ;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.netflix.appinfo.InstanceInfo;
import com.plateno.proxy.HttpUtilty;
import com.plateno.proxy.ProxApplicationConfig;
import com.plateno.proxy.filters.FiltersProcesser;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

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
	public  FiltersProcesser filtersProcesser ;
	
	@Autowired
	private ProxApplicationConfig appConfig ;
		
	@Autowired
    private Vertx vertx;
	
	
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
				
		// 所有请求前置处理
		proxyRouter.route("/*").handler( requestHandler -> {
			
			filterHander(requestHandler,client);
			
		} ) ;
			
		// 服务路由请求
		proxyRouter.route(proxyPath(appConfig.getAppName())).handler( requestHandler -> {
			
			InstanceInfo backServer = getRemoteService(requestHandler) ; 
			if( backServer == null ) return ;
			
			String path = getRemoteServicePath(requestHandler);
			
			// 执行远程请求
			//requestHttpClientHander(requestHandler , client , backServer.getPort() , backServer.getHostName() , path , getTimeOut(backServer,appConfig.getClientConfig().getConnectTimeout()) );
			HttpUtilty.requestHttpClientHander(requestHandler , client , backServer.getPort() , backServer.getHostName() , path , getTimeOut(backServer,appConfig.getClientConfig().getConnectTimeout()) );
			
		}) ;

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
			info = this.appConfig.getDiscoveryClient().getNextServerFromEureka(serviceName, false) ;
		}
		catch( Exception exp )
		{
			context.response().setStatusCode(404) ;
			context.response().end("service:"+serviceName+" is not available");
			logger.error("service :" + serviceName + " is not available" , exp);
		}
		
		return info ;
	}
	
	private String proxyPath( String appName )
	{
		return "/" + appName + "/:serviceName/*" ;
	}
	

	private void filterHander( RoutingContext requestHandler , HttpClient client )
	{
		// 代理前置处理
		filtersProcesser.process(requestHandler , client );
		
		if(FiltersProcesser.canProxy(requestHandler) )
		{
			requestHandler.next(); 
		}

	}
	
}
