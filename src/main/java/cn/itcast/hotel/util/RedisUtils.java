package cn.itcast.hotel.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class RedisUtils {

    @Resource
    private RedisTemplate redisTemplate;

    private static final String CACHE_KEY_SEPARATOR = ".";

    /**
     * 构建缓存key
     * 这种方法常用于构建缓存键或者数据库查询的键，将多个字符串组合成一个唯一的键
     */
    public String buildKey(String... strObjs) {
        return Stream.of(strObjs).collect(Collectors.joining(CACHE_KEY_SEPARATOR));
    }

    /**
     * 是否存在key
     */
    public boolean exist(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 删除key
     */
    public boolean del(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * set(不带过期)
     */
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * set(带过期)
     */
    public boolean setNx(String key, String value, Long time, TimeUnit timeUnit) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, time, timeUnit);
    }

    /**
     * 获取string类型缓存
     */
    public String get(String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }

    /**
     * 批量获取string类型缓存 - 使用Pipeline优化性能
     * @param keys 要获取的key列表
     * @return 对应的value列表，顺序与keys一致
     */
    public List<String> mget(List<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 批量获取string类型缓存 - 使用Pipeline优化性能
     * @param keys 要获取的key数组
     * @return 对应的value列表，顺序与keys一致
     */
    public List<String> mget(String... keys) {
        return redisTemplate.opsForValue().multiGet(java.util.Arrays.asList(keys));
    }

    public Boolean zAdd(String key, String value, Long score) {
        return redisTemplate.opsForZSet().add(key, value, Double.valueOf(String.valueOf(score)));
    }

    public Long countZset(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    public Set<String> rangeZset(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    public Long removeZset(String key, Object value) {
        return redisTemplate.opsForZSet().remove(key, value);
    }

    public void removeZsetList(String key, Set<String> value) {
        value.stream().forEach((val) -> redisTemplate.opsForZSet().remove(key, val));
    }

    public Double score(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    public Set<String> rangeByScore(String key, long start, long end) {
        return redisTemplate.opsForZSet().rangeByScore(key, Double.valueOf(String.valueOf(start)), Double.valueOf(String.valueOf(end)));
    }

    public Object addScore(String key, Object obj, double score) {
        return redisTemplate.opsForZSet().incrementScore(key, obj, score);
    }

    public Object rank(String key, Object obj) {
        return redisTemplate.opsForZSet().rank(key, obj);
    }


    public void setEx(String cacheKey, String country, int i) {
        redisTemplate.opsForHash().put(cacheKey, country, i);
    }

    public Boolean delete(String cacheKey) {
        Boolean delete = redisTemplate.delete(cacheKey);
        return delete;
    }

    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 批量设置缓存 - 使用Pipeline优化性能
     * @param keyValueMap key-value映射
     */
    public void mset(Map<String, String> keyValueMap) {
        redisTemplate.opsForValue().multiSet(keyValueMap);
    }

    /**
     * 批量删除缓存 - 使用Pipeline优化性能
     * @param keys 要删除的key列表
     * @return 删除成功的数量
     */
    public Long mdel(List<String> keys) {
        return redisTemplate.delete(keys);
    }

    /**
     * 批量删除缓存 - 使用Pipeline优化性能
     * @param keys 要删除的key数组
     * @return 删除成功的数量
     */
    public Long mdel(String... keys) {
        return redisTemplate.delete(java.util.Arrays.asList(keys));
    }

    /**
     * 设置缓存并设置过期时间
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    public void setEx(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取并删除缓存
     * @param key 键
     * @return 值
     */
    public String getAndDelete(String key) {
        String value = (String) redisTemplate.opsForValue().get(key);
        if (value != null) {
            redisTemplate.delete(key);
        }
        return value;
    }

    /**
     * 原子递增
     * @param key 键
     * @param delta 增量
     * @return 递增后的值
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 原子递减
     * @param key 键
     * @param delta 减量
     * @return 递减后的值
     */
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    /**
     * 设置Hash缓存
     * @param key 键
     * @param hashKey hash键
     * @param value 值
     */
    public void hset(String key, String hashKey, String value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * 获取Hash缓存
     * @param key 键
     * @param hashKey hash键
     * @return 值
     */
    public String hget(String key, String hashKey) {
        return (String) redisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * 批量获取Hash缓存
     * @param key 键
     * @param hashKeys hash键列表
     * @return 值列表
     */
    public List<String> hmget(String key, List<String> hashKeys) {
        return redisTemplate.opsForHash().multiGet(key, hashKeys);
    }

    /**
     * 设置Hash缓存（带过期时间）
     * @param key 键
     * @param hashKey hash键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    public void hsetEx(String key, String hashKey, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForHash().put(key, hashKey, value);
        redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 检查Hash缓存是否存在
     * @param key 键
     * @param hashKey hash键
     * @return 是否存在
     */
    public Boolean hExists(String key, String hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    /**
     * 获取Hash缓存的所有键
     * @param key 键
     * @return 所有hash键
     */
    public Set<Object> hKeys(String key) {
        return redisTemplate.opsForHash().keys(key);
    }

    /**
     * 获取Hash缓存的所有值
     * @param key 键
     * @return 所有值
     */
    public List<Object> hValues(String key) {
        return redisTemplate.opsForHash().values(key);
    }

    /**
     * 获取Hash缓存的大小
     * @param key 键
     * @return 大小
     */
    public Long hSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    /**
     * 删除Hash缓存
     * @param key 键
     * @param hashKeys hash键列表
     * @return 删除成功的数量
     */
    public Long hDelete(String key, Object... hashKeys) {
        return redisTemplate.opsForHash().delete(key, hashKeys);
    }
}
