package cn.itcast.hotel.service;

import cn.itcast.hotel.entity.DmAccount;

/**
 * (DmAccount)表服务接口
 *
 * @author makejava
 * @since 2024-06-12 16:41:30
 */
public interface DmAccountService {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DmAccount queryById(Long id);

    /**
     * 分页查询
     *
     * @param dmAccount 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
//    Page<DmAccount> queryByPage(DmAccount dmAccount, PageRequest pageRequest);

    /**
     * 新增数据
     *
     * @param dmAccount 实例对象
     * @return 实例对象
     */
    DmAccount insert(DmAccount dmAccount);

    /**
     * 修改数据
     *
     * @param dmAccount 实例对象
     * @return 实例对象
     */
    DmAccount update(DmAccount dmAccount);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(Long id);

}
