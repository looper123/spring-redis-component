package com.quark.redis.sentinel.factory;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.util.Hashing;

import java.util.List;

/**
 * Created by Administrator on 2018/1/24/024.
 */
public class JedisShardedFactory implements PooledObjectFactory<ShardedJedis> {

    private Hashing hashing;
    private List<JedisShardInfo> shardInfos;

    public JedisShardedFactory(Hashing hashing, List<JedisShardInfo> shardInfos) {
        this.hashing = hashing;
        this.shardInfos = shardInfos;
    }

    //创建pool中的实例
    @Override
    public PooledObject<ShardedJedis> makeObject() throws Exception {
        ShardedJedis jedis = new ShardedJedis(shardInfos, hashing);
        return new DefaultPooledObject<ShardedJedis>(jedis);
    }

    //销毁实例
    @Override
    public void destroyObject(PooledObject<ShardedJedis> pooledShardedJedis) throws Exception {
        final ShardedJedis shardedJedis = pooledShardedJedis.getObject();
        for (Jedis jedis : shardedJedis.getAllShards()) {
            try {
                try {
                    jedis.quit();
                } catch (Exception e) {

                }
                jedis.disconnect();
            } catch (Exception e) {

            }
        }
    }

    //对pool中的实例做连接校验
    @Override
    public boolean validateObject(PooledObject<ShardedJedis> pooledShardedJedis) {
        try {
            ShardedJedis jedis = pooledShardedJedis.getObject();
            for (Jedis shard : jedis.getAllShards()) {
                if (!shard.ping().equals("PONG")) {
                    return false;
                }
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public void activateObject(PooledObject<ShardedJedis> pooledShardedJedis) throws Exception {

    }

    @Override
    public void passivateObject(PooledObject<ShardedJedis> pooledShardedJedis) throws Exception {

    }
}
