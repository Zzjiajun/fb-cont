package cn.itcast.hotel.util;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.AsnResponse;
import com.maxmind.geoip2.model.CountryResponse;
// import com.maxmind.geoip2.model.AnonymousIpResponse;
// import com.maxmind.geoip2.model.ConnectionTypeResponse;

import java.io.InputStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class IpInfoUtil {
    /**
     * 获取IP的所有主要地理和网络信息，放入Map返回
     * 需要 static/templates/ 目录下有 GeoLite2-City.mmdb、GeoLite2-Country.mmdb、GeoLite2-ASN.mmdb
     */
    public static Map<String, Object> getIpAllInfo(String ip) {
        Map<String, Object> result = new HashMap<>();
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);

            // 城市信息（GeoLite2-City.mmdb）
            try (InputStream cityDb = IpInfoUtil.class.getClassLoader().getResourceAsStream("static/templates/GeoLite2-City.mmdb");
                 DatabaseReader cityReader = new DatabaseReader.Builder(cityDb).build()) {
                CityResponse cityResponse = cityReader.city(ipAddress);
                result.put("countryIsoCode_cityDb", cityResponse.getCountry().getIsoCode());
                result.put("countryName_cityDb", cityResponse.getCountry().getNames().get("zh-CN"));
                result.put("province", cityResponse.getMostSpecificSubdivision().getNames().get("zh-CN"));
                result.put("city", cityResponse.getCity().getNames().get("zh-CN"));
                result.put("latitude", cityResponse.getLocation().getLatitude());
                result.put("longitude", cityResponse.getLocation().getLongitude());
            } catch (Exception e) {
                result.put("cityError", "城市库查询失败: " + e.getMessage());
            }

            // 国家信息（GeoLite2-Country.mmdb）
            try (InputStream countryDb = IpInfoUtil.class.getClassLoader().getResourceAsStream("static/templates/GeoLite2-Country.mmdb");
                 DatabaseReader countryReader = new DatabaseReader.Builder(countryDb).build()) {
                CountryResponse countryResponse = countryReader.country(ipAddress);
                result.put("countryIsoCode_countryDb", countryResponse.getCountry().getIsoCode());
                result.put("countryName_countryDb", countryResponse.getCountry().getNames().get("zh-CN"));
            } catch (Exception e) {
                result.put("countryError", "国家库查询失败: " + e.getMessage());
            }

            // ASN信息（GeoLite2-ASN.mmdb）
            try (InputStream asnDb = IpInfoUtil.class.getClassLoader().getResourceAsStream("static/templates/GeoLite2-ASN.mmdb");
                 DatabaseReader asnReader = new DatabaseReader.Builder(asnDb).build()) {
                AsnResponse asnResponse = asnReader.asn(ipAddress);
                result.put("asn", asnResponse.getAutonomousSystemNumber());
                result.put("asnOrg", asnResponse.getAutonomousSystemOrganization());
            } catch (Exception e) {
                result.put("asnError", "ASN库查询失败: " + e.getMessage());
            }

            // 匿名IP信息（GeoIP2-Anonymous-IP.mmdb）
            // try (InputStream anonDb = IpInfoUtil.class.getClassLoader().getResourceAsStream("static/templates/GeoIP2-Anonymous-IP.mmdb");
            //      DatabaseReader anonReader = new DatabaseReader.Builder(anonDb).build()) {
            //     AnonymousIpResponse anonResponse = anonReader.anonymousIp(ipAddress);
            //     result.put("isAnonymous", anonResponse.isAnonymous());
            //     result.put("isAnonymousVpn", anonResponse.isAnonymousVpn());
            //     result.put("isHostingProvider", anonResponse.isHostingProvider());
            //     result.put("isPublicProxy", anonResponse.isPublicProxy());
            //     result.put("isTorExitNode", anonResponse.isTorExitNode());
            // } catch (Exception e) {
            //     result.put("anonError", "匿名IP库查询失败: " + e.getMessage());
            // }

            // 连接类型信息（GeoIP2-Connection-Type.mmdb）
            // try (InputStream connDb = IpInfoUtil.class.getClassLoader().getResourceAsStream("static/templates/GeoIP2-Connection-Type.mmdb");
            //      DatabaseReader connReader = new DatabaseReader.Builder(connDb).build()) {
            //     ConnectionTypeResponse connResponse = connReader.connectionType(ipAddress);
            //     result.put("connectionType", connResponse.getConnectionType());
            // } catch (Exception e) {
            //     result.put("connTypeError", "连接类型库查询失败: " + e.getMessage());
            // }

        } catch (Exception e) {
            result.put("error", "IP解析失败: " + e.getMessage());
        }
        return result;
    }

    public static void main(String[] args) {
        String string = new String("37.751");
        String string1 = new String("-97.822");
        System.out.println(string+","+string1);
    }
} 