package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.entity.Tonck;
import cn.itcast.hotel.dao.TonckDao;
import cn.itcast.hotel.service.TonckService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * (Tonck)表服务实现类
 *
 * @author makejava
 * @since 2024-06-12 01:56:14
 */
@Service("tonckService")
public class TonckServiceImpl implements TonckService {
    @Resource
    private TonckDao tonckDao;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public Tonck queryById(Integer id) {
        return this.tonckDao.queryById(id);
    }

    /**
     * 分页查询
     *
     * @param tonck 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */

    /**
     * 新增数据
     *
     * @param tonck 实例对象
     * @return 实例对象
     */
    @Override
    public Tonck insert(Tonck tonck) {
        this.tonckDao.insert(tonck);
        return tonck;
    }

    /**
     * 修改数据
     *
     * @param tonck 实例对象
     * @return 实例对象
     */
    @Override
    public Tonck update(Tonck tonck) {
        this.tonckDao.update(tonck);
        return this.queryById(tonck.getId());
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer id) {
        return this.tonckDao.deleteById(id) > 0;
    }
}
