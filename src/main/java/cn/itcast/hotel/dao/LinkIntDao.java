package cn.itcast.hotel.dao;

import cn.itcast.hotel.entity.LinkInt;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * (LinkInt)表数据库访问层
 *
 * @author makejava
 * @since 2024-03-28 15:10:29
 */
public interface LinkIntDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    LinkInt queryById(Integer id);


    LinkInt query(LinkInt linkInt);

    /**
     * 查询指定行数据
     *
     * @param linkInt 查询条件
     * @param pageable         分页对象
     * @return 对象列表
     */
//    List<LinkInt> queryAllByLimit(LinkInt linkInt, @Param("pageable") Pageable pageable);

    /**
     * 统计总行数
     *
     * @param linkInt 查询条件
     * @return 总行数
     */
    long count(LinkInt linkInt);

    /**
     * 新增数据
     *
     * @param linkInt 实例对象
     * @return 影响行数
     */
    int insert(LinkInt linkInt);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<LinkInt> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<LinkInt> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<LinkInt> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<LinkInt> entities);

    /**
     * 修改数据
     *
     * @param linkInt 实例对象
     * @return 影响行数
     */
    int update(LinkInt linkInt);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

}

