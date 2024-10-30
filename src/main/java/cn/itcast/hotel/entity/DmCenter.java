package cn.itcast.hotel.entity;

import lombok.Data;

import java.util.Date;
import java.io.Serializable;

/**
 * (DmCenter)实体类
 *
 * @author makejava
 * @since 2024-03-28 15:37:37
 */
@Data
public class DmCenter implements Serializable {

    private Integer id;
/**
     * 用户名
     */
    private String userName;
/**
     * 地区
     */
    private String region;
/**
     * 像素代码
     */
    private String pixel;
/**
     * 链接地址
     */
    private String link;
/**
     * 主域名
     */
    private String domain;
/**
     * 二级域名
     */
    private String secondaryDomain;
/**
     * 模版地址
     */
    private String serialNumber;
/**
     * 域名访问量
     */
    private Integer visitsNumber;
/**
     * 按钮点击数量
     */
    private Integer clicksNumber;
/**
     * 创建时间
     */
    private Date createTime;
/**
     * 修改时间
     */
    private Date updateTime;

    private Integer displayOption;

    private Integer diversion;

    private Integer trolls;
}

