package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.entity.*;
import cn.itcast.hotel.dao.DmCenterDao;
import cn.itcast.hotel.service.DmAccessService;
import cn.itcast.hotel.service.DmCenterService;
import cn.itcast.hotel.service.DmClickService;
import cn.itcast.hotel.service.DmTrollsService;
import cn.itcast.hotel.util.RedisUtils;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * (DmCenter)表服务实现类
 *
 * @author makejava
 * @since 2024-03-28 15:37:38
 */
@Service("dmCenterService")
public class DmCenterServiceImpl implements DmCenterService {
    @Resource
    private DmCenterDao dmCenterDao;

    @Resource
    private DmAccessService dmAccessService;
    @Resource
    private DmClickService dmClickService;
    @Autowired
    private RedisUtils redisUtil;

    @Resource
    private DmTrollsService dmTrollsService;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public DmCenter queryById(Integer id) {
        return this.dmCenterDao.queryById(id);
    }

    @Override
    public DmCenter query(DmCenter dmCenter) {
        return this.dmCenterDao.query(dmCenter);
    }

    /**
     * 分页查询
     *
     * @param dmCenter 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */

    /**
     * 新增数据
     *
     * @param dmCenter 实例对象
     * @return 实例对象
     */
    @Override
    public DmCenter insert(DmCenter dmCenter) {
        this.dmCenterDao.insert(dmCenter);
        return dmCenter;
    }

    /**
     * 修改数据
     *
     * @param dmCenter 实例对象
     * @return 实例对象
     */

    @Override
    @Async("taskExecutor") // 指定使用的线程池
    public void update(DmCenter dmCenter) {
        // 更新点击数
        DmCenter query = this.dmCenterDao.queryById(dmCenter.getId());
        query.setVisitsNumber(query.getVisitsNumber() + 1);
        this.dmCenterDao.update(query);
    }

    @Override
    @Async("taskExecutor") // 指定使用的线程池
    public void updateTrolls(DmCenter dmCenter) {
        // 更新点击数
        DmCenter query = this.dmCenterDao.queryById(dmCenter.getId());
        query.setTrolls(query.getTrolls() + 1);
        this.dmCenterDao.update(query);
    }

    @Override
    @Async("taskExecutor") // 指定使用的线程池
    public void updateTrophy(DmCenter dmCenter) {
        this.dmCenterDao.update(dmCenter);
    }

    @Override
    @Async("taskExecutor") // 指定使用的线程池
    public void updateClick(DmCenter dmCenter) {
        DmCenter query = this.dmCenterDao.queryById(dmCenter.getId());
        query.setClicksNumber(dmCenter.getClicksNumber()+1);
        this.dmCenterDao.update(query);
    }

    @Override
    public List<DmCenter> getAll() {
        return this.dmCenterDao.getAll();
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer id) {
        return this.dmCenterDao.deleteById(id) > 0;
    }

    @Override
    @Async("taskExecutor") // 指定使用的线程池
    public void addAccess(DmCenter dmCenter, String ip, DmCondition dmCondition, String country,String userMobile,String paraPath) {
        DmAccess dmAccess = new DmAccess();
        dmAccess.setCenterId(dmCenter.getId());
        dmAccess.setIp(ip);
        List<DmAccess> accessList = dmAccessService.queryByIp(dmAccess);
        dmAccess.setRegion(country);
        dmAccess.setCreateTime(new Date());
        dmAccess.setUpdateTime(new Date());
        if (accessList.size()>0) {
            dmAccess.setVisitorType("1");
        }else {
            dmAccess.setVisitorType("0");
        }
        if ("true".equals(userMobile)){
            dmAccess.setAccessDevice("0");
        }else {
            dmAccess.setAccessDevice("1");
        }
        if ("true".equals(paraPath)){
            dmAccess.setAccessPath("跳转访问");
        }else {
            dmAccess.setAccessPath("直接访问");
        }
        //将accessList的DmAccess字段的ip抽取出来形成新的list<String>
        List<String> listIp = accessList.stream().map(DmAccess::getIp).collect(Collectors.toList());
        listIp.add(ip);
        //更新redis、mysql
        String builtKey = redisUtil.buildKey("AoollyNumberIp", dmCondition.getAccessAddress());
        redisUtil.set(builtKey,new Gson().toJson(listIp));
        dmAccessService.insert(dmAccess);
    }


    @Override
    @Async("taskExecutor") // 指定使用的线程池
    public void addAccessVpn(DmCenter dmCenter, Map<String, String> params) {

    }

    @Override
    @Async("taskExecutor") // 指定使用的线程池
    public void addClickCount(DmCenter dmCenter, String ip,  String country, String userMobile) {
        DmClick dmClick = new DmClick();
        dmClick.setCenterId(dmCenter.getId());
        dmClick.setIp(ip);
        List<DmClick> dmClicks = dmClickService.queryByIp(dmClick);
        dmClick.setRegion(country);
        dmClick.setCreateTime(new Date());
        dmClick.setUpdateTime(new Date());
        if (dmClicks.size()>0) {
            dmClick.setClickType("1");
        }else {
            dmClick.setClickType("0");
        }
        if ("true".equals(userMobile)){
            dmClick.setClickDevice("0");
        }else {
            dmClick.setClickDevice("1");
        }
        dmClickService.insert(dmClick);
    }

    @Override
    @Async("taskExecutor") // 指定使用的线程池
    public void addTrolls(DmCenter dmCenter, String ip, String country, String userMobile ,String paraPath) {
        DmTrolls dmTroll = new DmTrolls();
        dmTroll.setCenterId(dmCenter.getId());
        dmTroll.setIp(ip);
        List<DmTrolls> dmTrollsList = dmTrollsService.queryByIp(dmTroll);
        dmTroll.setCreateTime(new Date());
        dmTroll.setUpdateTime(new Date());
        if (dmTrollsList.size()>0) {
            dmTroll.setVisitorType("1");
        }else {
            dmTroll.setVisitorType("0");
        }
        if ("true".equals(userMobile)){
            dmTroll.setTrollsDevice("0");
        }else {
            dmTroll.setTrollsDevice("1");
        }
        if ("true".equals(paraPath)){
            dmTroll.setTrollsPath("跳转访问");
        }else {
            dmTroll.setTrollsPath("直接访问");
        }
        dmTrollsService.insert(dmTroll);
    }
}
