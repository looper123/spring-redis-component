package com.quark.redis.sub;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

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
		stringRedisTemplate.convertAndSend("channel_1","hello");
//		stringRedisTemplate.opsForValue().set("foo","bar");
	}


}
