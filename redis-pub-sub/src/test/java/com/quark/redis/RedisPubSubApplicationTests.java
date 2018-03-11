package com.quark.redis;

import com.google.gson.Gson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisPubSubApplicationTests {

	@Resource(name = "myRedisTemplate")
	private RedisTemplate redisTemplate;

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
		String  serial_no = UUID.randomUUID().toString();
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


}
