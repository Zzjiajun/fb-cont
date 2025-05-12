package cn.itcast.hotel.util;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class IpApi {
    public static void main(String[] args) {
        long start = new Date().getTime();
        String apiKey = "qxGrNJTrggWAR31";  // 将your_api_key替换为实际的API密钥
        String ipToCheck = "83.37.30.54";    // 将8.8.8.8替换为要查询的IP
        try {
            String url = "https://pro.ip-api.com/json/" + ipToCheck + "?key=" + apiKey + "&fields=status,country,isp,proxy,hosting,query";
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
                System.out.println( inputLine );
            }
            in.close();
            JSONObject jsonObject = new JSONObject(in);
            System.out.println(jsonObject.toString());
//            String proxyStatus = jsonObject.getString("proxy");
//            String connectionType = jsonObject.getString("hosting");
//            System.out.println("Proxy Status: " + proxyStatus);
//            System.out.println("hosting: " + connectionType);
            // 输出服务器返回的JSON结果
            long end = new Date().getTime();
            System.out.println("Time taken : " + (end - start) + " ms");
            System.out.println(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
