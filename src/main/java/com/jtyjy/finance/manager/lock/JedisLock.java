package com.jtyjy.finance.manager.lock;

/**
 * Description:
 * Created by ZiYao Lee on 2022/09/21.
 * Time: 11:34
 */
import com.jtyjy.core.redis.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.Random;
import java.util.concurrent.TimeUnit;


/**
 * @author: Max
 * @time: 2022/6/29
 * @description: Jedis 锁机制的实现
 */
@Slf4j
public class JedisLock {
    /** 虚拟key的后缀 */
    private static final String PREFIX = "_lock";

    /** 加锁标志 */
    public static final String LOCKED = "TRUE";

    /** 毫秒与毫微秒的换算单位 1毫秒 = 1000000毫微秒 */
    public static final long MILLI_NANO_CONVERSION = 1000 * 1000L;

    /** 默认超时时间（毫秒） */
    public static final long DEFAULT_TIME_OUT = 1000;

    public static final Random RANDOM = new Random();

    /** 锁的超时时间（秒），过期删除 */
    public static final int EXPIRE = 20 ;

    private StringRedisTemplate redisTemplate;
    private RedisClient redisClient;

    /** 虚拟key，用于实现锁的key ,组合值：key_lock */
    private String key = "";

    /** 锁状态标志 */
    private boolean locked = false;

    /**
     * @Author：Max
     * @Description：构造函数：创建JedisLock对象
     * @Date：2022/6/29
     * @Param：[key]  key 需要加锁的key
     * @Return：
     */
    public JedisLock(String key) {
        this.key = key + PREFIX;
        this.redisClient = SpringFactory.getBean(RedisClient.class);
    }

    /**
     * @Author：Max
     * @Description：对指定的key进行加锁 说明：应该以：  try { if(lock()){doSomething(); }} finally { unlock(); }
     * @Date：2022/6/29
     * @Param：[]
     * @Return：boolean   返回加锁的状态：true 成功 | false 失败
     */
    public boolean lock() {
        return lock(DEFAULT_TIME_OUT);
    }

    /**
     * @Author：Max
     * @Description：对指定的key进行加锁 说明：应该以：  try { if(lock()){doSomething(); }} finally { unlock(); }
     * @Date：2022/6/29
     * @Param：[timeout]  设置超时时间
     * @Return：boolean  返回加锁的状态：true 成功 | false 失败
     */
    public  boolean lock(long timeout) {
        return lock(timeout,EXPIRE);
    }

    /**
     * @Author：Max
     * @Description：对指定的key进行加锁 说明：应该以：  try { if(lock()){doSomething(); }} finally { unlock(); }
     * @Date：2022/6/29
     * @Param：[timeout, expire]  设置超时时间
     * timeout 获取锁的超时时间
     * expire 锁的超时时间（秒），过期删除
     * @Return：boolean  返回加锁的状态：true 成功 | false 失败
     */
    public  boolean lock(long timeout, int expire) {
        long nano = System.nanoTime();
        timeout *= MILLI_NANO_CONVERSION;
        try {
            while ((System.nanoTime() - nano) < timeout) {
                if (!this.redisClient.exist(this.key)) {
                    this.redisClient.set(this.key, LOCKED);
                    this.redisClient.expire(this.key, expire);
                    this.locked = true;
                    log.debug("Redis Locked加锁过程,对"+this.key+"成功加锁!!");
                    return this.locked;
                }
                // 短暂休眠，避免出现活锁
                Thread.sleep(20, RANDOM.nextInt(500));
            }
        } catch (Exception e) {
            throw new RuntimeException("Jedis 在加锁过程中key:" + this.key+ "进行加锁失败!!!", e);
        }
        return false;
    }

    /**
     * @Author：Max
     * @Description：解锁 无论是否加锁成功，都需要调用unlock  try { if(lock()){doSomething(); }} finally { unlock(); }
     * @Date：2022/6/29
     * @Param：[]
     * @Return：void
     */
    public  void unlock() {
        synchronized (this) {
            try {
                if (this.locked && this.isExits()) {
                    this.redisClient.delete(this.key);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @Author：Max
     * @Description：判断当前键是否被锁
     * @Date：2022/6/29
     * @Param：[]
     * @Return：boolean
     */
    public synchronized  boolean isExits(){
        try {
            return this.redisClient.exist(key);
        } catch (Exception e) {
            log.error("Jedis Lock 判断当前key:"+this.key+"是否获取锁失败！！",e);
        }
        return false;
    }
}
