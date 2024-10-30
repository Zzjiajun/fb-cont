package cn.itcast.hotel.entity;

import lombok.Data;

import java.util.Date;
import java.io.Serializable;

/**
 * (LinkSrcs)实体类
 *
 * @author makejava
 * @since 2024-03-28 15:11:18
 */
@Data
public class LinkSrcs implements Serializable {


    private Integer id;

    private String linkSrc;

    private String userName;

    private String domain;
/**
     * 创建时间
     */
    private Date createTime;
/**
     * 修改时间
     */
    private Date updateTime;


}

