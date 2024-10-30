package cn.itcast.hotel.dao;

import cn.itcast.hotel.entity.LinkSrcs;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * (LinkSrcs)表数据库访问层
 *
 * @author makejava
 * @since 2024-03-28 15:11:17
 */
public interface LinkSrcsDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    LinkSrcs queryById(Integer id);

    List<LinkSrcs> quertList(LinkSrcs linkSrcs);
    /**
     * 查询指定行数据
     *
     * @param linkSrcs 查询条件
     * @param pageable         分页对象
     * @return 对象列表
     */
//    List<LinkSrcs> queryAllByLimit(LinkSrcs linkSrcs, @Param("pageable") Pageable pageable);

    /**
     * 统计总行数
     *
     * @param linkSrcs 查询条件
     * @return 总行数
     */
    long count(LinkSrcs linkSrcs);

    /**
     * 新增数据
     *
     * @param linkSrcs 实例对象
     * @return 影响行数
     */
    int insert(LinkSrcs linkSrcs);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<LinkSrcs> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<LinkSrcs> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<LinkSrcs> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<LinkSrcs> entities);

    /**
     * 修改数据
     *
     * @param linkSrcs 实例对象
     * @return 影响行数
     */
    int update(LinkSrcs linkSrcs);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

}

