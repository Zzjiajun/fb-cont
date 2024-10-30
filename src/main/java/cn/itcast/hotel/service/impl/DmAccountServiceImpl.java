package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.entity.DmAccount;
import cn.itcast.hotel.dao.DmAccountDao;
import cn.itcast.hotel.service.DmAccountService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * (DmAccount)表服务实现类
 *
 * @author makejava
 * @since 2024-06-12 16:41:30
 */
@Service("dmAccountService")
public class DmAccountServiceImpl implements DmAccountService {
    @Resource
    private DmAccountDao dmAccountDao;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public DmAccount queryById(Long id) {
        return this.dmAccountDao.queryById(id);
    }

    /**
     * 分页查询
     *
     * @param dmAccount 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
//    @Override
//    public Page<DmAccount> queryByPage(DmAccount dmAccount, PageRequest pageRequest) {
//        long total = this.dmAccountDao.count(dmAccount);
//        return new PageImpl<>(this.dmAccountDao.queryAllByLimit(dmAccount, pageRequest), pageRequest, total);
//    }

    /**
     * 新增数据
     *
     * @param dmAccount 实例对象
     * @return 实例对象
     */
    @Override
    public DmAccount insert(DmAccount dmAccount) {
        this.dmAccountDao.insert(dmAccount);
        return dmAccount;
    }

    /**
     * 修改数据
     *
     * @param dmAccount 实例对象
     * @return 实例对象
     */
    @Override
    public DmAccount update(DmAccount dmAccount) {
        this.dmAccountDao.update(dmAccount);
        return this.queryById(dmAccount.getId());
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Long id) {
        return this.dmAccountDao.deleteById(id) > 0;
    }
}
