package cn.itcast.hotel.entity;

import lombok.Data;

import java.util.Date;
import java.io.Serializable;

/**
 * (DmModles)实体类
 *
 * @author makejava
 * @since 2024-11-01 01:30:40
 */
@Data
public class DmModles implements Serializable {
    private static final long serialVersionUID = 267781327618218044L;

    private Integer id;
/**
     * 机型名字
     */
    private String modelName;
/**
     * 宽度
     */
    private Integer screenWidth;

    private Integer screenHeight;
/**
     * 像素
     */
    private Integer pixelRatio;

    private Integer isDelete;
/**
     * 注册时间
     */
    private Date createTime;
/**
     * 修改时间
     */
    private Date updateTime;

}

