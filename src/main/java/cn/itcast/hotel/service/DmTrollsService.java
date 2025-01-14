package cn.itcast.hotel.service;

import cn.itcast.hotel.entity.DmTrolls;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * (DmTrolls)表服务接口
 *
 * @author makejava
 * @since 2024-09-26 02:48:12
 */
public interface DmTrollsService {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DmTrolls queryById(Integer id);



    /**
     * 分页查询
     *
     * @param dmTrolls 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
//    Page<DmTrolls> queryByPage(DmTrolls dmTrolls, PageRequest pageRequest);

    /**
     * 新增数据
     *
     * @param dmTrolls 实例对象
     * @return 实例对象
     */
    DmTrolls insert(DmTrolls dmTrolls);

    /**
     * 修改数据
     *
     * @param dmTrolls 实例对象
     * @return 实例对象
     */
    DmTrolls update(DmTrolls dmTrolls);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(Integer id);


    List<DmTrolls> queryByIp(DmTrolls dmTrolls);
}
