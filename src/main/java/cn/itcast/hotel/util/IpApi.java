package cn.itcast.hotel.util;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
public class IpApi {
    public static void main(String[] args) {
        String apiKey = "qxGrNJTrggWAR31";  // 将your_api_key替换为实际的API密钥
        String ipToCheck = "35.212.205.97";    // 将8.8.8.8替换为要查询的IP
        try {
            String url = "https://pro.ip-api.com/json/" + ipToCheck + "?key=" + apiKey + "&fields=status,proxy,hosting,query";
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
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
            System.out.println(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
