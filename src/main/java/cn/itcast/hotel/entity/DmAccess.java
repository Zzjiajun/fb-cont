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
     * 创建时间
     */
    private Date createTime;
/**
     * 修改时间
     */
    private Date updateTime;



}

