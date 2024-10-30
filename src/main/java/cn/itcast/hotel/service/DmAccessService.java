package cn.itcast.hotel.service;

import cn.itcast.hotel.entity.DmAccess;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

/**
 * (DmAccess)表服务接口
 *
 * @author makejava
 * @since 2024-06-12 16:41:13
 */
public interface DmAccessService {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DmAccess queryById(Integer id);

    /**
     * 分页查询
     *
     * @param dmAccess 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
//    Page<DmAccess> queryByPage(DmAccess dmAccess, PageRequest pageRequest);

    /**
     * 新增数据
     *
     * @param dmAccess 实例对象
     * @return 实例对象
     */
    DmAccess insert(DmAccess dmAccess);

    /**
     * 修改数据
     *
     * @param dmAccess 实例对象
     * @return 实例对象
     */
    void update(DmAccess dmAccess);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(Integer id);

    List<DmAccess> queryByIp(DmAccess dmAccess);

}
