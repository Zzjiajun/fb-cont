package cn.itcast.hotel.po;

import lombok.Data;

/**
 * YAUAA设备检测结果PO类 - 简洁版
 * 与DeviceDetectorPo保持兼容
 */
@Data
public class YauaaDevicePo {
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
        private String name;           // 操作系统名称
        private String version;        // 操作系统版本
        private String osClass;        // 操作系统类型
        private String osName;         // 操作系统名称
        private String osVersion;      // 操作系统版本
        private String osVersionMajor; // 主版本号
        private String osVersionMinor; // 次版本号
    }

    @Data
    public static class Client {
        private String name;           // 客户端名称
        private String version;        // 客户端版本
        private String agentClass;     // 客户端类型
        private String agentName;      // 客户端名称
        private String agentVersion;   // 客户端版本
        private String layoutEngineClass;    // 布局引擎类型
        private String layoutEngineName;     // 布局引擎名称
        private String layoutEngineVersion;  // 布局引擎版本
    }

    @Data
    public static class Device {
        private String deviceClass;    // 设备类型
        private String deviceBrand;    // 设备品牌
        private String deviceName;     // 设备名称
        private String deviceBrandName; // 设备品牌名称
        private String deviceModel;    // 设备型号
    }
} 