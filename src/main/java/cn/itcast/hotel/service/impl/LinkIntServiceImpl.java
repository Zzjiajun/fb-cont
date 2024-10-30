package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.entity.LinkInt;
import cn.itcast.hotel.dao.LinkIntDao;
import cn.itcast.hotel.service.LinkIntService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * (LinkInt)表服务实现类
 *
 * @author makejava
 * @since 2024-03-28 15:10:32
 */
@Service("linkIntService")
public class LinkIntServiceImpl implements LinkIntService {
    @Resource
    private LinkIntDao linkIntDao;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public LinkInt queryById(Integer id) {
        return this.linkIntDao.queryById(id);
    }

    @Override
    public LinkInt query(LinkInt linkInt) {
        return this.linkIntDao.query(linkInt);
    }

    /**
     * 分页查询
     *
     * @param linkInt 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
    /**
     * 新增数据
     *
     * @param linkInt 实例对象
     * @return 实例对象
     */
    @Override
    public LinkInt insert(LinkInt linkInt) {
        this.linkIntDao.insert(linkInt);
        return linkInt;
    }

    /**
     * 修改数据
     *
     * @param linkInt 实例对象
     * @return 实例对象
     */
    @Override
    @Async("taskExecutor") // 指定使用的线程池
    public void update(LinkInt linkInt) {
        this.linkIntDao.update(linkInt);
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer id) {
        return this.linkIntDao.deleteById(id) > 0;
    }
}
