package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.entity.DmCondition;
import cn.itcast.hotel.dao.DmConditionDao;
import cn.itcast.hotel.service.DmConditionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.common.inject.internal.Stopwatch;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * (DmCondition)表服务实现类
 *
 * @author makejava
 * @since 2024-06-02 20:54:33
 */
@Slf4j
@Service("dmConditionService")
public class DmConditionServiceImpl implements DmConditionService {
    @Resource
    private DmConditionDao dmConditionDao;

    /**
     * 通过ID查询单条数据
     *
     * @return 实例对象
     */
    @Override
    public DmCondition queryById(DmCondition dmCondition) {
        return this.dmConditionDao.queryById(dmCondition);
    }

    /**
     * 分页查询
     *
     * @param dmCondition 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
//    @Override
//    public Page<DmCondition> queryByPage(DmCondition dmCondition, PageRequest pageRequest) {
//        long total = this.dmConditionDao.count(dmCondition);
//        return new PageImpl<>(this.dmConditionDao.queryAllByLimit(dmCondition, pageRequest), pageRequest, total);
//    }

    /**
     * 新增数据
     *
     * @param dmCondition 实例对象
     * @return 实例对象
     */
    @Override
    public DmCondition insert(DmCondition dmCondition) {
        this.dmConditionDao.insert(dmCondition);
        return dmCondition;
    }

    /**
     * 修改数据
     *
     * @param dmCondition 实例对象
     * @return 实例对象
     */
    @Override
    public void update(DmCondition dmCondition) {
        this.dmConditionDao.update(dmCondition);
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer id) {
        return this.dmConditionDao.deleteById(id) > 0;
    }

    @Override
    public String getPublicIP() throws Exception {
        String ipInfoUrl = "https://ip8.com/";
        HttpURLConnection connection = (HttpURLConnection) new URL(ipInfoUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("Failed to connect to " + ipInfoUrl + ": " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();

//        Document doc = Jsoup.parse(response.toString());
//        Element ipElement = doc.selectFirst(".ip-address");  // 假设IP地址在一个带有class为'ip-address'的元素中

//        if (ipElement != null) {
//            return ipElement.text();
//        } else {
//            throw new RuntimeException("Failed to parse IP address from response.");
//        }
    }

    @Override
    public Integer getVpn(List<String> conuntryList, String keyString ,String ip) {
        int conde = 0;
        String urlString = "https://vpnapi.io/api/" + ip + "?key=" + keyString;

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(2000) // 连接超时时间为2秒
                .setSocketTimeout(2000)  // 读取数据超时时间为2秒
                .build();

        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
        try {
            HttpGet httpGet = new HttpGet(urlString);
            try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpGet)) {
                JSONObject jsonResponse = new JSONObject(EntityUtils.toString(response.getEntity()));
                String country = jsonResponse.getJSONObject("location").getString("country");
                boolean vpn = jsonResponse.getJSONObject("security").getBoolean("vpn");
                boolean proxy = jsonResponse.getJSONObject("security").getBoolean("proxy");
                System.out.println("VPN: " + vpn);
                System.out.println("Proxy: " + proxy);
                System.out.println("country: " + country);
                if (conuntryList.contains(country)) {
                    conde = 1;
                } else {
                    conde = (vpn || proxy ? 2 : 0);
                }
            }
        } catch (Exception e) {
            System.out.println("Exception occurred: " + e.getMessage());
            // Ensure conde is set to 0 in case of any exception
            conde = 0;
        }
        return conde;
    }

    @Override
    public Integer getIpVpn(List<String> conuntryList, String keyString, String ip) {
        AtomicInteger conde = new AtomicInteger();
        String url = "https://proxycheck.io/v2/" + ip + "?key=" + keyString + "&vpn=1&asn=1";
        RestTemplate restTemplate = new RestTemplate();
            try {
                // 通过 proxycheck.io API 查询代理/VPN 信息
                StopWatch stopWatch = new StopWatch();
                stopWatch.start("延迟1:");
                String response = restTemplate.getForObject(url, String.class);
                stopWatch.stop();
                log.info("延迟1{}",stopWatch.getTotalTimeSeconds());
                JSONObject jsonResponse = new JSONObject(response);
                log.info(jsonResponse.toString());
                JSONObject ipData = jsonResponse.getJSONObject(ip);

                // 提取 "proxy", "type" 和 "country" 字段的值
                String proxyStatus = ipData.getString("proxy");
                String connectionType = ipData.getString("type");
                String country = jsonResponse.optJSONObject(ip).optString("country", "unknown");

                // 定义需要排除的国家/地区
                List<String> typeList = Arrays.asList(
                        "VPN","Compromised Server"
                );
                log.info("type: " + connectionType);
                // 判断是否为 VPN 或代理网络，并检查国家是否在排除列表中
                if ("yes".equals(proxyStatus) || typeList.contains(connectionType) || conuntryList.contains(country)) {
                    conde.set(1);
                } else {
                    conde.set(0);
                }
            } catch (Exception e) {
                log.error("Exception occurred: " + e.getMessage());
                conde.set(0);
            }
            return conde.get();
    }

    @Override
    public Integer getIpApiVpn(List<String> conuntryList, String keyString, String ip) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("演示1");
        AtomicInteger conde = new AtomicInteger();
        String url = "https://pro.ip-api.com/json/" + ip + "?key=" + keyString + "&fields=status,isp,country,proxy,hosting,query";
        RestTemplate restTemplate = new RestTemplate();
        stopWatch.stop();
        log.info("演示1:{}",stopWatch.getTotalTimeSeconds());
        try {
//            stopWatch.start("演示2");
            String response = restTemplate.getForObject(url, String.class);
//            stopWatch.stop();
//            log.info("演示2:{}",stopWatch.getTotalTimeSeconds());
            JSONObject jsonResponse = new JSONObject(response);
            log.info(jsonResponse.toString());
            // 使用 getBoolean 方法来获取布尔值
            stopWatch.start("演示3");
            boolean proxyStatus = jsonResponse.getBoolean("proxy");
            boolean hosting = jsonResponse.getBoolean("hosting");
            String countryCode = jsonResponse.getString("country");
            String ips = jsonResponse.getString("isp");
            List<String> typeList = Arrays.asList(
                    "VOCOM International Telecommunication, INC.","Cogent Communications","Google LLC"
            );
            if (proxyStatus || hosting ||typeList.contains(ips) || conuntryList.contains(countryCode)) {
                conde.set(1);
            } else {
                conde.set(0);
            }
            stopWatch.stop();
            log.info("演示3:{}",stopWatch.getTotalTimeSeconds());
        } catch (RestClientException e) {
            log.error("Exception occurred: " + e.getMessage());
            conde.set(0);
        }
        return conde.get();
    }


    @Override
    public List<DmCondition> getAll() {
        return this.dmConditionDao.getAll();
    }
}
