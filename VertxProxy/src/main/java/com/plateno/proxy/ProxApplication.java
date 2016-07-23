package com.plateno.proxy;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContext;

import com.plateno.proxy.config.VertxProxyConfig;
import com.plateno.proxy.verticles.IVertx;

import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

@SpringBootApplication
@EnableDiscoveryClient
public class ProxApplication implements InitializingBean {

	@Autowired
	private VertxProxyConfig config ;
	
	@Autowired
	private ApplicationContext ctx ;
	
	@Autowired
	private Vertx vertx ;
		
	public static void main(String[] args) {
		
		SpringApplication.run(ProxApplication.class, args);
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
		for( int i=0 ; i<config.getInstances() ; i++ )
		{
			for( IVertx vl : ctx.getBeansOfType(IVertx.class).values() )
			{
				this.vertx.deployVerticle((Verticle)vl);
			}
		}
		
	}
	
}
