package com.example.hejing2.controller;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.hejing2.utils.cache.RedisUtils;

@Controller
@RequestMapping("/jedisCluster")
public class TestJedisClusterController {
	@Autowired
	private RedisUtils redisUtils;
	
	@RequestMapping("/testString")
	public void testString(HttpServletRequest request,HttpServletResponse response) {
		redisUtils.setStringByKey("keys", "testkey");
		try {
			System.out.println(redisUtils.getStringBykey("keys").getBytes());
			response.getOutputStream().write(redisUtils.getStringBykey("keys").getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping("/testHash")
	public void testHash() {
//		JSONObject object =new JSONObject();
//		JSONObject object1 =new JSONObject();
//		try {
//			object.put("first", "values1");
//			object1.put("key2", "values2");
//			object.put("second", object1);
//			object.put("third", "third");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		redisUtils.setHashByKeyAndValue("hash", object);
//		Set<String> keySet = redisUtils.getHashMapByKey("hash").keySet();
//		Iterator<String> iterator = keySet.iterator();
//		while(iterator.hasNext()) {
//			System.out.println(redisUtils.getHashMapByKey("hash").get(iterator.next()));;
//		}
//		
	}
}
