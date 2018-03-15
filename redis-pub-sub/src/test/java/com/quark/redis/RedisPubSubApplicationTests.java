package com.quark.redis;

import com.google.gson.Gson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

import javax.annotation.Resource;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisPubSubApplicationTests {

	@Resource(name = "myRedisTemplate")
	private RedisTemplate redisTemplate;

	@Resource(name = "sentinelConfig")
	private RedisSentinelConfiguration  sentinelConfiguration;

	@Resource(name="jedisPoolConfig")
	private JedisPoolConfig jedisPoolConfig;


	@Test
	public void contextLoads() {
	}

//	redis  pub/sub 测试
	@Test
	public void pubMessageTest(){
		Map<String,Object> map = new HashMap<>();
		List<String> list = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			list.add("hello");
		}
		String  serial_no ="123456";
		map.put("serial_no", serial_no);
		map.put("data_list", list);
		String jsonStr = new Gson().toJson(map, Map.class);
		redisTemplate.convertAndSend("channel_1",jsonStr);

//		执行lua 脚本 实现原子性
//		String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
//		Object result = redisTemplate.execute(new RedisScript() {
//			@Override
//			public String getSha1() {
//				return null;
//			}
//			@Override
//			public Class getResultType() {
//				return String.class;
//			}
//			@Override
//			public String getScriptAsString() {
//				return script;
//			}
//		}, Collections.singletonList("syn_data_" + serial_no), Collections.singletonList(serial_no));
//		assert result !=null;
	}


	@Test
	public void getNxWithExpireTest(){
		boolean status = setNxWithExpire("key1", "value", "NX", "EX", 300);
		Long time = redisTemplate.getExpire("key1");
		assert  time != null;
	}

	/**
	 * 当key不存在 才执行set（key,value,expirtime）操作
	 *{@link redis.clients.jedis.Jedis#set(String, String, String, String, long)}
	 * @param key
	 * @param value
	 * @param keyMark NX|XX, Only set the key if it does not already exist. XX -- Only set the key
	 * if it already exist
	 * @param timeMark EX|PX, expire time units: EX = seconds; PX = milliseconds
	 * @param time
	 * @return
	 */
	public  boolean setNxWithExpire(String key ,String value ,String keyMark,String timeMark ,final long time){
		JedisSentinelPool jedisSentinelPool = new JedisSentinelPool(sentinelConfiguration.getMaster().getName(), convertToJedisSentinelSet(sentinelConfiguration.getSentinels()),
				jedisPoolConfig);
		String status = jedisSentinelPool.getResource().set(key , value , keyMark, timeMark ,  time);
		if("OK".equals(status)){
			return true;
		}
		return false;
	}

	private  Set<String> convertToJedisSentinelSet(Set<RedisNode> sentinels) {
		if (CollectionUtils.isEmpty(sentinels)) {
			return Collections.emptySet();
		}
		Set<String> convertedNodes = new LinkedHashSet<String>(sentinels.size());
		for (RedisNode node : sentinels) {
			if (node != null) {
				convertedNodes.add(node.asString());
			}
		}
		return convertedNodes;
	}

}
