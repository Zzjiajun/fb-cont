package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.entity.LinkSrcs;
import cn.itcast.hotel.dao.LinkSrcsDao;
import cn.itcast.hotel.service.LinkSrcsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * (LinkSrcs)表服务实现类
 *
 * @author makejava
 * @since 2024-03-28 15:11:19
 */
@Service("linkSrcsService")
public class LinkSrcsServiceImpl implements LinkSrcsService {
    @Resource
    private LinkSrcsDao linkSrcsDao;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public LinkSrcs queryById(Integer id) {
        return this.linkSrcsDao.queryById(id);
    }

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
    @Override
    public LinkSrcs insert(LinkSrcs linkSrcs) {
        this.linkSrcsDao.insert(linkSrcs);
        return linkSrcs;
    }

    /**
     * 修改数据
     *
     * @param linkSrcs 实例对象
     * @return 实例对象
     */
    @Override
    public LinkSrcs update(LinkSrcs linkSrcs) {
        this.linkSrcsDao.update(linkSrcs);
        return this.queryById(linkSrcs.getId());
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer id) {
        return this.linkSrcsDao.deleteById(id) > 0;
    }

    @Override
    public List<LinkSrcs> quertList(LinkSrcs linkSrcs) {
        return this.linkSrcsDao.quertList(linkSrcs);
    }
}
