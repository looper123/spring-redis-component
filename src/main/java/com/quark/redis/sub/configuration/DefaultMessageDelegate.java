package com.quark.redis.sub.configuration;


import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by ZhenpengLu on 2018/3/9.
 * 消息委托类  书写拿到消息后自定义的操作
 */
@Component
public class DefaultMessageDelegate implements MessageDelegate {

    @Override
    public void handleMessage(String message) {
        System.out.println(message+"---this is a string message");
    }

    @Override
    public void handleMessage(Map message) {
        System.out.println(message+"----this is a map message");
    }

    @Override
    public void handleMessage(byte[] message) {
        System.out.println(message+"----this is a byte[] message");
    }

    @Override
    public void handleMessage(Serializable message) {
        System.out.println(message+"----this is a serializable message");
    }

    @Override
    public void handleMessage(Serializable message, String channel) {
        System.out.println(message+"----this is a serializable message  from channel---"+channel);
    }
}
