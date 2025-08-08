package cn.itcast.hotel.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import okhttp3.*;


public class Ipapis {

    public static void main(String[] args) throws Exception {
        String version1 = "16.8.7";
        String version2="17.8.6";
        int result = comVersions(version1, version2);
        System.out.println(result);
        boolean b = compareVersion(version1, version2);
        System.out.println(b);
    }


    private static int comVersions(String version1, String version2) {
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");

        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

            if (num1 != num2) {
                return num1 - num2;
            }
        }
        return 0;
    }



    private static boolean compareVersion(String version, String iosVersion) {
        String[] versionArr = version.split("\\.");
        String[] iosVersionArr = iosVersion.split("\\.");
        for (int i = 0; i < versionArr.length; i++) {
            if (Integer.parseInt(versionArr[i]) > Integer.parseInt(iosVersionArr[i])) {
                return true;
            }
        }
        return false;
    }

}