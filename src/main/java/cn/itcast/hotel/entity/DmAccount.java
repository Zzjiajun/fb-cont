package cn.itcast.hotel.entity;

import java.util.Date;
import java.io.Serializable;

/**
 * (DmAccount)实体类
 *
 * @author makejava
 * @since 2024-06-12 16:41:30
 */
public class DmAccount implements Serializable {
    private static final long serialVersionUID = 761884087121535748L;
/**
     * ID
     */
    private Long id;
/**
     * 账户
     */
    private String accountName;
/**
     * 邮件
     */
    private String mail;
/**
     * 持有者
     */
    private String holder;
/**
     * 余额
     */
    private Double balance;
/**
     * 账户编号
     */
    private String mobileNo;
/**
     * 状态 {0:无效,1:有效}
     */
    private Integer status;
/**
     * 备注
     */
    private String comments;
/**
     * 接户时间
     */
    private Date createTime;
/**
     * 更新时间
     */
    private Date updateTime;
/**
     * 弃用时间
     */
    private Date deprecatedTime;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getHolder() {
        return holder;
    }

    public void setHolder(String holder) {
        this.holder = holder;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
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

    public Date getDeprecatedTime() {
        return deprecatedTime;
    }

    public void setDeprecatedTime(Date deprecatedTime) {
        this.deprecatedTime = deprecatedTime;
    }

}

