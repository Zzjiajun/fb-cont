package cn.itcast.hotel.dao;

import cn.itcast.hotel.entity.DmTrolls;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * (DmTrolls)表数据库访问层
 *
 * @author makejava
 * @since 2024-09-26 02:48:12
 */
public interface DmTrollsDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DmTrolls queryById(Integer id);

//    /**
//     * 查询指定行数据
//     *
//     * @param dmTrolls 查询条件
//     * @param pageable         分页对象
//     * @return 对象列表
//     */
//    List<DmTrolls> queryAllByLimit(DmTrolls dmTrolls, @Param("pageable") Pageable pageable);

    /**
     * 统计总行数
     *
     * @param dmTrolls 查询条件
     * @return 总行数
     */
    long count(DmTrolls dmTrolls);

    /**
     * 新增数据
     *
     * @param dmTrolls 实例对象
     * @return 影响行数
     */
    int insert(DmTrolls dmTrolls);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<DmTrolls> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<DmTrolls> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<DmTrolls> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<DmTrolls> entities);

    /**
     * 修改数据
     *
     * @param dmTrolls 实例对象
     * @return 影响行数
     */
    int update(DmTrolls dmTrolls);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    List<DmTrolls> queryByIp(DmTrolls dmTrolls);
}

