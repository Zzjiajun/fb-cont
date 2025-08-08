package cn.itcast.hotel.util;

import cn.itcast.hotel.po.DeviceDetectorPo;
import com.deevvi.device.detector.engine.api.DeviceDetectorParser;
import com.deevvi.device.detector.engine.api.DeviceDetectorResult;
import com.google.gson.Gson;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class Deevvi {
    private static final Logger logger = LoggerFactory.getLogger(Deevvi.class);
    
    // 单例实例
    private static volatile Deevvi instance;
    
    // 设备检测解析器
    private volatile DeviceDetectorParser parser;
    
    // 用户代理缓存
    private final ConcurrentHashMap<String, DeviceDetectorPo> userAgentCache;
    
    // 线程池
    private final ScheduledExecutorService scheduledExecutorService;
    private final ExecutorService executorService;
    
    // 信号量用于限制并发
    private final Semaphore semaphore;
    
    // 限流器
    private final RateLimiter rateLimiter;
    
    // 熔断器状态
    private final AtomicBoolean circuitBreakerOpen = new AtomicBoolean(false);
    private final AtomicBoolean circuitBreakerHalfOpen = new AtomicBoolean(false);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    
    // 系统容量配置
    private static final int CORE_POOL_SIZE = 4; // 4核CPU
    private static final int MAX_POOL_SIZE = 16; // 最大线程数设为8
    private static final int QUEUE_CAPACITY = 6000; // 队列容量增加到3000
    private static final int MAX_CONCURRENT_PARSING = 12; // 最大并发解析数
    private static final int MAX_REQUESTS_PER_SECOND = 1500; // 每秒最大请求数提升到1500
    private static final int CACHE_SIZE = 100000; // 缓存大小增加到100000
    
    // 缓存过期时间（毫秒）
    private static final long CACHE_EXPIRY_TIME = 10800000; // 3小时
    
    // 服务状态
    private final AtomicBoolean isServiceHealthy = new AtomicBoolean(true);
    
    // 重试次数
    private static final int MAX_RETRY_ATTEMPTS = 2; // 保持2次重试
    
    // 重试延迟（毫秒）
    private static final long RETRY_DELAY = 500; // 保持500ms重试延迟
    
    // 健康检查间隔（毫秒）
    private static final long HEALTH_CHECK_INTERVAL = 15000; // 15秒
    
    // 熔断器配置
    private static final long CIRCUIT_BREAKER_TIMEOUT = 15000; // 15秒
    private static final int CIRCUIT_BREAKER_THRESHOLD = 10; // 10次失败后触发熔断
    private static final int CIRCUIT_BREAKER_SUCCESS_THRESHOLD = 5; // 5次成功请求后关闭熔断
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    
    // 恢复策略配置
    private static final long INITIAL_RECOVERY_DELAY = 3000; // 初始恢复延迟3秒
    private static final long MAX_RECOVERY_DELAY = 30000; // 最大恢复延迟30秒
    private static final double RECOVERY_BACKOFF_MULTIPLIER = 1.2; // 保持1.2的恢复延迟增长倍数
    private long currentRecoveryDelay = INITIAL_RECOVERY_DELAY;
    
    // 性能监控
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final AtomicLong rejectedRequests = new AtomicLong(0);
    
    private Deevvi() {
        this.parser = DeviceDetectorParser.getClient();
        this.userAgentCache = new ConcurrentHashMap<>(CACHE_SIZE);
        
        // 创建定时任务线程池
        this.scheduledExecutorService = Executors.newScheduledThreadPool(1, r -> {
            Thread thread = new Thread(r);
            thread.setName("health-check-thread");
            thread.setPriority(Thread.MIN_PRIORITY); // 保持最低优先级
            return thread;
        });
        
        // 创建普通任务线程池
        this.executorService = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            30L, TimeUnit.SECONDS, // 保持30秒线程空闲时间
            new LinkedBlockingQueue<>(QUEUE_CAPACITY),
            new ThreadFactory() {
                private final AtomicInteger counter = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setName("device-detector-" + counter.getAndIncrement());
                    thread.setPriority(Thread.NORM_PRIORITY);
                    return thread;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        // 预热线程池
        for (int i = 0; i < CORE_POOL_SIZE; i++) {
            executorService.submit(() -> {
                try {
                    Thread.sleep(100); // 恢复预热时间
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        this.semaphore = new Semaphore(MAX_CONCURRENT_PARSING);
        this.rateLimiter = new RateLimiter(MAX_REQUESTS_PER_SECOND);
        
        // 启动健康检查
        startHealthCheck();
        
        // 启动性能监控
        startPerformanceMonitoring();
    }
    
    @PostConstruct
    public void init() {
        if (instance == null) {
            synchronized (Deevvi.class) {
                if (instance == null) {
                    instance = this;
                }
            }
        }
    }
    
    private void startHealthCheck() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                checkServiceHealth();
            } catch (Exception e) {
                logger.error("健康检查失败", e);
            }
        }, 0, HEALTH_CHECK_INTERVAL, TimeUnit.MILLISECONDS);
    }
    
    private void checkServiceHealth() {
        try {
            // 使用一个简单的User-Agent进行测试
            String testUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
            DeviceDetectorResult result = parser.parse(testUserAgent);
            if (result != null) {
                isServiceHealthy.set(true);
                logger.info("服务健康检查通过");
                
                // 如果服务恢复健康，重置恢复延迟
                currentRecoveryDelay = INITIAL_RECOVERY_DELAY;
            } else {
                isServiceHealthy.set(false);
                logger.error("服务健康检查失败：解析结果为空");
            }
        } catch (Exception e) {
            isServiceHealthy.set(false);
            logger.error("服务健康检查失败", e);
            // 尝试重新初始化解析器
            reinitializeParser();
        }
    }
    
    private void reinitializeParser() {
        try {
            DeviceDetectorParser newParser = DeviceDetectorParser.getClient();
            if (newParser != null) {
                this.parser = newParser;
                isServiceHealthy.set(true);
                logger.info("解析器重新初始化成功");
            }
        } catch (Exception e) {
            logger.error("解析器重新初始化失败", e);
        }
    }
    
    public static Deevvi getInstance() {
        if (instance == null) {
            synchronized (Deevvi.class) {
                if (instance == null) {
                    instance = new Deevvi();
                }
            }
        }
        return instance;
    }
    
    private void startPerformanceMonitoring() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            logger.info("性能监控 - 总请求数: {}, 成功请求: {}, 失败请求: {}, 拒绝请求: {}, 活跃线程数: {}, 队列大小: {}",
                totalRequests.get(),
                successfulRequests.get(),
                failedRequests.get(),
                rejectedRequests.get(),
                ((ThreadPoolExecutor) executorService).getActiveCount(),
                ((ThreadPoolExecutor) executorService).getQueue().size()
            );
        }, 0, 60, TimeUnit.SECONDS);
    }
    
    /**
     * 解析用户代理字符串并返回设备检测结果PO对象
     */
    public DeviceDetectorPo parseUserAgent(String userAgent) {
        totalRequests.incrementAndGet();
        
        if (userAgent == null || userAgent.trim().isEmpty()) {
            failedRequests.incrementAndGet();
            return new DeviceDetectorPo();
        }
        
        // 检查缓存
        DeviceDetectorPo cachedResult = userAgentCache.get(userAgent);
        if (cachedResult != null) {
            successfulRequests.incrementAndGet();
            return cachedResult;
        }
        
        // 检查熔断器状态
        if (circuitBreakerOpen.get()) {
            long now = System.currentTimeMillis();
            if (now - lastFailureTime.get() > currentRecoveryDelay) {
                circuitBreakerHalfOpen.set(true);
                circuitBreakerOpen.set(false);
                logger.info("熔断器进入半开状态，尝试恢复服务");
            } else {
                rejectedRequests.incrementAndGet();
                logger.warn("熔断器开启，返回空结果");
                return new DeviceDetectorPo();
            }
        }
        
        // 限流检查
        if (!rateLimiter.tryAcquire()) {
            rejectedRequests.incrementAndGet();
            logger.warn("请求超过QPS限制，返回空结果");
            return new DeviceDetectorPo();
        }
        
        // 检查服务健康状态
        if (!isServiceHealthy.get()) {
            failedRequests.incrementAndGet();
            logger.warn("服务不健康，返回空结果");
            return new DeviceDetectorPo();
        }
        
        int retryCount = 0;
        while (retryCount < MAX_RETRY_ATTEMPTS) {
            try {
                // 获取信号量许可
                if (!semaphore.tryAcquire(100, TimeUnit.MILLISECONDS)) {
                    rejectedRequests.incrementAndGet();
                    logger.warn("获取信号量超时，返回空结果");
                    return new DeviceDetectorPo();
                }
                
                // 异步解析
                Future<DeviceDetectorPo> future = executorService.submit(() -> {
                    try {
                        DeviceDetectorPo deviceDetectorPo = new DeviceDetectorPo();
                        DeviceDetectorResult result = parser.parse(userAgent);
                        String json = result.toJSON();
                        
                        // 将JSON转换为DeviceDetectorPo对象
                        Gson gson = new Gson();
                        deviceDetectorPo = gson.fromJson(json, DeviceDetectorPo.class);
                        
                        // 设置额外的属性
                        deviceDetectorPo.setFound(result.found());
                        deviceDetectorPo.setIsMobile(result.isMobileDevice());
                        deviceDetectorPo.setIsBot(result.isBot());
                        deviceDetectorPo.setUserAgent(userAgent);
                        
                        // 放入缓存
                        userAgentCache.put(userAgent, deviceDetectorPo);
                        
                        // 处理半开状态
                        if (circuitBreakerHalfOpen.get()) {
                            int successes = successCount.incrementAndGet();
                            if (successes >= CIRCUIT_BREAKER_SUCCESS_THRESHOLD) {
                                circuitBreakerHalfOpen.set(false);
                                failureCount.set(0);
                                successCount.set(0);
                                logger.info("服务已完全恢复");
                            }
                        }
                        
                        successfulRequests.incrementAndGet();
                        return deviceDetectorPo;
                    } catch (Exception e) {
                        failedRequests.incrementAndGet();
                        logger.error("解析用户代理失败: " + userAgent, e);
                        throw e;
                    }
                });
                
                return future.get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                failedRequests.incrementAndGet();
                logger.error("处理用户代理时发生错误: " + userAgent, e);
                retryCount++;
                
                // 处理半开状态下的失败
                if (circuitBreakerHalfOpen.get()) {
                    circuitBreakerHalfOpen.set(false);
                    circuitBreakerOpen.set(true);
                    lastFailureTime.set(System.currentTimeMillis());
                    currentRecoveryDelay = Math.min((long)(currentRecoveryDelay * RECOVERY_BACKOFF_MULTIPLIER), MAX_RECOVERY_DELAY);
                    logger.error("半开状态下请求失败，重新触发熔断，新的恢复延迟: {}ms", currentRecoveryDelay);
                }
                
                // 增加失败计数
                int failures = failureCount.incrementAndGet();
                if (failures >= CIRCUIT_BREAKER_THRESHOLD) {
                    circuitBreakerOpen.set(true);
                    lastFailureTime.set(System.currentTimeMillis());
                    logger.error("触发熔断器，服务暂时不可用");
                }
                
                if (retryCount < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_DELAY);
                        reinitializeParser();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } finally {
                semaphore.release();
            }
        }
        
        isServiceHealthy.set(false);
        return new DeviceDetectorPo();
    }
    
    /**
     * 清理过期缓存
     */
    public void clearExpiredCache() {
        userAgentCache.clear();
    }
    
    /**
     * 关闭资源
     */
    public void shutdown() {
        executorService.shutdown();
        scheduledExecutorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            if (!scheduledExecutorService.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduledExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            scheduledExecutorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 获取服务健康状态
     */
    public boolean isServiceHealthy() {
        return isServiceHealthy.get();
    }
    
    // 测试方法
    public static void main(String[] args) throws IOException {
        Deevvi deevvi = Deevvi.getInstance();
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36";
        
        DeviceDetectorPo result = deevvi.parseUserAgent(userAgent);
        System.out.println("设备检测结果: " + result);
        
        // 测试并发
        for (int i = 0; i < 20; i++) {
            final int index = i;
            new Thread(() -> {
                DeviceDetectorPo po = deevvi.parseUserAgent(userAgent);
                System.out.println("线程 " + index + " 结果: " + po);
            }).start();
        }
        
        // 关闭资源
        deevvi.shutdown();
    }
    
    // 简单的限流器实现
    private static class RateLimiter {
        private final int maxRequestsPerSecond;
        private final AtomicLong lastRequestTime = new AtomicLong(0);
        private final AtomicInteger requestCount = new AtomicInteger(0);
        
        public RateLimiter(int maxRequestsPerSecond) {
            this.maxRequestsPerSecond = maxRequestsPerSecond;
        }
        
        public boolean tryAcquire() {
            long now = System.currentTimeMillis();
            long last = lastRequestTime.get();
            
            if (now - last >= 1000) {
                // 重置计数器
                requestCount.set(0);
                lastRequestTime.set(now);
            }
            
            return requestCount.incrementAndGet() <= maxRequestsPerSecond;
        }
    }
}
