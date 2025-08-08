package cn.itcast.hotel.util;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Traits;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * IP代理/VPN检测测试类
 * 用于测试不同的IP检测方法
 */
@Slf4j
public class IpVpnDetectorTest {

    // 已知的ISP列表
    private static final List<String> KNOWN_ISPS = Arrays.asList(
            "VOCOM International Telecommunication, INC.", "Cogent Communications", "Google LLC",
            "Charter Communications Inc", "T-Mobile USA, Inc.", "Verizon Communications", "AT&T Services, Inc.",
            "Charter Communications", "TRUEMOVE", "Frontier Communications Solutions"
    );

    // 测试的国家列表
    private static final List<String> TEST_COUNTRIES = Arrays.asList("US", "CN", "JP", "GB", "DE");

    // IP-API的API密钥
    private static final String IP_API_KEY = "YOUR_API_KEY"; // 请替换为您的实际API密钥

    // GeoIP2数据库文件名
    private static final String GEOIP2_DB_FILE = "GeoLite2-City.mmdb";

    public static void main(String[] args) {
        // 测试IP列表
        String[] testIps = {
                "8.8.8.8",           // Google DNS
                "1.1.1.1",           // Cloudflare DNS
                "114.114.114.114",   // 中国114 DNS
                "185.199.108.153",   // GitHub
                "104.244.42.193",    // Twitter
                "157.240.3.35",      // Facebook
                "172.217.3.110",     // Google
                "13.107.42.12",      // Microsoft
                "104.16.182.15",     // Cloudflare
                "104.244.42.193"     // Twitter
        };

        log.info("开始IP代理/VPN检测测试...");
        log.info("测试IP数量: {}", testIps.length);

        // 测试API方法
        log.info("\n=== 测试API方法 ===");
        for (String ip : testIps) {
            int result = getIpApiVpn(TEST_COUNTRIES, IP_API_KEY, ip);
            log.info("IP: {} - 检测结果: {}", ip, result == 1 ? "代理/VPN" : "普通IP");
        }

        // 测试本地数据库方法
        log.info("\n=== 测试本地数据库方法 ===");
        for (String ip : testIps) {
            int result = getIpVpnLocal(TEST_COUNTRIES, ip);
            log.info("IP: {} - 检测结果: {}", ip, result == 1 ? "代理/VPN" : "普通IP");
        }

        // 测试多级检测方法
        log.info("\n=== 测试多级检测方法 ===");
        for (String ip : testIps) {
            int result = getIpVpnMultiLevel(ip, TEST_COUNTRIES);
            log.info("IP: {} - 检测结果: {}", ip, result == 1 ? "代理/VPN" : "普通IP");
        }

        // 测试异步方法
        log.info("\n=== 测试异步方法 ===");
        for (String ip : testIps) {
            CompletableFuture<Integer> future = getIpVpnAsync(ip, TEST_COUNTRIES, IP_API_KEY);
            int result = future.join();
            log.info("IP: {} - 检测结果: {}", ip, result == 1 ? "代理/VPN" : "普通IP");
        }

        log.info("测试完成!");
    }

    /**
     * 使用IP-API检测IP是否为代理/VPN
     */
    private static Integer getIpApiVpn(List<String> countryList, String keyString, String ip) {
        AtomicInteger code = new AtomicInteger(0);
        String url = "https://pro.ip-api.com/json/" + ip + "?key=" + keyString + "&fields=status,country,isp,proxy,hosting,query";
        RestTemplate restTemplate = new RestTemplate();
        
        try {
            String response = restTemplate.getForObject(url, String.class);
            JSONObject jsonResponse = new JSONObject(response);
            log.info("API响应: {}", jsonResponse.toString());
            
            boolean proxyStatus = jsonResponse.getBoolean("proxy");
            boolean hosting = jsonResponse.getBoolean("hosting");
            String countryCode = jsonResponse.getString("country");
            String isp = jsonResponse.getString("isp");
            
            if (proxyStatus || hosting || KNOWN_ISPS.contains(isp) || countryList.contains(countryCode)) {
                code.set(1);
            }
        } catch (Exception e) {
            log.error("API请求失败: {}", e.getMessage());
        }
        
        return code.get();
    }

    /**
     * 使用本地GeoIP2数据库检测IP
     */
    private static Integer getIpVpnLocal(List<String> countryList, String ip) {
        try {
            // 加载GeoIP2数据库
            InputStream database = IpVpnDetectorTest.class.getClassLoader().getResourceAsStream(GEOIP2_DB_FILE);
            if (database == null) {
                log.error("无法加载GeoIP2数据库文件: {}", GEOIP2_DB_FILE);
                return 0;
            }
            
            DatabaseReader reader = new DatabaseReader.Builder(database).build();
            
            // 查询IP信息
            InetAddress ipAddress = InetAddress.getByName(ip);
            CityResponse response = reader.city(ipAddress);
            
            // 获取国家信息
            Country country = response.getCountry();
            String countryCode = country.getIsoCode();
            
            // 获取ISP信息
            Traits traits = response.getTraits();
            String isp = traits.getIsp();
            
            // 检查是否为数据中心/托管服务
            boolean isHosting = traits.isHostingProvider();
            
            // 检查国家是否在列表中
            boolean isCountryInList = countryList.contains(countryCode);
            
            // 检查ISP是否在已知列表中
            boolean isKnownIsp = KNOWN_ISPS.contains(isp);
            
            log.info("本地检测 - IP: {}, 国家: {}, ISP: {}, 托管: {}, 已知ISP: {}", 
                    ip, countryCode, isp, isHosting, isKnownIsp);
            
            return (isHosting || isCountryInList || isKnownIsp) ? 1 : 0;
        } catch (IOException | GeoIp2Exception e) {
            log.error("本地IP检测失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 多级检测策略
     */
    private static Integer getIpVpnMultiLevel(String ip, List<String> countryList) {
        // 1. 首先使用本地数据库检测
        int localResult = getIpVpnLocal(countryList, ip);
        if (localResult == 1) {
            log.info("多级检测 - IP: {} - 本地数据库检测为代理/VPN", ip);
            return 1;
        }
        
        // 2. 如果本地检测失败，使用API
        int apiResult = getIpApiVpn(countryList, IP_API_KEY, ip);
        log.info("多级检测 - IP: {} - API检测结果: {}", ip, apiResult == 1 ? "代理/VPN" : "普通IP");
        
        return apiResult;
    }

    /**
     * 异步检测方法
     */
    private static CompletableFuture<Integer> getIpVpnAsync(String ip, List<String> countryList, String keyString) {
        return CompletableFuture.supplyAsync(() -> {
            // 先使用本地检测
            int localResult = getIpVpnLocal(countryList, ip);
            if (localResult == 1) {
                log.info("异步检测 - IP: {} - 本地数据库检测为代理/VPN", ip);
                return 1;
            }
            
            // 如果本地检测失败，再使用API
            int apiResult = getIpApiVpn(countryList, keyString, ip);
            log.info("异步检测 - IP: {} - API检测结果: {}", ip, apiResult == 1 ? "代理/VPN" : "普通IP");
            
            return apiResult;
        });
    }
} 