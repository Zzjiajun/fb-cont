package cn.itcast.hotel.entity;

import java.util.Date;
import java.io.Serializable;

/**
 * (DmTrolls)实体类
 *
 * @author makejava
 * @since 2024-09-26 02:48:12
 */
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
     * 创建时间
     */
    private Date createTime;
/**
     * 修改时间
     */
    private Date updateTime;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCenterId() {
        return centerId;
    }

    public void setCenterId(Integer centerId) {
        this.centerId = centerId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getTrollsPath() {
        return trollsPath;
    }

    public void setTrollsPath(String trollsPath) {
        this.trollsPath = trollsPath;
    }

    public String getTrollsDevice() {
        return trollsDevice;
    }

    public void setTrollsDevice(String trollsDevice) {
        this.trollsDevice = trollsDevice;
    }

    public String getVisitorType() {
        return visitorType;
    }

    public void setVisitorType(String visitorType) {
        this.visitorType = visitorType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}

