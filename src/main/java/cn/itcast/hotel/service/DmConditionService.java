package cn.itcast.hotel.service;

import cn.itcast.hotel.entity.DmCondition;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * (DmCondition)表服务接口
 *
 * @author makejava
 * @since 2024-06-02 20:54:33
 */
public interface DmConditionService {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DmCondition queryById(DmCondition dmCondition);

    /**
     * 分页查询
     *
     * @param dmCondition 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
//    Page<DmCondition> queryByPage(DmCondition dmCondition, PageRequest pageRequest);

    /**
     * 新增数据
     *
     * @param dmCondition 实例对象
     * @return 实例对象
     */
    DmCondition insert(DmCondition dmCondition);

    /**
     * 修改数据
     *
     * @param dmCondition 实例对象
     * @return 实例对象
     */
    void update(DmCondition dmCondition);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(Integer id);

    String getPublicIP() throws Exception;

    /**
     *
     */
    Integer getVpn(List<String> conuntryList,String keyString,String ip);


    Integer getIpVpn(List<String> conuntryList, String keyString, String ip);



    Integer getIpApiVpn(List<String> conuntryList, String keyString, String ip);
    List<DmCondition> getAll();

}
