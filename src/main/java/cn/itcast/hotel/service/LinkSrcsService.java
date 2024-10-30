package cn.itcast.hotel.service;

import cn.itcast.hotel.entity.LinkSrcs;

import java.util.List;

/**
 * (LinkSrcs)表服务接口
 *
 * @author makejava
 * @since 2024-03-28 15:11:19
 */
public interface LinkSrcsService {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    LinkSrcs queryById(Integer id);

    /**
     * 分页查询
     *
     * @param linkSrcs 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */

    /**
     * 新增数据
     *
     * @param linkSrcs 实例对象
     * @return 实例对象
     */
    LinkSrcs insert(LinkSrcs linkSrcs);

    /**
     * 修改数据
     *
     * @param linkSrcs 实例对象
     * @return 实例对象
     */
    LinkSrcs update(LinkSrcs linkSrcs);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(Integer id);

    List<LinkSrcs> quertList(LinkSrcs linkSrcs);
}
