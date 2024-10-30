//package cn.itcast.hotel.web;
//
//import cn.itcast.hotel.entity.*;
//import cn.itcast.hotel.service.*;
//import cn.itcast.hotel.util.RedisUtils;
//import cn.itcast.hotel.util.Result;
//import com.google.gson.Gson;
//import com.google.gson.JsonSyntaxException;
//import com.google.gson.reflect.TypeToken;
//import com.maxmind.geoip2.DatabaseReader;
//import com.maxmind.geoip2.exception.GeoIp2Exception;
//import com.maxmind.geoip2.model.CityResponse;
//import com.maxmind.geoip2.model.CountryResponse;
//import com.maxmind.geoip2.record.Country;
//import lombok.extern.slf4j.Slf4j;
//import org.json.JSONObject;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.util.StringUtils;
//import org.springframework.web.bind.annotation.*;
//
//import javax.annotation.Resource;
//import java.io.*;
//import java.lang.reflect.Type;
//import java.net.HttpURLConnection;
//import java.net.InetAddress;
//import java.net.URL;
//import java.net.UnknownHostException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//@Slf4j
//@RestController
//@RequestMapping("TestStock")
//@CrossOrigin(origins = "*")
//public class testController {
//    @Resource
//    private LinkIntService linkIntService;
//    @Resource
//    private LinkSrcsService linkSrcsService;
//
//    @Resource
//    private DmDomainService dmDomainService;
//
//    @Resource
//    private DmCenterService dmCenterService;
//    @Resource
//    private DmConditionService dmConditionService;
//    @Resource
//    private DmTrollsService dmTrollsService;
//    @Autowired
//    private RedisUtils redisUtil;
//    @Value("${geolite2.file.name}")
//    private String geoLite2FileName;
//
//    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
//
//
//    @PostMapping("/getNumber")
//    public Result<DmCondition> getNumber(@RequestBody Map<String, String> params) throws Exception {
//        String domainName = params.getOrDefault("domainName", "defaultDomain");
//        String ip = params.getOrDefault("userIp", "127.0.0.1");
//        String userMobile = params.getOrDefault("userMobile", "true");
//        String paraPath = params.getOrDefault("paraPath", "true");
//        String country = getMAXCountryByIp(ip);
//        domainName = domainName.substring(0, domainName.length() - 1);
//
//        // 使用并行流处理Redis读取
//        CompletableFuture<Map<String, DmCenter>> dmCenterFuture = CompletableFuture.supplyAsync(() -> {
//            String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
//            String jsonMap = redisUtil.get(dmCenterKey);
//            Type mapType = new TypeToken<Map<String, DmCenter>>() {}.getType();
//            return new Gson().fromJson(jsonMap, mapType);
//        });
//
//        CompletableFuture<Map<String, DmCondition>> dmConditionFuture = CompletableFuture.supplyAsync(() -> {
//            String dmConditionKey = redisUtil.buildKey("acooly", "dmConditionList");
//            String conMap = redisUtil.get(dmConditionKey);
//            Type mapCon = new TypeToken<Map<String, DmCondition>>() {}.getType();
//            return new Gson().fromJson(conMap, mapCon);
//        });
//
//        // 等待所有任务完成
//        CompletableFuture.allOf(dmCenterFuture, dmConditionFuture).join();
//
//        // 获取结果
//        Map<String, DmCenter> map = dmCenterFuture.get();
//        Map<String, DmCondition> mapCondition = dmConditionFuture.get();
//
//        DmCenter query = map.get(domainName);
//        DmCondition dmCondition = mapCondition.get(domainName);
//
//        // 处理VPN信息
//        CompletableFuture<Void> vpnCodeFuture = CompletableFuture.runAsync(() -> {
//            String builtKey = redisUtil.buildKey("acooly", "countryVpn");
//            String string = redisUtil.get(builtKey);
//            List<String> conuntryList = new Gson().fromJson(string, new TypeToken<List<String>>() {}.getType());
//            String keyVpn = redisUtil.buildKey("acooly", "keyVpn");
//            String keyString = redisUtil.get(keyVpn);
//
//            if (dmCondition.getIsVpn() == 1) {
//                String proKey = redisUtil.buildKey("acooly", "getIpInfoVpn");
//                String proxyIp = redisUtil.get(proKey);
//                List<String> proxyIpList = new Gson().fromJson(proxyIp, new TypeToken<List<String>>() {}.getType());
//                if (proxyIpList.contains(ip)) {
//                    dmCondition.setVpnCode(1);
//                }else {
//                    int vpnCode = dmConditionService.getIpVpn(conuntryList, keyString, ip);
//                    dmCondition.setVpnCode(vpnCode);
//                    if (vpnCode==1){
//                        proxyIpList.add(ip);
//                        redisUtil.set(proKey, new Gson().toJson(proxyIpList));
//                    }
//                }
//            } else {
//                dmCondition.setVpnCode(0);
//            }
//        });
//
//        vpnCodeFuture.get(); // 等待CompletableFuture完成
//
//        if (dmCondition.getIsIp() == 1 && country.equals(dmCondition.getIpCountry())) {
//            dmCondition.setIpCountry("true");
//        }
//
//        // 并行更新dmCenter信息
//        CompletableFuture.runAsync(() -> dmCenterService.update(query));
//        CompletableFuture.runAsync(() -> dmCenterService.addAccess(query, ip, dmCondition, country, userMobile, paraPath));
//
//        return Result.ok(dmCondition);
//    }
//
//    public String getFBStockCountryByIp(String ip) {
//        String url = "https://ipapi.co/" + ip + "/json/";
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
//            StringBuilder json = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                json.append(line);
//            }
//            JSONObject jsonObject = new JSONObject(json.toString());
//            return jsonObject.getString("country");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "未知";
//        }
//    }
//
//    public String getCountryByIp(String ip) {
//        try {
//            // 使用 InputStream 读取资源文件
//            InputStream database = FbStockController.class.getClassLoader().getResourceAsStream(geoLite2FileName);
//            if (database == null) {
//                throw new IllegalArgumentException("Database file not found!");
//            }
//
//            // 使用 MaxMind 的 GeoIP2 库读取文件并进行 IP 查询
//            try (DatabaseReader reader = new DatabaseReader.Builder(database).build()) {
//                InetAddress ipAddress = InetAddress.getByName(ip);
//                CountryResponse response = reader.country(ipAddress);
//                Country country = response.getCountry();
//                return country.getIsoCode();
//            }
//        } catch (IOException | GeoIp2Exception e) {
//            e.printStackTrace();
//            return "未知";
//        }
//    }
//
//    public String getMAXCountryByIp(String ip) {
//        try {
//            // 使用 ClassPathResource 来加载数据库文件
//            ClassPathResource resource = new ClassPathResource("templates/GeoLite2-Country.mmdb");
//            File database = resource.getFile();
//
//            // 创建 DatabaseReader
//            DatabaseReader reader = new DatabaseReader.Builder(database).build();
//
//            // 获取 IP 地址信息
//            InetAddress ipAddress = InetAddress.getByName(ip);
//            CountryResponse response = reader.country(ipAddress);
//
//            // 获取国家信息
//            Country country = response.getCountry();
//            return country.getIsoCode(); // 获取国家缩写
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "未知";
//        }
//    }
//
//
//    @PostMapping("/getFbNumber")
//    public Result<DmCondition> getMAXCountryByIp(@RequestBody Map<String, String> params) throws Exception {
//        String domainName = params.getOrDefault("domainName", "defaultDomain");
//        String ip = params.getOrDefault("userIp", "127.0.0.1");
//        String userMobile = params.getOrDefault("userMobile", "true");
//        String paraPath = params.getOrDefault("paraPath", "true");
//        String country = getCountryByIp(ip);
//        domainName = domainName.substring(0, domainName.length() - 1);
//
//        String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
//        String jsonMap = redisUtil.get(dmCenterKey);
//        Type mapType = new TypeToken<Map<String, DmCenter>>() {}.getType();
//        Map<String, DmCenter> map = new Gson().fromJson(jsonMap, mapType);
//        DmCenter query = map.get(domainName);
//
//        String dmConditionKey = redisUtil.buildKey("acooly", "dmConditionList");
//        String conMap = redisUtil.get(dmConditionKey);
//        Type mapCon = new TypeToken<Map<String, DmCondition>>() {}.getType();
//        Map<String, DmCondition> mapCondition = new Gson().fromJson(conMap, mapCon);
//        DmCondition dmCondition = mapCondition.get(domainName);
//
//        if (dmCondition.getIsVpn() == 1){
//            String builtKey = redisUtil.buildKey("acooly", "countryVpn");
//            String string = redisUtil.get(builtKey);
//            List<String> countryList = new Gson().fromJson(string, new TypeToken<List<String>>() {}.getType());
//            String keyVpn = redisUtil.buildKey("acooly", "keyVpn");
//            String keyString = redisUtil.get(keyVpn);
//            dmCondition.setVpnCode(dmConditionService.getIpVpn(countryList, keyString, ip));
//        } else {
//            dmCondition.setVpnCode(0);
//        }
//
//        if (dmCondition.getIsIp() == 1) {
//            if (country.equals(dmCondition.getIpCountry())) {
//                dmCondition.setIpCountry("true");
//            }
//        }
//
//        //先判断ip是否开启白名单并且是否在白名单中 在判断是否开启ips点击数限制
//        if (dmCondition.getIpWhite() == 1 && dmCondition.getWhiteList().equals(ip)) {
//            // 清空某些标志
//            dmCondition.setTimeZone(0);
//            dmCondition.setIsChinese(0);
//            dmCondition.setIsMobile(0);
//            dmCondition.setIsSpecificDevice(0);
//            dmCondition.setIsFbclid(0);
//            dmCondition.setIsIp(0);
//            dmCondition.setIsVpn(0);
//        } else {
//            // 判断是否开启IP点击数限制
//            if (dmCondition.getIpLimits() == 1) {
//                String ipLimitsKey = redisUtil.buildKey("AoollyNumberIp", dmCondition.getAccessAddress());
//                String ipLimits = redisUtil.get(ipLimitsKey);
//                List<String> ipList = new ArrayList<>();
//                // 是否有该 key
//                if (ipLimits != null) {
//                    // 进行解析
//                    try {
//                        // 进行解析
//                        ipList = new Gson().fromJson(ipLimits, new TypeToken<List<String>>() {}.getType());
//                    } catch (JsonSyntaxException e) {
//                        // 处理 JSON 解析错误
//                        log.error("Failed to parse IP limits from Redis for key: {}", ipLimitsKey, e);
//                    }
//                }
//
//                // 查看当前 IP 是否在 ipList 中的出现次数超过三次
//                long count = ipList.stream().filter(s -> s.equals(ip)).count();
//                if (count >= 3) {
//                    dmCondition.setIsVpn(1);
//                    dmCondition.setVpnCode(1);
//                }
//            }
//        }
//
//
//
//
//        dmCenterService.update(query);
//        dmCenterService.addAccess(query, ip, dmCondition, country, userMobile, paraPath);
//        return Result.ok(dmCondition);
//    }
//
//    @PostMapping("/getFbLink")
//    public Result<Map<String, Object>> getFbLink(@RequestBody Map<String, String> params) {
//        String domainName = params.getOrDefault("domainName", "defaultDomain");
//        String ip = params.getOrDefault("userIp", "127.0.0.1");
//        String userMobile = params.getOrDefault("userMobile", "true");
//        String paraPath = params.getOrDefault("paraPath", "true");
//        String country = getCountryByIp(ip);
//        HashMap<String, Object> map = new HashMap<>();
//        String link = "";
//        DmDomain domain = new DmDomain();
//        //单表单的接口
//        domainName = domainName.substring(0, domainName.length() - 1);
//        domain.setDomainName(domainName);
//        String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
//        String jsonMap = redisUtil.get(dmCenterKey);
//        Type mapType = new TypeToken<Map<String, DmCenter>>() {
//        }.getType();
//        Map<String, DmCenter> mapDmCenter = new Gson().fromJson(jsonMap, mapType);
//        DmCenter query = mapDmCenter.get(domainName);
//        //判断是否需要轮询
//        if (query.getDiversion() == 1) {
//            LinkInt linkInt = new LinkInt();
//            linkInt.setDomain(domainName);
//            LinkInt linkInt1 = linkIntService.query(linkInt);
//            Integer id = linkInt1.getCountLink();
//            LinkSrcs linkSrcs2 = new LinkSrcs();
//            linkSrcs2.setDomain(domainName);
//            List<LinkSrcs> linkSrcs3 = linkSrcsService.quertList(linkSrcs2);
//            LinkSrcs linkSrcs4 = linkSrcs3.get(id - 1);
//            if (linkSrcs3.size() == id) {
//                id = 1;
//            } else {
//                id = id + 1;
//            }
//            linkInt1.setCountLink(id);
//            linkIntService.update(linkInt1);
//            link = linkSrcs4.getLinkSrc();
//        } else {
//            link = dmDomainService.queryString(domain);
//        }
//        if (!StringUtils.isEmpty(link)) {
//            map.put("link", link);
//        } else {
//            map.put("link", "http://www.baidu.com");
//        }
//        String dmConditionKey = redisUtil.buildKey("acooly", "dmConditionList");
//        String conMap = redisUtil.get(dmConditionKey);
//        Type mapCon = new TypeToken<Map<String, DmCondition>>() {
//        }.getType();
//        Map<String, DmCondition> mapCondition = new Gson().fromJson(conMap, mapCon);
//        DmCondition dmCondition = mapCondition.get(domainName);
//
//        if (dmCondition.getIsVpn() == 1) {
//            String builtKey = redisUtil.buildKey("acooly", "countryVpn");
//            String string = redisUtil.get(builtKey);
//            List<String> conuntryList = new Gson().fromJson(string, new TypeToken<List<String>>() {
//            }.getType());
//            String keyVpn = redisUtil.buildKey("acooly", "keyVpn");
//            String keyString = redisUtil.get(keyVpn);
//            dmCondition.setVpnCode(dmConditionService.getIpVpn(conuntryList, keyString, ip));
//        }
//        if (dmCondition.getIsIp() == 1) {
//            if (country.equals(dmCondition.getIpCountry())) {
//                dmCondition.setIpCountry("true");
//            }
//        }
//
//        //先判断ip是否开启白名单并且是否在白名单中 在判断是否开启ips点击数限制
//        if (dmCondition.getIpWhite() == 1 && dmCondition.getWhiteList().equals(ip)) {
//            // 清空某些标志
//            dmCondition.setTimeZone(0);
//            dmCondition.setIsChinese(0);
//            dmCondition.setIsMobile(0);
//            dmCondition.setIsSpecificDevice(0);
//            dmCondition.setIsFbclid(0);
//            dmCondition.setIsIp(0);
//            dmCondition.setIsVpn(0);
//        } else {
//            // 判断是否开启IP点击数限制
//            if (dmCondition.getIpLimits() == 1) {
//                String ipLimitsKey = redisUtil.buildKey("AoollyNumberIp", dmCondition.getAccessAddress());
//                String ipLimits = redisUtil.get(ipLimitsKey);
//                List<String> ipList = new ArrayList<>();
//                // 是否有该 key
//                if (ipLimits != null) {
//                    // 进行解析
//                    try {
//                        // 进行解析
//                        ipList = new Gson().fromJson(ipLimits, new TypeToken<List<String>>() {}.getType());
//                    } catch (JsonSyntaxException e) {
//                        // 处理 JSON 解析错误
//                        log.error("Failed to parse IP limits from Redis for key: {}", ipLimitsKey, e);
//                    }
//                }
//
//                // 查看当前 IP 是否在 ipList 中的出现次数超过三次
//                long count = ipList.stream().filter(s -> s.equals(ip)).count();
//                if (count >= 3) {
//                    dmCondition.setIsVpn(1);
//                    dmCondition.setVpnCode(1);
//                }
//            }
//        }
//        dmCenterService.addAccess(query, ip, dmCondition, country, userMobile, paraPath);
//        dmCenterService.update(query);
//        map.put("dmCondition", dmCondition);
//        return Result.ok(map);
//    }
//
//
//
//    @PostMapping("/addTrollsIp")
//    public Result addTrollsIp(@RequestBody Map<String, String> params) {
//        try {
//            String domainName = params.getOrDefault("domainName", "defaultDomain");
//            String ip = params.getOrDefault("userIp", "127.0.0.1");
//            String userMobile = params.getOrDefault("userMobile", "true");
//            String paraPath = params.getOrDefault("paraPath", "true");
//            String country = getCountryByIp(ip);
//            domainName = domainName.substring(0, domainName.length() - 1);
//
//            String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
//            String jsonMap = redisUtil.get(dmCenterKey);
//            Type mapType = new TypeToken<Map<String, DmCenter>>() {}.getType();
//            Map<String, DmCenter> map = new Gson().fromJson(jsonMap, mapType);
//            DmCenter query = map.get(domainName);
//            dmCenterService.updateTrolls(query);
//            dmCenterService.addTrolls(query, ip,country,userMobile,paraPath);
//        } catch (JsonSyntaxException e) {
//            throw new RuntimeException(e);
//        }
//        return Result.ok();
//    }
//
//
//
//
//
//
//
//}
