package com.xingyao.platform.module.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingyao.platform.common.BizException;
import com.xingyao.platform.common.R;
import com.xingyao.platform.module.mes.entity.MesWorkOrder;
import com.xingyao.platform.module.mes.mapper.MesWorkOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mes/work-orders")
@RequiredArgsConstructor
public class MesWorkOrderController {

    private final MesWorkOrderMapper workOrderMapper;

    @GetMapping
    public R<IPage<MesWorkOrder>> page(@RequestParam(defaultValue = "1") int pageNum,
                                       @RequestParam(defaultValue = "10") int pageSize,
                                       @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<MesWorkOrder> qw = new LambdaQueryWrapper<MesWorkOrder>()
                .like(StringUtils.hasText(keyword), MesWorkOrder::getOrderNo, keyword)
                .orderByDesc(MesWorkOrder::getId);
        return R.ok(workOrderMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    @GetMapping("/all")
    public R<List<MesWorkOrder>> all() {
        return R.ok(workOrderMapper.selectList(new LambdaQueryWrapper<MesWorkOrder>()
                .orderByDesc(MesWorkOrder::getId)));
    }

    @PostMapping
    public R<Void> create(@RequestBody MesWorkOrder order) {
        if (!StringUtils.hasText(order.getOrderNo())) {
            throw new BizException("工单号不能为空");
        }
        workOrderMapper.insert(order);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody MesWorkOrder order) {
        order.setId(id);
        workOrderMapper.updateById(order);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        workOrderMapper.deleteById(id);
        return R.ok();
    }
}
