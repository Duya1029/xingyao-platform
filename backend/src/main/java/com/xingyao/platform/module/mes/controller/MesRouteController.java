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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mes/routes")
@RequiredArgsConstructor
public class MesRouteController {

    private final MesRouteMapper routeMapper;
    private final MesRouteCategoryMapper categoryMapper;

    @GetMapping
    public R<List<MesRoute>> list(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) Long categoryId) {
        List<MesRoute> list = routeMapper.selectList(new LambdaQueryWrapper<MesRoute>()
                .like(StringUtils.hasText(keyword), MesRoute::getName, keyword)
                .eq(categoryId != null, MesRoute::getCategoryId, categoryId)
                .orderByDesc(MesRoute::getId));
        Map<Long, MesRouteCategory> categoryMap = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(MesRouteCategory::getId, Function.identity()));
        list.forEach(r -> {
            MesRouteCategory c = categoryMap.get(r.getCategoryId());
            r.setCategoryName(c == null ? null : c.getName());
        });
        return R.ok(list);
    }

    @PostMapping
    public R<Void> create(@RequestBody MesRoute route) {
        validate(route, null);
        routeMapper.insert(route);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody MesRoute route) {
        validate(route, id);
        route.setId(id);
        routeMapper.updateById(route);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        routeMapper.deleteById(id);
        return R.ok();
    }

    private void validate(MesRoute route, Long excludeId) {
        if (!StringUtils.hasText(route.getCode()) || !StringUtils.hasText(route.getName())) {
            throw new BizException("路线编码与名称不能为空");
        }
        if (route.getCategoryId() == null) {
            throw new BizException("请选择工艺分类");
        }
        Long dup = routeMapper.selectCount(new LambdaQueryWrapper<MesRoute>()
                .eq(MesRoute::getCode, route.getCode())
                .ne(excludeId != null, MesRoute::getId, excludeId));
        if (dup > 0) {
            throw new BizException("路线编码已存在：" + route.getCode());
        }
    }
}
