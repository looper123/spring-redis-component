package com.quark.redis.message.delegate;

/**
 * Created by ZhenpengLu on 2018/3/9.
 * 消息委托顶层接口
 */
public interface MessageDelegate {

    void handleMessage(String message);

//    void handleMessage(Map message);
//
//    void handleMessage(byte[] message);
//
//    void handleMessage(Serializable message);
//
//    // pass the channel/pattern as well
//    void handleMessage(Serializable message, String channel);
}
