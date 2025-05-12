package cn.itcast.hotel.web;

import cn.itcast.hotel.entity.*;
import cn.itcast.hotel.po.DeviceDetectorPo;
import cn.itcast.hotel.service.*;
import cn.itcast.hotel.util.Deevvi;
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
    private DmPixelService dmPixelService;
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
//            int vpnCode = dmConditionService.getIpVpn(conuntryList, keyString, ip);
            int vpnCode = dmConditionService.getIpApiVpn(conuntryList, keyString, ip);
            dmCondition.setVpnCode(vpnCode);
            if (vpnCode==1){
                proxyIpList.add(ip);
                redisUtil.set(proKey, new Gson().toJson(proxyIpList));
            }
            return vpnCode;
        }
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
            String fbLink = getFbLink(domainName, query).get(0);
            dmResult.setLink(fbLink);
            result.setSuccess(true);
        } catch (Exception e) {
            result.setSuccess(false);
        }
        String userAgent = params.get("userAgent");
        DeviceDetectorPo deviceDetectorPo = Deevvi.parseUserAgent(userAgent);
        handledMap.put("shouldRedirect", "true");
        handledMap.put("logMessage","success");
        dmCenterService.update(query);
        dmCenterService.addAccessVpn(query,params,country,dmCondition,handledMap,deviceDetectorPo);
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
            String userAgent = params.get("userAgent");
            DeviceDetectorPo deviceDetectorPo = Deevvi.parseUserAgent(userAgent);
            dmCenterService.update(query);
            handledMap.put("shouldRedirect", "true");
            handledMap.put("logMessage","success");
            dmCenterService.addAccessVpn(query,params,country,dmCondition,handledMap,deviceDetectorPo);
            result.setSuccess(true);
        } catch (JsonSyntaxException e) {
            result.setSuccess(false);
        }
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
        String country = getCountrySetByIp(ip);
        dmCenterService.addClickVpnCount(query,params,country);
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
            country = getCountrySetByIp(ip);
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
            deviceDetectorPo = Deevvi.parseUserAgent(userAgent);
            handledMap =  handleMobileConditions(dmCondition, params, country,deviceDetectorPo);
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
        String country = getCountrySetByIp(ip);
        String fbLink = getFbLink(domainName, query).get(0);
        String keyys = getFbLink(domainName, query).get(1);
        dmResult.setKey(keyys);
        dmResult.setLink(fbLink);
        dmCenterService.addClickVpnCount(query,params,country);
        return Result.ok(dmResult);
    }

    //获取轮询链接
    @PostMapping("/getShuntLink")
    public Result<DmResult> getShuntLink(@RequestBody Map<String, String> params) throws Exception {
        Result<DmResult> result = new Result<>();
        DeviceDetectorPo deviceDetectorPo = new DeviceDetectorPo();
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
            String fbLink = getFbLink(domainName, query).get(0);
            String keyys = getFbLink(domainName, query).get(1);

            dmCondition.setVpnCode(dmCondition.getIsVpn() == 1 ? getVpnSetCode(dmCondition, ip) : 0);
            if (isInWhiteList(dmCondition, ip)) {
                clearConditionFlags(dmCondition);
            } else {
                handleIpLimitConditions(dmCondition, ip);
            }
            //通过userAgent 获取设备信息
            String userAgent = params.get("userAgent");
            deviceDetectorPo = Deevvi.parseUserAgent(userAgent);
            handledMap =  handleMobileConditions(dmCondition, params, country,deviceDetectorPo);
            String redirect = handledMap.get("shouldRedirect");
            shouldRedirect = Boolean.parseBoolean(redirect);
            dmResult.setDataSuccess(shouldRedirect);
            dmResult.setLink(fbLink);
            dmResult.setKey(keyys);
            result.setData(dmResult);
            result.setSuccess(true);
        } catch (Exception e) {
            result.setSuccess(false);
        }
        //改变主数据的点击数
        dmCenterService.addAccessVpn(query,params,country,dmCondition,handledMap,deviceDetectorPo);
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
        Result<DmResult> result = new Result<>();
        DmResult dmResult = new DmResult();
        DeviceDetectorPo deviceDetectorPo = new DeviceDetectorPo();
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
            //通过userAgent 获取设备信息
            String userAgent = params.get("userAgent");
            deviceDetectorPo = Deevvi.parseUserAgent(userAgent);

            dmCondition.setVpnCode(dmCondition.getIsVpn() == 1 ? getVpnSetCode(dmCondition, ip) : 0);
            if (isInWhiteList(dmCondition, ip)) {
                clearConditionFlags(dmCondition);
            } else {
                handleIpLimitConditions(dmCondition, ip);
            }
            handledMap =  handleMobileConditions(dmCondition, params, country,deviceDetectorPo);
            String redirect = handledMap.get("shouldRedirect");


            shouldRedirect = Boolean.parseBoolean(redirect);
            dmResult.setDataSuccess(shouldRedirect);
            dmResult.setKey(query.getKeyy());
            result.setData(dmResult);
            result.setSuccess(true);
        } catch (JsonSyntaxException e) {
            result.setSuccess(false);
        }
        //改变主数据的点击数
        dmCenterService.addAccessVpn(query,params,country,dmCondition,handledMap,deviceDetectorPo);
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

    private HashMap<String,String> handleMobileConditions(DmCondition dmCondition, Map<String, String> params,String country,
                                                          DeviceDetectorPo deviceDetectorPo) {
        HashMap<String, String> map = new HashMap<>();
        //是否通过
        Boolean shouldRedirect = true;
        //不通过原因
        StringBuilder logMessages = new StringBuilder();

        //判断是否为无法识别设备
        if(dmCondition.getIsIdentify() == 1){
            Boolean found = deviceDetectorPo.getFound();
            logIfNotRedirect(found!= null && found, "IsIdentify 未通过");
            if (found!= null && !found){
                shouldRedirect = false;
                logMessages.append("\"是否为无法识别设备\": \"未通过\"");
                }
        }


        //判断是否为爬虫机器人
        if(dmCondition.getIsRobot() == 1){
            Boolean isBot = deviceDetectorPo.getIsBot();
            logIfNotRedirect(isBot!= null && isBot, "IsRobot 未通过");
            if (isBot!= null && isBot){
                shouldRedirect = false;
                logMessages.append("\"是否为爬虫机器人\": \"未通过\"");
            }
        }

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
//            String isChinese = params.get("isChinese");
//            boolean equals = "false".equals(isChinese);
//            shouldRedirect &= equals;
//            logIfNotRedirect(equals, "IsChinese 未通过");
//            if(!equals){
//                appendLogMessage(logMessages, "\"中文检测\": \"未通过\"");
//            }
            String userLanguage = params.get("userLanguage");
            String language = dmCondition.getLanguage();
            List<String> languageList = Arrays.asList(language.split(","));

            if (languageList.contains("zh") && userLanguage != null && userLanguage.contains("zh")) {
                shouldRedirect = false;
                logIfNotRedirect(false, "包含 zh，未通过");
                appendLogMessage(logMessages, "\"中文检测\": \"包含 zh，未通过\"");
            } else if (userLanguage != null && languageList.contains(userLanguage)) {
                shouldRedirect = false;
                appendLogMessage(logMessages, "\"语言检测\": \"未通过\"");
            }
        }

        //当手机访问的时候
        if(deviceDetectorPo.getIsMobile()!=null && deviceDetectorPo.getIsMobile()){
            //当为手机的时候判断什么系统
            String name = deviceDetectorPo.getOs().getName();
            String version = deviceDetectorPo.getOs().getVersion();
            String iosVersion = dmCondition.getIosVersion();
            int i = compareVersions(iosVersion, version);
            //如果i>0说明版本不过关
            if(i>0){
                shouldRedirect = false;
                appendLogMessage(logMessages, "\"手机系统\": \"版本过低未通过\"");
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
//            Boolean isMobile = deviceDetectorPo.getIsMobile();
//            logIfNotRedirect(isMobile != null && isMobile, "IsMobile 未通过");
//            if (isMobile != null && !isMobile){
//                shouldRedirect = false;
//                appendLogMessage(logMessages, "\"设备必须为手机\": \"未通过\"");
//            }
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


        //判断是否是特定设备访问
        boolean isSpecificDevice = true;
        if(dmCondition.getIsSpecificDevice()==1){
            //ios低版本
//            String isBannedIOS = params.get("isBannedIOS");
//            if(isBannedIOS!=null && isBannedIOS.equals("true")){
//                boolean equals= false;
//                shouldRedirect &= equals;
//                logIfNotRedirect(equals, "ios版本过低 未通过");
//                appendLogMessage(logMessages, "\"ios版本过低\": \"未通过\"");
//            }
            //判断访问的是否包含华为
            if ("Huawei".equals(params.get("isHuawei"))){
                logIfNotRedirect(false, "IsHuawei 未通过");
                shouldRedirect = false;
                appendLogMessage(logMessages, "\"是否为华为\": \"未通过\"");
            }

            //判断是否为三星低端机
            if ("BannedSamsung".equals(params.get("isSamsung"))){
                logIfNotRedirect(false, "isBannedSamsung 未通过");
                shouldRedirect = false;
                appendLogMessage(logMessages, "\"是否为三星低端机\": \"未通过\"");
            }
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
}
