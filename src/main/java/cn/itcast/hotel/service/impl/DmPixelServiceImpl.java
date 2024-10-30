package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.entity.DmPixel;
import cn.itcast.hotel.dao.DmPixelDao;
import cn.itcast.hotel.service.DmPixelService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * (DmPixel)表服务实现类
 *
 * @author makejava
 * @since 2024-03-26 22:00:57
 */
@Service("dmPixelService")
public class DmPixelServiceImpl implements DmPixelService {
    @Resource
    private DmPixelDao dmPixelDao;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public DmPixel queryById(Integer id) {
        return this.dmPixelDao.queryById(id);
    }

    /**
     * 分页查询
     *
     * @param dmPixel 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
//    @Override
//    public Page<DmPixel> queryByPage(DmPixel dmPixel, PageRequest pageRequest) {
//        long total = this.dmPixelDao.count(dmPixel);
//        return new PageImpl<>(this.dmPixelDao.queryAllByLimit(dmPixel, pageRequest), pageRequest, total);
//    }

    /**
     * 新增数据
     *
     * @param dmPixel 实例对象
     * @return 实例对象
     */
    @Override
    public DmPixel insert(DmPixel dmPixel) {
        this.dmPixelDao.insert(dmPixel);
        return dmPixel;
    }

    /**
     * 修改数据
     *
     * @param dmPixel 实例对象
     * @return 实例对象
     */
    @Override
    public DmPixel update(DmPixel dmPixel) {
        this.dmPixelDao.update(dmPixel);
        return this.queryById(dmPixel.getId());
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer id) {
        return this.dmPixelDao.deleteById(id) > 0;
    }

    @Override
    public List<String> queryPixelIdsByDomain(DmPixel dmPixel) {
        return this.dmPixelDao.queryPixelIdsByDomain(dmPixel);
    }
}
