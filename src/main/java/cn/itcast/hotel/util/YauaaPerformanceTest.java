package cn.itcast.hotel.util;

import cn.itcast.hotel.po.DeviceDetectorPo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * YAUAA性能测试类
 * 对比YAUAA和Deevvi的性能差异
 */
@Component
@Slf4j
public class YauaaPerformanceTest {
    
    @Autowired
    private YauaaAdapter yauaaAdapter;
    
    @Autowired
    private Deevvi deevvi;


    /**
     * 性能对比测试
     */
    public void performanceComparisonTest() {
        log.info("开始YAUAA vs Deevvi性能对比测试...");
        
        // 测试用的User-Agent
        String[] testUserAgents = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (Linux; Android 13; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Mobile Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36",
            "Mozilla/5.0 (iPad; CPU OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1"
        };
        
        // 单次解析测试
        singleParseTest(testUserAgents);
        
        // 并发解析测试
        concurrentParseTest(testUserAgents);
        
        // 缓存效果测试
        cacheEffectTest(testUserAgents);
        
        log.info("性能对比测试完成");
    }
    
    /**
     * 单次解析测试
     */
    private void singleParseTest(String[] testUserAgents) {
        log.info("=== 单次解析测试 ===");
        
        for (String userAgent : testUserAgents) {
            log.info("测试User-Agent: {}", userAgent);
            
            // YAUAA测试
            long yauaaStart = System.nanoTime();
            DeviceDetectorPo yauaaResult = yauaaAdapter.parseUserAgent(userAgent);
            long yauaaTime = System.nanoTime() - yauaaStart;
            
            // Deevvi测试
            long deevviStart = System.nanoTime();
            DeviceDetectorPo deevviResult = deevvi.parseUserAgent(userAgent);
            long deevviTime = System.nanoTime() - deevviStart;
            
            log.info("YAUAA解析时间: {}μs, 结果: {}", yauaaTime / 1000, yauaaResult.getIsMobile());
            log.info("Deevvi解析时间: {}μs, 结果: {}", deevviTime / 1000, deevviResult.getIsMobile());
            log.info("性能提升: {}倍", (double) deevviTime / yauaaTime);
            log.info("---");
        }
    }
    
    /**
     * 并发解析测试
     */
    private void concurrentParseTest(String[] testUserAgents) {
        log.info("=== 并发解析测试 ===");
        
        int threadCount = 100;
        int iterations = 1000;
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        // YAUAA并发测试
        long yauaaStart = System.currentTimeMillis();
        CompletableFuture<?>[] yauaaFutures = new CompletableFuture[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            yauaaFutures[i] = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < iterations; j++) {
                    String userAgent = testUserAgents[j % testUserAgents.length];
                    yauaaAdapter.parseUserAgent(userAgent);
                }
            }, executor);
        }
        
        CompletableFuture.allOf(yauaaFutures).join();
        long yauaaTime = System.currentTimeMillis() - yauaaStart;
        
        // Deevvi并发测试
        long deevviStart = System.currentTimeMillis();
        CompletableFuture<?>[] deevviFutures = new CompletableFuture[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            deevviFutures[i] = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < iterations; j++) {
                    String userAgent = testUserAgents[j % testUserAgents.length];
                    deevvi.parseUserAgent(userAgent);
                }
            }, executor);
        }
        
        CompletableFuture.allOf(deevviFutures).join();
        long deevviTime = System.currentTimeMillis() - deevviStart;
        
        executor.shutdown();
        
        log.info("YAUAA并发测试: {}线程, {}次迭代, 耗时: {}ms, QPS: {}", 
                threadCount, iterations, yauaaTime, (threadCount * iterations * 1000L) / yauaaTime);
        log.info("Deevvi并发测试: {}线程, {}次迭代, 耗时: {}ms, QPS: {}", 
                threadCount, iterations, deevviTime, (threadCount * iterations * 1000L) / deevviTime);
        log.info("YAUAA性能提升: {}倍", (double) deevviTime / yauaaTime);
    }
    
    /**
     * 缓存效果测试
     */
    private void cacheEffectTest(String[] testUserAgents) {
        log.info("=== 缓存效果测试 ===");
        
        // 清理缓存
        yauaaAdapter.clearCache();
        
        // 第一次解析（无缓存）
        long firstStart = System.nanoTime();
        for (String userAgent : testUserAgents) {
            yauaaAdapter.parseUserAgent(userAgent);
        }
        long firstTime = System.nanoTime() - firstStart;
        
        // 第二次解析（有缓存）
        long secondStart = System.nanoTime();
        for (String userAgent : testUserAgents) {
            yauaaAdapter.parseUserAgent(userAgent);
        }
        long secondTime = System.nanoTime() - secondStart;
        
        log.info("首次解析（无缓存）: {}μs", firstTime / 1000);
        log.info("再次解析（有缓存）: {}μs", secondTime / 1000);
        log.info("缓存效果提升: {}倍", (double) firstTime / secondTime);
        log.info("缓存统计: {}", yauaaAdapter.getPerformanceStats());
    }
    
    /**
     * 内存使用测试
     */
    public void memoryUsageTest() {
        log.info("=== 内存使用测试 ===");
        
        Runtime runtime = Runtime.getRuntime();
        
        // 测试前内存
        long beforeMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // 大量解析测试
        for (int i = 0; i < 10000; i++) {
            String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36";
            yauaaAdapter.parseUserAgent(userAgent + "_" + i);
        }
        
        // 测试后内存
        long afterMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = afterMemory - beforeMemory;
        
        log.info("内存使用: {}MB", memoryUsed / 1024 / 1024);
        log.info("缓存大小: {}", yauaaAdapter.getCacheSize());
    }
} 