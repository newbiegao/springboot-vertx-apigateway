package com.plateno.proxy.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.streams.Pump;
import io.vertx.ext.web.RoutingContext;

public abstract class AbstractFilter {

	private static final Logger logger = LoggerFactory.getLogger(AbstractFilter.class);
	
	public void forward(RoutingContext event, HttpClient client, Integer port, String hostName, String path,
			Integer timeout) {

		// create httpclient
		HttpClientRequest clientReq = client.request(event.request().method(), port, hostName, path)
				.setTimeout(timeout);

		clientReq.headers().addAll(event.request().headers().remove("Host"));
		clientReq.putHeader("Host", "localhost");
		if (event.request().headers().get("Content-Length") == null) {
			clientReq.setChunked(true);
		}

		// 连接错误处理
		clientReq.exceptionHandler(exp -> {

			event.response().setStatusCode(500);
			event.response().end("exceptionHandler--->" + exp.getMessage());

		});

		// 处理http返回结果
		clientReq.handler(pResponse -> {

			// 获取请求响应结果
			event.response().headers().addAll(pResponse.headers());
			event.response().setStatusCode(pResponse.statusCode());
			event.response().setStatusMessage(pResponse.statusMessage());

			// 如果远程响应没有数据返回数据需要设置Chunked模式
			if (pResponse.headers().get("Content-Length") == null) {
				event.response().setChunked(true);
			}

			Pump targetToProxy = Pump.pump(pResponse, event.response());
			targetToProxy.start();
			// pResponse.handler( data ->{
			// requestHandler.response().write(data) ;
			//
			// });

			// 远程请求错误处理事件
			pResponse.exceptionHandler(exp -> {

				event.response().setStatusCode(500);
				event.response().end("error" + exp.getMessage());
				logger.error("client request error :", exp);

			});

			// 处理结束事件
			pResponse.endHandler(v -> event.response().end());

		});

		Pump proxyToTarget = Pump.pump(event.request(), clientReq);
		proxyToTarget.start();
		event.request().endHandler(v -> clientReq.end());

	}

	public abstract void handle(RoutingContext event, HttpClient client);

}
