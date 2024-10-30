package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.entity.DmDomain;
import cn.itcast.hotel.dao.DmDomainDao;
import cn.itcast.hotel.service.DmDomainService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * (DmDomain)表服务实现类
 *
 * @author makejava
 * @since 2024-03-22 04:15:09
 */
@Service("dmDomainService")
public class DmDomainServiceImpl implements DmDomainService {
    @Resource
    private DmDomainDao dmDomainDao;

    /**
     * 通过ID查询单条数据
     *
     */
    @Override
    @Async
    public DmDomain queryById(DmDomain dmDomain) {
        return this.dmDomainDao.queryGet(dmDomain);
    }

    @Override
    public String queryString(DmDomain dmDomain) {
        return this.dmDomainDao.queryString(dmDomain.getDomainName());
    }

    /**
     * 分页查询
     *
     * @param dmDomain 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
//    @Override
//    public Page<DmDomain> queryByPage(DmDomain dmDomain, PageRequest pageRequest) {
//        long total = this.dmDomainDao.count(dmDomain);
//        return new PageImpl<>(this.dmDomainDao.queryAllByLimit(dmDomain, pageRequest), pageRequest, total);
//    }

    /**
     * 新增数据
     *
     * @param dmDomain 实例对象
     * @return 实例对象
     */
    @Override
    public DmDomain insert(DmDomain dmDomain) {
        this.dmDomainDao.insert(dmDomain);
        return dmDomain;
    }

    /**
     * 修改数据
     *
     * @param dmDomain 实例对象
     * @return 实例对象
     */
    @Override
    public void update(DmDomain dmDomain) {
        this.dmDomainDao.update(dmDomain);
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer id) {
        return this.dmDomainDao.deleteById(id) > 0;
    }
}
