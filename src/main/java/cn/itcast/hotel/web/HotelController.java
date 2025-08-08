package cn.itcast.hotel.web;


import cn.itcast.hotel.entity.*;
import cn.itcast.hotel.po.DeviceDetectorPo;
import cn.itcast.hotel.service.*;
import cn.itcast.hotel.util.RedisUtils;
import cn.itcast.hotel.util.Result;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("hotel")
@CrossOrigin(origins = "*")
public class HotelController {

    @Resource
    private LinkIntService linkIntService;
    @Resource
    private LinkSrcsService linkSrcsService;

    @Resource
    private DmDomainService dmDomainService;

    @Resource
    private DmCenterService dmCenterService;

    @Resource
    private DmPixelService dmPixelService;
    @Resource
    private DmConditionService dmConditionService;
    @Autowired
    private RedisUtils redisUtil;
    @Resource
    private DmCountryService dmCountryService;
    @Resource
    private TonckService tonckService;



    @PostMapping("/protectLink")
    public Result<String> protectLink(@RequestBody Map<String, String> params) {
        String domainName = params.getOrDefault("domainName", "defaultDomain");
        String ip = params.getOrDefault("userIp", "127.0.0.1");
        String userMobile = params.getOrDefault("userMobile", "true");
        String paraPath = params.getOrDefault("paraPath", "true");
        String country = getCountryByIp(ip);
        domainName = domainName.substring(0, domainName.length() - 1);
        //网站浏览记录+1
        String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
        String jsonMap = redisUtil.get(dmCenterKey);
        Type mapType = new TypeToken<Map<String, DmCenter>>() {
        }.getType();
        Map<String, DmCenter> map = new Gson().fromJson(jsonMap, mapType);
        DmCenter query = map.get(domainName);
        String dmConditionKey = redisUtil.buildKey("acooly", "dmConditionList");
        String conMap = redisUtil.get(dmConditionKey);
        Type mapCon = new TypeToken<Map<String, DmCondition>>() {
        }.getType();
        Map<String, DmCondition> mapCondition = new Gson().fromJson(conMap, mapCon);
        DmCondition dmCondition = mapCondition.get(domainName);

        //先判断ip是否开启白名单并且是否在白名单中 在判断是否开启ips点击数限制
//        if (dmCondition.getIpWhite() == 1 && dmCondition.getWhiteList().equals(ip)) {
//            log.info(ip + " is in white list");
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
////                    dmCondition.setIsVpn(1);
//////                    dmCondition.setVpnCode(1);
//                    throw new RuntimeException("ip number is limited");
//                }
//            }
//        }



        dmCenterService.update(query);
        dmCenterService.addAccess(query, ip, dmCondition, country, userMobile, paraPath);
        return Result.ok("suceess");
    }


    //通过ip地址获取 城市
    public String getCountryByIp(String ip) {
        String url = "https://ipapi.co/" + ip + "/json/";
        StringBuilder json = new StringBuilder();
        try {
            URL httpUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            // 使用 try-with-resources 关闭 BufferedReader
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
            }


            conn.disconnect();
            JSONObject jsonObject = new JSONObject(json.toString());
            return jsonObject.getString("country"); // 返回国家信息
        } catch (Exception e) {
            e.printStackTrace();
            return "未知";
        }
    }

    @PostMapping("/protectGetLink")
    public Result protectGetLink(@RequestBody Map<String, String> params) {
        String domainName = params.getOrDefault("domainName", "defaultDomain");
        String ip = params.getOrDefault("userIp", "127.0.0.1");
        String userMobile = params.getOrDefault("userMobile", "true");
        String paraPath = params.getOrDefault("paraPath", "true");
        String country = params.getOrDefault("userCountry", "US");
        HashMap<String, Object> map = new HashMap<>();
        String link = "";
        DmDomain domain = new DmDomain();
        //单表单的接口
        domainName = domainName.substring(0, domainName.length() - 1);
        domain.setDomainName(domainName);
        //网站浏览记录+1
        String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
        String jsonMap = redisUtil.get(dmCenterKey);
        Type mapType = new TypeToken<Map<String, DmCenter>>() {
        }.getType();
        Map<String, DmCenter> mapDmCenter = new Gson().fromJson(jsonMap, mapType);
        DmCenter query = mapDmCenter.get(domainName);
        //判断是否需要轮询
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
            map.put("link", link);
        } else {
            map.put("link", "https://www.google.com");
        }
        String dmConditionKey = redisUtil.buildKey("acooly", "dmConditionList");
        String conMap = redisUtil.get(dmConditionKey);
        Type mapCon = new TypeToken<Map<String, DmCondition>>() {
        }.getType();
        Map<String, DmCondition> mapCondition = new Gson().fromJson(conMap, mapCon);
        DmCondition dmCondition = mapCondition.get(domainName);
        dmCenterService.addAccess(query, ip, dmCondition, country, userMobile, paraPath);
        dmCenterService.update(query);
        map.put("dmCondition", dmCondition);
        return Result.ok(map);
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody LinkInt linkInt1) {
//
//        LinkInt linkInt = new LinkInt();
//        linkInt.setName(linkInt1.getName());
//        LinkInt query = linkIntService.query(linkInt);
//        Integer id = query.getCountLink();
////        LinkSrcs linkSrcs = new LinkSrcs();
////
////        linkSrcs.setIntId(id);
////        linkSrcs.setName(linkInt1.getName());
////        LinkSrcs linkSrcs1 = linkSrcsService.query(linkSrcs);
//
//        LinkSrcs linkSrcs2 = new LinkSrcs();
//        linkSrcs2.setName(query.getName());
//        List<LinkSrcs> linkSrcs3 = linkSrcsService.quertList(linkSrcs2);
//        LinkSrcs linkSrcs4 = linkSrcs3.get(id-1);
//        if (linkSrcs3.size()==id){
//            id=1;
//        }else {
//            id=id+1;
//        }
//        linkInt.setId(query.getId());
//        linkInt.setCountLink(id);
//        linkIntService.update(linkInt);
//        return Result.ok(linkSrcs4.getLinkSrc());
        return null;
    }

    //只增加域名访问记录
    @PostMapping("/getNumber")
    public Result<DmCondition> getNumber(@RequestBody Map<String, String> params) throws Exception {
        String domainName = params.getOrDefault("domainName", "defaultDomain");
        String ip = params.getOrDefault("userIp", "127.0.0.1");
        String userMobile = params.getOrDefault("userMobile", "true");
        String paraPath = params.getOrDefault("paraPath", "true");
        String country = params.getOrDefault("userCountry", "US");
        domainName = domainName.substring(0, domainName.length() - 1);
        //网站浏览记录+1
        String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
        String jsonMap = redisUtil.get(dmCenterKey);
        Type mapType = new TypeToken<Map<String, DmCenter>>() {
        }.getType();
        Map<String, DmCenter> map = new Gson().fromJson(jsonMap, mapType);
        DmCenter query = map.get(domainName);
        String dmConditionKey = redisUtil.buildKey("acooly", "dmConditionList");
        String conMap = redisUtil.get(dmConditionKey);
        Type mapCon = new TypeToken<Map<String, DmCondition>>() {
        }.getType();
        Map<String, DmCondition> mapCondition = new Gson().fromJson(conMap, mapCon);
        DmCondition dmCondition = mapCondition.get(domainName);


        String builtKey = redisUtil.buildKey("acooly", "countryVpn");
        String string = redisUtil.get(builtKey);
        List<String> conuntryList = new Gson().fromJson(string, new TypeToken<List<String>>() {
        }.getType());
        String keyVpn = redisUtil.buildKey("acooly", "keyVpn");
        String keyString = redisUtil.get(keyVpn);
        if (dmCondition.getIsVpn() == 1){
            dmCondition.setVpnCode(dmConditionService.getIpVpn(conuntryList, keyString, ip));
        }else {
            dmCondition.setVpnCode(0);
        }


        if (dmCondition.getIsIp() == 1) {
            if (country.equals(dmCondition.getIpCountry())) {
                dmCondition.setIpCountry("true");
            }
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

        //修改点击数
        dmCenterService.update(query);
        //增加点击记录
        dmCenterService.addAccess(query, ip, dmCondition, country, userMobile, paraPath);
        return Result.ok(dmCondition);
    }


    @PostMapping("/getNumber1")
    public Result<String> getNumber1(@RequestBody Map<String, String> params) {
        String domainName = params.getOrDefault("domainName", "defaultDomain");
        String ip = params.getOrDefault("userIp", "127.0.0.1");
        String userMobile = params.getOrDefault("userMobile", "true");
        String country = params.getOrDefault("userCountry", "US");
        DmDomain domain = new DmDomain();
        domainName = domainName.substring(0, domainName.length() - 1);
        String link1 = params.get("link");
        domain.setDomainName(domainName);
        //按钮点击加1
        String[] split = domainName.split("/", 2);
        String firstPart = split[0];
        String secondPart = split.length > 1 ? split[1] : ""; // 第一个斜杠后的部分
        String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
        String jsonMap = redisUtil.get(dmCenterKey);
        Type mapType = new TypeToken<Map<String, DmCenter>>() {
        }.getType();
        Map<String, DmCenter> map = new Gson().fromJson(jsonMap, mapType);
        DmCenter query = map.get(domainName);
        dmCenterService.addClickCount(query, ip, country, userMobile);
        dmCenterService.updateClick(query);
        return Result.ok(dmDomainService.queryString(domain));
    }


    @PostMapping("/getLink")
    public Result<Map<String, Object>> getLink(@RequestBody Map<String, String> params) {
        String domainName = params.getOrDefault("domainName", "defaultDomain");
        String ip = params.getOrDefault("userIp", "127.0.0.1");
        String userMobile = params.getOrDefault("userMobile", "true");
        String paraPath = params.getOrDefault("paraPath", "true");
        String country = params.getOrDefault("userCountry", "US");
        HashMap<String, Object> map = new HashMap<>();
        String link = "";
        DmDomain domain = new DmDomain();
        //单表单的接口
        domainName = domainName.substring(0, domainName.length() - 1);
        domain.setDomainName(domainName);
        //网站浏览记录+1
        String[] split = domainName.split("/", 2);
        String firstPart = split[0];
        String secondPart = split.length > 1 ? split[1] : ""; // 第一个斜杠后的部分
        String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
        String jsonMap = redisUtil.get(dmCenterKey);
        Type mapType = new TypeToken<Map<String, DmCenter>>() {
        }.getType();
        Map<String, DmCenter> mapDmCenter = new Gson().fromJson(jsonMap, mapType);
        DmCenter query = mapDmCenter.get(domainName);
        //判断是否需要轮询
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
            map.put("link", link);
        } else {
            map.put("link", "http://www.baidu.com");
        }

        String dmConditionKey = redisUtil.buildKey("acooly", "dmConditionList");
        String conMap = redisUtil.get(dmConditionKey);
        Type mapCon = new TypeToken<Map<String, DmCondition>>() {
        }.getType();
        Map<String, DmCondition> mapCondition = new Gson().fromJson(conMap, mapCon);
        DmCondition dmCondition = mapCondition.get(domainName);

        if (dmCondition.getIsVpn() == 1) {
            dmCondition.setVpnCode(getVpnCodeGet(dmCondition,ip));
        }
        if (dmCondition.getIsIp() == 1) {
            if (country.equals(dmCondition.getIpCountry())) {
                dmCondition.setIpCountry("true");
            }
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
        dmCenterService.addAccess(query, ip, dmCondition, country, userMobile, paraPath);
        dmCenterService.update(query);
        map.put("dmCondition", dmCondition);
        return Result.ok(map);
    }


    @PostMapping("/getLink1")
    public Result<Map<String, Object>> getLink1(@RequestBody Map<String, String> params) {
        String domainName = params.getOrDefault("domainName", "defaultDomain");
        String ip = params.getOrDefault("userIp", "127.0.0.1");
        String userMobile = params.getOrDefault("userMobile", "true");
        String paraPath = params.getOrDefault("paraPath", "true");
        String country = params.getOrDefault("userCountry", "US");
        DmDomain domain = new DmDomain();
        //落地页 进入的接口
        domainName = domainName.substring(0, domainName.length() - 1);
        //网站浏览记录+1
        String[] split = domainName.split("/", 2);
        String firstPart = split[0];
        String secondPart = split.length > 1 ? split[1] : ""; // 第一个斜杠后的部分
//        DmCenter dmCenter = new DmCenter();
//        dmCenter.setDomain(firstPart);
//        dmCenter.setSecondaryDomain(secondPart);
//        DmCenter query = dmCenterService.query(dmCenter);
        String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
        String jsonMap = redisUtil.get(dmCenterKey);
        Type mapType = new TypeToken<Map<String, DmCenter>>() {
        }.getType();
        Map<String, DmCenter> mapDmCenter = new Gson().fromJson(jsonMap, mapType);
        DmCenter query = mapDmCenter.get(domainName);


        DmPixel dmPixel = new DmPixel();
        dmPixel.setDomain(domainName);
        List<String> strings = dmPixelService.queryPixelIdsByDomain(dmPixel);
        HashMap<String, Object> map = new HashMap<>();
        map.put("list", strings);

//        DmCondition dmCondition = new DmCondition();
//        dmCondition.setAccessAddress(domainName);
//        dmCondition = dmConditionService.queryById(dmCondition);
        String dmConditionKey = redisUtil.buildKey("acooly", "dmConditionList");
        String conMap = redisUtil.get(dmConditionKey);
        Type mapCon = new TypeToken<Map<String, DmCondition>>() {
        }.getType();
        Map<String, DmCondition> mapCondition = new Gson().fromJson(conMap, mapCon);
        DmCondition dmCondition = mapCondition.get(domainName);

        if (dmCondition.getIsVpn() == 1) {
            dmCondition.setVpnCode(getVpnCodeGet(dmCondition,ip));
        }
        if (dmCondition.getIsIp() == 1) {
            if (country.equals(dmCondition.getIpCountry())) {
                dmCondition.setIpCountry("true");
            }
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
        dmCenterService.addAccess(query, ip, dmCondition, country, userMobile, paraPath);
        dmCenterService.update(query);
        map.put("dmCondition", dmCondition);
        return Result.ok(map);
    }


    @PostMapping("/addTrolls")
    public Result addTrolls(@RequestBody DmDomain dmDomain) {
        String domainName = dmDomain.getDomainName();
        domainName = domainName.substring(0, domainName.length() - 1);
        String[] split = domainName.split("/", 2);
        String firstPart = split[0];
        String secondPart = split.length > 1 ? split[1] : ""; // 第一个斜杠后的部分
        DmCenter dmCenter = new DmCenter();
        dmCenter.setDomain(firstPart);
        dmCenter.setSecondaryDomain(secondPart);
        DmCenter query = dmCenterService.query(dmCenter);
        if (query.getTrolls() == null) {
            query.setTrolls(1);
        } else {
            query.setTrolls(query.getTrolls() + 1);
        }
        dmCenterService.updateTrophy(query);
        return Result.ok();
    }




    @GetMapping("/countryRedis")
    public Result countryRedis() {
        try {
            List<DmCountry> list = dmCountryService.getAll();
            String builtKey = redisUtil.buildKey("acooly", "countryVpn");
            String keyVpn = redisUtil.buildKey("acooly", "keyVpn");
            Tonck tonck = tonckService.queryById(2);
            List<String> stringList = list.stream()
                    .map(DmCountry::getCountry)
                    .filter(country -> country != null && !country.isEmpty()) // 过滤掉空值
                    .collect(Collectors.toList());
            redisUtil.set(builtKey, new Gson().toJson(stringList));
            redisUtil.set(keyVpn, tonck.getTonck());
            return Result.ok("更新成功");
        } catch (Exception e) {
            return Result.fail("更新失败");
        }
    }


    @GetMapping("/dmCenterRedis")
    public Result dmCenterRedis() {
        List<DmCenter> list = dmCenterService.getAll();
        String buildKey = redisUtil.buildKey("acooly", "dmCenterMap");
        Map<String, DmCenter> map = list.stream()
                .filter(dm -> dm.getDomain() != null && !dm.getDomain().isEmpty() &&
                        dm.getSecondaryDomain() != null && !dm.getSecondaryDomain().isEmpty())
                .collect(Collectors.toMap(
                        dm -> dm.getDomain() + "/" + dm.getSecondaryDomain(),
                        dm -> dm
                ));
        redisUtil.set(buildKey, new Gson().toJson(map));
        return Result.ok(buildKey);
    }


    @GetMapping("/dmConditionRedis")
    public Result dmConditionRedis() {
        List<DmCondition> conditionList = dmConditionService.getAll();
        String buildKey = redisUtil.buildKey("acooly", "dmConditionList");
        Map<String, DmCondition> map = conditionList.stream()
                .filter(dm -> dm.getAccessAddress() != null && !dm.getAccessAddress().isEmpty())
                .collect(Collectors.toMap(DmCondition::getAccessAddress, dm -> dm));
        redisUtil.set(buildKey, new Gson().toJson(map));
        return Result.ok(buildKey);
    }



    @PostMapping("/getLink2")
    public Result<String> getLink2(@RequestBody Map<String, String> params) {
        String domainName = params.getOrDefault("domainName", "defaultDomain");
        String ip = params.getOrDefault("userIp", "127.0.0.1");
        String userMobile = params.getOrDefault("userMobile", "true");
        String country = params.getOrDefault("userCountry", "US");
        DmDomain domain = new DmDomain();
        //落地页中按钮 点击的接口  并且获取 像素id
        domainName = domainName.substring(0, domainName.length() - 1);
        String link1 = params.get("link");
        domain.setDomainName(domainName);
        String link = "";
        //按钮点击加1
        String[] split = domainName.split("/", 2);
        String firstPart = split[0];
        String secondPart = split.length > 1 ? split[1] : ""; // 第一个斜杠后的部分
//        DmCenter dmCenter = new DmCenter();
//        dmCenter.setDomain(firstPart);
//        dmCenter.setSecondaryDomain(secondPart);
//        DmCenter query = dmCenterService.query(dmCenter);
        String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
        String jsonMap = redisUtil.get(dmCenterKey);
        Type mapType = new TypeToken<Map<String, DmCenter>>() {
        }.getType();
        Map<String, DmCenter> map = new Gson().fromJson(jsonMap, mapType);
        DmCenter query = map.get(domainName);
        //判断是否需要轮询
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

//        RestTemplate restTemplate = new RestTemplate();
//        String url = "https://ipapi.co/json/";
//        String result = restTemplate.getForObject(url, String.class);
//        Gson gson = new Gson();
//        JsonObject jsonObject = gson.fromJson(result, JsonObject.class);
//        String country = String.valueOf(jsonObject.get("country")).replace("\"", "");
        dmCenterService.addClickCount(query, ip, country, userMobile);
        dmCenterService.updateClick(query);
        if (!StringUtils.isEmpty(link)) {
            return Result.ok(link);
        } else {
            return Result.ok("http://www.baidu.com");
        }
    }


    public Integer getVpn(String ip, List<String> countryList, String key) {
        int data = 0; // 0:未获取到IP，1：获取到IP
        if (!StringUtils.isEmpty(ip)) {
//            String key = "79f0f49fe49b4e07994e61a1c4bd23c2";
            String urlString = "https://vpnapi.io/api/" + ip + "?key=" + key;
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(2000) // 连接超时时间为5秒
                    .setSocketTimeout(2000)  // 读取数据超时时间为5秒
                    .build();
            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
            try {
                HttpGet httpGet = new HttpGet(urlString);
                try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpGet)) {
                    JSONObject jsonResponse = new JSONObject(EntityUtils.toString(response.getEntity()));
                    String country = jsonResponse.getJSONObject("location").getString("country");
                    boolean vpn = jsonResponse.getJSONObject("security").getBoolean("vpn");
                    boolean proxy = jsonResponse.getJSONObject("security").getBoolean("proxy");
                    log.info("Proxy: " + proxy);
                    log.info("IP: " + ip);
                    log.info("Country: " + country);
                    log.info("Vpn: " + vpn);
                    if (countryList.contains(country)) {
                        data = 1;
                    } else {
                        data = vpn || proxy ? 2 : 0;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            log.error("IP地址为空");
        }
        return data;
    }

    private static String sendGET() throws Exception {
        String ipv6 = "false"; // Default to "false" in case of failure
        try {
            URL url = new URL("https://api6.ipify.org");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 200
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                ipv6 = in.readLine();
                in.close();
                if (ipv6 == null || ipv6.isEmpty()) {
                    ipv6 = "false";
                }
                System.out.println("Public IPv6 Address: " + ipv6);
            } else {
                System.out.println("Failed to get IPv6 address. Response code: " + responseCode);
            }
        } catch (Exception e) {
            ipv6 = "false";
        }
        return ipv6;
    }


    public static void getIPAddress() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                if (!inetAddress.isLoopbackAddress()) {
                    System.out.println("IP地址： " + inetAddress.getHostAddress());
                    System.out.println("IP类型： " + (inetAddress.getHostAddress().contains(":") ? "IPv6" : "IPv4"));
                    // 获取IP所在的国家需要使用第三方库，例如：ip2region
                    // String country = IP2RegionUtil.getCountry(inetAddress.getHostAddress());
                    // System.out.println("所在国家： " + country);
                }
            }
        }
    }



    private Integer getVpnCodeGet(DmCondition dmCondition , String ip ){
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
            int vpnCode = dmConditionService.getIpVpn(conuntryList, keyString, ip);
            dmCondition.setVpnCode(vpnCode);
            if (vpnCode==1){
                proxyIpList.add(ip);
                redisUtil.set(proKey, new Gson().toJson(proxyIpList));
            }
            return vpnCode;
        }
    }


    public static void getIP() throws Exception {
        URL url = new URL("http://checkip.amazonaws.com");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String publicIP = reader.readLine();
        System.out.println("公网IP地址： " + publicIP);
        System.out.println("公网IP类型： " + (publicIP.contains(":") ? "IPv6" : "IPv4"));
    }

    private static String getIpFromUrl() throws Exception {
        try {
            URL url = new URL("https://api64.ipify.org");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String ipAddress = reader.readLine();
            System.out.println("公网IP地址： " + ipAddress);
            return ipAddress;
        } catch (IOException e) {
            return null;
        }
    }

//    @PostMapping("/getVpnNumber")
//    public Result<DmResult> getVpnNumber(@RequestBody Map<String, String> params) throws Exception {
//        Result<DmResult> result = new Result<>();
//        DmResult dmResult = new DmResult();
//        HashMap<String, String> handledMap = new HashMap<>();
//        CompletableFuture<DmCenter> queryFuture;
//        CompletableFuture<DmCondition> conditionFuture;
//        CompletableFuture<String> countryFuture;
//        CompletableFuture<Integer> vpnCodeFuture;
//        CompletableFuture<Boolean> ipLimitFuture;
//
//        try {
//            String domainName = params.getOrDefault("domainName", "defaultDomain").substring(0, params.get("domainName").length() - 1);
//            String ip = params.get("userIp");
//
//            // 异步获取国家信息
//            countryFuture = CompletableFuture.supplyAsync(() -> getCountrySetByIp(ip));
//
//            // 异步从 Redis 获取 DmCenter 数据
//            queryFuture = CompletableFuture.supplyAsync(() -> {
//                String dmCenterKey = redisUtil.buildKey("acooly", "dmCenterMap");
//                String jsonMap = redisUtil.get(dmCenterKey);
//                Type mapType = new TypeToken<Map<String, DmCenter>>() {}.getType();
//                Map<String, DmCenter> map = new Gson().fromJson(jsonMap, mapType);
//                return map.get(domainName);
//            });
//
//            // 异步从 Redis 获取 DmCondition 数据
//            conditionFuture = CompletableFuture.supplyAsync(() -> {
//                String dmConditionKey = redisUtil.buildKey("acooly", "dmConditionList");
//                String conMap = redisUtil.get(dmConditionKey);
//                Type mapCon = new TypeToken<Map<String, DmCondition>>() {}.getType();
//                Map<String, DmCondition> mapCondition = new Gson().fromJson(conMap, mapCon);
//                return mapCondition.get(domainName);
//            });
//
//            // 异步设置 VPN Code（如果需要）
//            vpnCodeFuture = conditionFuture.thenApplyAsync(dmCondition -> {
//                return dmCondition.getIsVpn() == 1 ? getVpnSetCode(dmCondition, ip) : 0;
//            });
//
//            // 异步判断是否在白名单中并清除标志或设置 IP 限制条件
//            ipLimitFuture = conditionFuture.thenCombineAsync(vpnCodeFuture, (dmCondition, vpnCode) -> {
//                dmCondition.setVpnCode(vpnCode);
//                if (isInWhiteList(dmCondition, ip)) {
//                    clearConditionFlags(dmCondition);
//                    return true;
//                } else {
//                    handleIpLimitConditions(dmCondition, ip);
//                    return false;
//                }
//            });
//
//            // 等待所有异步任务完成
//            CompletableFuture.allOf(queryFuture, conditionFuture, countryFuture, ipLimitFuture).join();
//
//            // 处理移动设备条件
//            handledMap = handleMobileConditions(conditionFuture.join(), params, countryFuture.join());
//            Boolean shouldRedirect = Boolean.parseBoolean(handledMap.get("shouldRedirect"));
//
//            // 设置结果
//            dmResult.setDataSuccess(shouldRedirect);
//            result.setData(dmResult);
//            result.setSuccess(true);
//
//        } catch (Exception e) {
//            result.setSuccess(false);
//            log.error("Error processing request: ", e);
//        }
//
//        // 更新主数据的点击数及添加 VPN 访问日志，保持异步
//        CompletableFuture.runAsync(() -> {
//            dmCenterService.update(queryFuture.join());
//            dmCenterService.addAccessVpn(queryFuture.join(), params, countryFuture.join(), conditionFuture.join(), handledMap);
//            log.info("成功还是失败：" + dmResult.toString());
//        });
//
//        return result;
//    }
}
