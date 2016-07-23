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
import com.plateno.proxy.ProxApplicationConfig;
import com.plateno.proxy.filters.FiltersProcesser;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.streams.Pump;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * VERYX 告诉API GATEWAY代理
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
			
			proxyHander(requestHandler , client );
			
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
	

	private void proxyHander(RoutingContext requestHandler , HttpClient client )
	{
		
		InstanceInfo backServer = getRemoteService(requestHandler) ; 
		
		if( backServer == null ) return ;
		
		String path = getRemoteServicePath(requestHandler);
		
		// 构造远程http服务
		HttpClientRequest clientReq = client.request(requestHandler.request().method(), backServer.getPort(),backServer.getHostName(), path ).setTimeout(getTimeOut(backServer,appConfig.getClientConfig().getConnectTimeout()));

		clientReq.headers().addAll(requestHandler.request().headers().remove("Host"));
		clientReq.putHeader("Host", "localhost");
		if (requestHandler.request().headers().get("Content-Length")==null) {
            clientReq.setChunked(true);
        }

		// 连接错误处理
		clientReq.exceptionHandler(exp -> {
			
			requestHandler.response().setStatusCode(500);
			requestHandler.response().end("exceptionHandler--->" + exp.getMessage());

		});

		// 处理http返回结果
		clientReq.handler(pResponse -> {
			
			// 获取请求响应结果
			requestHandler.response().headers().addAll(pResponse.headers());
			requestHandler.response().setStatusCode(pResponse.statusCode());
			requestHandler.response().setStatusMessage(pResponse.statusMessage());
			
			// 如果远程响应没有数据返回数据需要设置Chunked模式
			if (pResponse.headers().get("Content-Length") == null) {
				 requestHandler.response().setChunked(true);
	        }
			
			Pump targetToProxy = Pump.pump(pResponse, requestHandler.response());
			targetToProxy.start();
//			pResponse.handler( data ->{
//				requestHandler.response().write(data) ;
//				
//			});
			
			// 远程请求错误处理事件
			pResponse.exceptionHandler(exp -> {

				requestHandler.response().setStatusCode(500);
				requestHandler.response().end("error" + exp.getMessage() );
				logger.error("client request error :" , exp );
				
			});
			
			// 处理结束事件
			pResponse.endHandler(v -> requestHandler.response().end());

		});

		Pump proxyToTarget = Pump.pump(requestHandler.request(), clientReq);
		proxyToTarget.start();
		requestHandler.request().endHandler(v -> clientReq.end());
	}

}
