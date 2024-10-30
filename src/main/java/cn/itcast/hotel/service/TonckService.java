package cn.itcast.hotel.service;

import cn.itcast.hotel.entity.Tonck;

/**
 * (Tonck)表服务接口
 *
 * @author makejava
 * @since 2024-06-12 01:56:14
 */
public interface TonckService {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    Tonck queryById(Integer id);

    /**
     * 分页查询
     *
     * @param tonck 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */

    /**
     * 新增数据
     *
     * @param tonck 实例对象
     * @return 实例对象
     */
    Tonck insert(Tonck tonck);

    /**
     * 修改数据
     *
     * @param tonck 实例对象
     * @return 实例对象
     */
    Tonck update(Tonck tonck);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(Integer id);

}
