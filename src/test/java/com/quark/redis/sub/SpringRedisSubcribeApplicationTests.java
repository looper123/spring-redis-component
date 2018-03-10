package com.quark.redis.sub;

import com.google.gson.Gson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringRedisSubcribeApplicationTests {

	@Autowired
	private RedisTemplate stringRedisTemplate;


	@Test
	public void contextLoads() {
	}

	@Test
	public void redisConnectionTest(){
		Map<String,Object>  map = new HashMap<>();
		List<String> list = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			list.add("hello");
		}
		map.put("serial_no", UUID.randomUUID().toString());
		map.put("data_list", list);
		String jsonStr = new Gson().toJson(map, Map.class);
		stringRedisTemplate.convertAndSend("channel_1",jsonStr);
//		stringRedisTemplate.convertAndSend("channel_1","helloWorld");
//		stringRedisTemplate.opsForValue().set("foo","bar");
	}




}
