package cn.itcast.hotel.dao;

import cn.itcast.hotel.entity.DmCountry;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * (DmCountry)表数据库访问层
 *
 * @author makejava
 * @since 2024-06-06 07:10:43
 */
public interface DmCountryDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DmCountry queryById(Integer id);

    List<DmCountry> getAll();

    /**
     * 查询指定行数据
     *
     * @param dmCountry 查询条件
     * @param pageable         分页对象
     * @return 对象列表
     */
    List<DmCountry> queryAllByLimit(DmCountry dmCountry, @Param("pageable") Pageable pageable);

    /**
     * 统计总行数
     *
     * @param dmCountry 查询条件
     * @return 总行数
     */
    long count(DmCountry dmCountry);

    /**
     * 新增数据
     *
     * @param dmCountry 实例对象
     * @return 影响行数
     */
    int insert(DmCountry dmCountry);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<DmCountry> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<DmCountry> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<DmCountry> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<DmCountry> entities);

    /**
     * 修改数据
     *
     * @param dmCountry 实例对象
     * @return 影响行数
     */
    int update(DmCountry dmCountry);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

}

