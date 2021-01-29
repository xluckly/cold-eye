package com.cold.common.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.util.SafeEncoder;

import java.io.Serializable;

/**
 * @author hanqingkuo
 * @date 2021/1/29 16:10
 */
public class JedisUtil implements Serializable {

    /**
     * 缓存时间
     */
    private final int expire = 2592000;

    /**
     * 操作Key的方法
     */
    public Keys KEYS;


    private static JedisPool jedisPool = null;
    private volatile static JedisUtil jedisUtil = null;
    private JedisUtil() {}
    static {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100);
        config.setMaxIdle(50);
        config.setMinIdle(10);
        config.setMaxWaitMillis(30000);
        config.setTestOnBorrow(true);
        jedisPool = new JedisPool(config, "localhost", 7379);
    }

    public JedisPool getPool() {return jedisPool;}
    public static JedisUtil getInstance() {
        if (jedisUtil == null) {
            synchronized(JedisUtil.class) {
                if (jedisUtil == null) {
                    jedisUtil = new JedisUtil();
                }

            }
        }
        return jedisUtil;
    }

    public Jedis getJedis() {return jedisPool.getResource();}
    public static void close(Jedis jedis) {
        if (null != jedis) {
            jedis.close();
        }
    }

    public void returnJedis(Jedis jedis) {
        if (null != jedis && null != jedisPool) {
            // 替代方法   jedis.close();
            jedisPool.returnResource(jedis);
        }
    }
    public void expire(String key, int seconds) {
        if (seconds <= 0) {
            return;
        }
        Jedis jedis = getJedis();
        jedis.expire(key, seconds);
        returnJedis(jedis);
    }
    public void expire(String key) {
        expire(key, expire);
    }

    public class Keys {
        /**
         * 清空所有的key
         */
        public String flushAll() {
            Jedis jedis = getJedis();
            String stata = jedis.flushAll();
            returnJedis(jedis);
            return stata;
        }
        /**
         * 更改key
         *
         * @param oldkey
         * @param newkey
         * @return 状态码
         */
        public String rename(String oldkey, String newkey) {
            return rename(SafeEncoder.encode(oldkey), SafeEncoder.encode(newkey));
        }

        /**
         * 更改key
         * @param oldkey
         * @param newkey
         * @return
         */

        public String rename(byte[] oldkey, byte[] newkey) {
            Jedis jedis = getJedis();
            String status = jedis.rename(oldkey, newkey);
            returnJedis(jedis);
            return status;
        }

        public long renamenx(String oldkey, String newkey) {
            Jedis jedis = getJedis();
            long status = jedis.renamenx(oldkey, newkey);
            returnJedis(jedis);
            return status;
        }
        /**
         * 设置key的过期时间，以秒为单位
         *
         * @param key
         * @param seconds,已秒为单位
         * @return 影响的记录数
         */
        public long expired(String key, int seconds) {
            Jedis jedis = getJedis();
            long count = jedis.expire(key, seconds);
            returnJedis(jedis);
            return count;
        }

        /**
         * 查询key的过期时间
         *
         * @param key
         * @return 以秒为单位的时间表示
         */
        public long ttl(String key) {
            //ShardedJedis sjedis = getShardedJedis();
            Jedis sjedis = getJedis();
            long len = sjedis.ttl(key);
            returnJedis(sjedis);
            return len;
        }

        /**
         * 取消对key的过期设置
         * @param key
         * @return
         */
        public long persist(String key) {
            Jedis jedis = getJedis();
            long count = jedis.persist(key);
            returnJedis(jedis);
            return count;
        }

        /**
         * 删除key
         * @param keys
         * @return
         */
        public long del(String... keys) {
            Jedis jedis = getJedis();
            long count = jedis.del(keys);
            returnJedis(jedis);
            return count;
        }
    }
}
