package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.entity.DmAccess;
import cn.itcast.hotel.dao.DmAccessDao;
import cn.itcast.hotel.service.DmAccessService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * (DmAccess)表服务实现类
 *
 * @author makejava
 * @since 2024-06-12 16:41:13
 */
@Service("dmAccessService")
public class DmAccessServiceImpl implements DmAccessService {
    @Resource
    private DmAccessDao dmAccessDao;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public DmAccess queryById(Integer id) {
        return this.dmAccessDao.queryById(id);
    }

    /**
     * 分页查询
     *
     * @param dmAccess 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
//    @Override
//    public Page<DmAccess> queryByPage(DmAccess dmAccess, PageRequest pageRequest) {
//        long total = this.dmAccessDao.count(dmAccess);
//        return new PageImpl<>(this.dmAccessDao.queryAllByLimit(dmAccess, pageRequest), pageRequest, total);
//    }

    /**
     * 新增数据
     *
     * @param dmAccess 实例对象
     * @return 实例对象
     */
    @Override
    public DmAccess insert(DmAccess dmAccess) {
        this.dmAccessDao.insert(dmAccess);
        return dmAccess;
    }

    /**
     * 修改数据
     *
     * @param dmAccess 实例对象
     * @return 实例对象
     */
    @Override
    public void update(DmAccess dmAccess) {
        this.dmAccessDao.update(dmAccess);
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer id) {
        return this.dmAccessDao.deleteById(id) > 0;
    }

    @Override
    public List<DmAccess> queryByIp(DmAccess dmAccess) {
        return this.dmAccessDao.queryByIp(dmAccess);
    }
}
