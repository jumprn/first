/**
 * 
 */
package com.example.hejing2.utils.cache;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.hejing2.utils.Fire.LimitProcessEnum;

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPubSub;


/**
 * @author 雷加伟
 *
 */
@Component
public class RedisUtils {

	private Log logger=LogFactory.getLog(getClass());
	@Autowired
	private JedisCluster jedisCluster;
	@Autowired
	private LimitProcessJedisPubSub limitProcessJedisPubSub;
	public String getStringBykey(String key) {
		return jedisCluster.get(key);
	}
	
	public String setStringByKey(String key,String value) {
		return jedisCluster.set(key.getBytes(), value.getBytes());
	}
	
	/**
	 * 操作hash
	 * @param key
	 * @param object
	 */
	public void setHashByKeyAndValue(String key ,JSONObject object) {
		jedisCluster.hmset(key, parseJsonToMap(object));
	}
	
	public Map<String,String> getHashMapByKey(String key){
		Map<String,String> map= null;
		map=jedisCluster.hgetAll(key);
		return map;
	}
	/**
	 * 操作list,单key批量添加
	 * @param key
	 * @param String
	 */
	public void setListByKeyAndValues(String key,String[] string) {
		jedisCluster.lpush(key, string);
	}
	/**
	 * 单次在list首位添加值
	 * @param key
	 * @param value
	 */
	public void setListByKeyAndValue(String key,String value) {
		System.out.println("++++++++++++++往list中塞数据返回值+++++++++++++"+jedisCluster.lpush(key, value));
	}
	/**
	 * 非阻塞取出列表的最后一个
	 * @param key
	 * @return
	 */
	public String getLastOfKey(String key) {
		return jedisCluster.rpop(key);
	}
	/**
	 * 非阻塞取出列表的第一个
	 * @param key
	 * @return
	 */
	public String getFirstOfKey(String key) {
		return jedisCluster.lpop(key);
	}
	/**
	 * 阻塞方式得到list最后一个
	 * @param key
	 * @param timeout
	 * @return
	 */
	public String getBlockLastOfKey(String key,int timeout) {
		List<String> list =new ArrayList<String>();
		list=jedisCluster.brpop(timeout, key);
		if(list.size()>1) {
			return list.get(1);
		}else if(list.size()>0) {
			return list.get(0);
		}else {
			return "nil";
		}
	}
	/**
	 * 阻塞方式得到list第一个
	 * @param key
	 * @param timeout
	 * @return
	 */
	public String getBlockFirstOfKey(String key,int timeout) {
		List<String> list =new ArrayList<String>();
		list=jedisCluster.blpop(timeout, key);
		if(list.size()>1) {
			return list.get(1);
		}else if(list.size()>0) {
			return list.get(0);
		}else {
			return "nil";
		}
	}
	/**
	 * 抽奖
	 * @param id
	 * @param key
	 * @return
	 */
	public String partiReward(String id,String sourcekey,String desKey) {
		try {
			String rewardId = getBlockLastOfKey(sourcekey, 10);
			System.out.println("++++++++++blocking rewardId+++++++++++++++"+rewardId);
			if(!rewardId.equals("nil")) {
				setListByKeyAndValue(desKey, rewardId);
				System.out.println("+++++++++此时奖池中还有:"+jedisCluster.llen(sourcekey)+"张奖券可用+++++++++");
				return rewardId;
			}else {
				System.out.println("++++++++++超时仍未从抽奖池中获取到可用奖券+++++++++++");
				return "fail";
			}
		} catch (Exception e) {
			System.out.println("抽奖过程中，没有找到对应的key");
			System.out.println(e);
			return "fail";
		}
	}
	/**
	 * 非阻塞式设置列表首部值,并返回列表已有数据长度（可供下游应用提供限流）
	 * @param key
	 * @param value
	 * @return
	 */
	public long addValueToList(String key,String value) {
		return jedisCluster.lpush(key, value);
	}
	/**
	 *  发布(限流使用)
	 * @param key
	 * @param value
	 */
	public void publishPub(String key,String value) {
		jedisCluster.publish(key, value);
	}
	/**
	 *  订阅，收到消息后，进行处理
	 * @param channel
	 */
	public void subscribeSub(String channel) {
		jedisCluster.subscribe(limitProcessJedisPubSub, channel);
	}
	/**
	 * sourceKey目标key，存放sourceValue可用于在descKey中查询hash值
	 * @param sourceKey
	 * @param descKey
	 * @param sourceValue
	 * @return
	 */
	public String limitMessageToProcess(String sourceKey,String descKey,String channel , String sourceValue,long len,String json) {
		try {
			long startSe = System.currentTimeMillis();
			long length = jedisCluster.lpush(sourceKey, sourceValue);
			while(length>len&&(System.currentTimeMillis()-startSe)/1000<1) {//尝试一秒,然后便返回给用户报告此次操作失败，等待一段时间再重试
				String mess = jedisCluster.ltrim(sourceKey, 0, len-1);
				if(mess.toLowerCase().equals("ok")) {
					length = jedisCluster.lpush(sourceKey, sourceValue);
				}else {
					System.out.println("+++++++++key为:"+sourceKey+"的redis list不存在了!++++++++++++");
				}
				
			}
			if(length<=len) {
				setHashByKeyAndValue(descKey+":"+sourceValue, JSONObject.parseObject(json));
				publishPub(channel,sourceValue);//如果在限流范围内，则发布，进入业务处理
				return "success";
			}else {//超出范围了，则丢弃这一次操作
				jedisCluster.ltrim(sourceKey, 0, len-1);
				return "fail";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "fail";
		}
	}
	public Map<String,String> parseJsonToMap(JSONObject object){
		Map<String,String> map =new HashMap<String,String>();
		@SuppressWarnings("unchecked")
		Iterator<String> iterator = object.keySet().iterator();
		if(iterator!=null) {
			while(iterator.hasNext()) {
				try {
					String key = iterator.next();
					Object value = object.get(key);
					
					if(value instanceof JSONArray) {
						map.put(key, ((JSONObject)value).toString());
					}else if(value instanceof JSONObject) {
						map.put(key, ((JSONObject)value).toString());
					} else if(value instanceof String){
						map.put(key, (String)value);
					} else if(value instanceof Integer){
						map.put(key, String.valueOf(((Integer)value).longValue()));
					} else if(value instanceof BigDecimal){
						map.put(key, String.valueOf(((BigDecimal)value).longValue()));
					}else if(value instanceof Object){//只能针对对象中只含有普通字符型类型字段的对象
						Map<String, String> childMap=null;
						try {
							childMap = BeanUtils.describe(value);
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						} catch (NoSuchMethodException e) {
							e.printStackTrace();
						}
						JSONObject object1 =mapToJSONOject(childMap);
						map.put(key, object1.toString());
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}else {
			return null;
		}
		return map;
	}
	
	public JSONObject mapToJSONOject(Map<String,String> map) {
		Iterator<String> iterator =map.keySet().iterator();
		JSONObject jsonObject = new JSONObject();
		while(iterator.hasNext()) {
			String key =iterator.next();
			try {
				jsonObject.put(key, map.get(key));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return jsonObject;
	}
}
