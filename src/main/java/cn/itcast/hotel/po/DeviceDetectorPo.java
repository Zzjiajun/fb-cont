package cn.itcast.hotel.po;

import lombok.Data;

/**
 * 设备检测结果PO类
 */
@Data
public class DeviceDetectorPo {
    private String userAgent;
    private Os os;
    private Client client;
    private Device device;
    private Boolean found;
    private Boolean isMobile;
    private Boolean isBot;
    private Boolean isTablet;
    private Boolean isDesktop;
    private Boolean isSmartphone;
    private Boolean isFeaturePhone;
    private Boolean isConsole;
    private Boolean isCarBrowser;
    private Boolean isCamera;
    private Boolean isPortableMediaPlayer;
    private Boolean isPhablet;
    private Boolean isSmartDisplay;
    private Boolean isSmartSpeaker;
    private Boolean isWearable;
    private Boolean isPeripheral;
    private Boolean isTV;

    @Data
    public static class Os {
        private String osFamily;
        private String name;
        private String shortName;
        private String version;
        private String platform;
    }

    @Data
    public static class Client {
        private String deviceType;
        private String name;
        private String version;
    }

    @Data
    public static class Device {
        private String deviceType;
        private String model;
        private String brand;
    }
} 