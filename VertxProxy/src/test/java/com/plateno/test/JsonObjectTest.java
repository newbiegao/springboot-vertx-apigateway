package com.plateno.test;

import org.junit.Test;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class JsonObjectTest {

	@Test
	public void jsonTest()
	{
			
		JsonArray json = getRoutersJson() ;
		
		String js = Json.encode(json) ;
		
		System.out.println(js);
		
		JsonArray obj = new JsonArray(js) ;
		
		for( int i=0 ; i< obj.size() ; i++ )
		{
			String path = "/helps/" + i ;
			JsonObject router = obj.getJsonObject(i) ;
			System.out.println(router.encode() );
		}
	
		
	}
	
	
	
	private JsonArray getRoutersJson()
	{
		JsonArray root = new JsonArray() ;
		
		for( int i=0 ; i<5 ; i++ )
		{
			
			JsonObject rjson = new JsonObject() ;
			rjson.put("host", "localhost");
			rjson.put("path", "/helps/" + i );
			rjson.put("port", 8080) ;
			rjson.put("uri", "/help/"+i) ;
			
			root.add(rjson) ;
			
		}
		
		return root ;
	
	}
}
