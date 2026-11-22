package com.xingyao.platform.module.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingyao.platform.common.BizException;
import com.xingyao.platform.common.R;
import com.xingyao.platform.module.mes.entity.MesRoute;
import com.xingyao.platform.module.mes.entity.MesRouteCategory;
import com.xingyao.platform.module.mes.mapper.MesRouteCategoryMapper;
import com.xingyao.platform.module.mes.mapper.MesRouteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** JB-2：工艺流程支持分类（参考物料分类实现） */
@RestController
@RequestMapping("/api/mes/route-categories")
@RequiredArgsConstructor
public class MesRouteCategoryController {

    private final MesRouteCategoryMapper categoryMapper;
    private final MesRouteMapper routeMapper;

    @GetMapping
    public R<List<MesRouteCategory>> list(@RequestParam(required = false) String keyword) {
        List<MesRouteCategory> list = categoryMapper.selectList(new LambdaQueryWrapper<MesRouteCategory>()
                .like(StringUtils.hasText(keyword), MesRouteCategory::getName, keyword)
                .orderByAsc(MesRouteCategory::getSort).orderByAsc(MesRouteCategory::getId));
        for (MesRouteCategory c : list) {
            c.setRouteCount(routeMapper.selectCount(new LambdaQueryWrapper<MesRoute>()
                    .eq(MesRoute::getCategoryId, c.getId())));
        }
        return R.ok(list);
    }

    @PostMapping
    public R<Void> create(@RequestBody MesRouteCategory category) {
        validate(category, null);
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        categoryMapper.insert(category);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody MesRouteCategory category) {
        validate(category, id);
        category.setId(id);
        categoryMapper.updateById(category);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        Long used = routeMapper.selectCount(new LambdaQueryWrapper<MesRoute>().eq(MesRoute::getCategoryId, id));
        if (used > 0) {
            throw new BizException("该分类下还有 " + used + " 条工艺路线，不能删除");
        }
        Long children = categoryMapper.selectCount(new LambdaQueryWrapper<MesRouteCategory>()
                .eq(MesRouteCategory::getParentId, id));
        if (children > 0) {
            throw new BizException("存在下级分类，不能删除");
        }
        categoryMapper.deleteById(id);
        return R.ok();
    }

    private void validate(MesRouteCategory category, Long excludeId) {
        if (!StringUtils.hasText(category.getCode()) || !StringUtils.hasText(category.getName())) {
            throw new BizException("分类编码与名称不能为空");
        }
        LambdaQueryWrapper<MesRouteCategory> qw = new LambdaQueryWrapper<MesRouteCategory>()
                .eq(MesRouteCategory::getCode, category.getCode())
                .ne(excludeId != null, MesRouteCategory::getId, excludeId);
        if (categoryMapper.selectCount(qw) > 0) {
            throw new BizException("分类编码已存在：" + category.getCode());
        }
    }
}
