package cn.itcast.hotel.entity;

import lombok.Data;

import java.util.Date;
import java.io.Serializable;

/**
 * (DmPixel)实体类
 *
 * @author makejava
 * @since 2024-03-26 22:00:57
 */
@Data
public class DmPixel implements Serializable {

    private Integer id;

    private String domain;

    private String pixelId;

    private String userName;

    private Date createTime;

    private Date updateTime;

}

