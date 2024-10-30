package cn.itcast.hotel.entity;

import lombok.Data;

import java.beans.Transient;
import java.util.Date;
import java.io.Serializable;

/**
 * (DmCondition)实体类
 *
 * @author makejava
 * @since 2024-06-02 20:54:33
 */
@Data
public class DmCondition implements Serializable {
    private static final long serialVersionUID = -44184997961662860L;

    private Integer id;
/**
     * 用户名
     */
    private String userName;
/**
     * 二级域名
     */
    private String accessAddress;
/**
     * 国家限制
     */
    private String ipCountry;
/**
     * 是否添加时区
     */
    private Integer timeZone;
/**
     * 时区洲
     */
    private String timeContinent;
/**
     * 设备语言
     */
    private Integer isChinese;
/**
     * 移动设备
     */
    private Integer isMobile;
/**
     * 指定设备
     */
    private Integer isSpecificDevice;
/**
     * 访问路径
     */
    private Integer isFbclid;
/**
     * 创建时间
     */
    private Date createTime;
/**
     * 修改时间
     */
    private Date updateTime;
/**
     * ip
     */
    private Integer isIp;

    private Integer isVpn;

    private Integer vpnCode;
    /**
     * ip限制
     */
    private Integer ipLimits;
    /**
     * 白名单开关
     */
    private Integer ipWhite;
    /**
     * 白名单
     */
    private String whiteList;
}

