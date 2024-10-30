package cn.itcast.hotel.service;

import cn.itcast.hotel.entity.LinkInt;

/**
 * (LinkInt)表服务接口
 *
 * @author makejava
 * @since 2024-03-28 15:10:31
 */
public interface LinkIntService {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    LinkInt queryById(Integer id);


    LinkInt query(LinkInt linkInt);

    /**
     * 分页查询
     *
     * @param linkInt 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */

    /**
     * 新增数据
     *
     * @param linkInt 实例对象
     * @return 实例对象
     */
    LinkInt insert(LinkInt linkInt);

    /**
     * 修改数据
     *
     * @param linkInt 实例对象
     * @return 实例对象
     */
    void update(LinkInt linkInt);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(Integer id);

}
