package cn.itcast.hotel.service;

import cn.itcast.hotel.entity.DmDomain;

/**
 * (DmDomain)表服务接口
 *
 * @author makejava
 * @since 2024-03-22 04:15:09
 */
public interface DmDomainService {

    /**
     * 通过ID查询单条数据
     *
     */
    DmDomain queryById(DmDomain dmDomain);


    String queryString(DmDomain dmDomain);
    /**
     * 分页查询
     *
     * @param dmDomain 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
//    Page<DmDomain> queryByPage(DmDomain dmDomain, PageRequest pageRequest);

    /**
     * 新增数据
     *
     * @param dmDomain 实例对象
     * @return 实例对象
     */
    DmDomain insert(DmDomain dmDomain);

    /**
     * 修改数据
     *
     * @param dmDomain 实例对象
     * @return 实例对象
     */
    void update(DmDomain dmDomain);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(Integer id);

}
