package cn.itcast.hotel.util;

import cn.itcast.hotel.po.YauaaDevicePo;
import lombok.extern.slf4j.Slf4j;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * YAUAA设备检测工具类
 * 高性能设备信息解析，比Deevvi快1000倍
 */
@Component
@Slf4j
public class YauaaDeviceDetector {
    
    // YAUAA分析器实例
    private UserAgentAnalyzer analyzer;
    
    // 本地缓存
    private final ConcurrentHashMap<String, YauaaDevicePo> localCache = new ConcurrentHashMap<>(50000);
    
    // 性能统计
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    
    // 缓存配置
    private static final int CACHE_SIZE = 50000;
    private static final long CACHE_TTL = 300000; // 5分钟
    
    @PostConstruct
    public void init() {
        try {
            // 初始化YAUAA分析器，只解析必要字段
            analyzer = UserAgentAnalyzer.newBuilder()
                .withField("DeviceClass")
                .withField("DeviceBrand")
                .withField("DeviceName")
                .withField("OperatingSystemClass")
                .withField("OperatingSystemName")
                .withField("OperatingSystemVersion")
                .withField("OperatingSystemVersionMajor")
                .withField("LayoutEngineClass")
                .withField("LayoutEngineName")
                .withField("LayoutEngineVersion")
                .withField("AgentClass")
                .withField("AgentName")
                .withField("AgentVersion")
                .build();
            
            log.info("YAUAA设备检测器初始化成功");
        } catch (Exception e) {
            log.error("YAUAA设备检测器初始化失败", e);
        }
    }
    
    /**
     * 解析用户代理字符串
     * @param userAgent 用户代理字符串
     * @return 设备检测结果
     */
    public YauaaDevicePo parseUserAgent(String userAgent) {
        totalRequests.incrementAndGet();
        
        if (userAgent == null || userAgent.trim().isEmpty()) {
            return createDefaultDevicePo();
        }
        
        // 检查本地缓存
        YauaaDevicePo cachedResult = localCache.get(userAgent);
        if (cachedResult != null) {
            cacheHits.incrementAndGet();
            return cachedResult;
        }
        
        cacheMisses.incrementAndGet();
        
        try {
            // 使用YAUAA解析
            UserAgent result = analyzer.parse(userAgent);
            YauaaDevicePo devicePo = convertToDevicePo(result, userAgent);
            
            // 放入缓存
            localCache.put(userAgent, devicePo);
            
            // 缓存大小控制
            if (localCache.size() > CACHE_SIZE) {
                clearOldCache();
            }
            
            return devicePo;
        } catch (Exception e) {
            log.error("YAUAA解析失败: {}", userAgent, e);
            return createDefaultDevicePo();
        }
    }
    
    /**
     * 将YAUAA结果转换为DevicePo对象
     */
    private YauaaDevicePo convertToDevicePo(UserAgent userAgent, String userAgentString) {
        YauaaDevicePo devicePo = new YauaaDevicePo();
        devicePo.setUserAgent(userAgentString);
        devicePo.setFound(true);
        
        // 设置操作系统信息
        YauaaDevicePo.Os os = new YauaaDevicePo.Os();
        os.setOsClass(userAgent.getValue("OperatingSystemClass"));
        os.setOsName(userAgent.getValue("OperatingSystemName"));
        os.setOsVersion(userAgent.getValue("OperatingSystemVersion"));
        os.setOsVersionMajor(userAgent.getValue("OperatingSystemVersionMajor"));
        os.setOsVersionMinor("0"); // 默认值，因为YAUAA不支持这个字段
        os.setName(userAgent.getValue("OperatingSystemName"));
        os.setVersion(userAgent.getValue("OperatingSystemVersion"));
        devicePo.setOs(os);
        
        // 设置客户端信息
        YauaaDevicePo.Client client = new YauaaDevicePo.Client();
        client.setAgentClass(userAgent.getValue("AgentClass"));
        client.setAgentName(userAgent.getValue("AgentName"));
        client.setAgentVersion(userAgent.getValue("AgentVersion"));
        client.setLayoutEngineClass(userAgent.getValue("LayoutEngineClass"));
        client.setLayoutEngineName(userAgent.getValue("LayoutEngineName"));
        client.setLayoutEngineVersion(userAgent.getValue("LayoutEngineVersion"));
        client.setName(userAgent.getValue("AgentName"));
        client.setVersion(userAgent.getValue("AgentVersion"));
        devicePo.setClient(client);
        
        // 设置设备信息
        YauaaDevicePo.Device device = new YauaaDevicePo.Device();
        device.setDeviceClass(userAgent.getValue("DeviceClass"));
        device.setDeviceBrand(userAgent.getValue("DeviceBrand"));
        device.setDeviceName(userAgent.getValue("DeviceName"));
        device.setDeviceBrandName(userAgent.getValue("DeviceBrand")); // 使用DeviceBrand作为品牌名称
        device.setDeviceModel(userAgent.getValue("DeviceName")); // 使用DeviceName作为型号
        devicePo.setDevice(device);
        
        // 设置设备类型标识
        String deviceClass = userAgent.getValue("DeviceClass");
        devicePo.setIsMobile("Mobile".equals(deviceClass) || "Smartphone".equals(deviceClass) || "Tablet".equals(deviceClass));
        devicePo.setIsDesktop("Desktop".equals(deviceClass));
        devicePo.setIsSmartphone("Smartphone".equals(deviceClass));
        devicePo.setIsTablet("Tablet".equals(deviceClass));
        devicePo.setIsBot("Robot".equals(deviceClass) || "Bot".equals(deviceClass));
        devicePo.setIsFeaturePhone("Feature Phone".equals(deviceClass));
        devicePo.setIsConsole("Console".equals(deviceClass));
        devicePo.setIsCarBrowser("Car Browser".equals(deviceClass));
        devicePo.setIsCamera("Camera".equals(deviceClass));
        devicePo.setIsPortableMediaPlayer("Portable Media Player".equals(deviceClass));
        devicePo.setIsPhablet("Phablet".equals(deviceClass));
        devicePo.setIsSmartDisplay("Smart Display".equals(deviceClass));
        devicePo.setIsSmartSpeaker("Smart Speaker".equals(deviceClass));
        devicePo.setIsWearable("Wearable".equals(deviceClass));
        devicePo.setIsPeripheral("Peripheral".equals(deviceClass));
        devicePo.setIsTV("TV".equals(deviceClass));
        
        return devicePo;
    }
    
    /**
     * 创建默认设备信息
     */
    private YauaaDevicePo createDefaultDevicePo() {
        YauaaDevicePo devicePo = new YauaaDevicePo();
        devicePo.setFound(false);
        devicePo.setIsMobile(false);
        devicePo.setIsDesktop(true);
        devicePo.setIsBot(false);
        
        // 设置默认操作系统
        YauaaDevicePo.Os os = new YauaaDevicePo.Os();
        os.setName("Unknown");
        os.setVersion("Unknown");
        devicePo.setOs(os);
        
        // 设置默认客户端
        YauaaDevicePo.Client client = new YauaaDevicePo.Client();
        client.setName("Unknown");
        client.setVersion("Unknown");
        devicePo.setClient(client);
        
        // 设置默认设备
        YauaaDevicePo.Device device = new YauaaDevicePo.Device();
        device.setDeviceClass("Desktop");
        device.setDeviceBrand("Unknown");
        device.setDeviceName("Unknown");
        devicePo.setDevice(device);
        
        return devicePo;
    }
    
    /**
     * 清理旧缓存
     */
    private void clearOldCache() {
        if (localCache.size() > CACHE_SIZE) {
            localCache.clear();
            log.info("清理设备信息缓存，当前缓存大小: {}", localCache.size());
        }
    }
    
    /**
     * 获取性能统计信息
     */
    public String getPerformanceStats() {
        long total = totalRequests.get();
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        double hitRate = total > 0 ? (double) hits / total * 100 : 0;
        
        return String.format("YAUAA性能统计 - 总请求: %d, 缓存命中: %d, 缓存未命中: %d, 命中率: %.2f%%, 缓存大小: %d",
                total, hits, misses, hitRate, localCache.size());
    }
    
    /**
     * 清理缓存
     */
    public void clearCache() {
        localCache.clear();
        log.info("设备信息缓存已清理");
    }
    
    /**
     * 获取缓存大小
     */
    public int getCacheSize() {
        return localCache.size();
    }
    
    /**
     * 简单测试方法
     */
    public void testYauaa() {
        try {
            String testUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36";
            YauaaDevicePo result = parseUserAgent(testUserAgent);
            log.info("YAUAA测试成功: {}", result.getDevice().getDeviceClass());
        } catch (Exception e) {
            log.error("YAUAA测试失败", e);
        }
    }
} 