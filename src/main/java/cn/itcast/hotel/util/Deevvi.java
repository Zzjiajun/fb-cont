package cn.itcast.hotel.util;

import cn.itcast.hotel.po.DeviceDetectorPo;
import com.deevvi.device.detector.engine.api.DeviceDetectorParser;
import com.deevvi.device.detector.engine.api.DeviceDetectorResult;
import com.google.gson.Gson;
import org.json.JSONObject;

import java.io.IOException;

public class Deevvi {
    public static void main(String[] args) throws IOException {
        DeviceDetectorParser parser = DeviceDetectorParser.getClient();
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36 Edg/130.0.0.0";
        DeviceDetectorResult result = parser.parse(userAgent);
        String json = result.toJSON();
        
        // 将result.toJSON() 转化为json格式
        JSONObject jsonObject = new JSONObject(json);
        System.out.println(jsonObject);
        
        // 将JSON转换为DeviceDetectorPo对象
        DeviceDetectorPo deviceDetectorPo = parseUserAgent(userAgent);
        System.out.println(deviceDetectorPo.getOs());
        System.out.println(deviceDetectorPo.getDevice());
        System.out.println(deviceDetectorPo.getClient());
        System.out.println(deviceDetectorPo.getFound());
        System.out.println(deviceDetectorPo.getIsMobile());
        System.out.println(deviceDetectorPo.getIsBot());

        // 打印DeviceDetectorPo对象
        System.out.println("设备检测结果PO对象: " + deviceDetectorPo);
        
        System.out.println("Result found: " + result.found());
        System.out.println("User-agent is mobile: " + result.isMobileDevice());
        System.out.println("User-agent is bot: " + result.isBot());
        System.out.println("Result as JSON: " + result.toJSON());
    }
    
    /**
     * 解析用户代理字符串并返回设备检测结果PO对象
     * 
     * @param userAgent 用户代理字符串
     * @return 设备检测结果PO对象
     */
    public static DeviceDetectorPo parseUserAgent(String userAgent) {
        DeviceDetectorPo deviceDetectorPo = new DeviceDetectorPo();
        try {
            DeviceDetectorParser parser = DeviceDetectorParser.getClient();
            DeviceDetectorResult result = parser.parse(userAgent);
            System.out.println("Result as JSON: " + result.toJSON());
            String json = result.toJSON();
            
            // 将JSON转换为DeviceDetectorPo对象
            Gson gson = new Gson();
            deviceDetectorPo= gson.fromJson(json, DeviceDetectorPo.class);
            
            // 设置一些额外的属性
            deviceDetectorPo.setFound(result.found());
            deviceDetectorPo.setIsMobile(result.isMobileDevice());
            deviceDetectorPo.setIsBot(result.isBot());
            deviceDetectorPo.setUserAgent(userAgent);
            
            return deviceDetectorPo;
        } catch (Exception e) {
            e.printStackTrace();
            return deviceDetectorPo;
        }
    }
}
