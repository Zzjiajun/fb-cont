package cn.itcast.hotel.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Date;

public class IpapiIs {
    public static void main(String[] args) throws IOException {
        Date start = new Date();
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.ipapi.is?q=151.185.8.233&key=a7a72f40f2d6131e")
                .method("GET", null)
                .build();

        Response response = client.newCall(request).execute();
        Date end = new Date();
        System.out.println("Time elapsed: " + (end.getTime() - start.getTime()) + "ms");
        System.out.println(response.body().string());
    }
}
