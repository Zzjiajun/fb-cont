package cn.itcast.hotel.entity;

import lombok.Data;

import java.util.Date;
import java.io.Serializable;

/**
 * (LinkInt)实体类
 *
 * @author makejava
 * @since 2024-03-28 15:10:30
 */
@Data
public class LinkInt implements Serializable {


    private Integer id;
/**
     * 轮询目前的数量
     */
    private Integer countLink;
/**
     * 用户名
     */
    private String userName;
/**
     * 域名
     */
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

