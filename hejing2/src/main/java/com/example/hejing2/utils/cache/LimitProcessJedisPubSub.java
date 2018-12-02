package com.example.hejing2.utils.cache;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.hejing2.utils.Fire.LimitProcessEnum;

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPubSub;
@Component
public class LimitProcessJedisPubSub extends JedisPubSub {

	private static int i=0;
	private static int j=0;
	@Autowired
	private JedisCluster jedisCluster;
	@Autowired
	private RedisUtils redisUtils;
	@Override
	public void onMessage(String channel, String message) {
		j++;
		Map<String,String> map = redisUtils.getHashMapByKey(LimitProcessEnum.descKey+":"+message);
		Set<Entry<String,String>> entrySet = map.entrySet();
		System.out.println("+++++++++++this is the "+j+" return of sub+++++++++");
		for(Entry<String,String> entry : entrySet) {
			System.out.println("您购买了:"+entry.getKey()+"花了"+entry.getValue()+"元");
		}
		System.out.println("++++++++++++++++处理结束+++++++++++++++userid:"+message);
		redisUtils.getLastOfKey(LimitProcessEnum.sourceKey);
		
	}

	@Override
	public void onSubscribe(String channel, int subscribedChannels) {
		i++;
		System.out.println("===============this is the "+i +"subscribe");
	}

}
