package com.plateno.proxy.config;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.MyDataCenterInstanceConfig;
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClient;

// @Configuration
public class EurekaClientConfig {

	//@Bean
	public EurekaClient eurekaClientInit() {

		// 构造eureka客户端
		MyDataCenterInstanceConfig instanceConfig = new MyDataCenterInstanceConfig();
		
		InstanceInfo instanceInfo = new EurekaConfigBasedInstanceInfoProvider(instanceConfig).get();
		ApplicationInfoManager applicationInfoManager = new ApplicationInfoManager(instanceConfig, instanceInfo);

		DefaultEurekaClientConfig clientConfig = new DefaultEurekaClientConfig();
		EurekaClient eurekaClient = new DiscoveryClient(applicationInfoManager, clientConfig);
		
		return eurekaClient ; 

	}

}
