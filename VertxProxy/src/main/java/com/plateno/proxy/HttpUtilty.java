package com.plateno.proxy;

import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.streams.Pump;
import io.vertx.ext.web.RoutingContext;

public class HttpUtilty {


	public static void requestHttpClientHander(RoutingContext requestHandler , HttpClient client , Integer prot , String hostName , String path , Integer timeOut )
	{
		
		HttpClientRequest clientReq = client.request(requestHandler.request().method(), prot , hostName , path ).setTimeout(timeOut);
		
		Pump proxyToTarget = Pump.pump(requestHandler.request(), clientReq);
		proxyToTarget.start();
		
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
				
			});
			
			// 处理结束事件
			pResponse.endHandler(v -> requestHandler.response().end());

		});

		requestHandler.request().endHandler(v -> clientReq.end());
	}
	
}
