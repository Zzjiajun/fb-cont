package cn.itcast.hotel.entity;

import lombok.Data;

import java.util.Date;
import java.io.Serializable;

/**
 * (Tonck)实体类
 *
 * @author makejava
 * @since 2024-06-12 01:56:14
 */
@Data
public class Tonck implements Serializable {
    private static final long serialVersionUID = -43185938547541964L;

    private Integer id;
/**
     * 图片识别tonck
     */
    private String tonck;
/**
     * 创建时间
     */
    private Date createTime;
/**
     * 修改时间
     */
    private Date updateTime;


}

