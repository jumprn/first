package com.example.hejing2;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@SpringBootApplication//(exclude = DataSourceAutoConfiguration.class)
@MapperScan("com.example.hejing2.dao")
@ComponentScan
@EnableScheduling
public class Hejing2Application extends SpringBootServletInitializer{

	public static void main(String[] args) {
		SpringApplication.run(Hejing2Application.class, args);
	}
	
	@Override 
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) { 
		return builder.sources(Hejing2Application.class); 
		}
//	 @Bean 
//	 public RedisTemplate<Object, Object> redisTemplateKeyObject(RedisConnectionFactory redisConnectionFactory) { 
//		 RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>(); 
//		 redisTemplate.setConnectionFactory(redisConnectionFactory); 
//		 @SuppressWarnings("rawtypes")
//		Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class); 
//		 ObjectMapper objectMapper = new ObjectMapper(); objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY); 
//		 objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL); 
//		 objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); 
//		 objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); 
//		 jackson2JsonRedisSerializer.setObjectMapper(objectMapper); 
//		 redisTemplate.setKeySerializer(new StringRedisSerializer()); 
//		 redisTemplate.setValueSerializer(jackson2JsonRedisSerializer); 
//		 redisTemplate.afterPropertiesSet(); 
//		 return redisTemplate; 
//		 }
}
