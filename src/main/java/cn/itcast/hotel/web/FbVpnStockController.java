package cn.itcast.hotel.web;

import cn.itcast.hotel.entity.*;
import cn.itcast.hotel.service.*;
import cn.itcast.hotel.util.RedisUtils;
import cn.itcast.hotel.util.Result;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Country;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

@Slf4j
@RestController
@RequestMapping("fbVpnStock")
@CrossOrigin(origins = "*")
public class FbVpnStockController {
    @Autowired
    private RedisUtils redisUtil;
    @Value("${geolite2.file.name}")
    private String geoLite2FileName;
    @Resource
    private LinkIntService linkIntService;
    @Resource
    private LinkSrcsService linkSrcsService;

    @Resource
    private DmDomainService dmDomainService;

    @Resource
    private DmModlesService dmModlesService;

    @Resource
    private DmCenterService dmCenterService;
    @Resource
    private DmConditionService dmConditionService;
    @Resource
    private DmAccessService dmAccessService;


    private Integer getVpnSetCode(DmCondition dmCondition , String ip ){
        String builtKey = redisUtil.buildKey("acooly", "countryVpn");
        String string = redisUtil.get(builtKey);
        List<String> conuntryList = new Gson().fromJson(string, new TypeToken<List<String>>() {}.getType());
        String keyVpn = redisUtil.buildKey("acooly", "keyVpn");
        String keyString = redisUtil.get(keyVpn);
        String proKey = redisUtil.buildKey("acooly", "getIpInfoVpn");
        String proxyIp = redisUtil.get(proKey);
        List<String> proxyIpList = new Gson().fromJson(proxyIp, new TypeToken<List<String>>() {}.getType());
        if (proxyIpList.contains(ip)) {
            return 1;
        }else {
            int vpnCode = dmConditionService.getIpApiVpn(conuntryList, keyString, ip);
            dmCondition.setVpnCode(vpnCode);
            if (vpnCode==1){
                proxyIpList.add(ip);
                redisUtil.set(proKey, new Gson().toJson(proxyIpList));
            }
            return vpnCode;
        }
    }

    @PostMapping("/getNumber")
    public Result<DmResult> getNumber(@RequestBody Map<String, String> params) throws Exception {
        Result<DmResult> result = new Result<>();
        DmResult dmResult = new DmResult();
        String domainName = params.getOrDefault("domainName", "defaultDomain");
        domainName = domainName.substring(0, domainName.length() - 1);
        String country = getCountrySetByIp(params.get("userIp"));
        String ip = params.get("userIp");

        String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
        String jsonMap = redisUtil.get(dmCenterKey);
        Type mapType = new TypeToken<Map<String, DmCenter>>() {}.getType();
        Map<String, DmCenter> map = new Gson().fromJson(jsonMap, mapType);
        DmCenter query = map.get(domainName);

        String dmConditionKey = redisUtil.buildKey("acooly", "dmConditionList");
        String conMap = redisUtil.get(dmConditionKey);
        Type mapCon = new TypeToken<Map<String, DmCondition>>() {}.getType();
        Map<String, DmCondition> mapCondition = new Gson().fromJson(conMap, mapCon);
        DmCondition dmCondition = mapCondition.get(domainName);

        if (dmCondition.getIsVpn() == 1){
            dmCondition.setVpnCode(getVpnSetCode(dmCondition,ip));
        } else {
            dmCondition.setVpnCode(0);
        }

        if (dmCondition.getIsIp() == 1) {
            if (country.equals(dmCondition.getIpCountry())) {
                dmCondition.setIpCountry("true");
            }
        }
        //判断访问手机类型 并且是苹果手机的时候，判断是否是禁用的手机类型
        if("Mobile".equals(params.get("userMobile")) && "iOS".equals(params.get("isIOSS"))){
            String buildKey = redisUtil.buildKey("Acoolys", "dmModles");
            String modelsMap = redisUtil.get(buildKey);
            Type modelsType = new TypeToken<List<DmModles>>() {}.getType();
            List<DmModles> modelsList = new Gson().fromJson(modelsMap, modelsType);
            Integer screenWidth= Integer.valueOf(params.get("screenWidth"));
            Integer screenHeight= Integer.valueOf(params.get("screenHeight"));
            Integer pixelRatio= Integer.valueOf(params.get("pixelRatio"));
            modelsList.forEach(s->{
                if (s.getScreenWidth()==screenWidth && s.getScreenHeight()==screenHeight && pixelRatio==s.getPixelRatio()){
                    dmCondition.setTimeZone(1);
                    dmCondition.setIsChinese(1);
                    dmCondition.setIsMobile(1);
                    dmCondition.setIsSpecificDevice(1);
                    dmCondition.setIsFbclid(1);
                    dmCondition.setIsIp(1);
                    dmCondition.setIsVpn(1);
                }
            });
        }


        //先判断ip是否开启白名单并且是否在白名单中 在判断是否开启ips点击数限制
        if (dmCondition.getIpWhite() == 1 && dmCondition.getWhiteList().equals(ip)) {
            // 清空某些标志
            dmCondition.setTimeZone(0);
            dmCondition.setIsChinese(0);
            dmCondition.setIsMobile(0);
            dmCondition.setIsSpecificDevice(0);
            dmCondition.setIsFbclid(0);
            dmCondition.setIsIp(0);
            dmCondition.setIsVpn(0);
        } else {
            // 判断是否开启IP点击数限制
            if (dmCondition.getIpLimits() == 1) {
                String ipLimitsKey = redisUtil.buildKey("AoollyNumberIp", dmCondition.getAccessAddress());
                String ipLimits = redisUtil.get(ipLimitsKey);
                List<String> ipList = new ArrayList<>();
                // 是否有该 key
                if (ipLimits != null) {
                    // 进行解析
                    try {
                        // 进行解析
                        ipList = new Gson().fromJson(ipLimits, new TypeToken<List<String>>() {}.getType());
                    } catch (JsonSyntaxException e) {
                        // 处理 JSON 解析错误
                        log.error("Failed to parse IP limits from Redis for key: {}", ipLimitsKey, e);
                    }
                }

                // 查看当前 IP 是否在 ipList 中的出现次数超过三次
                long count = ipList.stream().filter(s -> s.equals(ip)).count();
                if (count >= 3) {
                    dmCondition.setIsVpn(1);
                    dmCondition.setVpnCode(1);
                }
            }
        }
        //判断所有条件是否过关
        if(dmCondition.getTimeZone()==1 || dmCondition.getIsChinese()==1 || dmCondition.getIsMobile()==1 || dmCondition.getIsSpecificDevice()==1
                || dmCondition.getIsFbclid()==1 || dmCondition.getIsIp()==1 || dmCondition.getIsVpn()==1){
            dmResult.setDataSuccess(false);
        }else {
            // 所有条件都通过，可以进行访问
            dmResult.setDataSuccess(true);
        }
        result.setData(dmResult);
        dmCenterService.update(query);

        return result;
    }
    //无防分流
    @PostMapping("/protectGetLink")
    public Result<DmResult> protectGetLink(@RequestBody Map<String, String> params) throws Exception {
        Result<DmResult> result = new Result<>();
        HashMap<String, String> handledMap = new HashMap<>();
        DmResult dmResult = new DmResult();
        DmCondition dmCondition = null;
        DmCenter query = null;
        String country="";
        try {
            String domainName = params.getOrDefault("domainName", "defaultDomain").substring(0, params.get("domainName").length() - 1);
            String ip = params.get("userIp");
            country = getCountrySetByIp(ip);
            String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
            String jsonMap = redisUtil.get(dmCenterKey);
            Type mapType = new TypeToken<Map<String, DmCenter>>() {}.getType();
            Map<String, DmCenter> map = new Gson().fromJson(jsonMap, mapType);
            query = map.get(domainName);

            String dmConditionKey = redisUtil.buildKey("acooly", "dmConditionList");
            String conMap = redisUtil.get(dmConditionKey);
            Type mapCon = new TypeToken<Map<String, DmCondition>>() {}.getType();
            Map<String, DmCondition> mapCondition = new Gson().fromJson(conMap, mapCon);
            dmCondition = mapCondition.get(domainName);
            //获取轮询链接
            String fbLink = getFbLink(domainName, query);
            dmResult.setLink(fbLink);
            result.setSuccess(true);
        } catch (Exception e) {
            result.setSuccess(false);
        }
        dmCenterService.update(query);
        handledMap.put("shouldRedirect", "true");
        handledMap.put("logMessage","success");
        dmCenterService.addAccessVpn(query,params,country,dmCondition,handledMap);
        result.setData(dmResult);
        return result;
    }
    //无防护记录点击数
    @PostMapping("/protectLink")
    public Result<DmResult> protectLink(@RequestBody Map<String, String> params) throws Exception {
        Result<DmResult> result = new Result<>();
        HashMap<String, String> handledMap = new HashMap<>();
        try {
            String domainName = params.getOrDefault("domainName", "defaultDomain").substring(0, params.get("domainName").length() - 1);
            String ip = params.get("userIp");
            String country = getCountrySetByIp(ip);
            String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
            String jsonMap = redisUtil.get(dmCenterKey);
            Type mapType = new TypeToken<Map<String, DmCenter>>() {}.getType();
            Map<String, DmCenter> map = new Gson().fromJson(jsonMap, mapType);
            DmCenter query = map.get(domainName);

            String dmConditionKey = redisUtil.buildKey("acooly", "dmConditionList");
            String conMap = redisUtil.get(dmConditionKey);
            Type mapCon = new TypeToken<Map<String, DmCondition>>() {}.getType();
            Map<String, DmCondition> mapCondition = new Gson().fromJson(conMap, mapCon);
            DmCondition dmCondition = mapCondition.get(domainName);
            dmCenterService.update(query);
            handledMap.put("shouldRedirect", "true");
            handledMap.put("logMessage","success");
            dmCenterService.addAccessVpn(query,params,country,dmCondition,handledMap);
            result.setSuccess(true);
        } catch (JsonSyntaxException e) {
            result.setSuccess(false);
        }
        return result;
    }



    @PostMapping("/getShuntLink")
    public Result<DmResult> getShuntLink(@RequestBody Map<String, String> params) throws Exception {
        Result<DmResult> result = new Result<>();
        DmResult dmResult = new DmResult();
        DmCondition dmCondition = null;
        DmCenter query = null;
        String country="";
        Boolean shouldRedirect = true;
        HashMap<String, String> handledMap = new HashMap<>();
        try {
            String domainName = params.getOrDefault("domainName", "defaultDomain").substring(0, params.get("domainName").length() - 1);
            String ip = params.get("userIp");
            country = getCountrySetByIp(ip);
            String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
            String jsonMap = redisUtil.get(dmCenterKey);
            Type mapType = new TypeToken<Map<String, DmCenter>>() {}.getType();
            Map<String, DmCenter> map = new Gson().fromJson(jsonMap, mapType);
            query = map.get(domainName);

            String dmConditionKey = redisUtil.buildKey("acooly", "dmConditionList");
            String conMap = redisUtil.get(dmConditionKey);
            Type mapCon = new TypeToken<Map<String, DmCondition>>() {}.getType();
            Map<String, DmCondition> mapCondition = new Gson().fromJson(conMap, mapCon);
            dmCondition = mapCondition.get(domainName);
            //获取轮询链接
            String fbLink = getFbLink(domainName, query);

            dmCondition.setVpnCode(dmCondition.getIsVpn() == 1 ? getVpnSetCode(dmCondition, ip) : 0);
            if (isInWhiteList(dmCondition, ip)) {
                clearConditionFlags(dmCondition);
            } else {
                handleIpLimitConditions(dmCondition, ip);
            }
            handledMap =  handleMobileConditions(dmCondition, params, country);
            String redirect = handledMap.get("shouldRedirect");
            shouldRedirect = Boolean.parseBoolean(redirect);
            dmResult.setDataSuccess(shouldRedirect);
            dmResult.setLink(fbLink);
            result.setData(dmResult);
            result.setSuccess(true);
        } catch (Exception e) {
            result.setSuccess(false);
        }
        //改变主数据的点击数
        dmCenterService.update(query);
        dmCenterService.addAccessVpn(query,params,country,dmCondition,handledMap);
        return result;
    }

    private String getFbLink(String domainName,DmCenter query){
        String link = "";
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
        } else {
            link = dmDomainService.queryString(domain);
        }
        if (!StringUtils.isEmpty(link)) {
            return link;
        } else {
            return "https://www.google.com";
        }
    }


    @PostMapping("/getVpnNumber")
    public Result<DmResult> getVpnNumber(@RequestBody Map<String, String> params) throws Exception {
        Result<DmResult> result = new Result<>();
        DmResult dmResult = new DmResult();
        DmCondition dmCondition = null;
        DmCenter query = null;
        String country="";
        Boolean shouldRedirect = true;
        HashMap<String, String> handledMap = new HashMap<>();
        try {
            String domainName = params.getOrDefault("domainName", "defaultDomain").substring(0, params.get("domainName").length() - 1);
            String ip = params.get("userIp");
            country = getCountrySetByIp(ip);

            String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
            String jsonMap = redisUtil.get(dmCenterKey);
            Type mapType = new TypeToken<Map<String, DmCenter>>() {}.getType();
            Map<String, DmCenter> map = new Gson().fromJson(jsonMap, mapType);
            query = map.get(domainName);

            String dmConditionKey = redisUtil.buildKey("acooly", "dmConditionList");
            String conMap = redisUtil.get(dmConditionKey);
            Type mapCon = new TypeToken<Map<String, DmCondition>>() {}.getType();
            Map<String, DmCondition> mapCondition = new Gson().fromJson(conMap, mapCon);
            dmCondition = mapCondition.get(domainName);

            dmCondition.setVpnCode(dmCondition.getIsVpn() == 1 ? getVpnSetCode(dmCondition, ip) : 0);
            if (isInWhiteList(dmCondition, ip)) {
                clearConditionFlags(dmCondition);
            } else {
                handleIpLimitConditions(dmCondition, ip);
            }
            handledMap =  handleMobileConditions(dmCondition, params, country);
            String redirect = handledMap.get("shouldRedirect");
            shouldRedirect = Boolean.parseBoolean(redirect);
            dmResult.setDataSuccess(shouldRedirect);
            result.setData(dmResult);
            result.setSuccess(true);
        } catch (JsonSyntaxException e) {
            result.setSuccess(false);
        }
        //改变主数据的点击数
        dmCenterService.addAccessVpn(query,params,country,dmCondition,handledMap);
        dmCenterService.update(query);
        log.info( "成功还是失败："+dmResult.toString());
        return result;
    }




    private String getCountrySetByIp(String ip) {
        try {
            // 检查是否是本地 IP
            if ("127.0.0.1".equals(ip) || "localhost".equals(ip) ||"".equals(ip)) {
                return "Localhost"; // 或者返回其他表示本地的默认值
            }
            // 使用 InputStream 读取资源文件
            InputStream database = FbStockController.class.getClassLoader().getResourceAsStream(geoLite2FileName);
            if (database == null) {
                throw new IllegalArgumentException("Database file not found!");
            }

            // 使用 MaxMind 的 GeoIP2 库读取文件并进行 IP 查询
            try (DatabaseReader reader = new DatabaseReader.Builder(database).build()) {
                InetAddress ipAddress = InetAddress.getByName(ip);
                CountryResponse response = reader.country(ipAddress);
                Country country = response.getCountry();
                return country.getIsoCode();
            }
        } catch (IOException | GeoIp2Exception e) {
            e.printStackTrace();
            return "未知";
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

//    private HashMap<String, String> handleMobileConditions(DmCondition dmCondition, Map<String, String> params, String country) {
//        HashMap<String, String> map = new HashMap<>();
//        AtomicReference<Boolean> shouldRedirect = new AtomicReference<>(true);
//        StringBuilder logMessages = new StringBuilder();
//
//        // 用于处理条件检查和记录日志的工具方法
//        BiConsumer<String, Boolean> checkAndLog = (message, condition) -> {
//            if (!condition) {
//                shouldRedirect.set(false);
//                appendLogMessage(logMessages, message);
//            }
//        };
//
//        // 判断时区是否符合要求
//        if (dmCondition.getTimeZone() == 1) {
//            String timeZone = params.get("continent");
//            boolean isMatch = dmCondition.getTimeContinent().equals(timeZone);
//            checkAndLog.accept("\"时区\": \"未通过\"", isMatch);
//        }
//
//        // 判断国家ip是否正常
//        if (dmCondition.getIsIp() == 1) {
//            boolean isMatch = country.equals(dmCondition.getIpCountry());
//            checkAndLog.accept("\"IP国家限制\": \"未通过\"", isMatch);
//        }
//
//        // 判断是否是中文访问
//        if (dmCondition.getIsChinese() == 1) {
//            String isChinese = params.get("isChinese");
//            boolean isMatch = "false".equals(isChinese);
//            checkAndLog.accept("\"中文检测\": \"未通过\"", isMatch);
//        }
//
//        // 判断是否是手机访问
//        if (dmCondition.getIsMobile() == 1) {
//            String isMobile = params.get("isMobile");
//            boolean isMatch = "true".equals(isMobile);
//            checkAndLog.accept("\"设备必须为手机\": \"未通过\"", isMatch);
//        }
//
//        // 判断vpn是否过关
//        if (dmCondition.getIsVpn() == 1) {
//            boolean isMatch = dmCondition.getVpnCode() == 0;
//            checkAndLog.accept("\"Vpn\": \"未通过\"", isMatch);
//        }
//
//        // 判断访问的是否包含华为
//        if ("Huawei".equals(params.get("isHuawei"))) {
//            shouldRedirect.set(false);
//            appendLogMessage(logMessages, "\"是否为华为\": \"未通过\"");
//        }
//
//        // 判断是否是特定设备访问
//        if (dmCondition.getIsSpecificDevice() == 1) {
//            if ("Mobile".equals(params.get("userMobile")) && "iOS".equals(params.get("isIOSS"))) {
//                String buildKey = redisUtil.buildKey("Acoolys", "dmModles");
//                String modelsMap = redisUtil.get(buildKey);
//                Type modelsType = new TypeToken<List<DmModles>>() {}.getType();
//                List<DmModles> modelsList = new Gson().fromJson(modelsMap, modelsType);
//
//                Integer screenWidth = Integer.valueOf(params.get("screenWidth"));
//                Integer screenHeight = Integer.valueOf(params.get("screenHeight"));
//                Integer pixelRatio = Integer.valueOf(params.get("pixelRatio"));
//
//                // 查找符合条件的设备
//                boolean matchingDeviceFound = modelsList.stream()
//                        .anyMatch(s -> s.getScreenWidth().equals(screenWidth)
//                                && s.getScreenHeight().equals(screenHeight)
//                                && s.getPixelRatio().equals(pixelRatio));
//
//                // 如果找到了匹配的设备
//                if (matchingDeviceFound) {
//                    shouldRedirect.set(false);
//                    appendLogMessage(logMessages, "\"设定的低端指定设备\": \"未通过\"");
//                }
//            }
//        }
//
//        map.put("shouldRedirect", String.valueOf(shouldRedirect));
//        map.put("logMessage", logMessages.toString());
//        return map;
//    }

    private HashMap<String,String> handleMobileConditions(DmCondition dmCondition, Map<String, String> params,String country) {
        HashMap<String, String> map = new HashMap<>();
        Boolean shouldRedirect = true;
        StringBuilder logMessages = new StringBuilder();
        //待开发 记录不过关的信息返回给前端

        //判断时区是否符合要求
        if (dmCondition.getTimeZone() == 1) {
            String timeZone = params.get("continent");
            boolean equals = dmCondition.getTimeContinent().equals(timeZone);
            shouldRedirect &= equals;
            logIfNotRedirect(equals, "Timezone 未通过");
            if(!equals){
                appendLogMessage(logMessages, "\"时区\": \"未通过\"");
            }
        }

        //判断国家ip是否正常
        if (dmCondition.getIsIp() == 1){
            boolean equals = country.equals(dmCondition.getIpCountry());
            shouldRedirect &= equals;
            logIfNotRedirect(equals, "IP国家限制 未通过");
            if(!equals){
                appendLogMessage(logMessages, "\"IP国家限制\": \"未通过\"");
            }
        }

        // 判断是否是中文访问
        if (dmCondition.getIsChinese() == 1) {
            String isChinese = params.get("isChinese");
            boolean equals = "false".equals(isChinese);
            shouldRedirect &= equals;
            logIfNotRedirect(equals, "IsChinese 未通过");
            if(!equals){
                appendLogMessage(logMessages, "\"中文检测\": \"未通过\"");
            }
        }

        // 判断是否是手机访问
        if (dmCondition.getIsMobile() == 1) {
            String isMobile = params.get("isMobile");
            boolean equals = "true".equals(isMobile);
            shouldRedirect &= equals;
            logIfNotRedirect(equals, "IsMobile 未通过");
            if(!equals){
                appendLogMessage(logMessages, "\"设备必须为手机\": \"未通过\"");
            }
        }

        //判断vpn是否过关
        if(dmCondition.getIsVpn() == 1) {
            boolean equals = dmCondition.getVpnCode() == 0;
            shouldRedirect &= equals;
            logIfNotRedirect(dmCondition.getVpnCode() == 0, "Vpn 未通过");
            if(!equals){
                appendLogMessage(logMessages, "\"Vpn\": \"未通过\"");
            }
        }

        //判断访问的是否包含华为
        if ("Huawei".equals(params.get("isHuawei"))){
            logIfNotRedirect(false, "IsHuawei 未通过");
            shouldRedirect = false;
            appendLogMessage(logMessages, "\"是否为华为\": \"未通过\"");
        }
        //判断是否是特定设备访问
        boolean isSpecificDevice = true;
        if(dmCondition.getIsSpecificDevice()==1){
            if ("Mobile".equals(params.get("userMobile")) && "iOS".equals(params.get("isIOSS"))) {
                String buildKey = redisUtil.buildKey("Acoolys", "dmModles");
                String modelsMap = redisUtil.get(buildKey);
                Type modelsType = new TypeToken<List<DmModles>>() {}.getType();
                List<DmModles> modelsList = new Gson().fromJson(modelsMap, modelsType);

                Integer screenWidth = Integer.valueOf(params.get("screenWidth"));
                Integer screenHeight = Integer.valueOf(params.get("screenHeight"));
                Integer pixelRatio = Integer.valueOf(params.get("pixelRatio"));


                // 查找符合条件的设备
                Optional<DmModles> matchingModel = modelsList.stream()
                        .filter(s -> s.getScreenWidth().equals(screenWidth)
                                && s.getScreenHeight().equals(screenHeight)
                                && s.getPixelRatio().equals(pixelRatio))
                        .findFirst();
                // 如果找到了匹配的设备，设置标志为 false
                if (matchingModel.isPresent()) {
                    isSpecificDevice = false;
                    logIfNotRedirect(shouldRedirect, "指定设备 未通过");
                    appendLogMessage(logMessages, "\"设定的低端指定设备\": \"未通过\"");
                }
//            modelsList.stream()
//                    .filter(s -> s.getScreenWidth() == screenWidth && s.getScreenHeight() == screenHeight && pixelRatio.equals(s.getPixelRatio()))
//                    .findFirst()
//                    .ifPresent(s -> {
//                    });
            }
        }
        shouldRedirect = shouldRedirect && isSpecificDevice;
        map.put("shouldRedirect", shouldRedirect.toString());
        map.put("logMessage", logMessages.toString());
        return map;

    }

    private void handleIpLimitConditions(DmCondition dmCondition, String ip) {
        if (dmCondition.getIpLimits() == 1) {
            String ipLimitsKey = redisUtil.buildKey("AoollyNumberIp", dmCondition.getAccessAddress());
            List<String> ipList = getCachedIpList(ipLimitsKey);
            if (ipList != null) {
                long count = ipList.stream().filter(s -> s.equals(ip)).count();
                if (count >= 1) {
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
}
