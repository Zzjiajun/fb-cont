package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.entity.DmClick;
import cn.itcast.hotel.dao.DmClickDao;
import cn.itcast.hotel.service.DmClickService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * (DmClick)表服务实现类
 *
 * @author makejava
 * @since 2024-06-12 16:42:28
 */
@Service("dmClickService")
public class DmClickServiceImpl implements DmClickService {
    @Resource
    private DmClickDao dmClickDao;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public DmClick queryById(Integer id) {
        return this.dmClickDao.queryById(id);
    }

    /**
     * 分页查询
     *
     * @param dmClick 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
//    @Override
//    public Page<DmClick> queryByPage(DmClick dmClick, PageRequest pageRequest) {
//        long total = this.dmClickDao.count(dmClick);
//        return new PageImpl<>(this.dmClickDao.queryAllByLimit(dmClick, pageRequest), pageRequest, total);
//    }

    /**
     * 新增数据
     *
     * @param dmClick 实例对象
     * @return 实例对象
     */
    @Override
    public DmClick insert(DmClick dmClick) {
        this.dmClickDao.insert(dmClick);
        return dmClick;
    }

    /**
     * 修改数据
     *
     * @param dmClick 实例对象
     * @return 实例对象
     */
    @Override
    public void update(DmClick dmClick) {
        this.dmClickDao.update(dmClick);
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer id) {
        return this.dmClickDao.deleteById(id) > 0;
    }

    @Override
    public List<DmClick> queryByIp(DmClick dmClick) {
        return this.dmClickDao.queryByIp(dmClick);
    }
}
