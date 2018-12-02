/**
 * 
 */
package com.example.hejing2.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

/**
 * @author 雷加伟
 *
 */
@Configuration
public class JedisClusterInstance extends CachingConfigurerSupport{
	
	@Autowired
	private RedisClusterConfig redisConfig;
	@Bean
	public JedisCluster getJedisCluster() {
		String[] serverArray = redisConfig.getNodes().split(",");
		Set<HostAndPort> set = new HashSet<HostAndPort>();
		for(int i=0 ; i< serverArray.length; i++) {
			set.add(new HostAndPort(serverArray[i].split(":")[0].trim(), Integer.valueOf(serverArray[i].split(":")[1].trim())));
		}
		return new JedisCluster(set, redisConfig.getCommandTimeout(), redisConfig.getExpireSeconds());
	}
}
