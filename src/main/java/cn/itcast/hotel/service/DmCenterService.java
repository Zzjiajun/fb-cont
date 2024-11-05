package cn.itcast.hotel.service;

import cn.itcast.hotel.entity.DmAccess;
import cn.itcast.hotel.entity.DmCenter;
import cn.itcast.hotel.entity.DmCondition;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Map;

/**
 * (DmCenter)表服务接口
 *
 * @author makejava
 * @since 2024-03-28 15:37:38
 */
public interface DmCenterService {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DmCenter queryById(Integer id);


    DmCenter query(DmCenter dmCenter);


    /**
     * 新增数据
     *
     * @param dmCenter 实例对象
     * @return 实例对象
     */
    DmCenter insert(DmCenter dmCenter);

    /**
     * 修改数据
     *
     * @param dmCenter 实例对象
     * @return 实例对象
     */
    void update(DmCenter dmCenter);

    @Async("taskExecutor") // 指定使用的线程池
    void updateTrolls(DmCenter dmCenter);

    void updateTrophy(DmCenter dmCenter);


    void updateClick(DmCenter dmCenter);

    List<DmCenter>  getAll();

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(Integer id);

    void addAccess(DmCenter dmCenter , String ip, DmCondition dmCondition,String country,String userMobile,String paraPath);
    void addAccessVpn(DmCenter dmCenter, Map<String, String> params,String country,DmCondition dmCondition,Map<String,String> handledMap);


    void addClickCount(DmCenter dmCenter , String ip,String country,String userMobile);

    void addTrolls(DmCenter dmCenter,String ip ,String country,String userMobile,String paraPath);

}
