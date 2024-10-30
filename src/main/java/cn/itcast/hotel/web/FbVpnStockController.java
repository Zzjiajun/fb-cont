package cn.itcast.hotel.web;

import cn.itcast.hotel.entity.DmCenter;
import cn.itcast.hotel.entity.DmCondition;
import cn.itcast.hotel.entity.DmResult;
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
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private DmCenterService dmCenterService;
    @Resource
    private DmConditionService dmConditionService;


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

        dmCenterService.update(query);

        return null;
    }


    private String getCountrySetByIp(String ip) {
        try {
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
}
