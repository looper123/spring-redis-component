package com.quark.redis.message.delegate;


import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by ZhenpengLu on 2018/3/9.
 * 消息委托类  书写拿到消息后自定义的操作
 */
@Component
public class DefaultMessageDelegate implements MessageDelegate {

    @Autowired
    private RedisTemplate myRedisTemplate;

    @Autowired
    private RedisSentinelConfiguration sentinelConfig;

    @Autowired
    private JedisPoolConfig jedisPoolConfig;
    /**
     * note 默认取回的message是object类型的
     * 而在MessageListenerAdapter中  {@link org.springframework.data.redis.listener.adapter.MessageListenerAdapter}
     * 中的invoke方法 中的一个判断 types[0].isInstance(args[0]) 意思是只要委托类中的方法是 Object下子类 就直接调用该方法
     * 并返回  所以是需要留下自己想接收的类型的方法即可 (顺便说一句MessageListenerAdapter中的methods（表示所有委托类下的方法）是
     * 按照参数类型大小来排序 aA-zZ...)
     */

    @Override
    public void handleMessage(String message) {
        System.out.println("receiving the string message....");
        //可以通过此方法 完成 json 到 java bean的转化
        Map<String,Object> map = new Gson().fromJson(message, Map.class);
        String serial_no = (String)map.get("serial_no");
        System.out.println("message handing starting.....");
        if(checkHandingStatus(serial_no)){
            System.out.println("message handing running.....");
            List dataList = (List)map.get("data_list");
            for(int i = 0; i<dataList.size();i++){
                System.out.println("data_element-----"+dataList.get(i));
            }
            System.out.println("message handing end.....");
        }
            //message处理完成后删除key
//            myRedisTemplate.delete("syn_data_"+ serial_no);
            //保存完毕删除key
//            myRedisTemplate.delete("syn_data_" + serial_no);
//            lua script 实现锁的释放
//            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
//            stringRedisTemplate.execute(new RedisScript() {
//                @Override
//                public String getSha1() {
//                    return null;
//                }
//                @Override
//                public Class getResultType() {
//                    return String.class;
//                }
//                @Override
//                public String getScriptAsString() {
//                    return script;
//                }
//            }, Collections.singletonList("syn_data_" + serial_no), Collections.singletonList(serial_no));

    }

//    @Override
//    public void handleMessage(Map message) {
////        System.out.println(message+"----this is a map message");
//    }
//
//    @Override
//    public void handleMessage(byte[] message) {
////        System.out.println(message+"----this is a byte[] message");
//    }
//
//    @Override
//    public void handleMessage(Serializable message) {
////           System.out.println("----this is a serializable message");
//    }
//
//    @Override
//    public void handleMessage(Serializable message, String channel) {
////        System.out.println(message+"----this is a serializable message  from channel---"+channel);
//    }

    private boolean checkHandingStatus(String serial_no)  {
//        try {
//            Thread.sleep(2000L);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        //集群服务下让处理逻辑只执行一次
        boolean  setStatus = setNxWithExpire("syn_data_" + serial_no,serial_no,"NX","EX",600);
        return setStatus;
    }


    /**
     * 当key不存在 才执行set（key,value,expiretime）操作
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
        JedisSentinelPool jedisSentinelPool = new JedisSentinelPool(sentinelConfig.getMaster().getName(), convertToJedisSentinelSet(sentinelConfig.getSentinels()),
                jedisPoolConfig);
        String status = jedisSentinelPool.getResource().set(key , value , keyMark, timeMark ,  time);
        if("OK".equals(status)){
            return true;
        }
        return false;
    }

    private Set<String> convertToJedisSentinelSet(Set<RedisNode> sentinels) {
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
