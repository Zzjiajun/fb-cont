package cn.itcast.hotel.entity;

import lombok.Data;

import java.util.Date;
import java.io.Serializable;

/**
 * (DmClick)实体类
 *
 * @author makejava
 * @since 2024-06-12 16:42:28
 */
@Data
public class DmClick implements Serializable {
    private static final long serialVersionUID = 596446222756172038L;

    private Integer id;
/**
     * 数据中心id
     */
    private Integer centerId;
/**
     * 点击ip
     */
    private String ip;

    private String region;
/**
     * 点击设备
     */
    private String clickDevice;
/**
     * 点击类型
     */
    private String clickType;
/**
     * 创建时间
     */
    private Date createTime;
/**
     * 修改时间
     */
    private Date updateTime;
}

