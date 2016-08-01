package com.plateno.proxy;

public class FilterRule {

	private static final Integer DEFAULT_TIMEOUT = 7000 ;
	
	private String path ;
	
	private Integer port ;
	
	private String uri ;
	
	private String appName ;
	
	private String host ;
	
	private String zone ;

	private Integer timeOut ;
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

	public Integer getTimeOut() {
			
		if( this.timeOut == null ) return DEFAULT_TIMEOUT ;
		return timeOut;
	}

	public void setTimeOut(Integer timeOut) {
		this.timeOut = timeOut;
	}
	
}
