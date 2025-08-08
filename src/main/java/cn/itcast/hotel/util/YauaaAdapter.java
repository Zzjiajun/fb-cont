package cn.itcast.hotel.util;

import cn.itcast.hotel.po.DeviceDetectorPo;
import cn.itcast.hotel.po.YauaaDevicePo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * YAUAA适配器类
 * 将YAUAA结果转换为DeviceDetectorPo格式，保持与现有代码兼容
 */
@Component
@Slf4j
public class YauaaAdapter {
    
    @Autowired
    private YauaaDeviceDetector yauaaDetector;
    
    /**
     * 解析用户代理字符串，返回DeviceDetectorPo格式
     * @param userAgent 用户代理字符串
     * @return DeviceDetectorPo对象
     */
    public DeviceDetectorPo parseUserAgent(String userAgent) {
        try {
            // 使用YAUAA解析
            YauaaDevicePo yauaaResult = yauaaDetector.parseUserAgent(userAgent);
            
            // 转换为DeviceDetectorPo格式
            return convertToDeviceDetectorPo(yauaaResult);
        } catch (Exception e) {
            log.error("YAUAA解析失败: {}", userAgent, e);
            return createDefaultDeviceDetectorPo();
        }
    }
    
    /**
     * 将YauaaDevicePo转换为DeviceDetectorPo
     */
    private DeviceDetectorPo convertToDeviceDetectorPo(YauaaDevicePo yauaaResult) {
        DeviceDetectorPo devicePo = new DeviceDetectorPo();
        
        // 设置基本信息
        devicePo.setUserAgent(yauaaResult.getUserAgent());
        devicePo.setFound(yauaaResult.getFound());
        devicePo.setIsMobile(yauaaResult.getIsMobile());
        devicePo.setIsBot(yauaaResult.getIsBot());
        devicePo.setIsTablet(yauaaResult.getIsTablet());
        devicePo.setIsDesktop(yauaaResult.getIsDesktop());
        devicePo.setIsSmartphone(yauaaResult.getIsSmartphone());
        devicePo.setIsFeaturePhone(yauaaResult.getIsFeaturePhone());
        devicePo.setIsConsole(yauaaResult.getIsConsole());
        devicePo.setIsCarBrowser(yauaaResult.getIsCarBrowser());
        devicePo.setIsCamera(yauaaResult.getIsCamera());
        devicePo.setIsPortableMediaPlayer(yauaaResult.getIsPortableMediaPlayer());
        devicePo.setIsPhablet(yauaaResult.getIsPhablet());
        devicePo.setIsSmartDisplay(yauaaResult.getIsSmartDisplay());
        devicePo.setIsSmartSpeaker(yauaaResult.getIsSmartSpeaker());
        devicePo.setIsWearable(yauaaResult.getIsWearable());
        devicePo.setIsPeripheral(yauaaResult.getIsPeripheral());
        devicePo.setIsTV(yauaaResult.getIsTV());
        
        // 设置操作系统信息
        if (yauaaResult.getOs() != null) {
            DeviceDetectorPo.Os os = new DeviceDetectorPo.Os();
            os.setName(yauaaResult.getOs().getName());
            os.setVersion(yauaaResult.getOs().getVersion());
            os.setOsFamily(yauaaResult.getOs().getOsClass());
            os.setShortName(yauaaResult.getOs().getOsName());
            os.setPlatform(yauaaResult.getOs().getOsClass());
            devicePo.setOs(os);
        }
        
        // 设置客户端信息
        if (yauaaResult.getClient() != null) {
            DeviceDetectorPo.Client client = new DeviceDetectorPo.Client();
            client.setName(yauaaResult.getClient().getName());
            client.setVersion(yauaaResult.getClient().getVersion());
            client.setDeviceType(yauaaResult.getClient().getAgentClass());
            devicePo.setClient(client);
        }
        
        // 设置设备信息
        if (yauaaResult.getDevice() != null) {
            DeviceDetectorPo.Device device = new DeviceDetectorPo.Device();
            device.setDeviceType(yauaaResult.getDevice().getDeviceClass());
            device.setModel(yauaaResult.getDevice().getDeviceModel());
            device.setBrand(yauaaResult.getDevice().getDeviceBrand());
            devicePo.setDevice(device);
        }
        
        return devicePo;
    }
    
    /**
     * 创建默认的DeviceDetectorPo对象
     */
    private DeviceDetectorPo createDefaultDeviceDetectorPo() {
        DeviceDetectorPo devicePo = new DeviceDetectorPo();
        devicePo.setFound(false);
        devicePo.setIsMobile(false);
        devicePo.setIsDesktop(true);
        devicePo.setIsBot(false);
        
        // 设置默认操作系统
        DeviceDetectorPo.Os os = new DeviceDetectorPo.Os();
        os.setName("Unknown");
        os.setVersion("Unknown");
        os.setOsFamily("Unknown");
        os.setShortName("Unknown");
        os.setPlatform("Unknown");
        devicePo.setOs(os);
        
        // 设置默认客户端
        DeviceDetectorPo.Client client = new DeviceDetectorPo.Client();
        client.setName("Unknown");
        client.setVersion("Unknown");
        client.setDeviceType("Unknown");
        devicePo.setClient(client);
        
        // 设置默认设备
        DeviceDetectorPo.Device device = new DeviceDetectorPo.Device();
        device.setDeviceType("Desktop");
        device.setBrand("Unknown");
        device.setModel("Unknown");
        devicePo.setDevice(device);
        
        return devicePo;
    }
    
    /**
     * 获取YAUAA性能统计信息
     */
    public String getPerformanceStats() {
        return yauaaDetector.getPerformanceStats();
    }
    
    /**
     * 清理YAUAA缓存
     */
    public void clearCache() {
        yauaaDetector.clearCache();
    }
    
    /**
     * 获取YAUAA缓存大小
     */
    public int getCacheSize() {
        return yauaaDetector.getCacheSize();
    }
} 