package cn.itcast.hotel.dao;

import cn.itcast.hotel.entity.DmClick;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * (DmClick)表数据库访问层
 *
 * @author makejava
 * @since 2024-06-12 16:42:28
 */
public interface DmClickDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DmClick queryById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param dmClick 查询条件
     * @param pageable         分页对象
     * @return 对象列表
     */
    List<DmClick> queryAllByLimit(DmClick dmClick, @Param("pageable") Pageable pageable);

    /**
     * 统计总行数
     *
     * @param dmClick 查询条件
     * @return 总行数
     */
    long count(DmClick dmClick);

    /**
     * 新增数据
     *
     * @param dmClick 实例对象
     * @return 影响行数
     */
    int insert(DmClick dmClick);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<DmClick> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<DmClick> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<DmClick> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<DmClick> entities);

    /**
     * 修改数据
     *
     * @param dmClick 实例对象
     * @return 影响行数
     */
    int update(DmClick dmClick);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    List<DmClick> queryByIp(DmClick dmClick);

}

