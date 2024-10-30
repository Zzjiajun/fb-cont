package cn.itcast.hotel.dao;

import cn.itcast.hotel.entity.DmDomain;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * (DmDomain)表数据库访问层
 *
 * @author makejava
 * @since 2024-03-22 04:15:07
 */
public interface DmDomainDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DmDomain queryById(Integer id);





    DmDomain queryGet(DmDomain domain);



    String queryString(String domainName);
    /**
     * 查询指定行数据
     *
     * @param dmDomain 查询条件
     * @param pageable         分页对象
     * @return 对象列表
     */
//    List<DmDomain> queryAllByLimit(DmDomain dmDomain, @Param("pageable") Pageable pageable);

    /**
     * 统计总行数
     *
     * @param dmDomain 查询条件
     * @return 总行数
     */
    long count(DmDomain dmDomain);

    /**
     * 新增数据
     *
     * @param dmDomain 实例对象
     * @return 影响行数
     */
    int insert(DmDomain dmDomain);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<DmDomain> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<DmDomain> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<DmDomain> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<DmDomain> entities);

    /**
     * 修改数据
     *
     * @param dmDomain 实例对象
     * @return 影响行数
     */
    int update(DmDomain dmDomain);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

}

