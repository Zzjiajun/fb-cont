package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.entity.*;
import cn.itcast.hotel.dao.DmCenterDao;
import cn.itcast.hotel.po.DeviceDetectorPo;
import cn.itcast.hotel.service.DmAccessService;
import cn.itcast.hotel.service.DmCenterService;
import cn.itcast.hotel.service.DmClickService;
import cn.itcast.hotel.service.DmTrollsService;
import cn.itcast.hotel.util.RedisUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Type;
import java.util.ArrayList;
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
    public void updateDmCenter(DmCenter dmCenter) {
        this.dmCenterDao.update(dmCenter);
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
    public void addAccessVpn(DmCenter dmCenter, Map<String, String> params ,String country,DmCondition dmCondition,Map<String,String> handledMap,
                            DeviceDetectorPo deviceDetectorPo) {
        String redirect = handledMap.get("shouldRedirect");
        List<DmAccess> accessList = new ArrayList<>(); // 初始化为空列表
        boolean shouldRedirect=Boolean.parseBoolean(redirect);
        DmAccess dmAccess = new DmAccess();
        dmAccess.setCenterId(dmCenter.getId());
        dmAccess.setIp(params.get("userIp"));
        DmCenter query = this.queryById(dmCenter.getId());
        query.setVisitsNumber(query.getVisitsNumber()+1);
        accessList = dmAccessService.queryByIp(dmAccess);
        dmAccess.setRegion(country);
        dmAccess.setCreateTime(new Date());
        dmAccess.setUpdateTime(new Date());
        dmAccess.setVisitorType(accessList.isEmpty() ? "0" : "1");
        dmAccess.setAccessDevice("Mobile".equals(params.get("userMobile")) ? "0" : "1");
        dmAccess.setPassed(shouldRedirect ? "0" : "1");
        dmAccess.setAccessPath("true".equals(params.get("paraPath")) ? "跳转访问" : "直接访问");
        if("iOS".equals(deviceDetectorPo.getOs().getName())){
            //通过modles判断设备型号
            Integer screenWidth = Integer.valueOf(params.get("screenWidth"));
            Integer screenHeight = Integer.valueOf(params.get("screenHeight"));
            Integer pixelRatio = Integer.valueOf(params.get("pixelRatio"));
            String buildKeyAll = redisUtil.buildKey("Acoolys", "dmModlesAll");
            String modelsMap = redisUtil.get(buildKeyAll);
            Type modelsType = new TypeToken<List<DmModles>>() {}.getType();
            List<DmModles> modelsList = new Gson().fromJson(modelsMap, modelsType);
            // 设置一个标志变量
            String modelName = modelsList.stream()
                    .filter(s -> s.getScreenWidth().equals(screenWidth)
                            && s.getScreenHeight().equals(screenHeight)
                            && s.getPixelRatio().equals(pixelRatio))
                    .map(DmModles::getModelName)
                    .findFirst()
                    .orElse("苹果x以上高端机型"); // 找不到就返回默认值
            dmAccess.setModels(modelName);
        }else if("Android".equals(deviceDetectorPo.getOs().getName())) {
            dmAccess.setModels("安卓机型");
        }else {
            dmAccess.setModels("其他设备");
        }
        if ("Huawei".equals(deviceDetectorPo.getDevice().getBrand())){
            dmAccess.setModels("华为手机");
        }
        if (deviceDetectorPo.getDevice().getBrand() != null && "Samsung".equals(deviceDetectorPo.getDevice().getBrand())) {
            dmAccess.setModels("三星手机");
        }
        //将设备信息和客户端信息保存
        getClientDetail(dmAccess,deviceDetectorPo);
        dmAccess.setSource(params.get("fb"));
        String builtKey = redisUtil.buildKey("AoollyNumberIp", dmCondition.getAccessAddress());
        String string = redisUtil.get(builtKey);
        List<String> listIp;
        if (string != null && !string.isEmpty()) {
            listIp = new Gson().fromJson(string, new TypeToken<List<String>>() {}.getType());
        } else {
            listIp = new ArrayList<>(); // 如果 string 为空，初始化一个空的 List
        }
        listIp.add(params.get("userIp"));
        //更新redis、mysql
        redisUtil.set(builtKey,new Gson().toJson(listIp));
        dmAccessService.insert(dmAccess);
        //如果shouldRedirect == true，表示所有条件通过，可以重定向到目的页面，否则不通过表添加记录
        if(!shouldRedirect){
            DmTrolls dmTrolls = new DmTrolls();
            dmTrolls.setCenterId(dmAccess.getCenterId());
            dmTrolls.setIp(dmAccess.getIp());
            dmTrolls.setTrollsPath(dmAccess.getAccessPath());
            dmTrolls.setVisitorType(dmAccess.getVisitorType());
            dmTrolls.setTrollsDevice(dmAccess.getAccessDevice());
            dmTrolls.setModels(dmAccess.getModels());
            dmTrolls.setSource(dmAccess.getSource());

            dmTrolls.setRegion(dmAccess.getRegion());
            dmTrolls.setDetails(handledMap.get("logMessage"));
            dmTrolls.setCreateTime(new Date());
            dmTrolls.setUpdateTime(new Date());
            query.setTrolls(query.getTrolls() + 1);
            dmTrollsService.insert(dmTrolls);
        }
        this.updateDmCenter(query);
    }

    @Override
    @Async("taskExecutor") // 指定使用的线程池
    public void addAccessPageVpn(DmCenter dmCenter, Map<String, String> params, String country, DmCondition dmCondition, Map<String, String> handledMap,
                                 DeviceDetectorPo deviceDetectorPo) {
        String redirect = handledMap.get("shouldRedirect");
        List<DmAccess> accessList = new ArrayList<>(); // 初始化为空列表
        boolean shouldRedirect=Boolean.parseBoolean(redirect);
        DmAccess dmAccess = new DmAccess();
        dmAccess.setCenterId(dmCenter.getId());
        dmAccess.setIp(params.get("userIp"));
        DmCenter query = this.queryById(dmCenter.getId());
        query.setVisitsNumber(query.getVisitsNumber()+1);
        this.updateDmCenter(query);
        accessList = dmAccessService.queryByIp(dmAccess);
        dmAccess.setRegion(country);
        dmAccess.setCreateTime(new Date());
        dmAccess.setUpdateTime(new Date());
        dmAccess.setVisitorType(accessList.isEmpty() ? "0" : "1");
        dmAccess.setAccessDevice("Mobile".equals(params.get("userMobile")) ? "0" : "1");
        dmAccess.setPassed(shouldRedirect ? "0" : "1");
        dmAccess.setAccessPath("true".equals(params.get("paraPath")) ? "跳转访问" : "直接访问");
        if("iOS".equals(params.get("isIOSS"))){
            //通过modles判断设备型号
            Integer screenWidth = Integer.valueOf(params.get("screenWidth"));
            Integer screenHeight = Integer.valueOf(params.get("screenHeight"));
            Integer pixelRatio = Integer.valueOf(params.get("pixelRatio"));
            String buildKeyAll = redisUtil.buildKey("Acoolys", "dmModlesAll");
            String modelsMap = redisUtil.get(buildKeyAll);
            Type modelsType = new TypeToken<List<DmModles>>() {}.getType();
            List<DmModles> modelsList = new Gson().fromJson(modelsMap, modelsType);
            // 设置一个标志变量
            String modelName = modelsList.stream()
                    .filter(s -> s.getScreenWidth().equals(screenWidth)
                            && s.getScreenHeight().equals(screenHeight)
                            && s.getPixelRatio().equals(pixelRatio))
                    .map(DmModles::getModelName)
                    .findFirst()
                    .orElse("苹果x以上高端机型"); // 找不到就返回默认值
            dmAccess.setModels(modelName);
        }else if("Android".equals(deviceDetectorPo.getOs().getName())) {
            dmAccess.setModels("安卓机型");
        }else {
            dmAccess.setModels("其他设备");
        }
        if ("Huawei".equals(deviceDetectorPo.getDevice().getBrand())){
            dmAccess.setModels("华为手机");
        }
        if (deviceDetectorPo.getDevice().getBrand() != null && "Samsung".equals(deviceDetectorPo.getDevice().getBrand())) {
            dmAccess.setModels("三星手机");
        }
        getClientDetail(dmAccess,deviceDetectorPo);
        dmAccess.setSource(params.get("fb"));
        //将accessList的DmAccess字段的ip抽取出来形成新的list<String>
        String builtKey = redisUtil.buildKey("AoollyNumberIp", dmCondition.getAccessAddress());
        String string = redisUtil.get(builtKey);
        List<String> listIp;
        if (string != null && !string.isEmpty()) {
            listIp = new Gson().fromJson(string, new TypeToken<List<String>>() {}.getType());
        } else {
            listIp = new ArrayList<>(); // 如果 string 为空，初始化一个空的 List
        }
        listIp.add(params.get("userIp"));
        //更新redis、mysql
        redisUtil.set(builtKey,new Gson().toJson(listIp));
        //更新redis、mysql
        redisUtil.set(builtKey,new Gson().toJson(listIp));
        dmAccessService.insert(dmAccess);
    }

    @Override
    @Async("taskExecutor") // 指定使用的线程池
    public void addClickVpnCount(DmCenter dmCenter, Map<String, String> params,String country) {
        DmClick dmClick = new DmClick();
        dmClick.setCenterId(dmCenter.getId());
        dmClick.setIp(params.get("userIp"));
        DmCenter query = this.queryById(dmCenter.getId());
        query.setClicksNumber(query.getClicksNumber() + 1);
        List<DmClick> dmClicks = dmClickService.queryByIp(dmClick);
        dmClick.setRegion(country);
        dmClick.setCreateTime(new Date());
        dmClick.setUpdateTime(new Date());
        boolean shouldRedirect = Boolean.parseBoolean(params.get("shouldRedirect"));
        dmClick.setClickType(dmClicks.isEmpty() ? "0" : "1");
        dmClick.setClickDevice("Mobile".equals(params.get("userMobile")) ? "0" : "1");
        dmClick.setSource(params.get("fb"));
        if("iOS".equals(params.get("isIOSS"))){
            //通过modles判断设备型号
            Integer screenWidth = Integer.valueOf(params.get("screenWidth"));
            Integer screenHeight = Integer.valueOf(params.get("screenHeight"));
            Integer pixelRatio = Integer.valueOf(params.get("pixelRatio"));
            String buildKeyAll = redisUtil.buildKey("Acoolys", "dmModlesAll");
            String modelsMap = redisUtil.get(buildKeyAll);
            Type modelsType = new TypeToken<List<DmModles>>() {}.getType();
            List<DmModles> modelsList = new Gson().fromJson(modelsMap, modelsType);
            // 设置一个标志变量
            String modelName = modelsList.stream()
                    .filter(s -> s.getScreenWidth().equals(screenWidth)
                            && s.getScreenHeight().equals(screenHeight)
                            && s.getPixelRatio().equals(pixelRatio))
                    .map(DmModles::getModelName)
                    .findFirst()
                    .orElse("苹果x以上高端机型"); // 找不到就返回默认值
            dmClick.setModels(modelName);
        }else if("Android".equals(params.get("isIOSS"))) {
            dmClick.setModels("安卓机型");
        }else {
            dmClick.setModels("其他设备");
        }
        if ("Huawei".equals(params.get("isHuawei"))){
            dmClick.setModels("华为手机");
        }
        if (params.get("isSamsung") != null && !"Not Samsung".equals(params.get("isSamsung"))) {
            dmClick.setModels("三星手机");
        }
        dmClickService.insert(dmClick);
        if(!shouldRedirect){
            DmTrolls dmTrolls = new DmTrolls();
            dmTrolls.setCenterId(dmClick.getCenterId());
            dmTrolls.setIp(dmClick.getIp());
            dmTrolls.setTrollsPath("true".equals(params.get("paraPath")) ? "跳转访问" : "直接访问");
            dmTrolls.setVisitorType(dmClick.getClickType());
            dmTrolls.setTrollsDevice(dmClick.getClickDevice());
            dmTrolls.setModels(dmClick.getModels());
            dmTrolls.setSource(dmClick.getSource());
            dmTrolls.setRegion(country);
            dmTrolls.setDetails(params.get("detail"));
            dmTrolls.setCreateTime(new Date());
            dmTrolls.setUpdateTime(new Date());
            query.setTrolls(query.getTrolls() + 1);
            dmTrollsService.insert(dmTrolls);
        }
        this.updateDmCenter(query);

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

    private <T> List<T> getCachedList(String key, Class<T> clazz) {
        String jsonMap = redisUtil.get(key);
        Type listType = new TypeToken<List<T>>() {}.getType();
        return new Gson().fromJson(jsonMap, listType);
    }

    private void getClientDetail(DmAccess dmAccess,DeviceDetectorPo deviceDetectorPo){
        StringBuilder clientBuilder = new StringBuilder();
        StringBuilder detailsBuilder = new StringBuilder();
        DeviceDetectorPo.Device device = deviceDetectorPo.getDevice();
        DeviceDetectorPo.Client client = deviceDetectorPo.getClient();
        if (client!= null){
            String deviceType = client.getDeviceType();
            if ("browser".equals(deviceType)) {
                clientBuilder.append("类型：").append("浏览器").append("\n");
            } else {
                clientBuilder.append("类型：").append(deviceType != null ? deviceType : "").append("\n");
            }
            clientBuilder.append("名称：").append(client.getName() != null ? client.getName() : "").append("\n");
            clientBuilder.append("版本：").append(client.getVersion() != null ? client.getVersion() : "").append("\n");
        }else {
            clientBuilder.append("类型：").append("未知").append("\n");
            clientBuilder.append("名称：").append("未知").append("\n");
            clientBuilder.append("版本：").append("未知").append("\n");
        }
        dmAccess.setClientDetails(clientBuilder.toString());
        if(device!= null){
            String deviceType = device.getDeviceType();
            if ("desktop".equals(deviceType)){
                detailsBuilder.append("类型：").append("桌面设备").append("\n");
            }else if ("smartphone".equals(deviceType)){
                detailsBuilder.append("类型：").append("智能手机").append("\n");
            }else if ("tablet".equals(deviceType)){
                detailsBuilder.append("类型：").append("平板电脑").append("\n");
            }else {
                detailsBuilder.append("类型：").append(deviceType != null ? deviceType : "").append("\n");
            }
            detailsBuilder.append("类型：").append(device.getDeviceType() != null ? device.getDeviceType() : "").append("\n");
            detailsBuilder.append("制造商：").append(device.getBrand() != null ? device.getBrand() : "").append("\n");
            detailsBuilder.append("型号：").append(device.getModel() != null ? device.getModel() : "").append("\n");
            detailsBuilder.append("操作系统：").append(deviceDetectorPo.getOs().getName()).append(" ").append(deviceDetectorPo.getOs().getVersion()).append("\n");
        }else {
            detailsBuilder.append("类型：").append("未知").append("\n");
            detailsBuilder.append("制造商：").append("未知").append("\n");
            detailsBuilder.append("型号：").append("未知").append("\n");
            detailsBuilder.append("操作系统：").append("未知").append("\n");
        }
        dmAccess.setDeviceDetails(detailsBuilder.toString());

    }
}
