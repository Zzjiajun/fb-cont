package cn.itcast.hotel.entity;

import java.util.Date;
import java.io.Serializable;

/**
 * (DmCountry)实体类
 *
 * @author makejava
 * @since 2024-06-06 07:10:43
 */
public class DmCountry implements Serializable {
    private static final long serialVersionUID = 137412348378092292L;

    private Integer id;
/**
     * 国家
     */
    private String country;
/**
     * 创建时间
     */
    private Date createTime;
/**
     * 修改时间
     */
    private Date updateTime;
/**
     * 中文名称
     */
    private String name;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

