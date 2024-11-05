package cn.itcast.hotel.service;

import cn.itcast.hotel.entity.DmModles;

import java.util.List;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;

/**
 * (DmModles)表服务接口
 *
 * @author makejava
 * @since 2024-11-01 01:30:40
 */
public interface DmModlesService {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DmModles queryById(Integer id);

//    /**
//     * 分页查询
//     *
//     * @param dmModles 筛选条件
//     * @param pageRequest      分页对象
//     * @return 查询结果
//     */
//    Page<DmModles> queryByPage(DmModles dmModles, PageRequest pageRequest);

    /**
     * 新增数据
     *
     * @param dmModles 实例对象
     * @return 实例对象
     */
    DmModles insert(DmModles dmModles);

    /**
     * 修改数据
     *
     * @param dmModles 实例对象
     * @return 实例对象
     */
    DmModles update(DmModles dmModles);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(Integer id);


    List<DmModles> getAll();

}
