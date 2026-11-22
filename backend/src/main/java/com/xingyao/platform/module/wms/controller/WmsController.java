package com.xingyao.platform.module.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingyao.platform.common.BizException;
import com.xingyao.platform.common.R;
import com.xingyao.platform.module.wms.entity.*;
import com.xingyao.platform.module.wms.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wms")
@RequiredArgsConstructor
public class WmsController {

    private final WmsWarehouseMapper warehouseMapper;
    private final WmsItemMapper itemMapper;
    private final WmsItemCategoryMapper categoryMapper;
    private final WmsBrandMapper brandMapper;
    private final WmsPartnerMapper partnerMapper;
    private final WmsStockMapper stockMapper;
    private final WmsInoutMapper inoutMapper;
    private final WmsInoutLineMapper inoutLineMapper;

    /* 仓库 */
    @GetMapping("/warehouses")
    public R<List<WmsWarehouse>> warehouses() {
        return R.ok(warehouseMapper.selectList(null));
    }

    @PostMapping("/warehouses")
    public R<Void> createWarehouse(@RequestBody WmsWarehouse warehouse) {
        warehouseMapper.insert(warehouse);
        return R.ok();
    }

    @PutMapping("/warehouses/{id}")
    public R<Void> updateWarehouse(@PathVariable Long id, @RequestBody WmsWarehouse warehouse) {
        warehouse.setId(id);
        warehouseMapper.updateById(warehouse);
        return R.ok();
    }

    @DeleteMapping("/warehouses/{id}")
    public R<Void> deleteWarehouse(@PathVariable Long id) {
        warehouseMapper.deleteById(id);
        return R.ok();
    }

    /* 物料分类 / 品牌 / 往来企业 */
    @GetMapping("/item-categories")
    public R<List<WmsItemCategory>> categories() {
        return R.ok(categoryMapper.selectList(null));
    }

    @PostMapping("/item-categories")
    public R<Void> createCategory(@RequestBody WmsItemCategory category) {
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        categoryMapper.insert(category);
        return R.ok();
    }

    @GetMapping("/brands")
    public R<List<WmsBrand>> brands() {
        return R.ok(brandMapper.selectList(null));
    }

    @PostMapping("/brands")
    public R<Void> createBrand(@RequestBody WmsBrand brand) {
        brandMapper.insert(brand);
        return R.ok();
    }

    @GetMapping("/partners")
    public R<List<WmsPartner>> partners(@RequestParam(required = false) String type) {
        return R.ok(partnerMapper.selectList(new LambdaQueryWrapper<WmsPartner>()
                .eq(StringUtils.hasText(type), WmsPartner::getType, type)));
    }

    @PostMapping("/partners")
    public R<Void> createPartner(@RequestBody WmsPartner partner) {
        partnerMapper.insert(partner);
        return R.ok();
    }

    /* 物料 */
    @GetMapping("/items")
    public R<IPage<WmsItem>> items(@RequestParam(defaultValue = "1") int pageNum,
                                  @RequestParam(defaultValue = "10") int pageSize,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) Long categoryId) {
        LambdaQueryWrapper<WmsItem> qw = new LambdaQueryWrapper<WmsItem>()
                .and(StringUtils.hasText(keyword), w -> w.like(WmsItem::getCode, keyword).or().like(WmsItem::getName, keyword))
                .eq(categoryId != null, WmsItem::getCategoryId, categoryId)
                .orderByDesc(WmsItem::getId);
        IPage<WmsItem> page = itemMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        Map<Long, WmsItemCategory> categoryMap = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(WmsItemCategory::getId, Function.identity()));
        Map<Long, WmsBrand> brandMap = brandMapper.selectList(null).stream()
                .collect(Collectors.toMap(WmsBrand::getId, Function.identity()));
        for (WmsItem item : page.getRecords()) {
            WmsItemCategory c = categoryMap.get(item.getCategoryId());
            item.setCategoryName(c == null ? null : c.getName());
            WmsBrand b = brandMap.get(item.getBrandId());
            item.setBrandName(b == null ? null : b.getName());
            List<WmsStock> stocks = stockMapper.selectList(new LambdaQueryWrapper<WmsStock>()
                    .eq(WmsStock::getItemId, item.getId()));
            item.setStockQty(stocks.stream().map(WmsStock::getQty).reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        return R.ok(page);
    }

    @PostMapping("/items")
    public R<Void> createItem(@RequestBody WmsItem item) {
        if (!StringUtils.hasText(item.getCode())) {
            throw new BizException("物料编码不能为空");
        }
        Long dup = itemMapper.selectCount(new LambdaQueryWrapper<WmsItem>().eq(WmsItem::getCode, item.getCode()));
        if (dup > 0) {
            throw new BizException("物料编码已存在：" + item.getCode());
        }
        if (item.getStatus() == null) {
            item.setStatus(1);
        }
        item.setCreateTime(LocalDateTime.now());
        itemMapper.insert(item);
        return R.ok();
    }

    @PutMapping("/items/{id}")
    public R<Void> updateItem(@PathVariable Long id, @RequestBody WmsItem item) {
        item.setId(id);
        itemMapper.updateById(item);
        return R.ok();
    }

    @DeleteMapping("/items/{id}")
    public R<Void> deleteItem(@PathVariable Long id) {
        itemMapper.deleteById(id);
        return R.ok();
    }

    /* 库存 */
    @GetMapping("/stocks")
    public R<List<WmsStock>> stocks(@RequestParam(required = false) Long warehouseId) {
        List<WmsStock> list = stockMapper.selectList(new LambdaQueryWrapper<WmsStock>()
                .eq(warehouseId != null, WmsStock::getWarehouseId, warehouseId));
        Map<Long, WmsWarehouse> warehouseMap = warehouseMapper.selectList(null).stream()
                .collect(Collectors.toMap(WmsWarehouse::getId, Function.identity()));
        Map<Long, WmsItem> itemMap = itemMapper.selectList(null).stream()
                .collect(Collectors.toMap(WmsItem::getId, Function.identity()));
        for (WmsStock stock : list) {
            WmsWarehouse w = warehouseMap.get(stock.getWarehouseId());
            stock.setWarehouseName(w == null ? null : w.getName());
            WmsItem i = itemMap.get(stock.getItemId());
            if (i != null) {
                stock.setItemName(i.getName());
                stock.setItemCode(i.getCode());
                stock.setUnit(i.getUnit());
            }
        }
        return R.ok(list);
    }

    /* 出入库单 */
    @GetMapping("/inouts")
    public R<IPage<WmsInout>> inouts(@RequestParam(defaultValue = "1") int pageNum,
                                     @RequestParam(defaultValue = "10") int pageSize,
                                     @RequestParam(required = false) String type) {
        LambdaQueryWrapper<WmsInout> qw = new LambdaQueryWrapper<WmsInout>()
                .eq(StringUtils.hasText(type), WmsInout::getType, type)
                .orderByDesc(WmsInout::getId);
        IPage<WmsInout> page = inoutMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        Map<Long, WmsWarehouse> warehouseMap = warehouseMapper.selectList(null).stream()
                .collect(Collectors.toMap(WmsWarehouse::getId, Function.identity()));
        page.getRecords().forEach(o -> {
            WmsWarehouse w = warehouseMap.get(o.getWarehouseId());
            o.setWarehouseName(w == null ? null : w.getName());
        });
        return R.ok(page);
    }

    @PostMapping("/inouts")
    public R<Void> createInout(@RequestBody Map<String, Object> body) {
        WmsInout order = new WmsInout();
        order.setOrderNo("IO" + System.currentTimeMillis());
        order.setType(String.valueOf(body.get("type")));
        order.setWarehouseId(Long.valueOf(String.valueOf(body.get("warehouseId"))));
        order.setRemark(body.get("remark") == null ? null : String.valueOf(body.get("remark")));
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        inoutMapper.insert(order);

        List<Map<String, Object>> lines = (List<Map<String, Object>>) body.get("lines");
        if (lines != null) {
            for (Map<String, Object> line : lines) {
                WmsInoutLine l = new WmsInoutLine();
                l.setOrderId(order.getId());
                l.setItemId(Long.valueOf(String.valueOf(line.get("itemId"))));
                l.setQty(new BigDecimal(String.valueOf(line.get("qty"))));
                inoutLineMapper.insert(l);
            }
        }
        return R.ok();
    }

    /** 过账：按单据类型增减库存 */
    @PostMapping("/inouts/{id}/post")
    @Transactional
    public R<Void> post(@PathVariable Long id) {
        WmsInout order = inoutMapper.selectById(id);
        if (order == null) {
            throw new BizException("单据不存在");
        }
        if (order.getStatus() != null && order.getStatus() == 1) {
            throw new BizException("单据已过账，不能重复过账");
        }
        List<WmsInoutLine> lines = inoutLineMapper.selectList(new LambdaQueryWrapper<WmsInoutLine>()
                .eq(WmsInoutLine::getOrderId, id));
        for (WmsInoutLine line : lines) {
            WmsStock stock = stockMapper.selectOne(new LambdaQueryWrapper<WmsStock>()
                    .eq(WmsStock::getWarehouseId, order.getWarehouseId())
                    .eq(WmsStock::getItemId, line.getItemId()));
            BigDecimal delta = "OUT".equals(order.getType()) ? line.getQty().negate() : line.getQty();
            if (stock == null) {
                if (delta.compareTo(BigDecimal.ZERO) < 0) {
                    throw new BizException("物料 " + line.getItemId() + " 在该仓库无库存，无法出库");
                }
                stock = new WmsStock();
                stock.setWarehouseId(order.getWarehouseId());
                stock.setItemId(line.getItemId());
                stock.setQty(delta);
                stockMapper.insert(stock);
            } else {
                BigDecimal next = stock.getQty().add(delta);
                if (next.compareTo(BigDecimal.ZERO) < 0) {
                    throw new BizException("库存不足，无法出库");
                }
                stock.setQty(next);
                stockMapper.updateById(stock);
            }
        }
        order.setStatus(1);
        inoutMapper.updateById(order);
        return R.ok();
    }
}
