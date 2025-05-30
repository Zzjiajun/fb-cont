package cn.itcast.hotel.dao;

import cn.itcast.hotel.entity.Tonck;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * (Tonck)表数据库访问层
 *
 * @author makejava
 * @since 2024-06-12 01:56:13
 */
public interface TonckDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    Tonck queryById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param tonck 查询条件
     * @param pageable         分页对象
     * @return 对象列表
     */
    List<Tonck> queryAllByLimit(Tonck tonck, @Param("pageable") Pageable pageable);

    /**
     * 统计总行数
     *
     * @param tonck 查询条件
     * @return 总行数
     */
    long count(Tonck tonck);

    /**
     * 新增数据
     *
     * @param tonck 实例对象
     * @return 影响行数
     */
    int insert(Tonck tonck);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<Tonck> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<Tonck> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<Tonck> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<Tonck> entities);

    /**
     * 修改数据
     *
     * @param tonck 实例对象
     * @return 影响行数
     */
    int update(Tonck tonck);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

}

