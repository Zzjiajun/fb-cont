package cn.itcast.hotel.dao;

import cn.itcast.hotel.entity.DmPixel;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * (DmPixel)表数据库访问层
 *
 * @author makejava
 * @since 2024-03-26 22:00:57
 */
public interface DmPixelDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DmPixel queryById(Integer id);


    List<String> queryPixelIdsByDomain(DmPixel dmPixel);

    /**
     * 查询指定行数据
     *
     * @param dmPixel 查询条件
     * @param pageable         分页对象
     * @return 对象列表
     */
//    List<DmPixel> queryAllByLimit(DmPixel dmPixel, @Param("pageable") Pageable pageable);

    /**
     * 统计总行数
     *
     * @param dmPixel 查询条件
     * @return 总行数
     */
    long count(DmPixel dmPixel);

    /**
     * 新增数据
     *
     * @param dmPixel 实例对象
     * @return 影响行数
     */
    int insert(DmPixel dmPixel);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<DmPixel> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<DmPixel> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<DmPixel> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<DmPixel> entities);

    /**
     * 修改数据
     *
     * @param dmPixel 实例对象
     * @return 影响行数
     */
    int update(DmPixel dmPixel);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

}

