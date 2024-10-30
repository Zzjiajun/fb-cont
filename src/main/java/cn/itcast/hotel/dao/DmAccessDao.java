package cn.itcast.hotel.dao;

import cn.itcast.hotel.entity.DmAccess;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * (DmAccess)表数据库访问层
 *
 * @author makejava
 * @since 2024-06-12 16:41:13
 */
public interface DmAccessDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DmAccess queryById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param dmAccess 查询条件
     * @param pageable         分页对象
     * @return 对象列表
     */
    List<DmAccess> queryAllByLimit(DmAccess dmAccess, @Param("pageable") Pageable pageable);

    /**
     * 统计总行数
     *
     * @param dmAccess 查询条件
     * @return 总行数
     */
    long count(DmAccess dmAccess);

    /**
     * 新增数据
     *
     * @param dmAccess 实例对象
     * @return 影响行数
     */
    int insert(DmAccess dmAccess);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<DmAccess> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<DmAccess> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<DmAccess> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<DmAccess> entities);

    /**
     * 修改数据
     *
     * @param dmAccess 实例对象
     * @return 影响行数
     */
    int update(DmAccess dmAccess);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    List<DmAccess> queryByIp(DmAccess dmAccess);
}

