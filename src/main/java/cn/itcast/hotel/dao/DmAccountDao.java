package cn.itcast.hotel.dao;

import cn.itcast.hotel.entity.DmAccount;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * (DmAccount)表数据库访问层
 *
 * @author makejava
 * @since 2024-06-12 16:41:30
 */
public interface DmAccountDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DmAccount queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param dmAccount 查询条件
     * @param pageable         分页对象
     * @return 对象列表
     */
    List<DmAccount> queryAllByLimit(DmAccount dmAccount, @Param("pageable") Pageable pageable);

    /**
     * 统计总行数
     *
     * @param dmAccount 查询条件
     * @return 总行数
     */
    long count(DmAccount dmAccount);

    /**
     * 新增数据
     *
     * @param dmAccount 实例对象
     * @return 影响行数
     */
    int insert(DmAccount dmAccount);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<DmAccount> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<DmAccount> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<DmAccount> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<DmAccount> entities);

    /**
     * 修改数据
     *
     * @param dmAccount 实例对象
     * @return 影响行数
     */
    int update(DmAccount dmAccount);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

}

