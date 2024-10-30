package cn.itcast.hotel.util;

import com.ipqualityscore.JavaIPQSDBReader.DBReader;
import com.ipqualityscore.JavaIPQSDBReader.FileReader;
import com.ipqualityscore.JavaIPQSDBReader.IPQSRecord;

import java.nio.file.FileSystems;

public class IqDbServer {
    public static void main(String[] args) {
        try {
            // Open a DB file reader.
            FileReader reader = DBReader.Open(String.valueOf(FileSystems.getDefault().getPath("IPQualityScore-IP-Reputation-Database-IPv4-Sample.ipqs")));
            // Request data about a given IP address.
            String ip = "5.62.20.19";
            IPQSRecord record = reader.Fetch(ip);

            // Use the IPQSRecord to print some data about this IP.
            if(record.isProxy()){
                System.out.println(ip + " is a proxy.");
            } else {
                System.out.println(ip + " is not a proxy.");
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
