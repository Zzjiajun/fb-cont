package cn.itcast.hotel.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class ipCountVpn {
    public static void main(String[] args) {
        long start = new Date().getTime();
        String apiKey = "9fo0uk-754692-268td8-096056";  // 将your_api_key替换为实际的API密钥
        String ipToCheck = "38.244.21.107";    // 将8.8.8.8替换为要查询的IP
        try {
            String url = "https://proxycheck.io/v2/" + ipToCheck + "?key=" + apiKey + "&vpn=1&asn=1&risk=2";
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            long start1 = new Date().getTime();
            int responseCode = con.getResponseCode();
            long end1 = new Date().getTime();
            System.out.println("Time taken : " + (end1 - start1) + " ms");
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            long end = new Date().getTime();
            System.out.println("Time taken : " + (end - start) + " ms");
            // 输出服务器返回的JSON结果
            System.out.println(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
