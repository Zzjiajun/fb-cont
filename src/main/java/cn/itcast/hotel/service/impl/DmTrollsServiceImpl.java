package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.dao.DmCenterDao;
import cn.itcast.hotel.entity.DmCenter;
import cn.itcast.hotel.entity.DmTrolls;
import cn.itcast.hotel.dao.DmTrollsDao;
import cn.itcast.hotel.service.DmTrollsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.annotation.Resource;
import java.util.List;

/**
 * (DmTrolls)表服务实现类
 *
 * @author makejava
 * @since 2024-09-26 02:48:12
 */
@Service("dmTrollsService")
public class DmTrollsServiceImpl implements DmTrollsService {
    @Resource
    private DmTrollsDao dmTrollsDao;

    @Autowired
    private DmCenterDao dmCenterDao;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public DmTrolls queryById(Integer id) {
        return this.dmTrollsDao.queryById(id);
    }

    /**
     * 分页查询
     *
     * @param dmTrolls 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
//    @Override
//    public Page<DmTrolls> queryByPage(DmTrolls dmTrolls, PageRequest pageRequest) {
//        long total = this.dmTrollsDao.count(dmTrolls);
//        return new PageImpl<>(this.dmTrollsDao.queryAllByLimit(dmTrolls, pageRequest), pageRequest, total);
//    }

    /**
     * 新增数据
     *
     * @param dmTrolls 实例对象
     * @return 实例对象
     */
    @Override
    public DmTrolls insert(DmTrolls dmTrolls) {
        this.dmTrollsDao.insert(dmTrolls);
        return dmTrolls;
    }

    /**
     * 修改数据
     *
     * @param dmTrolls 实例对象
     * @return 实例对象
     */
    @Override
    public DmTrolls update(DmTrolls dmTrolls) {
        this.dmTrollsDao.update(dmTrolls);
        return this.queryById(dmTrolls.getId());
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer id) {
        return this.dmTrollsDao.deleteById(id) > 0;
    }

    @Override
    public List<DmTrolls> queryByIp(DmTrolls dmTrolls) {
        return this.dmTrollsDao.queryByIp(dmTrolls);
    }
}
