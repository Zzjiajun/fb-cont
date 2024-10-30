package cn.itcast.hotel.service;

import cn.itcast.hotel.entity.DmCountry;

import java.util.List;

/**
 * (DmCountry)表服务接口
 *
 * @author makejava
 * @since 2024-06-06 07:10:43
 */
public interface DmCountryService {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DmCountry queryById(Integer id);

    /**
     * 分页查询
     *
     * @param dmCountry 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */

    /**
     * 新增数据
     *
     * @param dmCountry 实例对象
     * @return 实例对象
     */
    DmCountry insert(DmCountry dmCountry);

    /**
     * 修改数据
     *
     * @param dmCountry 实例对象
     * @return 实例对象
     */
    DmCountry update(DmCountry dmCountry);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(Integer id);

    List<DmCountry> getAll();

}
