package cn.itcast.hotel.entity;

import lombok.Data;

import java.util.Date;
import java.io.Serializable;

/**
 * (DmAccess)实体类
 *
 * @author makejava
 * @since 2024-06-12 16:41:13
 */
@Data
public class DmAccess implements Serializable {
    private static final long serialVersionUID = 920350287317378547L;

    private Integer id;
/**
     * 数据中心绑定的id父类
     */
    private Integer centerId;
/**
     * IP地址
     */
    private String ip;
/**
     * 访客地区
     */
    private String region;
/**
     * 访问路径
     */
    private String accessPath;
/**
     * 访问设备
     */
    private String accessDevice;
/**
     * 访客类型
     */
    private String visitorType;
    /**
     * 机型
     */
    private String models;

    /**
     * 来源
     */
    private String source;
    /**
     * 是否通过
     */
    private String passed;
    /**
     * 设备详情
     */
    private String deviceDetails;
    /**
     * 客户端详情
     */
    private String clientDetails;
    /**
     * 访客语言
     */
    private String language;

    /**
     * 设备语言
     */
    private String continent;
    /**
     * ip详情
     */
    private String ipDetails;
    /**
     * IP时区
     */
    private String ipTime;

    private String Details;

    private String virtualDetails;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改时间
     */
    private Date updateTime;


}

