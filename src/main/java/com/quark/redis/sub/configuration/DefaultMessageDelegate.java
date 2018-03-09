package com.quark.redis.sub.configuration;


import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by ZhenpengLu on 2018/3/9.
 */
@Component
public class DefaultMessageDelegate implements MessageDelegate {

    @Override
    public void handleMessage(String message) {
        System.out.println(message+"message");
    }

    @Override
    public void handleMessage(Map message) {
        System.out.println(message+"message");
    }

    @Override
    public void handleMessage(byte[] message) {
        System.out.println(message+"message");
    }

    @Override
    public void handleMessage(Serializable message) {
        System.out.println(message+"message");
    }

    @Override
    public void handleMessage(Serializable message, String channel) {
        System.out.println(message+"message");
    }
}
