package cn.itcast.hotel.service;

import cn.itcast.hotel.entity.DmClick;

import java.util.List;

/**
 * (DmClick)表服务接口
 *
 * @author makejava
 * @since 2024-06-12 16:42:28
 */
public interface DmClickService {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DmClick queryById(Integer id);

    /**
     * 分页查询
     *
     * @param dmClick 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
//    Page<DmClick> queryByPage(DmClick dmClick, PageRequest pageRequest);

    /**
     * 新增数据
     *
     * @param dmClick 实例对象
     * @return 实例对象
     */
    DmClick insert(DmClick dmClick);

    /**
     * 修改数据
     *
     * @param dmClick 实例对象
     * @return 实例对象
     */
    void update(DmClick dmClick);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(Integer id);

    List<DmClick> queryByIp(DmClick dmClick);

}
