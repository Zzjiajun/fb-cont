package cn.itcast.hotel.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.InetAddress;

public class Fpjs {
    public static void main(String[] args) throws IOException {
        InetAddress byName = InetAddress.getByName("31.13.127.15");
        System.out.println(byName);
    }

}
