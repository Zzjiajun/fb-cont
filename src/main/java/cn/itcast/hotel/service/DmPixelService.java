package cn.itcast.hotel.service;

import cn.itcast.hotel.entity.DmPixel;

import java.util.List;

/**
 * (DmPixel)表服务接口
 *
 * @author makejava
 * @since 2024-03-26 22:00:57
 */
public interface DmPixelService {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DmPixel queryById(Integer id);

    /**
     * 分页查询
     *
     * @param dmPixel 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
//    Page<DmPixel> queryByPage(DmPixel dmPixel, PageRequest pageRequest);

    /**
     * 新增数据
     *
     * @param dmPixel 实例对象
     * @return 实例对象
     */
    DmPixel insert(DmPixel dmPixel);

    /**
     * 修改数据
     *
     * @param dmPixel 实例对象
     * @return 实例对象
     */
    DmPixel update(DmPixel dmPixel);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(Integer id);

    List<String> queryPixelIdsByDomain(DmPixel dmPixel);

}
