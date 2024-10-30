package cn.itcast.hotel.dao;

import cn.itcast.hotel.entity.DmCondition;
import org.apache.ibatis.annotations.Param;
//import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * (DmCondition)表数据库访问层
 *
 * @author makejava
 * @since 2024-06-02 20:54:33
 */
public interface DmConditionDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DmCondition queryById(DmCondition dmCondition);

    /**
     * 查询指定行数据
     *
     * @param dmCondition 查询条件
     * @param pageable         分页对象
     * @return 对象列表
     */
//    List<DmCondition> queryAllByLimit(DmCondition dmCondition, @Param("pageable") Pageable pageable);

    /**
     * 统计总行数
     *
     * @param dmCondition 查询条件
     * @return 总行数
     */
    long count(DmCondition dmCondition);

    /**
     * 新增数据
     *
     * @param dmCondition 实例对象
     * @return 影响行数
     */
    int insert(DmCondition dmCondition);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<DmCondition> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<DmCondition> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<DmCondition> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<DmCondition> entities);

    /**
     * 修改数据
     *
     * @param dmCondition 实例对象
     * @return 影响行数
     */
    int update(DmCondition dmCondition);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    List<DmCondition> getAll();
}

