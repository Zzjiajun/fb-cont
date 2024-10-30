package cn.itcast.hotel.entity;

import lombok.Data;

import java.beans.Transient;
import java.util.Date;
import java.io.Serializable;

/**
 * (DmDomain)实体类
 *
 * @author makejava
 * @since 2024-03-22 04:15:07
 */
@Data
public class DmDomain implements Serializable {
    private static final long serialVersionUID = 332154693199940993L;

    private Integer id;

    private String domainName;

    private String link;

    private String userName;

    private Date createTime;

    private Date updateTime;

}

