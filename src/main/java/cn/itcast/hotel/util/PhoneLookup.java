package cn.itcast.hotel.util;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class PhoneLookup {

    public static void main(String[] args) {
        String phoneNumber = "14158586273"; // 替换为你要查询的电话号码
        String apiKey = "your_api_key"; // 替换为你的API密钥

        try {
            String result = lookupPhoneNumber(phoneNumber, apiKey);
            System.out.println("查询结果: " + result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String lookupPhoneNumber(String phoneNumber, String apiKey) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String url = "https://phonevalidation.abstractapi.com/v1/?api_key=" + apiKey
                + "&phone=" + phoneNumber;

        HttpGet request = new HttpGet(url);
        request.setHeader("Accept", "application/json");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            } else {
                throw new IOException("Failed to lookup phone number: " + response.getStatusLine().getStatusCode());
            }
        }
    }
}