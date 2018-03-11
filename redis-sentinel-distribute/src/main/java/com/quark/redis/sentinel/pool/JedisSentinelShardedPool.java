package com.quark.redis.sentinel.pool;

import com.quark.redis.sentinel.factory.JedisShardedFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.Hashing;
import redis.clients.util.Pool;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by looper on 2018/1/24/024.
 * 同时支持jedisSentinel 和 jedisSharded
 */
public class JedisSentinelShardedPool extends Pool<ShardedJedis> {

    protected GenericObjectPoolConfig poolConfig;

    protected int connectionTimeout = Protocol.DEFAULT_TIMEOUT;
    protected int soTimeout = Protocol.DEFAULT_TIMEOUT;

    protected String password;

    protected int database = Protocol.DEFAULT_DATABASE;

    protected String clientName;

    protected Set<MasterListener> masterListeners = new HashSet<MasterListener>();

    protected Logger log = Logger.getLogger(getClass().getName());

//    private volatile JedisFactory factory;

    private volatile List<HostAndPort> currentHostMasters;

    protected int timeout = Protocol.DEFAULT_TIMEOUT;

//    private volatile HostAndPort currentHostMaster;

    public JedisSentinelShardedPool(String masterName, Set<String> sentinels,
                                    final GenericObjectPoolConfig poolConfig) {
        this(masterName, sentinels, poolConfig, Protocol.DEFAULT_TIMEOUT, null,
                Protocol.DEFAULT_DATABASE);
    }

    public JedisSentinelShardedPool(String masterName, Set<String> sentinels) {
        this(masterName, sentinels, new GenericObjectPoolConfig(), Protocol.DEFAULT_TIMEOUT, null,
                Protocol.DEFAULT_DATABASE);
    }

    public JedisSentinelShardedPool(String masterName, Set<String> sentinels, String password) {
        this(masterName, sentinels, new GenericObjectPoolConfig(), Protocol.DEFAULT_TIMEOUT, password);
    }

    public JedisSentinelShardedPool(String masterName, Set<String> sentinels,
                                    final GenericObjectPoolConfig poolConfig, int timeout, final String password) {
        this(masterName, sentinels, poolConfig, timeout, password, Protocol.DEFAULT_DATABASE);
    }

    public JedisSentinelShardedPool(String masterName, Set<String> sentinels,
                                    final GenericObjectPoolConfig poolConfig, final int timeout) {
        this(masterName, sentinels, poolConfig, timeout, null, Protocol.DEFAULT_DATABASE);
    }

    public JedisSentinelShardedPool(String masterName, Set<String> sentinels,
                                    final GenericObjectPoolConfig poolConfig, final String password) {
        this(masterName, sentinels, poolConfig, Protocol.DEFAULT_TIMEOUT, password);
    }

    public JedisSentinelShardedPool(String masterName, Set<String> sentinels,
                                    final GenericObjectPoolConfig poolConfig, int timeout, final String password,
                                    final int database) {
        this(masterName, sentinels, poolConfig, timeout, timeout, password, database);
    }

    public JedisSentinelShardedPool(String masterName, Set<String> sentinels,
                                    final GenericObjectPoolConfig poolConfig, int timeout, final String password,
                                    final int database, final String clientName) {
        this(masterName, sentinels, poolConfig, timeout, timeout, password, database, clientName);
    }

    public JedisSentinelShardedPool(String masterName, Set<String> sentinels,
                                    final GenericObjectPoolConfig poolConfig, final int timeout, final int soTimeout,
                                    final String password, final int database) {
        this(masterName, sentinels, poolConfig, timeout, soTimeout, password, database, null);
    }

    public JedisSentinelShardedPool(String masterName, Set<String> sentinels,
                                    final GenericObjectPoolConfig poolConfig, final int connectionTimeout, final int soTimeout,
                                    final String password, final int database, final String clientName) {
        this.poolConfig = poolConfig;
        this.connectionTimeout = connectionTimeout;
        this.soTimeout = soTimeout;
        this.password = password;
        this.database = database;
        this.clientName = clientName;
        //update: change return type to List<HostAndPort>
        //初始化sentinel
        List<HostAndPort> masters = initSentinels(sentinels, masterName);
        //初始化pool
        initPool(masters);
    }

    public void destroy() {
        for (MasterListener m : masterListeners) {
            m.shutdown();
        }

        super.destroy();
    }

    public List<HostAndPort> getCurrentHostMaster() {
        return currentHostMasters;
    }


    //update change param  type to list
    private void initPool(List<HostAndPort> masters) {
        if (!equals(currentHostMasters, masters)) {
            StringBuffer sb = new StringBuffer();
            for (HostAndPort master : masters) {
                sb.append(master.toString());
                sb.append(" ");
            }
            log.info("Created ShardedJedisPool to master at [" + sb.toString() + "]");
            //build shardinfo
            List<JedisShardInfo> shardInfos = BuildShardInfo(masters);
            //init sharded pool
            initPool(poolConfig, new JedisShardedFactory(Hashing.MURMUR_HASH, shardInfos));
        }

    }


    //build shardinfo
    private List<JedisShardInfo> BuildShardInfo(List<HostAndPort> masters) {
        List<JedisShardInfo> shardInfos = new ArrayList<>();
        for (int i = 0; i < masters.size(); i++) {
            JedisShardInfo jedisShardInfo = new JedisShardInfo(masters.get(i).getHost(), masters.get(i).getPort(), timeout);
            shardInfos.add(jedisShardInfo);
        }
        return shardInfos;
    }

    private boolean equals(List<HostAndPort> currentHostMaster, List<HostAndPort> masters) {
        if (currentHostMaster != null && masters != null) {
            for (int i = 0; i < masters.size(); i++) {
                if (!currentHostMaster.get(i).equals(masters.get(i))) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    private List<HostAndPort> initSentinels(Set<String> sentinels, final String masterName) {

        List<HostAndPort> masters = new ArrayList<>();
        boolean sentinelAvailable = false;

        log.info("Trying to find master from available Sentinels...");

        for (String sentinel : sentinels) {
            final HostAndPort hap = HostAndPort.parseString(sentinel);

            log.fine("Connecting to Sentinel " + hap);

            Jedis jedis = null;
            try {
                jedis = new Jedis(hap.getHost(), hap.getPort());
                //获取sentinel中配置的监控的redis master地址
                List<String> masterAddr = jedis.sentinelGetMasterAddrByName(masterName);

                // connected to sentinel...
                sentinelAvailable = true;

                if (masterAddr == null || masterAddr.size() != 2) {
                    log.warning("Can not get master addr, master name: " + masterName + ". Sentinel: " + hap
                            + ".");
                    continue;
                }

                HostAndPort master = toHostAndPort(masterAddr);
                log.fine("Found Redis master at " + master);

                if (master == null) {
                    if (sentinelAvailable) {
                        // can connect to sentinel, but master name seems to not
                        // monitored
                        throw new JedisException("Can connect to sentinel, but " + masterName
                                + " seems to be not monitored...");
                    } else {
                        //continue searching next sentinel for masterAddr
                    }
                }
                log.info("Redis master running at " + master + ", starting Sentinel listeners...");
                masters.add(master);
            } catch (JedisException e) {
                // resolves #1036, it should handle JedisException there's another chance
                // of raising JedisDataException
                log.warning("Cannot get master address from sentinel running @ " + hap + ". Reason: " + e
                        + ". Trying next one.");
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }


        for (String sentinel : sentinels) {
            final HostAndPort hap = HostAndPort.parseString(sentinel);
            MasterListener masterListener = new MasterListener(masterName, hap.getHost(), hap.getPort());
            // whether MasterListener threads are alive or not, process can be stopped
            masterListener.setDaemon(true);
            masterListeners.add(masterListener);
            masterListener.start();
        }

        return masters;
    }

    private HostAndPort toHostAndPort(List<String> getMasterAddrByNameResult) {
        String host = getMasterAddrByNameResult.get(0);
        int port = Integer.parseInt(getMasterAddrByNameResult.get(1));

        return new HostAndPort(host, port);
    }


    protected class MasterListener extends Thread {

        protected List<String> masters;
        protected String masterName;
        protected String host;
        protected int port;
        protected long subscribeRetryWaitTimeMillis = 5000;
        protected volatile Jedis j;
        protected AtomicBoolean running = new AtomicBoolean(false);

        protected MasterListener() {
        }

        public MasterListener(String masterName, String host, int port) {
            super(String.format("MasterListener-%s-[%s:%d]", masterName, host, port));
            this.masterName = masterName;
            this.host = host;
            this.port = port;
        }

        public MasterListener(String masterName, String host, int port,
                              long subscribeRetryWaitTimeMillis) {
            this(masterName, host, port);
            this.subscribeRetryWaitTimeMillis = subscribeRetryWaitTimeMillis;
        }

        @Override
        public void run() {

            running.set(true);

            while (running.get()) {

                j = new Jedis(host, port);

                try {
                    // double check that it is not being shutdown
                    if (!running.get()) {
                        break;
                    }
                    //发布订阅消息处理
                    j.subscribe(new JedisPubSub() {
                        @Override
                        public void onMessage(String channel, String message) {
                            log.fine("Sentinel " + host + ":" + port + " published: " + message + ".");

                            String[] switchMasterMsg = message.split(" ");

                            if (switchMasterMsg.length > 3) {

                                int index = masters.indexOf(switchMasterMsg[0]);
                                if (index >= 0) {
                                    HostAndPort newHostMaster = toHostAndPort(Arrays.asList(switchMasterMsg[3], switchMasterMsg[4]));
                                    List<HostAndPort> newHostMasters = new ArrayList<HostAndPort>();
                                    for (int i = 0; i < masters.size(); i++) {
                                        newHostMasters.add(null);
                                    }
                                    Collections.copy(newHostMasters, currentHostMasters);
                                    newHostMasters.set(index, newHostMaster);

                                    initPool(newHostMasters);
                                } else {
                                    StringBuffer sb = new StringBuffer();
                                    for (String masterName : masters) {
                                        sb.append(masterName);
                                        sb.append(",");
                                    }
                                    log.fine("Ignoring message on +switch-master for master name "
                                            + switchMasterMsg[0]
                                            + ", our monitor master name are ["
                                            + sb + "]");
                                }

                            } else {
                                log.severe("Invalid message received on Sentinel "
                                        + host
                                        + ":"
                                        + port
                                        + " on channel +switch-master: "
                                        + message);
                            }
                        }
                    }, "+switch-master");

                } catch (JedisConnectionException e) {

                    if (running.get()) {
                        log.log(Level.SEVERE, "Lost connection to Sentinel at " + host + ":" + port
                                + ". Sleeping 5000ms and retrying.", e);
                        try {
                            Thread.sleep(subscribeRetryWaitTimeMillis);
                        } catch (InterruptedException e1) {
                            log.log(Level.SEVERE, "Sleep interrupted: ", e1);
                        }
                    } else {
                        log.fine("Unsubscribing from Sentinel at " + host + ":" + port);
                    }
                } finally {
                    j.close();
                }
            }
        }

        public void shutdown() {
            try {
                log.fine("Shutting down listener on " + host + ":" + port);
                running.set(false);
                // This isn't good, the Jedis object is not thread safe
                if (j != null) {
                    j.disconnect();
                }
            } catch (Exception e) {
                log.log(Level.SEVERE, "Caught exception while shutting down: ", e);
            }
        }
    }


}
