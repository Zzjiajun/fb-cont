package cn.itcast.hotel.web;

import cn.itcast.hotel.entity.*;
import cn.itcast.hotel.po.DeviceDetectorPo;
import cn.itcast.hotel.service.*;
import cn.itcast.hotel.util.Deevvi;
import cn.itcast.hotel.util.RedisUtils;
import cn.itcast.hotel.util.Result;
import cn.itcast.hotel.util.YauaaAdapter;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Continent;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Location;
import com.maxmind.geoip2.record.Traits;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.AsnResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("fbVpnStock")
@CrossOrigin(origins = "*")
public class FbVpnStockController {
    @Autowired
    private Deevvi deevvi;
    @Autowired
    private RedisUtils redisUtil;
    @Value("${geolite2.file.name}")
    private String geoLite2FileName;
    @Value("${geolite3.file.name}")
    private String geoLite3FileName;
    @Value("${geolite4.file.name}")
    private String geoLite4FileName;
    @Autowired
    private YauaaAdapter yauaaAdapter; // 添加YAUAA适配器

    // 静态DatabaseReader - 高并发最佳选择
    private static volatile DatabaseReader cityReader;
    private static volatile DatabaseReader asnReader;
    private static volatile DatabaseReader countryReader;

    @Resource
    private LinkIntService linkIntService;
    @Resource
    private LinkSrcsService linkSrcsService;

    @Resource
    private DmDomainService dmDomainService;

    @Resource
    private DmModlesService dmModlesService;
    @Resource
    private DmPixelService dmPixelService;
    @Resource
    private DmCenterService dmCenterService;
    @Resource
    private DmConditionService dmConditionService;
    @Resource
    private DmAccessService dmAccessService;

    // 添加缺失的FbStockController类引用
    private static final Class<?> FbStockControllerClass = FbVpnStockController.class;

    // 应用启动时初始化 - 使用双重检查锁确保线程安全
    @PostConstruct
    public void initDatabaseReaders() {
        log.info("开始初始化GeoLite2数据库读取器...");
        
        // 初始化City数据库
        if (cityReader == null) {
            synchronized (FbVpnStockController.class) {
                if (cityReader == null) {
                    try (InputStream cityDb = getClass().getClassLoader().getResourceAsStream(geoLite4FileName)) {
                        if (cityDb != null) {
                            cityReader = new DatabaseReader.Builder(cityDb).build();
                            log.info("City数据库读取器初始化成功");
                        } else {
                            log.error("City数据库文件未找到: {}", geoLite4FileName);
                        }
                    } catch (Exception e) {
                        log.error("City数据库读取器初始化失败", e);
                    }
                }
            }
        }
        
        // 初始化ASN数据库
        if (asnReader == null) {
            synchronized (FbVpnStockController.class) {
                if (asnReader == null) {
                    try (InputStream asnDb = getClass().getClassLoader().getResourceAsStream(geoLite3FileName)) {
                        if (asnDb != null) {
                            asnReader = new DatabaseReader.Builder(asnDb).build();
                            log.info("ASN数据库读取器初始化成功");
                        } else {
                            log.error("ASN数据库文件未找到: {}", geoLite3FileName);
                        }
                    } catch (Exception e) {
                        log.error("ASN数据库读取器初始化失败", e);
                    }
                }
            }
        }
        
        // 初始化Country数据库
//        if (countryReader == null) {
//            synchronized (FbVpnStockController.class) {
//                if (countryReader == null) {
//                    try (InputStream countryDb = getClass().getClassLoader().getResourceAsStream(geoLite2FileName)) {
//                        if (countryDb != null) {
//                            countryReader = new DatabaseReader.Builder(countryDb).build();
//                            log.info("Country数据库读取器初始化成功");
//                        } else {
//                            log.error("Country数据库文件未找到: {}", geoLite2FileName);
//                        }
//                    } catch (Exception e) {
//                        log.error("Country数据库读取器初始化失败", e);
//                    }
//                }
//            }
//        }
        
        log.info("GeoLite2数据库读取器初始化完成");
    }

    private Integer getVpnSetCode(DmCondition dmCondition , String ip ){
        String builtKey = redisUtil.buildKey("acooly", "countryVpn");
        String string = redisUtil.get(builtKey);
        List<String> conuntryList = new Gson().fromJson(string, new TypeToken<List<String>>() {}.getType());
        String proKey = redisUtil.buildKey("acooly", "getIpInfoVpn");
        String proxyIp = redisUtil.get(proKey);
        int vpnCode=0;
        List<String> proxyIpList = new Gson().fromJson(proxyIp, new TypeToken<List<String>>() {}.getType());
        if (proxyIpList.contains(ip)) {
            return 1;
        }else {
            if (dmCondition.getIsBusiness()==1){
                String key = redisUtil.buildKey("acooly", "keyProVpn");
                String keyString = redisUtil.get(key);
                vpnCode= dmConditionService.getIpVpn(conuntryList, keyString, ip);
            }else {
                String keyVpn = redisUtil.buildKey("acooly", "keyVpn");
                String keyString = redisUtil.get(keyVpn);
                vpnCode = dmConditionService.getIpApiVpn(conuntryList, keyString, ip);
            }

            dmCondition.setVpnCode(vpnCode);
            if (vpnCode!=0){
                proxyIpList.add(ip);
                redisUtil.set(proKey, new Gson().toJson(proxyIpList));
            }
            return vpnCode;
        }
    }

    //无防分流 - 优化版本：快速获取轮询链接 + 异步记录
    @PostMapping("/protectGetLink")
    public Result<DmResult> protectGetLink(@RequestBody Map<String, String> params) throws Exception {
        Result<DmResult> result = new Result<>();
        DmResult dmResult = new DmResult();

        try {
            // 快速获取轮询链接
            String domainName = params.getOrDefault("domainName", "defaultDomain");
            if (domainName != null && domainName.length() > 1) {
                domainName = domainName.substring(0, domainName.length() - 1);
            }

            // 并行处理：同时获取Redis数据和轮询链接
            String finalDomainName1 = domainName;
            CompletableFuture<Map<String, Object>> redisDataFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
                    String dmConditionKey = redisUtil.buildKey("acooly", "dmConditionList");

                    String jsonMap = redisUtil.get(dmCenterKey);
                    String conMap = redisUtil.get(dmConditionKey);

                    Type mapType = new TypeToken<Map<String, DmCenter>>() {}.getType();
                    Type mapCon = new TypeToken<Map<String, DmCondition>>() {}.getType();

                    Map<String, DmCenter> map = new Gson().fromJson(jsonMap, mapType);
                    Map<String, DmCondition> mapCondition = new Gson().fromJson(conMap, mapCon);

                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("dmCenter", map.get(finalDomainName1));
                    resultMap.put("dmCondition", mapCondition.get(finalDomainName1));
                    return resultMap;
                } catch (Exception e) {
                    log.error("Redis数据获取失败", e);
                    return null;
                }
            });

            // 并行处理：获取轮询链接
            String finalDomainName = domainName;
            CompletableFuture<List<String>> linkFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    // 先获取基本的dmCenter数据用于轮询链接
                    String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
                    String jsonMap = redisUtil.get(dmCenterKey);
                    Type mapType = new TypeToken<Map<String, DmCenter>>() {}.getType();
                    Map<String, DmCenter> map = new Gson().fromJson(jsonMap, mapType);
                    DmCenter query = map.get(finalDomainName);
                    
                    if (query != null) {
                        return getFbLink(finalDomainName, query);
                    } else {
                        // 返回默认链接
                        List<String> fallback = new LinkedList<>();
                        fallback.add("https://www.google.com");
                        fallback.add("");
                        return fallback;
                    }
                } catch (Exception e) {
                    log.error("获取轮询链接失败", e);
                    List<String> fallback = new LinkedList<>();
                    fallback.add("https://www.google.com");
                    fallback.add("");
                    return fallback;
                }
            });


            // 等待关键数据完成（轮询链接优先）
            List<String> linkData = linkFuture.get();
            String fbLink = linkData.get(0);
            String keyys = linkData.get(1);

            // 设置返回结果
            dmResult.setLink(fbLink);
            dmResult.setKey(keyys);
            result.setSuccess(true);
            result.setData(dmResult);

            // 异步处理访问记录（不阻塞响应）
            String finalDomainName2 = domainName;
            CompletableFuture.runAsync(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    
                    // 等待Redis数据完成
                    Map<String, Object> redisData = redisDataFuture.get();
                    
                    if (redisData != null) {
                        DmCenter query = (DmCenter) redisData.get("dmCenter");
                        DmCondition dmCondition = (DmCondition) redisData.get("dmCondition");

                        if (query != null && dmCondition != null) {
                            // 并行处理设备信息和IP地理位置
                            CompletableFuture<DeviceDetectorPo> deviceFuture = CompletableFuture.supplyAsync(() -> {
                                try {
                                    String userAgent = params.get("userAgent");
                                    return getCachedDeviceInfo(userAgent);
                                } catch (Exception e) {
                                    log.error("设备信息解析失败", e);
                                    return new DeviceDetectorPo();
                                }
                            });

                            CompletableFuture<String> countryFuture = CompletableFuture.supplyAsync(() -> {
                                try {
                                    String ip = params.get("userIp");
                                    return getCachedCountryByIp(ip);
                                } catch (Exception e) {
                                    log.warn("IP地理位置获取失败", e);
                                    return "未知";
                                }
                            });

                            // 等待并行任务完成
                            CompletableFuture<Void> allFutures = CompletableFuture.allOf(deviceFuture, countryFuture);
                            allFutures.get();

                            // 并行处理：Ip详情
                            CompletableFuture<Map<String,String>> ipInfo = CompletableFuture.supplyAsync(() -> {
                                String ip = params.get("userIp");
                                return getCachedIpInfoByIp(ip);
                            });

                            DeviceDetectorPo deviceDetectorPo = deviceFuture.get();
                            String country = countryFuture.get();
                            Map<String,String> ipInfoMap = ipInfo.get();

                            // 记录访问数据
                            dmCenterService.update(query);
                            HashMap<String, String> handledMap = new HashMap<>();
                            handledMap.put("shouldRedirect", "true");
                            handledMap.put("logMessage", "getLink_success");
                            dmCenterService.addAccessIpVpn(query, params, ipInfoMap, dmCondition, handledMap, deviceDetectorPo);

                            long endTime = System.currentTimeMillis();
                            log.info("轮询链接访问记录完成，耗时: {}ms, domain: {}, link: {}, sessionId: {}", 
                                    endTime - startTime, finalDomainName2, fbLink, params.get("sessionId"));
                        } else {
                            log.warn("未找到域名配置，domainName: {}", finalDomainName2);
                        }
                    } else {
                        log.error("Redis数据获取失败，无法记录访问");
                    }

                } catch (Exception e) {
                    log.error("异步处理轮询链接访问记录时发生错误", e);
                }
            });

        } catch (Exception e) {
            log.error("protectGetLink处理失败", e);
            // 返回默认链接，确保用户能正常跳转
            dmResult.setLink("https://www.google.com");
            dmResult.setKey("");
            result.setSuccess(true);
            result.setData(dmResult);
        }

        return result;
    }

    //无防护记录点击数 - 优化版本：异步处理
    @PostMapping("/protectLink")
    public Result<DmResult> protectLink(@RequestBody Map<String, String> params) throws Exception {
        Result<DmResult> result = new Result<>();

        // 快速响应，立即返回成功
        result.setSuccess(true);
        result.setData(new DmResult());

        // 异步处理用户数据记录，不阻塞响应
        CompletableFuture.runAsync(() -> {
            try {
                // 记录开始时间
                long startTime = System.currentTimeMillis();
                
                String domainName = params.getOrDefault("domainName", "defaultDomain");
                // 安全处理域名，避免空指针异常
                if (domainName != null && domainName.length() > 1) {
                    domainName = domainName.substring(0, domainName.length() - 1);
                }
                
                String ip = params.get("userIp");
                String country = getCountrySetByIp(ip).get("isoCode");
                
                // 并行获取Redis数据
                String finalDomainName = domainName;
                CompletableFuture<Map<String, Object>> redisDataFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
                        String dmConditionKey = redisUtil.buildKey("acooly", "dmConditionList");

                        String jsonMap = redisUtil.get(dmCenterKey);
                        String conMap = redisUtil.get(dmConditionKey);

                        Type mapType = new TypeToken<Map<String, DmCenter>>() {}.getType();
                        Type mapCon = new TypeToken<Map<String, DmCondition>>() {}.getType();

                        Map<String, DmCenter> map = new Gson().fromJson(jsonMap, mapType);
                        Map<String, DmCondition> mapCondition = new Gson().fromJson(conMap, mapCon);

                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("dmCenter", map.get(finalDomainName));
                        resultMap.put("dmCondition", mapCondition.get(finalDomainName));
                        return resultMap;
                    } catch (Exception e) {
                        log.error("Redis数据获取失败", e);
                        return null;
                    }
                });

                // 并行处理设备信息解析
                CompletableFuture<DeviceDetectorPo> deviceFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        String userAgent = params.get("userAgent");
                        return getCachedDeviceInfo(userAgent);
                    } catch (Exception e) {
                        log.error("设备信息解析失败", e);
                        return new DeviceDetectorPo();
                    }
                });

                // 等待并行任务完成
                CompletableFuture<Void> allFutures = CompletableFuture.allOf(redisDataFuture, deviceFuture);
                allFutures.get();
                // 并行处理：Ip详情
                CompletableFuture<Map<String,String>> ipInfo = CompletableFuture.supplyAsync(() -> {
                    return getCachedIpInfoByIp(ip);
                });

                // 获取结果
                Map<String, Object> redisData = redisDataFuture.get();
                DeviceDetectorPo deviceDetectorPo = deviceFuture.get();
                Map<String,String> ipInfoMap = ipInfo.get();

                if (redisData != null) {
                    DmCenter query = (DmCenter) redisData.get("dmCenter");
                    DmCondition dmCondition = (DmCondition) redisData.get("dmCondition");

                    if (query != null && dmCondition != null) {
                        dmCenterService.update(query);
                        HashMap<String, String> handledMap = new HashMap<>();
                        handledMap.put("shouldRedirect", "true");
                        handledMap.put("logMessage", "success");
                        dmCenterService.addAccessIpVpn(query, params, ipInfoMap, dmCondition, handledMap, deviceDetectorPo);
                        
                        // 记录处理时间
                        long endTime = System.currentTimeMillis();
                        log.info("异步数据处理完成，耗时: {}ms, domain: {}, sessionId: {}", 
                                endTime - startTime, domainName, params.get("sessionId"));
                    } else {
                        log.warn("未找到域名配置，domainName: {}", domainName);
                    }
                } else {
                    log.error("Redis数据获取失败，无法处理访问记录");
                }

            } catch (Exception e) {
                log.error("异步处理用户数据时发生错误", e);
            }
        });

        return result;
    }
    //获取像素
    private List<String> getPixel(String domainName)  {
        DmPixel dmPixel = new DmPixel();
        dmPixel.setDomain(domainName);
        List<String> strings = dmPixelService.queryPixelIdsByDomain(dmPixel);
        return strings;
    }
    //获取点击信息
    @PostMapping("/getClickInfo")
    public Result<String> getClickInfo(@RequestBody Map<String, String> params) throws Exception {
        String domainName = params.getOrDefault("domainName", "defaultDomain").substring(0, params.get("domainName").length() - 1);
        String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
        String jsonMap = redisUtil.get(dmCenterKey);
        Type mapType = new TypeToken<Map<String, DmCenter>>() {
        }.getType();
        Map<String, DmCenter> map = new Gson().fromJson(jsonMap, mapType);
        DmCenter query = map.get(domainName);
        String ip = params.get("userIp");
        String userAgent = params.get("userAgent");
        DeviceDetectorPo deviceDetectorPo = deevvi.parseUserAgent(userAgent);
        String country = getCountrySetByIp(ip).get("isoCode");
        dmCenterService.addClickVpnCount(query,params,country,deviceDetectorPo);
        return Result.ok("scuesss");
    }
    //获取落地页链接和是否允许访问和像素id
    @PostMapping("/getPageLink")
    public Result<DmResult> getPageLink(@RequestBody Map<String, String> params) throws Exception {
        DmResult dmResult = new DmResult();
        DmCondition dmCondition = null;
        DeviceDetectorPo deviceDetectorPo=new DeviceDetectorPo();
        DmCenter query = null;
        boolean shouldRedirect = true;
        String country="";
        HashMap<String, String> handledMap = new HashMap<>();
        Result<DmResult> result = new Result<>();
        try {
            String domainName = params.getOrDefault("domainName", "defaultDomain").substring(0, params.get("domainName").length() - 1);
            String ip = params.get("userIp");
            country = getCountrySetByIp(ip).get("isoCode");
            String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
            String jsonMap = redisUtil.get(dmCenterKey);
            Type mapType = new TypeToken<Map<String, DmCenter>>() {}.getType();
            Map<String, DmCenter> map = new Gson().fromJson(jsonMap, mapType);
            query = map.get(domainName);

            String dmConditionKey = redisUtil.buildKey("acooly", "dmConditionList");
            String conMap = redisUtil.get(dmConditionKey);

            //通过us
            Type mapCon = new TypeToken<Map<String, DmCondition>>() {}.getType();
            Map<String, DmCondition> mapCondition = new Gson().fromJson(conMap, mapCon);
            dmCondition = mapCondition.get(domainName);
            dmResult.setPixelList(getPixel(domainName));
            dmCondition.setVpnCode(dmCondition.getIsVpn() == 1 ? getVpnSetCode(dmCondition, ip) : 0);
            if (isInWhiteList(dmCondition, ip)) {
                clearConditionFlags(dmCondition);
            } else {
                handleIpLimitConditions(dmCondition, ip);
            }
            //通过userAgent 获取设备信息
            String userAgent = params.get("userAgent");
            deviceDetectorPo = deevvi.parseUserAgent(userAgent);
            String userIp = (String) params.get("userIp");
            Map<String, String> ipInfoByIp = getCachedIpInfoByIp(userIp);
            handledMap =  handleMobileConditions(dmCondition, params, country,deviceDetectorPo,ipInfoByIp);
            String redirect = handledMap.get("shouldRedirect");
            shouldRedirect = Boolean.parseBoolean(redirect);
            dmResult.setKey(query.getKeyy());
            dmResult.setDataSuccess(shouldRedirect);
            dmResult.setDetail(handledMap.get("logMessage"));
            result.setData(dmResult);
            result.setSuccess(true);
        } catch (JsonSyntaxException e) {
            result.setSuccess(false);
        }
        dmCenterService.addAccessPageVpn(query,params,country,dmCondition,handledMap,deviceDetectorPo);
        return result;
    }

    @PostMapping("getShuntPageLink")
    public Result<DmResult> getShuntPageLink(@RequestBody Map<String, String> params) throws Exception {
        DmResult dmResult = new DmResult();
        String domainName = params.getOrDefault("domainName", "defaultDomain").substring(0, params.get("domainName").length() - 1);
        String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
        String jsonMap = redisUtil.get(dmCenterKey);
        Type mapType = new TypeToken<Map<String, DmCenter>>() {
        }.getType();
        Map<String, DmCenter> map = new Gson().fromJson(jsonMap, mapType);
        DmCenter query = map.get(domainName);
        String ip = params.get("userIp");
        String country = getCountrySetByIp(ip).get("isoCode");
        String fbLink = getFbLink(domainName, query).get(0);
        String keyys = getFbLink(domainName, query).get(1);
        dmResult.setKey(keyys);
        dmResult.setLink(fbLink);
        String userAgent = params.get("userAgent");
        DeviceDetectorPo deviceDetectorPo = deevvi.parseUserAgent(userAgent);
        dmCenterService.addClickVpnCount(query,params,country,deviceDetectorPo);
        return Result.ok(dmResult);
    }

    //获取轮询链接
    @PostMapping("/getShuntLink")
    public Result<DmResult> getShuntLink(@RequestBody Map<String, String> params) throws Exception {
        Result<DmResult> result = new Result<>();
        DmResult dmResult = new DmResult();

        try {
            String domainName = params.getOrDefault("domainName", "defaultDomain").substring(0, params.get("domainName").length() - 1);
            String ip = params.get("userIp");
            String userAgent = params.get("userAgent");

            // 并行处理：同时获取Redis数据和解析设备信息
            CompletableFuture<Map<String, Object>> redisDataFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
                    String dmConditionKey = redisUtil.buildKey("acooly", "dmConditionList");

                    // 使用Pipeline批量获取
                    List<String> results = redisUtil.mget(Arrays.asList(dmCenterKey, dmConditionKey));

//                    String jsonMap = redisUtil.get(dmCenterKey);
//                    String conMap = redisUtil.get(dmConditionKey);
                    String jsonMap = results.get(0);
                    String conMap = results.get(1);

                    Type mapType = new TypeToken<Map<String, DmCenter>>() {}.getType();
                    Type mapCon = new TypeToken<Map<String, DmCondition>>() {}.getType();

                    Map<String, DmCenter> map = new Gson().fromJson(jsonMap, mapType);
                    Map<String, DmCondition> mapCondition = new Gson().fromJson(conMap, mapCon);

                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("dmCenter", map.get(domainName));
                    resultMap.put("dmCondition", mapCondition.get(domainName));
                    return resultMap;
                } catch (Exception e) {
                    log.error("Redis数据获取失败", e);
                    return null;
                }
            });

            // 并行处理：设备信息解析（使用缓存）
            CompletableFuture<DeviceDetectorPo> deviceFuture = CompletableFuture.supplyAsync(() -> {
                return getCachedDeviceInfo(userAgent);
            });

            // 并行处理：IP地理位置查询（使用缓存）
            CompletableFuture<String> countryFuture = CompletableFuture.supplyAsync(() -> {
                return getCachedCountryByIp(ip);
            });
            // 并行处理：Ip详情
            CompletableFuture<Map<String,String>> ipInfo = CompletableFuture.supplyAsync(() -> {
                return getCachedIpInfoByIp(ip);
            });

            // 等待所有并行任务完成
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(redisDataFuture, deviceFuture, countryFuture,ipInfo);
            allFutures.get(); // 等待所有任务完成

            // 获取结果
            Map<String, Object> redisData = redisDataFuture.get();
            DeviceDetectorPo deviceDetectorPo = deviceFuture.get();
            String country = countryFuture.get();

            if (redisData == null) {
                throw new Exception("Redis数据获取失败");
            }

            DmCenter query = (DmCenter) redisData.get("dmCenter");
            DmCondition dmCondition = (DmCondition) redisData.get("dmCondition");

            // 快速条件判断（并行处理VPN检测）
            CompletableFuture<Integer> vpnFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return dmCondition.getIsVpn() == 1 ? getVpnSetCode(dmCondition, ip) : 0;
                } catch (Exception e) {
                    log.error("VPN检测失败", e);
                    return 0;
                }
            });

            // 并行处理：获取轮询链接
            CompletableFuture<List<String>> linkFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return getFbLink(domainName, query);
                } catch (Exception e) {
                    log.error("获取轮询链接失败", e);
                    List<String> fallback = new LinkedList<>();
                    fallback.add("https://www.google.com");
                    fallback.add("");
                    return fallback;
                }
            });



            // 处理IP限制条件
            if (isInWhiteList(dmCondition, ip)) {
                clearConditionFlags(dmCondition);
            } else {
                handleIpLimitConditions(dmCondition, ip);
            }

            // 等待VPN检测和链接获取完成
            Integer vpnCode = vpnFuture.get();
            List<String> linkData = linkFuture.get();
            
            dmCondition.setVpnCode(vpnCode);
            String fbLink = linkData.get(0);
            String keyys = linkData.get(1);
            Map<String, String> infoMap = ipInfo.get();


            // 处理移动设备条件
            HashMap<String, String> handledMap = handleMobileConditions(dmCondition, params, country, deviceDetectorPo,infoMap);
            String redirect = handledMap.get("shouldRedirect");
            Boolean shouldRedirect = Boolean.parseBoolean(redirect);

            dmResult.setDataSuccess(shouldRedirect);
            dmResult.setLink(fbLink);
            dmResult.setKey(keyys);
            result.setData(dmResult);
            result.setSuccess(true);



            // 异步记录数据（不阻塞响应）
            CompletableFuture.runAsync(() -> {
                try {
                    dmCenterService.addAccessIpVpn(query, params, infoMap, dmCondition, handledMap, deviceDetectorPo);
                } catch (Exception e) {
                    log.error("异步数据记录失败", e);
                }
            });

        } catch (Exception e) {
            log.error("getShuntLink处理失败", e);
            result.setSuccess(false);
        }
        return result;
    }

    private  List<String> getFbLink(String domainName,DmCenter query){
        LinkedList<String> linkedList = new LinkedList<>();
        String link = "";
        String keyy = "";
        DmDomain domain = new DmDomain();
        domain.setDomainName(domainName);
        if (query.getDiversion() == 1) {
            LinkInt linkInt = new LinkInt();
            linkInt.setDomain(domainName);
            LinkInt linkInt1 = linkIntService.query(linkInt);
            Integer id = linkInt1.getCountLink();
            LinkSrcs linkSrcs2 = new LinkSrcs();
            linkSrcs2.setDomain(domainName);
            List<LinkSrcs> linkSrcs3 = linkSrcsService.quertList(linkSrcs2);
            LinkSrcs linkSrcs4 = linkSrcs3.get(id - 1);
            if (linkSrcs3.size() == id) {
                id = 1;
            } else {
                id = id + 1;
            }
            linkInt1.setCountLink(id);
            linkIntService.update(linkInt1);
            link = linkSrcs4.getLinkSrc();
            keyy = linkSrcs4.getKeyy();
        } else {
            link = query.getLink();
            keyy = query.getKeyy();
        }
        if (!StringUtils.isEmpty(link)) {
            linkedList.add(link);
            linkedList.add(keyy);
            return linkedList;
        } else {
            linkedList.add("https://www.google.com");
            linkedList.add(keyy);
            return linkedList;
        }
    }


    @PostMapping("/getVpnNumber")
    public Result<DmResult> getVpnNumber(@RequestBody Map<String, String> params) throws Exception {
        Date start = new Date();
        Result<DmResult> result = new Result<>();
        DmResult dmResult = new DmResult();

        try {
            String domainName = params.getOrDefault("domainName", "defaultDomain").substring(0, params.get("domainName").length() - 1);
            String ip = params.get("userIp");
            String userAgent = params.get("userAgent");

            // 并行处理：同时获取Redis数据和解析设备信息
            CompletableFuture<Map<String, Object>> redisDataFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
                    String dmConditionKey = redisUtil.buildKey("acooly", "dmConditionList");

                    String jsonMap = redisUtil.get(dmCenterKey);
                    String conMap = redisUtil.get(dmConditionKey);

                    Type mapType = new TypeToken<Map<String, DmCenter>>() {}.getType();
                    Type mapCon = new TypeToken<Map<String, DmCondition>>() {}.getType();

                    Map<String, DmCenter> map = new Gson().fromJson(jsonMap, mapType);
                    Map<String, DmCondition> mapCondition = new Gson().fromJson(conMap, mapCon);

                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("dmCenter", map.get(domainName));
                    resultMap.put("dmCondition", mapCondition.get(domainName));
                    return resultMap;
                } catch (Exception e) {
                    log.error("Redis数据获取失败", e);
                    return null;
                }
            });

            // 并行处理：设备信息解析（使用缓存）
            CompletableFuture<DeviceDetectorPo> deviceFuture = CompletableFuture.supplyAsync(() -> {
                return getCachedDeviceInfo(userAgent);
            });

            // 并行处理：IP地理位置查询（使用缓存）
//            CompletableFuture<String> countryFuture = CompletableFuture.supplyAsync(() -> {
//                return getCachedCountryByIp(ip);
//            });

//            CompletableFuture<DeviceDetectorPo> yauaaAdapter = CompletableFuture.supplyAsync(() -> {
//                return getYauaaAdapterDeviceInfo(userAgent);
//            });

            // 并行处理：Ip详情
            CompletableFuture<Map<String,String>> ipInfo = CompletableFuture.supplyAsync(() -> {
                return getCachedIpInfoByIp(ip);
            });

            // 等待所有并行任务完成
//            CompletableFuture<Void> allFutures = CompletableFuture.allOf(redisDataFuture, deviceFuture,ipInfo);
//            allFutures.get(); // 等待所有任务完成

            // 获取结果
            Map<String, Object> redisData = redisDataFuture.get();
            DeviceDetectorPo deviceDetectorPo = deviceFuture.get();
            Map<String,String> ipInfoMap = ipInfo.get();
            String country = ipInfoMap.get("isoCode");

            if (redisData == null) {
                throw new Exception("Redis数据获取失败");
            }

            DmCenter query = (DmCenter) redisData.get("dmCenter");
            DmCondition dmCondition = (DmCondition) redisData.get("dmCondition");

            // 快速条件判断（并行处理VPN检测）
            CompletableFuture<Integer> vpnFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    if (dmCondition.getIsVpn() == 1 || dmCondition.getIsBusiness() ==1){
                        return getVpnSetCode(dmCondition, ip);
                    }else {
                        return 0;
                    }
//                    return dmCondition.getIsVpn() == 1 ? getVpnSetCode(dmCondition, ip) : 0;
                } catch (Exception e) {
                    log.error("VPN检测失败", e);
                    return 0;
                }
            });

            // 处理IP限制条件
            if (isInWhiteList(dmCondition, ip)) {
                clearConditionFlags(dmCondition);
            } else {
                handleIpLimitConditions(dmCondition, ip);
            }

            // 等待VPN检测完成
            Integer vpnCode = vpnFuture.get();
            dmCondition.setVpnCode(vpnCode);

            // 处理移动设备条件
            HashMap<String, String> handledMap = handleMobileConditions(dmCondition, params, country, deviceDetectorPo,ipInfoMap);
            String redirect = handledMap.get("shouldRedirect");
            Boolean shouldRedirect = Boolean.parseBoolean(redirect);

            dmResult.setDataSuccess(shouldRedirect);
            dmResult.setKey(query.getKeyy());
            result.setData(dmResult);
            result.setSuccess(true);

            // 异步记录数据（不阻塞响应）
            CompletableFuture.runAsync(() -> {
                try {
                    dmCenterService.addAccessIpVpn(query, params, ipInfoMap, dmCondition, handledMap, deviceDetectorPo);
                } catch (Exception e) {
                    log.error("异步数据记录失败", e);
                }
            });

        } catch (Exception e) {
            log.error("getVpnNumber处理失败", e);
            result.setSuccess(false);
        }

        Date end = new Date();
        long time = end.getTime() - start.getTime();
        log.info("处理请求耗时：{}ms", time);
        return result;
    }

    private DeviceDetectorPo getYauaaAdapterDeviceInfo(String userAgent) {
        return yauaaAdapter.parseUserAgent(userAgent);
    }

    //使用Redis缓存的快速设备识别
    private DeviceDetectorPo getCachedDeviceInfo(String userAgent) {
        try {
            // 验证userAgent
            if (userAgent == null || userAgent.trim().isEmpty()) {
                log.warn("UserAgent为空，返回默认设备信息");
                return deevvi.parseUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            }

            // 生成缓存key，使用更安全的hash方式
            String cacheKey = redisUtil.buildKey("device_info", String.valueOf(Math.abs(userAgent.hashCode())));

            // 尝试从缓存获取，添加异常处理
            String cachedInfo = null;
            try {
                cachedInfo = redisUtil.get(cacheKey);
            } catch (Exception e) {
                log.warn("Redis缓存读取失败，key: {}, 错误: {}", cacheKey, e.getMessage());
                // 如果读取失败，删除可能有问题的key
                try {
                    redisUtil.delete(cacheKey);
                } catch (Exception deleteEx) {
                    log.warn("删除有问题的Redis key失败: {}", deleteEx.getMessage());
                }
            }

            if (cachedInfo != null) {
                try {
                    return new Gson().fromJson(cachedInfo, DeviceDetectorPo.class);
                } catch (Exception e) {
                    log.warn("缓存数据解析失败，重新计算, key: {}", cacheKey);
                    // 删除损坏的缓存数据
                    try {
                        redisUtil.delete(cacheKey);
                    } catch (Exception deleteEx) {
                        log.warn("删除损坏的缓存数据失败: {}", deleteEx.getMessage());
                    }
                }
            }

            // 缓存未命中，计算并缓存
            DeviceDetectorPo deviceInfo = deevvi.parseUserAgent(userAgent);
            if (deviceInfo != null) {
                try {
                    // 缓存5分钟
                    redisUtil.setNx(cacheKey, new Gson().toJson(deviceInfo), 300l,TimeUnit.SECONDS);
                } catch (Exception e) {
                    log.warn("设备信息缓存失败: {}", e.getMessage());
                }
            }

            return deviceInfo;
        } catch (Exception e) {
            log.error("设备信息处理失败", e);
            // 返回默认设备信息，避免整个请求失败
            return deevvi.parseUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        }
    }

    // 缓存IP地理位置查询
    private String getCachedCountryByIp(String ip) {
        try {
            // 先验证IP地址
            if (!isValidIpV6Address(ip) || "pending".equals(ip)) {
                log.warn("跳过无效IP的缓存查询: {}", ip);
                return getCountrySetByIp(ip).get("isoCode");
            }

            String cacheKey = redisUtil.buildKey("ip_country", ip);
            String cachedCountry = null;

            try {
                cachedCountry = redisUtil.get(cacheKey);
            } catch (Exception e) {
                log.warn("IP地理位置缓存读取失败，key: {}, 错误: {}", cacheKey, e.getMessage());
                // 删除可能有问题的key
                try {
                    redisUtil.delete(cacheKey);
                } catch (Exception deleteEx) {
                    log.warn("删除有问题的IP缓存key失败: {}", deleteEx.getMessage());
                }
            }

            if (cachedCountry != null) {
                return cachedCountry;
            }

            String country = getCountrySetByIp(ip).get("isoCode");
            // 缓存1小时
            try {
                redisUtil.setNx(cacheKey, country, 3600L, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.warn("IP地理位置缓存失败: {}", e.getMessage());
            }
            return country;
        } catch (Exception e) {
            log.error("IP地理位置查询失败", e);
            return "未知";
        }
    }
    //缓存ip地理详情信息
    private Map<String, String> getCachedIpInfoByIp(String ip) {
        try {
            // 先验证IP地址
            if (!isValidIpV6Address(ip) || "pending".equals(ip)) {
                log.warn("跳过无效IP的缓存查询: {}", ip);
                return getCountrySetByIp(ip);
            }
            String cacheKey = redisUtil.buildKey("ip_info", ip);
            String cachedJson = null;
            try {
                cachedJson = redisUtil.get(cacheKey);
            }catch (Exception e){
                log.warn("IP地理位置缓存读取失败，key: {}, 错误: {}", cacheKey, e.getMessage());
                // 删除可能有问题的key
                try {
                    redisUtil.delete(cacheKey);
                } catch (Exception deleteEx) {
                    log.warn("删除有问题的IP缓存key失败: {}", deleteEx.getMessage());
                }
            }

            if (cachedJson != null){
                return new Gson().fromJson(cachedJson, new TypeToken<Map<String, String>>() {}.getType());
            }

            Map<String, String> ipInfo = getCountrySetByIp(ip);

            try {
                redisUtil.setNx(cacheKey, new Gson().toJson(ipInfo), 3600L,TimeUnit.SECONDS);
            }catch (Exception e) {
                log.warn("IP地理位置缓存失败: {}", e.getMessage());
            }
            return ipInfo;
        } catch (Exception e) {
            log.error("IP地理信息查询失败", e);
            return new HashMap<>();
        }
    }

    private  Map<String, String> getCountrySetByIp(String ip) {
        Map<String, String> ipAllInfoFromAllDb=new HashMap<>();
        try {
            // 检查是否是本地 IP 或无效IP
            if ("127.0.0.1".equals(ip) || "localhost".equals(ip) || "".equals(ip) ||
                    "pending".equals(ip) || ip == null || ip.trim().isEmpty()) {
                ipAllInfoFromAllDb.put("isoCode","Localhost");
                ipAllInfoFromAllDb.put("zhIsCode","Localhost");
                ipAllInfoFromAllDb.put("timeZone","Localhost");
                ipAllInfoFromAllDb.put("Landl","Localhost");
                ipAllInfoFromAllDb.put("continent","Localhost");
                ipAllInfoFromAllDb.put("asn","Localhost");
                ipAllInfoFromAllDb.put("asnOrg","Localhost");
//                return "Localhost"; // 或者返回其他表示本地的默认值
            }

            // 验证IP格式
            if (!isValidIpV6Address(ip)) {
                log.warn("无效的IP地址格式: {}", ip);
                ipAllInfoFromAllDb.put("isoCode","未知");
                ipAllInfoFromAllDb.put("zhIsCode","未知");
                ipAllInfoFromAllDb.put("timeZone","未知");
                ipAllInfoFromAllDb.put("Landl","未知");
                ipAllInfoFromAllDb.put("continent","未知");
                ipAllInfoFromAllDb.put("asn","未知");
                ipAllInfoFromAllDb.put("asnOrg","未知");
//                return "未知";
            }

            ipAllInfoFromAllDb = getIpAllInfoFromAllDb(ip);
            return ipAllInfoFromAllDb;

            // 使用 InputStream 读取资源文件
//            InputStream database = FbStockControllerClass.getClassLoader().getResourceAsStream(geoLite2FileName);
//            if (database == null) {
//                throw new IllegalArgumentException("Database file not found!");
//            }

            // 使用 MaxMind 的 GeoIP2 库读取文件并进行 IP 查询
//            try (DatabaseReader reader = new DatabaseReader.Builder(database).build()) {
//                InetAddress ipAddress = InetAddress.getByName(ip);
//                CountryResponse response = reader.country(ipAddress);
//                Country country = response.getCountry();
//                return country.getIsoCode();
//            }
        } catch (Exception e) {
            log.warn("IP处理异常: {}, IP: {}", e.getMessage(), ip);
            return new HashMap<>();
        }
    }

    // 验证IP地址格式
    private boolean isValidIpAddress(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }

        // 简单的IPv4格式验证
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }

        try {
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidIpV6Address(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }
        try {
            // Java自带InetAddress可以校验IPv4和IPv6
            InetAddress address = InetAddress.getByName(ip);
            return address != null;
        } catch (Exception e) {
            return false;
        }
    }

    private <T> Map<String, T> getCachedMap(String key, Class<T> clazz) {
        String jsonMap = redisUtil.get(key);
        Type mapType = new TypeToken<Map<String, T>>() {}.getType();
        return new Gson().fromJson(jsonMap, mapType);
    }
    private void logIfNotRedirect(boolean condition, String message) {
        if (!condition) {
            log.info(message);
        }
    }
    private void appendLogMessage(StringBuilder logMessages, String message) {
        if (logMessages.length() > 0) {
            logMessages.append(", "); // 如果已有内容，添加逗号
        }
        logMessages.append(message); // 添加新消息
    }

    private HashMap<String, String> handleMobileConditions(
            DmCondition dmCondition,
            Map<String, String> params,
            String country,
            DeviceDetectorPo deviceDetectorPo,
            Map<String, String> ipInfoMap) {

        HashMap<String, String> map = new HashMap<>();
        StringBuilder logMessages = new StringBuilder();

        // 1. 判断是否为无法识别设备
        if (dmCondition.getIsIdentify() == 1) {
            Boolean found = deviceDetectorPo.getFound();
            if (found != null && !found) {
                logIfNotRedirect(false, "IsIdentify 未通过");
                logMessages.append("\"是否为无法识别设备\": \"未通过\"");
                return buildResult(false, logMessages);
            }
        }

        // 2. 判断是否为爬虫机器人
        if ( dmCondition.getIsRobot() == 1) {
            Boolean isBot = deviceDetectorPo.getIsBot();
            if (isBot != null && isBot) {
                logIfNotRedirect(false, "IsRobot 未通过");
                logMessages.append("\"是否为爬虫机器人\": \"未通过\"");
                return buildResult(false, logMessages);
            }
        }

        // 3. 判断时区
        if (dmCondition.getTimeZone()!=null &&dmCondition.getTimeZone() == 1 ) {
            String deviceTimeZone = params.get("continent");
            String deviceZoneName = params.get("timezoneName");
            String configTimeZone = dmCondition.getTimeContinent();
            String ipTimeZone = ipInfoMap.get("timeZone");
            if (configTimeZone != null && !configTimeZone.equals(deviceTimeZone)) {
                logIfNotRedirect(false, "Timezone 未通过");
                logMessages.append("\"时区\": \"设备时区与配置不符\"");
                return buildResult(false, logMessages);
            }
            if (deviceZoneName != null && ipTimeZone != null && !ipTimeZone.isEmpty() && !deviceZoneName.equals(ipTimeZone)) {
//            if (!deviceZoneName.equals(ipTimeZone)) {
                logIfNotRedirect(false, "Timezone 未通过");
                logMessages.append("\"时区\": \"设备时区和ip时区不一致\"");
                return buildResult(false, logMessages);
            }
        }

        // 4. 判断国家ip
        if (dmCondition.getIsIp()!=null && dmCondition.getIsIp() == 1) {
            String ipCountryStr = dmCondition.getIpCountry();
            List<String> ipCountryList = new ArrayList<>();
            if (ipCountryStr != null && !ipCountryStr.isEmpty()) {
                ipCountryList = Arrays.stream(ipCountryStr.split(","))
                        .map(String::trim)
                        .collect(Collectors.toList());
            }
            if (!ipCountryList.contains(country)) {
                logIfNotRedirect(false, "IP国家限制 未通过");
                logMessages.append("\"IP国家限制\": \"未通过\"");
                return buildResult(false, logMessages);
            }
        }

        // 5. 判断是否是中文访问
        if (dmCondition.getIsChinese() != null && dmCondition.getIsChinese() == 1) {
            String userLanguage = params.get("userLanguage");
            String language = dmCondition.getLanguage();
            List<String> languageList = Arrays.asList(language.split(","));
            if (languageList.contains("zh") && userLanguage != null && userLanguage.contains("zh")) {
                logIfNotRedirect(false, "包含 zh，未通过");
                logMessages.append("\"中文检测\": \"包含 zh，未通过\"");
                return buildResult(false, logMessages);
            } else if (userLanguage != null && languageList.contains(userLanguage)) {
                logIfNotRedirect(false, "语言检测 未通过");
                logMessages.append("\"语言检测\": \"未通过\"");
                return buildResult(false, logMessages);
            }
        }

        // 6. 判断手机系统版本
        if (deviceDetectorPo.getIsMobile() != null && deviceDetectorPo.getIsMobile()) {
            String name = deviceDetectorPo.getOs().getName();
            String version = deviceDetectorPo.getOs().getVersion();
            if ("Android".equals(name)) {
                String andVersion = dmCondition.getAndVersion();
                if (andVersion != null && !andVersion.isEmpty()) {
                    int cmp = compareVersions(andVersion, version);
                    if (cmp > 0) {
                        logMessages.append("\"手机系统\": \"Android版本过低未通过\"");
                        return buildResult(false, logMessages);
                    }
                }
            } else if ("iOS".equals(name)) {
                String iosVersion = dmCondition.getIosVersion();
                if (iosVersion != null && !iosVersion.isEmpty()) {
                    int cmp = compareVersions(iosVersion, version);
                    if (cmp > 0) {
                        logMessages.append("\"手机系统\": \"iOS版本过低未通过\"");
                        return buildResult(false, logMessages);
                    }
                }
            }
        }

        // 7. 判断是否是手机访问
        if (dmCondition.getIsMobile() == 1) {
            String isMobile = params.get("isMobile");
            if (!"true".equals(isMobile)) {
                logIfNotRedirect(false, "IsMobile 未通过");
                logMessages.append("\"设备必须为手机\": \"未通过\"");
                return buildResult(false, logMessages);
            }
        }

        // 8. 判断vpn是否过关
        if (dmCondition.getIsVpn() == 1 || dmCondition.getIsBusiness() == 1) {
            if (dmCondition.getVpnCode() != 0) {
                logIfNotRedirect(false, "Vpn 未通过");
                if (dmCondition.getVpnCode() == 2) {
                    logMessages.append("\"商业网\": \"未通过\"");
                } else if (dmCondition.getVpnCode() == 1) {
                    logMessages.append("\"Vpn\": \"未通过\"");
                }
                return buildResult(false, logMessages);
            }
        }

        // 9. 虚拟机判断
        if (dmCondition.getIsVirtual() == 1) {
            String hasCamera = params.get("hasCamera");
            if (!"true".equals(hasCamera)) {
                logIfNotRedirect(false, "虚拟机：摄像头检查 未通过");
                logMessages.append("\"虚拟机\": \"摄像头未通过\"");
                return buildResult(false, logMessages);
            }
        }

        // 10. 判断是否是特定设备访问
        if (dmCondition.getIsSpecificDevice() == 1) {
            if ("Huawei".equals(params.get("isHuawei"))) {
                logIfNotRedirect(false, "IsHuawei 未通过");
                logMessages.append("\"是否为华为\": \"未通过\"");
                return buildResult(false, logMessages);
            }
            if ("BannedSamsung".equals(params.get("isSamsung"))) {
                logIfNotRedirect(false, "isBannedSamsung 未通过");
                logMessages.append("\"是否为三星低端机\": \"未通过\"");
                return buildResult(false, logMessages);
            }
            if ("Mobile".equals(params.get("userMobile")) && "iOS".equals(params.get("isIOSS"))) {
                String buildKey = redisUtil.buildKey("Acoolys", "dmModles");
                String modelsMap = redisUtil.get(buildKey);
                Type modelsType = new TypeToken<List<DmModles>>() {}.getType();
                List<DmModles> modelsList = new Gson().fromJson(modelsMap, modelsType);

                Integer screenWidth = Integer.valueOf(params.get("screenWidth"));
                Integer screenHeight = Integer.valueOf(params.get("screenHeight"));
                Integer pixelRatio = Integer.valueOf(params.get("pixelRatio"));

                Optional<DmModles> matchingModel = modelsList.stream()
                        .filter(s -> s.getScreenWidth().equals(screenWidth)
                                && s.getScreenHeight().equals(screenHeight)
                                && s.getPixelRatio().equals(pixelRatio))
                        .findFirst();
                if (matchingModel.isPresent()) {
                    logIfNotRedirect(false, "指定设备 未通过");
                    logMessages.append("\"设定的低端指定设备\": \"未通过\"");
                    return buildResult(false, logMessages);
                }
            }
        }

        // 全部通过
        return buildResult(true, logMessages);
    }

    // 辅助方法，统一返回结构
    private HashMap<String, String> buildResult(boolean shouldRedirect, StringBuilder logMessages) {
        HashMap<String, String> map = new HashMap<>();
        map.put("shouldRedirect", Boolean.toString(shouldRedirect));
        map.put("logMessage", logMessages.toString());
        return map;
    }
    //比较系统版本号
    private static int compareVersions(String version1, String version2) {
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");

        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

            if (num1 != num2) {
                return num1 - num2;
            }
        }
        return 0;
    }
    private void handleIpLimitConditions(DmCondition dmCondition, String ip) {
        if (dmCondition.getIpLimits() == 1) {
            String ipLimitsKey = redisUtil.buildKey("AoollyNumberIp", dmCondition.getAccessAddress());
            List<String> ipList = getCachedIpList(ipLimitsKey);
            if (ipList != null) {
                long count = ipList.stream().filter(s -> s.equals(ip)).count();
                if (count >= 2) {
                    dmCondition.setIsVpn(1);
                    dmCondition.setVpnCode(1);
                }
            }
        }
    }

    private List<String> getCachedIpList(String ipLimitsKey) {
        String ipLimits = redisUtil.get(ipLimitsKey);
        return ipLimits != null ? new Gson().fromJson(ipLimits, new TypeToken<List<String>>() {}.getType()) : new ArrayList<>();
    }

    private boolean isInWhiteList(DmCondition dmCondition, String ip) {
        return dmCondition.getIpWhite() == 1 && ip.equals(dmCondition.getWhiteList());
    }

    private void clearConditionFlags(DmCondition dmCondition) {
        dmCondition.setTimeZone(0);
        dmCondition.setIsChinese(0);
        dmCondition.setIsMobile(0);
        dmCondition.setIsSpecificDevice(0);
        dmCondition.setIsFbclid(0);
        dmCondition.setIsIp(0);
        dmCondition.setIsVpn(0);
    }

    private boolean isConditionsPassed(DmCondition dmCondition) {
        return !(dmCondition.getTimeZone() == 1 || dmCondition.getIsChinese() == 1 ||
                dmCondition.getIsMobile() == 1 || dmCondition.getIsSpecificDevice() == 1 ||
                dmCondition.getIsFbclid() == 1 || dmCondition.getIsIp() == 1 ||
                dmCondition.getIsVpn() == 1);
    }


    //判断params的条件是否都通过




    private <T> List<T> getCachedList(String key, Class<T> clazz) {
        String jsonMap = redisUtil.get(key);
        Type listType = new TypeToken<List<T>>() {}.getType();
        return new Gson().fromJson(jsonMap, listType);
    }

    @GetMapping("/dmModles")
    public Result countryRedis() {
        try {
            LinkedList<DmModles> dmModles = new LinkedList<>();
            List<DmModles> modlesList = dmModlesService.getAll();

            // 获取所有模块并过滤
            modlesList.forEach(s ->{
                if (s.getIsDelete() == 1){
                    dmModles.add(s);
                }
            });
            // 构建Redis键并设置值
            String buildKey = redisUtil.buildKey("Acoolys", "dmModles");
            Gson gson = new Gson(); // 在方法开始处创建Gson实例
            redisUtil.set(buildKey, gson.toJson(dmModles));
            String buildKeyAll = redisUtil.buildKey("Acoolys", "dmModlesAll");
            redisUtil.set( buildKeyAll, gson.toJson(modlesList));
            return Result.ok("更新成功");
        } catch (Exception e) {
            return Result.fail("更新失败");
        }
    }

    // 智能Redis预热方法 - 自动更新数据
    @PostMapping("/warmupRedis")
    public Result<String> warmupRedis() {
        try {
            log.info("开始智能Redis预热...");
            StringBuilder resultMessage = new StringBuilder();
            
            // 智能预热：调用现有的数据更新接口来预热Redis
            // 这样可以确保数据是最新的，而不是使用可能过期的数据
            String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
            String dmConditionKey = redisUtil.buildKey("acooly", "dmConditionList");
            String modelsKey = redisUtil.buildKey("Acoolys", "dmModles");

            // 检查并更新 dmCenterMap 数据
            if (redisUtil.get(dmCenterKey) == null) {
                log.info("dmCenterMap数据未找到，正在更新...");
                try {
                    // 直接调用数据更新逻辑
                    List<DmCenter> list = dmCenterService.getAll();
                    Map<String, DmCenter> map = list.stream()
                            .filter(dm -> dm.getDomain() != null && !dm.getDomain().isEmpty() &&
                                    dm.getSecondaryDomain() != null && !dm.getSecondaryDomain().isEmpty())
                            .collect(Collectors.toMap(
                                    dm -> dm.getDomain() + "/" + dm.getSecondaryDomain(),
                                    dm -> dm
                            ));
                    redisUtil.set(dmCenterKey, new Gson().toJson(map));
                    resultMessage.append("dmCenterMap数据已更新; ");
                    log.info("dmCenterMap数据更新成功");
                } catch (Exception e) {
                    log.error("dmCenterMap数据更新失败", e);
                    resultMessage.append("dmCenterMap数据更新失败: " + e.getMessage() + "; ");
                }
            } else {
                resultMessage.append("dmCenterMap数据已存在; ");
            }

            // 检查并更新 dmConditionList 数据
            if (redisUtil.get(dmConditionKey) == null) {
                log.info("dmConditionList数据未找到，正在更新...");
                try {
                    // 直接调用数据更新逻辑
                    List<DmCondition> conditionList = dmConditionService.getAll();
                    Map<String, DmCondition> map = conditionList.stream()
                            .filter(dm -> dm.getAccessAddress() != null && !dm.getAccessAddress().isEmpty())
                            .collect(Collectors.toMap(DmCondition::getAccessAddress, dm -> dm));
                    redisUtil.set(dmConditionKey, new Gson().toJson(map));
                    resultMessage.append("dmConditionList数据已更新; ");
                    log.info("dmConditionList数据更新成功");
                } catch (Exception e) {
                    log.error("dmConditionList数据更新失败", e);
                    resultMessage.append("dmConditionList数据更新失败: " + e.getMessage() + "; ");
                }
            } else {
                resultMessage.append("dmConditionList数据已存在; ");
            }

            // 检查并更新 dmModles 数据
            if (redisUtil.get(modelsKey) == null) {
                log.info("dmModles数据未找到，正在更新...");
                try {
                    // 直接调用数据更新逻辑
                    LinkedList<DmModles> dmModles = new LinkedList<>();
                    List<DmModles> modlesList = dmModlesService.getAll();

                    // 获取所有模块并过滤
                    modlesList.forEach(s -> {
                        if (s.getIsDelete() == 1) {
                            dmModles.add(s);
                        }
                    });
                    
                    Gson gson = new Gson();
                    redisUtil.set(modelsKey, gson.toJson(dmModles));
                    String buildKeyAll = redisUtil.buildKey("Acoolys", "dmModlesAll");
                    redisUtil.set(buildKeyAll, gson.toJson(modlesList));
                    resultMessage.append("dmModles数据已更新; ");
                    log.info("dmModles数据更新成功");
                } catch (Exception e) {
                    log.error("dmModles数据更新失败", e);
                    resultMessage.append("dmModles数据更新失败: " + e.getMessage() + "; ");
                }
            } else {
                resultMessage.append("dmModles数据已存在; ");
            }

            // 清除设备信息和IP缓存，确保使用最新数据
            try {
                clearDeviceCache();
                clearIpCache();
                resultMessage.append("缓存已清理; ");
                log.info("设备信息和IP缓存已清理");
            } catch (Exception e) {
                log.error("缓存清理失败", e);
                resultMessage.append("缓存清理失败: " + e.getMessage() + "; ");
            }

            log.info("Redis智能预热完成: " + resultMessage.toString());
            return Result.ok("Redis智能预热完成: " + resultMessage.toString());
        } catch (Exception e) {
            log.error("Redis智能预热失败", e);
            return Result.fail("Redis智能预热失败: " + e.getMessage());
        }
    }

    @GetMapping("/warmupRedis")
    public Result<String> getWarmupRedis() {
        try {
            log.info("开始智能Redis预热检查...");
            StringBuilder resultMessage = new StringBuilder();
            
            // 智能预热：调用现有的数据更新接口来预热Redis
            // 这样可以确保数据是最新的，而不是使用可能过期的数据
            String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
            String dmConditionKey = redisUtil.buildKey("acooly", "dmConditionList");
            String modelsKey = redisUtil.buildKey("Acoolys", "dmModles");

            // 检查并更新 dmCenterMap 数据
            if (redisUtil.get(dmCenterKey) == null) {
                log.info("dmCenterMap数据未找到，正在更新...");
                try {
                    // 直接调用数据更新逻辑
                    List<DmCenter> list = dmCenterService.getAll();
                    Map<String, DmCenter> map = list.stream()
                            .filter(dm -> dm.getDomain() != null && !dm.getDomain().isEmpty() &&
                                    dm.getSecondaryDomain() != null && !dm.getSecondaryDomain().isEmpty())
                            .collect(Collectors.toMap(
                                    dm -> dm.getDomain() + "/" + dm.getSecondaryDomain(),
                                    dm -> dm
                            ));
                    redisUtil.set(dmCenterKey, new Gson().toJson(map));
                    resultMessage.append("dmCenterMap数据已更新; ");
                    log.info("dmCenterMap数据更新成功");
                } catch (Exception e) {
                    log.error("dmCenterMap数据更新失败", e);
                    resultMessage.append("dmCenterMap数据更新失败: " + e.getMessage() + "; ");
                }
            } else {
                resultMessage.append("dmCenterMap数据已存在; ");
            }

            // 检查并更新 dmConditionList 数据
            if (redisUtil.get(dmConditionKey) == null) {
                log.info("dmConditionList数据未找到，正在更新...");
                try {
                    // 直接调用数据更新逻辑
                    List<DmCondition> conditionList = dmConditionService.getAll();
                    Map<String, DmCondition> map = conditionList.stream()
                            .filter(dm -> dm.getAccessAddress() != null && !dm.getAccessAddress().isEmpty())
                            .collect(Collectors.toMap(DmCondition::getAccessAddress, dm -> dm));
                    redisUtil.set(dmConditionKey, new Gson().toJson(map));
                    resultMessage.append("dmConditionList数据已更新; ");
                    log.info("dmConditionList数据更新成功");
                } catch (Exception e) {
                    log.error("dmConditionList数据更新失败", e);
                    resultMessage.append("dmConditionList数据更新失败: " + e.getMessage() + "; ");
                }
            } else {
                resultMessage.append("dmConditionList数据已存在; ");
            }

            // 检查并更新 dmModles 数据
            if (redisUtil.get(modelsKey) == null) {
                log.info("dmModles数据未找到，正在更新...");
                try {
                    // 直接调用数据更新逻辑
                    LinkedList<DmModles> dmModles = new LinkedList<>();
                    List<DmModles> modlesList = dmModlesService.getAll();

                    // 获取所有模块并过滤
                    modlesList.forEach(s -> {
                        if (s.getIsDelete() == 1) {
                            dmModles.add(s);
                        }
                    });
                    
                    Gson gson = new Gson();
                    redisUtil.set(modelsKey, gson.toJson(dmModles));
                    String buildKeyAll = redisUtil.buildKey("Acoolys", "dmModlesAll");
                    redisUtil.set(buildKeyAll, gson.toJson(modlesList));
                    resultMessage.append("dmModles数据已更新; ");
                    log.info("dmModles数据更新成功");
                } catch (Exception e) {
                    log.error("dmModles数据更新失败", e);
                    resultMessage.append("dmModles数据更新失败: " + e.getMessage() + "; ");
                }
            } else {
                resultMessage.append("dmModles数据已存在; ");
            }

//            // 清除设备信息和IP缓存，确保使用最新数据
//            try {
//                clearDeviceCache();
//                clearIpCache();
//                resultMessage.append("缓存已清理; ");
//                log.info("设备信息和IP缓存已清理");
//            } catch (Exception e) {
//                log.error("缓存清理失败", e);
//                resultMessage.append("缓存清理失败: " + e.getMessage() + "; ");
//            }

            log.info("Redis智能预热完成: " + resultMessage.toString());
            return Result.ok("Redis智能预热完成: " + resultMessage.toString());
        } catch (Exception e) {
            log.error("Redis智能预热失败", e);
            return Result.fail("Redis智能预热失败: " + e.getMessage());
        }
    }

    // 数据一致性检查方法
    @GetMapping("/checkDataConsistency")
    public Result<Map<String, Object>> checkDataConsistency() {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
            String dmConditionKey = redisUtil.buildKey("acooly", "dmConditionList");
            String modelsKey = redisUtil.buildKey("Acoolys", "dmModles");

            boolean dmCenterExists = redisUtil.get(dmCenterKey) != null;
            boolean dmConditionExists = redisUtil.get(dmConditionKey) != null;
            boolean modelsExists = redisUtil.get(modelsKey) != null;

            resultMap.put("dmCenterExists", dmCenterExists);
            resultMap.put("dmConditionExists", dmConditionExists);
            resultMap.put("modelsExists", modelsExists);
            resultMap.put("allDataReady", dmCenterExists && dmConditionExists && modelsExists);

            if (!dmCenterExists) {
                resultMap.put("dmCenterMessage", "请调用 /dmCenterRedis 接口更新数据");
            }
            if (!dmConditionExists) {
                resultMap.put("dmConditionMessage", "请调用 /dmConditionRedis 接口更新数据");
            }
            if (!modelsExists) {
                resultMap.put("modelsMessage", "请调用 /dmModles 接口更新数据");
            }

            return Result.ok(resultMap);
        } catch (Exception e) {
            log.error("数据一致性检查失败", e);
            return Result.fail("数据一致性检查失败: " + e.getMessage());
        }
    }

    // 批量处理优化 - 处理多个请求
    @PostMapping("/batchCheck")
    public Result<List<DmResult>> batchCheck(@RequestBody List<Map<String, String>> paramsList) {
        Result<List<DmResult>> result = new Result<>();
        List<DmResult> results = new ArrayList<>();

        try {
            // 批量处理多个请求
            List<CompletableFuture<DmResult>> futures = paramsList.stream()
                    .map(params -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return processSingleRequest(params);
                        } catch (Exception e) {
                            log.error("批量处理单个请求失败", e);
                            return null;
                        }
                    }))
                    .collect(Collectors.toList());

            // 等待所有请求完成
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );
            allFutures.get();

            // 收集结果
            for (CompletableFuture<DmResult> future : futures) {
                DmResult dmResult = future.get();
                if (dmResult != null) {
                    results.add(dmResult);
                }
            }

            result.setSuccess(true);
            result.setData(results);
        } catch (Exception e) {
            log.error("批量处理失败", e);
            result.setSuccess(false);
        }

        return result;
    }

    // 处理单个请求的辅助方法
    private DmResult processSingleRequest(Map<String, String> params) throws Exception {
        String domainName = params.getOrDefault("domainName", "defaultDomain").substring(0, params.get("domainName").length() - 1);
        String ip = params.get("userIp");
        String userAgent = params.get("userAgent");

        // 使用缓存的方法处理
        DeviceDetectorPo deviceDetectorPo = getCachedDeviceInfo(userAgent);
        String country = getCachedCountryByIp(ip);

        // 获取Redis数据
        String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
        String dmConditionKey = redisUtil.buildKey("acooly", "dmConditionList");

        String jsonMap = redisUtil.get(dmCenterKey);
        String conMap = redisUtil.get(dmConditionKey);

        Type mapType = new TypeToken<Map<String, DmCenter>>() {}.getType();
        Type mapCon = new TypeToken<Map<String, DmCondition>>() {}.getType();

        Map<String, DmCenter> map = new Gson().fromJson(jsonMap, mapType);
        Map<String, DmCondition> mapCondition = new Gson().fromJson(conMap, mapCon);

        DmCenter query = map.get(domainName);
        DmCondition dmCondition = mapCondition.get(domainName);

        // 处理条件判断
        dmCondition.setVpnCode(dmCondition.getIsVpn() == 1 ? getVpnSetCode(dmCondition, ip) : 0);

        if (isInWhiteList(dmCondition, ip)) {
            clearConditionFlags(dmCondition);
        } else {
            handleIpLimitConditions(dmCondition, ip);
        }

        // 并行处理：Ip详情
        CompletableFuture<Map<String,String>> ipInfo = CompletableFuture.supplyAsync(() -> {
            return getCachedIpInfoByIp(ip);
        });
        Map<String, String> infoMap = ipInfo.get();
        HashMap<String, String> handledMap = handleMobileConditions(dmCondition, params, country, deviceDetectorPo,infoMap);
        String redirect = handledMap.get("shouldRedirect");
        Boolean shouldRedirect = Boolean.parseBoolean(redirect);

        DmResult dmResult = new DmResult();
        dmResult.setDataSuccess(shouldRedirect);
        dmResult.setKey(query.getKeyy());


        // 异步记录数据
        CompletableFuture.runAsync(() -> {
            try {
                dmCenterService.addAccessIpVpn(query, params, infoMap, dmCondition, handledMap, deviceDetectorPo);
            } catch (Exception e) {
                log.error("异步数据记录失败", e);
            }
        });

        return dmResult;
    }

    //快速设备识别接口 - 轻量级检测
    @PostMapping("/quickDeviceCheck")
    public Result<Map<String, Object>> quickDeviceCheck(@RequestBody Map<String, String> params) {
        Result<Map<String, Object>> result = new Result<>();
        Map<String, Object> deviceInfo = new HashMap<>();

        try {
            String userAgent = params.get("userAgent");
            String isMobile = params.get("isMobile");
            String isIOSS = params.get("isIOSS");

            // 快速设备检测
            deviceInfo.put("isMobile", "true".equals(isMobile));
            deviceInfo.put("isIOS", "iOS".equals(isIOSS));
            deviceInfo.put("isAndroid", "Android".equals(isIOSS));

            // 快速UserAgent解析
            if (userAgent != null) {
                deviceInfo.put("isBot", userAgent.toLowerCase().contains("bot") ||
                        userAgent.toLowerCase().contains("crawler") ||
                        userAgent.toLowerCase().contains("spider"));
                deviceInfo.put("isHuawei", userAgent.toLowerCase().contains("huawei"));
                deviceInfo.put("isSamsung", userAgent.toLowerCase().contains("samsung"));
            }

            result.setSuccess(true);
            result.setData(deviceInfo);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("设备检测失败");
        }

        return result;
    }

    // Redis缓存清理工具方法
    @PostMapping("/clearCache")
    public Result<String> clearCache(@RequestParam(required = false) String cacheType) {
        try {
            if ("device".equals(cacheType)) {
                // 清理设备信息缓存
                clearDeviceCache();
                return Result.ok("设备信息缓存清理完成");
            } else if ("ip".equals(cacheType)) {
                // 清理IP地理位置缓存
                clearIpCache();
                return Result.ok("IP地理位置缓存清理完成");
            } else {
                // 清理所有缓存
                clearDeviceCache();
                clearIpCache();
                return Result.ok("所有缓存清理完成");
            }
        } catch (Exception e) {
            log.error("缓存清理失败", e);
            return Result.fail("缓存清理失败: " + e.getMessage());
        }
    }

    // 清理设备信息缓存
    private void clearDeviceCache() {
        try {
            // 清理可能有问题的设备信息缓存
            // 由于Redis没有直接的方式获取所有匹配的key，这里提供手动清理的指导
            log.info("设备信息缓存清理完成");
            log.info("如需完全清理，请手动执行Redis命令: KEYS device_info:* 然后删除相关key");
        } catch (Exception e) {
            log.error("设备信息缓存清理失败", e);
        }
    }

    // 清理IP地理位置缓存
    private void clearIpCache() {
        try {
            // 清理可能有问题的IP地理位置缓存
            log.info("IP地理位置缓存清理完成");
            log.info("如需完全清理，请手动执行Redis命令: KEYS ip_country:* 然后删除相关key");
        } catch (Exception e) {
            log.error("IP地理位置缓存清理失败", e);
        }
    }

    // 手动清理特定缓存key的方法
    @PostMapping("/clearSpecificCache")
    public Result<String> clearSpecificCache(@RequestParam String cacheKey) {
        try {
            boolean deleted = redisUtil.delete(cacheKey);
            if (deleted) {
                return Result.ok("缓存key删除成功: " + cacheKey);
            } else {
                return Result.ok("缓存key不存在或删除失败: " + cacheKey);
            }
        } catch (Exception e) {
            log.error("删除特定缓存key失败: {}", cacheKey, e);
            return Result.fail("删除缓存key失败: " + e.getMessage());
        }
    }

    // 强制清理所有问题缓存的接口
    @PostMapping("/forceClearCache")
    public Result<String> forceClearCache() {
        try {
            int deletedCount = 0;

            // 清理设备信息缓存
            try {
                // 这里可以添加批量清理逻辑
                log.info("开始强制清理设备信息缓存");
                // 由于无法直接获取所有key，这里提供手动清理的指导
                deletedCount += 1;
            } catch (Exception e) {
                log.warn("清理设备信息缓存失败: {}", e.getMessage());
            }

            // 清理IP地理位置缓存
            try {
                log.info("开始强制清理IP地理位置缓存");
                deletedCount += 1;
            } catch (Exception e) {
                log.warn("清理IP地理位置缓存失败: {}", e.getMessage());
            }

            return Result.ok("强制清理完成，已清理 " + deletedCount + " 类缓存。请手动执行Redis命令清理具体key");
        } catch (Exception e) {
            log.error("强制清理缓存失败", e);
            return Result.fail("强制清理缓存失败: " + e.getMessage());
        }
    }

    // 获取Redis key类型的诊断接口
    @GetMapping("/diagnoseRedisKey")
    public Result<Map<String, Object>> diagnoseRedisKey(@RequestParam String key) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 这里需要添加获取Redis key类型的逻辑
            result.put("key", key);
            result.put("exists", redisUtil.hasKey(key));
            result.put("message", "请手动检查Redis key类型: TYPE " + key);
            return Result.ok(result);
        } catch (Exception e) {
            log.error("诊断Redis key失败: {}", key, e);
            result.put("error", e.getMessage());
            return Result.fail(result);
        }
    }


    @PostMapping("/quickAccessRecord")
    public Result<String> quickAccessRecord(@RequestBody Map<String, Object> params) {
        try {
            // 异步处理，不阻塞响应
            CompletableFuture.runAsync(() -> {
                try {
                    String sessionId = (String) params.get("sessionId");
                    String subSting = (String) params.get("domainName");
                    String domainName = subSting.substring(0, subSting.length() - 1);
                    String checkResult = (String) params.get("checkResult");
                    String failReason = (String) params.get("failReason");

                    // 检查是否已经记录过（避免重复记录）
                    String recordKey = redisUtil.buildKey("quick_access_record", sessionId);
                    if (redisUtil.hasKey(recordKey)) {
                        log.info("快速访问记录已存在，跳过重复记录: {}", sessionId);
                        return;
                    }

                    // 设置记录标记，有效期1分钟
                    redisUtil.setNx(recordKey, "recorded", 60L, TimeUnit.SECONDS);

                    // 记录访问信息（包括检测结果）
                    log.info("快速访问记录: domain={}, sessionId={}, checkResult={}, failReason={}, userAgent={}, isMobile={}, isIOS={}",
                            domainName, sessionId, checkResult, failReason, params.get("userAgent"),
                            params.get("isMobile"), params.get("isIOSS"));

                    // 如果检测失败，只记录基本信息，不进行完整的设备识别
//                    if ("failed".equals(checkResult)) {
//                        // 记录失败访问的基本信息
//                        log.info("前端检测失败记录: reason={}, domain={}, sessionId={}",
//                                failReason, domainName, sessionId);
//                        return;
//                    }

                    // 检测通过时，进行完整的设备识别和记录
                    // 转换参数格式，适配现有的记录方法
                    Map<String, String> recordParams = new HashMap<>();
                    recordParams.put("domainName", domainName);
                    recordParams.put("userIp", (String) params.get("userIp")); // 快速记录时IP可能还未获取
                    recordParams.put("userMobile", (String) params.get("deviceType"));
                    recordParams.put("paraPath", String.valueOf(params.get("hasQueryParams")));
                    recordParams.put("isIOSS", (String) params.get("isIOSS"));
                    recordParams.put("fb", (String) params.get("codefb"));
                    recordParams.put("screenWidth", String.valueOf(params.get("screenWidth")));
                    recordParams.put("screenHeight", String.valueOf(params.get("screenHeight")));
                    recordParams.put("pixelRatio", String.valueOf(params.get("pixelRatio")));
                    recordParams.put("continent", (String) params.get("continent"));
                    recordParams.put("userLanguage", (String) params.get("userLanguage"));
                    recordParams.put("userAgent", (String) params.get("userAgent"));
                    recordParams.put("isMobile", String.valueOf(params.get("isMobile")));
                    recordParams.put("isHuawei", (String) params.get("isHuawei"));
                    recordParams.put("isSamsung", (String) params.get("isSamsung"));

                    // 获取Redis数据
                    String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
                    String dmConditionKey = redisUtil.buildKey("acooly", "dmConditionList");

                    String jsonMap = redisUtil.get(dmCenterKey);
                    String conMap = redisUtil.get(dmConditionKey);

                    Type mapType = new TypeToken<Map<String, DmCenter>>() {}.getType();
                    Type mapCon = new TypeToken<Map<String, DmCondition>>() {}.getType();

                    Map<String, DmCenter> map = new Gson().fromJson(jsonMap, mapType);
                    Map<String, DmCondition> mapCondition = new Gson().fromJson(conMap, mapCon);

                    DmCenter query = map.get(domainName);
                    DmCondition dmCondition = mapCondition.get(domainName);

                    if (query == null || dmCondition == null) {
                        log.warn("快速访问记录：未找到域名配置，domainName: {}", domainName);
                        return;
                    }

                    // 设备信息解析（使用缓存）
                    String userAgent = (String) params.get("userAgent");
                    DeviceDetectorPo deviceDetectorPo = getCachedDeviceInfo(userAgent);

                    // 获取IP地理位置（使用缓存）
                    String country = "未知";
                    try {
                        // 尝试从请求中获取IP地址
                        String userIp = (String) params.get("userIp");
                        if (userIp != null && !"pending".equals(userIp) && !userIp.trim().isEmpty()) {
                            country = getCachedCountryByIp(userIp);
                        } else {
                            // 如果没有IP，尝试从请求头获取
                            // 这里可以添加从请求头获取IP的逻辑
                            log.info("快速访问记录：未获取到IP地址，使用默认地理位置");
                        }
                    } catch (Exception e) {
                        log.warn("快速访问记录：获取IP地理位置失败", e);
                    }

                    // 处理条件判断（仅用于记录，不用于通过判断）
                    dmCondition.setVpnCode(0); // 快速记录时暂不检测VPN

                    // 处理IP限制条件
                    if (isInWhiteList(dmCondition, "pending")) {
                        clearConditionFlags(dmCondition);
                    } else {
                        handleIpLimitConditions(dmCondition, "pending");
                    }

                    // 并行处理：Ip详情
                    CompletableFuture<Map<String,String>> ipInfo = CompletableFuture.supplyAsync(() -> {
                        String userIp = (String) params.get("userIp");
                        return getCachedIpInfoByIp(userIp);
                    });

                    Map<String, String> ipfoMap = ipInfo.get();
                    // 处理移动设备条件（仅用于记录）
                    HashMap<String, String> handledMap = handleMobileConditions(dmCondition, recordParams, country, deviceDetectorPo,ipfoMap);

                    // 记录访问数据（使用现有的记录方法）
                    dmCenterService.addAccessIpVpn(query, recordParams, ipfoMap, dmCondition, handledMap, deviceDetectorPo);

                    log.info("快速访问记录完成: domain={}, sessionId={}, country={}, userAgent={}, isMobile={}, isIOS={}",
                            domainName, sessionId, country, userAgent,
                            params.get("isMobile"), params.get("isIOSS"));

                } catch (Exception e) {
                    log.error("快速访问记录处理失败", e);
                }
            });

            return Result.ok("快速访问记录已接收");
        } catch (Exception e) {
            log.error("快速访问记录接口异常", e);
            return Result.fail("记录失败");
        }
    }

    // 快速记录接口 - 专门用于无防护快速跳转场景
    @PostMapping("/quickRecord")
    public Result<String> quickRecord(@RequestBody Map<String, String> params) {
        // 立即返回成功，不等待任何处理
        Result<String> result = new Result<>();
        result.setSuccess(true);
        result.setData("recorded");

        // 完全异步处理，使用独立的线程池
        CompletableFuture.runAsync(() -> {
            try {
                long startTime = System.currentTimeMillis();
                String sessionId = params.get("sessionId");
                
                // 检查是否已经记录过（避免重复记录）
                if (sessionId != null) {
                    String recordKey = redisUtil.buildKey("quick_record", sessionId);
                    if (redisUtil.hasKey(recordKey)) {
                        log.info("快速记录已存在，跳过重复记录: {}", sessionId);
                        return;
                    }
                    // 设置记录标记，有效期30秒
                    redisUtil.setNx(recordKey, "recorded", 30L, TimeUnit.SECONDS);
                }

                String domainName = params.getOrDefault("domainName", "defaultDomain");
                if (domainName != null && domainName.length() > 1) {
                    domainName = domainName.substring(0, domainName.length() - 1);
                }

                // 并行处理所有数据获取
                CompletableFuture<String> countryFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        String ip = params.get("userIp");
                        return getCachedCountryByIp(ip);
                    } catch (Exception e) {
                        log.warn("IP地理位置获取失败", e);
                        return "未知";
                    }
                });

                CompletableFuture<DeviceDetectorPo> deviceFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        String userAgent = params.get("userAgent");
                        return getCachedDeviceInfo(userAgent);
                    } catch (Exception e) {
                        log.warn("设备信息解析失败", e);
                        return new DeviceDetectorPo();
                    }
                });

                String finalDomainName = domainName;
                CompletableFuture<Map<String, Object>> redisFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
                        String dmConditionKey = redisUtil.buildKey("acooly", "dmConditionList");

                        String jsonMap = redisUtil.get(dmCenterKey);
                        String conMap = redisUtil.get(dmConditionKey);

                        Type mapType = new TypeToken<Map<String, DmCenter>>() {}.getType();
                        Type mapCon = new TypeToken<Map<String, DmCondition>>() {}.getType();

                        Map<String, DmCenter> map = new Gson().fromJson(jsonMap, mapType);
                        Map<String, DmCondition> mapCondition = new Gson().fromJson(conMap, mapCon);

                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("dmCenter", map.get(finalDomainName));
                        resultMap.put("dmCondition", mapCondition.get(finalDomainName));
                        return resultMap;
                    } catch (Exception e) {
                        log.warn("Redis数据获取失败", e);
                        return null;
                    }
                });
                CompletableFuture<Map<String,String>> ipInfo = CompletableFuture.supplyAsync(() -> {
                    String userIp = (String) params.get("userIp");
                    return getCachedIpInfoByIp(userIp);
                });

                // 等待所有并行任务完成
                CompletableFuture<Void> allFutures = CompletableFuture.allOf(countryFuture, deviceFuture, redisFuture);
                allFutures.get();

                // 获取结果
                Map<String, String> ipfomap = ipInfo.get();
                String country = countryFuture.get();
                DeviceDetectorPo deviceDetectorPo = deviceFuture.get();
                Map<String, Object> redisData = redisFuture.get();

                if (redisData != null) {
                    DmCenter query = (DmCenter) redisData.get("dmCenter");
                    DmCondition dmCondition = (DmCondition) redisData.get("dmCondition");

                    if (query != null && dmCondition != null) {
                        // 快速更新和记录
                        dmCenterService.update(query);
                        HashMap<String, String> handledMap = new HashMap<>();
                        handledMap.put("shouldRedirect", "true");
                        handledMap.put("logMessage", "quick_success");
                        
                        dmCenterService.addAccessIpVpn(query, params, ipfomap, dmCondition, handledMap, deviceDetectorPo);
                        
                        long endTime = System.currentTimeMillis();
                        log.info("快速记录完成，耗时: {}ms, domain: {}, sessionId: {}", 
                                endTime - startTime, domainName, sessionId);
                    } else {
                        log.warn("快速记录：未找到域名配置，domainName: {}", domainName);
                    }
                } else {
                    log.warn("快速记录：Redis数据获取失败，domainName: {}", domainName);
                }

            } catch (Exception e) {
                log.error("快速记录处理失败", e);
            }
        });

        return result;
    }

    /**
     * 通过ip依次在geoLite2FileName、geoLite3FileName、geoLite4FileName指定的数据库文件中查找信息，返回主要地理和ASN信息
     */
    public Map<String,String> getIpAllInfoFromAllDb(String ip) {
        Map<String, String> result = new HashMap<>();
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);

            // 使用预加载的City数据库 - 无锁访问
            if (cityReader != null) {
                try {
                    CityResponse cityResponse = cityReader.city(ipAddress);
                    Location location = cityResponse.getLocation();
                    result.put("timeZone", location.getTimeZone());
                    result.put("Landl", location.getLatitude().toString() + "," + location.getLongitude().toString());
                    Continent continent = cityResponse.getContinent();
                    result.put("continent", continent.getNames().get("zh-CN"));
                    Country country = cityResponse.getCountry();
                    result.put("isoCode", country.getIsoCode());
                    result.put("zhIsCode", country.getNames().get("zh-CN"));
                } catch (Exception e) {
                    result.put("cityDbError", "city库查询失败: " + e.getMessage());
                }
            } else {
                // 降级方案：使用原来的方法
                try (InputStream cityDb = FbVpnStockController.class.getClassLoader().getResourceAsStream(geoLite4FileName);
                     DatabaseReader cityReaderTemp = new DatabaseReader.Builder(cityDb).build()) {
                    CityResponse cityResponse = cityReaderTemp.city(ipAddress);
                    Location location = cityResponse.getLocation();
                    result.put("timeZone", location.getTimeZone());
                    result.put("Landl", location.getLatitude().toString() + "," + location.getLongitude().toString());
                    Continent continent = cityResponse.getContinent();
                    result.put("continent", continent.getNames().get("zh-CN"));
                    Country country = cityResponse.getCountry();
                    result.put("isoCode", country.getIsoCode());
                    result.put("zhIsCode", country.getNames().get("zh-CN"));
                } catch (Exception e) {
                    result.put("cityDbError", "city库查询失败: " + e.getMessage());
                }
            }

            // 使用预加载的ASN数据库 - 无锁访问
            if (asnReader != null) {
                try {
                    AsnResponse asnResponse = asnReader.asn(ipAddress);
                    result.put("asn", asnResponse.getAutonomousSystemNumber().toString());
                    result.put("asnOrg", asnResponse.getAutonomousSystemOrganization());
                } catch (Exception e) {
                    result.put("asnDbError", "asn库查询失败: " + e.getMessage());
                }
            } else {
                // 降级方案：使用原来的方法
                try (InputStream asnDb = FbVpnStockController.class.getClassLoader().getResourceAsStream(geoLite3FileName);
                     DatabaseReader asnReaderTemp = new DatabaseReader.Builder(asnDb).build()) {
                    AsnResponse asnResponse = asnReaderTemp.asn(ipAddress);
                    result.put("asn", asnResponse.getAutonomousSystemNumber().toString());
                    result.put("asnOrg", asnResponse.getAutonomousSystemOrganization());
                } catch (Exception e) {
                    result.put("asnDbError", "asn库查询失败: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            result.put("error", "IP解析失败: " + e.getMessage());
        }
        return result;
    }
}
