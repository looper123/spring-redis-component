package com.quark.redis.message.delegate;


import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by ZhenpengLu on 2018/3/9.
 * 消息委托类  书写拿到消息后自定义的操作
 */
@Component
public class DefaultMessageDelegate implements MessageDelegate {

    @Autowired
    private RedisTemplate myRedisTemplate;
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
        Map<String,Object> map = new Gson().fromJson(message, Map.class);
        String serial_no = (String)map.get("serial_no");
        if(checkHandingStatus(serial_no)){
            List dataList = (List)map.get("data_list");
            for(int i = 0; i<dataList.size();i++){
                System.out.println("data_element-----"+dataList.get(i));
            }
            System.out.println(message+"---this is a string message");
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
        String  oldValue =  (String) myRedisTemplate.opsForValue().getAndSet("syn_data_"+ serial_no,serial_no);
        if(oldValue == null){
            //设置超时时间
            Boolean setExpire = myRedisTemplate.expire("syn_data_" + serial_no, 1000L, TimeUnit.SECONDS);
            return  true;
        }
        return false;
    }


}
