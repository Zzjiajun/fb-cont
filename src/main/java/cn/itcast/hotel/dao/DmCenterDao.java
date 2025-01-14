package cn.itcast.hotel.dao;

import cn.itcast.hotel.entity.DmCenter;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * (DmCenter)表数据库访问层
 *
 * @author makejava
 * @since 2024-03-28 15:37:36
 */
public interface DmCenterDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DmCenter queryById(Integer id);


    DmCenter query(DmCenter dmCenter);

    List<DmCenter> getAll();

    /**
     * 查询指定行数据
     *
     * @param dmCenter 查询条件
     * @param pageable         分页对象
     * @return 对象列表
     */

    /**
     * 统计总行数
     *
     * @param dmCenter 查询条件
     * @return 总行数
     */
    long count(DmCenter dmCenter);

    /**
     * 新增数据
     *
     * @param dmCenter 实例对象
     * @return 影响行数
     */
    int insert(DmCenter dmCenter);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<DmCenter> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<DmCenter> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<DmCenter> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<DmCenter> entities);

    /**
     * 修改数据
     *
     * @param dmCenter 实例对象
     * @return 影响行数
     */
    int update(DmCenter dmCenter);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

}

