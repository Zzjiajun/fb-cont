package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.entity.DmCountry;
import cn.itcast.hotel.dao.DmCountryDao;
import cn.itcast.hotel.service.DmCountryService;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.annotation.Resource;
import java.util.List;

/**
 * (DmCountry)表服务实现类
 *
 * @author makejava
 * @since 2024-06-06 07:10:43
 */
@Service("dmCountryService")
public class DmCountryServiceImpl implements DmCountryService {
    @Resource
    private DmCountryDao dmCountryDao;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public DmCountry queryById(Integer id) {
        return this.dmCountryDao.queryById(id);
    }

    /**
     * 分页查询
     *
     * @param dmCountry 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */

    /**
     * 新增数据
     *
     * @param dmCountry 实例对象
     * @return 实例对象
     */
    @Override
    public DmCountry insert(DmCountry dmCountry) {
        this.dmCountryDao.insert(dmCountry);
        return dmCountry;
    }

    /**
     * 修改数据
     *
     * @param dmCountry 实例对象
     * @return 实例对象
     */
    @Override
    public DmCountry update(DmCountry dmCountry) {
        this.dmCountryDao.update(dmCountry);
        return this.queryById(dmCountry.getId());
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer id) {
        return this.dmCountryDao.deleteById(id) > 0;
    }

    @Override
    public List<DmCountry> getAll() {
        return this.dmCountryDao.getAll();
    }
}
