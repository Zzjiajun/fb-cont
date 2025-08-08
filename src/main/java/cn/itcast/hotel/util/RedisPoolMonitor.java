package cn.itcast.hotel.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Redis连接池监控工具类
 * 用于监控连接池性能和连接状态
 */
@Component
@Slf4j
public class RedisPoolMonitor {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    private ScheduledExecutorService scheduler;

    @PostConstruct
    public void init() {
        // 启动连接池监控
        startPoolMonitoring();
    }

    /**
     * 启动连接池监控
     */
    public void startPoolMonitoring() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::monitorPoolStatus, 0, 60, TimeUnit.SECONDS);
        log.info("Redis连接池监控已启动");
    }

    /**
     * 监控连接池状态
     */
    private void monitorPoolStatus() {
        try {
            if (redisConnectionFactory instanceof LettuceConnectionFactory) {
                LettuceConnectionFactory lettuceFactory = (LettuceConnectionFactory) redisConnectionFactory;
                
                // 获取连接池统计信息
                log.info("=== Redis连接池状态监控 ===");
                log.info("连接工厂类型: {}", lettuceFactory.getClass().getSimpleName());
                log.info("连接超时: {}ms", lettuceFactory.getTimeout());
                
                // 测试连接
                boolean isConnected = testConnection();
                log.info("Redis连接状态: {}", isConnected ? "正常" : "异常");
                
                if (!isConnected) {
                    log.warn("Redis连接异常，请检查配置");
                }
            }
        } catch (Exception e) {
            log.error("Redis连接池监控异常", e);
        }
    }

    /**
     * 测试Redis连接
     */
    public boolean testConnection() {
        try {
            redisConnectionFactory.getConnection().ping();
            return true;
        } catch (Exception e) {
            log.error("Redis连接测试失败", e);
            return false;
        }
    }

    /**
     * 获取连接池性能指标
     */
    public void getPoolMetrics() {
        try {
            log.info("=== Redis连接池性能指标 ===");
            log.info("当前时间: {}", new java.util.Date());
            
            // 测试连接性能
            long startTime = System.currentTimeMillis();
            testConnection();
            long endTime = System.currentTimeMillis();
            
            log.info("连接响应时间: {}ms", endTime - startTime);
            
            if (endTime - startTime > 100) {
                log.warn("Redis连接响应时间过长: {}ms", endTime - startTime);
            }
            
        } catch (Exception e) {
            log.error("获取连接池指标失败", e);
        }
    }

    /**
     * 停止监控
     */
    public void stopMonitoring() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            log.info("Redis连接池监控已停止");
        }
    }
} 