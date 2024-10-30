package cn.itcast.hotel.controller;

import cn.itcast.hotel.entity.DmTrolls;
import cn.itcast.hotel.service.DmTrollsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (DmTrolls)表控制层
 *
 * @author makejava
 * @since 2024-09-26 02:48:09
 */
@RestController
@RequestMapping("dmTrolls")
public class DmTrollsController {
    /**
     * 服务对象
     */
    @Resource
    private DmTrollsService dmTrollsService;

    /**
     * 分页查询
     *
     * @param dmTrolls 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
//    @GetMapping
//    public ResponseEntity<Page<DmTrolls>> queryByPage(DmTrolls dmTrolls, PageRequest pageRequest) {
//        return ResponseEntity.ok(this.dmTrollsService.queryByPage(dmTrolls, pageRequest));
//    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public ResponseEntity<DmTrolls> queryById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(this.dmTrollsService.queryById(id));
    }

    /**
     * 新增数据
     *
     * @param dmTrolls 实体
     * @return 新增结果
     */
    @PostMapping
    public ResponseEntity<DmTrolls> add(DmTrolls dmTrolls) {
        return ResponseEntity.ok(this.dmTrollsService.insert(dmTrolls));
    }

    /**
     * 编辑数据
     *
     * @param dmTrolls 实体
     * @return 编辑结果
     */
    @PutMapping
    public ResponseEntity<DmTrolls> edit(DmTrolls dmTrolls) {
        return ResponseEntity.ok(this.dmTrollsService.update(dmTrolls));
    }

    /**
     * 删除数据
     *
     * @param id 主键
     * @return 删除是否成功
     */
    @DeleteMapping
    public ResponseEntity<Boolean> deleteById(Integer id) {
        return ResponseEntity.ok(this.dmTrollsService.deleteById(id));
    }

}

