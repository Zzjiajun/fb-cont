package cn.itcast.hotel.entity;

import lombok.Data;

import java.util.Date;
import java.io.Serializable;

/**
 * (DmTrolls)实体类
 *
 * @author makejava
 * @since 2024-09-26 02:48:12
 */
@Data
public class DmTrolls implements Serializable {
    private static final long serialVersionUID = -18228857997724671L;

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
    private String trollsPath;
/**
     * 访问设备
     */
    private String trollsDevice;
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
     * 失败明细
     */
    private String details;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改时间
     */
    private Date updateTime;


}

