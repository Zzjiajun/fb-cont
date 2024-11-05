package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.entity.DmModles;
import cn.itcast.hotel.dao.DmModlesDao;
import cn.itcast.hotel.service.DmModlesService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * (DmModles)表服务实现类
 *
 * @author makejava
 * @since 2024-11-01 01:30:40
 */
@Service("dmModlesService")
public class DmModlesServiceImpl implements DmModlesService {
    @Resource
    private DmModlesDao dmModlesDao;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public DmModles queryById(Integer id) {
        return this.dmModlesDao.queryById(id);
    }

    /**
     * 分页查询
     *
     * @param dmModles 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
//    @Override
//    public Page<DmModles> queryByPage(DmModles dmModles, PageRequest pageRequest) {
//        long total = this.dmModlesDao.count(dmModles);
//        return new PageImpl<>(this.dmModlesDao.queryAllByLimit(dmModles, pageRequest), pageRequest, total);
//    }

    /**
     * 新增数据
     *
     * @param dmModles 实例对象
     * @return 实例对象
     */
    @Override
    public DmModles insert(DmModles dmModles) {
        this.dmModlesDao.insert(dmModles);
        return dmModles;
    }

    /**
     * 修改数据
     *
     * @param dmModles 实例对象
     * @return 实例对象
     */
    @Override
    public DmModles update(DmModles dmModles) {
        this.dmModlesDao.update(dmModles);
        return this.queryById(dmModles.getId());
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer id) {
        return this.dmModlesDao.deleteById(id) > 0;
    }

    @Override
    public List<DmModles> getAll() {
        return this.dmModlesDao.getAll();
    }
}
