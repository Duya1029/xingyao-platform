package com.xingyao.platform.module.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingyao.platform.common.R;
import com.xingyao.platform.module.erp.entity.*;
import com.xingyao.platform.module.erp.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/erp")
@RequiredArgsConstructor
public class ErpController {

    private final ErpProductMapper productMapper;
    private final ErpCustomerMapper customerMapper;
    private final ErpSupplierMapper supplierMapper;
    private final ErpOrderMapper orderMapper;

    @GetMapping("/products")
    public R<IPage<ErpProduct>> products(@RequestParam(defaultValue = "1") int pageNum,
                                         @RequestParam(defaultValue = "10") int pageSize,
                                         @RequestParam(required = false) String keyword) {
        return R.ok(productMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ErpProduct>()
                        .and(StringUtils.hasText(keyword), w -> w.like(ErpProduct::getCode, keyword).or().like(ErpProduct::getName, keyword))
                        .orderByDesc(ErpProduct::getId)));
    }

    @PostMapping("/products")
    public R<Void> createProduct(@RequestBody ErpProduct product) {
        productMapper.insert(product);
        return R.ok();
    }

    @PutMapping("/products/{id}")
    public R<Void> updateProduct(@PathVariable Long id, @RequestBody ErpProduct product) {
        product.setId(id);
        productMapper.updateById(product);
        return R.ok();
    }

    @DeleteMapping("/products/{id}")
    public R<Void> deleteProduct(@PathVariable Long id) {
        productMapper.deleteById(id);
        return R.ok();
    }

    @GetMapping("/customers")
    public R<List<ErpCustomer>> customers() {
        return R.ok(customerMapper.selectList(null));
    }

    @GetMapping("/suppliers")
    public R<List<ErpSupplier>> suppliers() {
        return R.ok(supplierMapper.selectList(null));
    }

    @GetMapping("/orders")
    public R<IPage<ErpOrder>> orders(@RequestParam(defaultValue = "1") int pageNum,
                                     @RequestParam(defaultValue = "10") int pageSize,
                                     @RequestParam(required = false) String type) {
        return R.ok(orderMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ErpOrder>()
                        .eq(StringUtils.hasText(type), ErpOrder::getType, type)
                        .orderByDesc(ErpOrder::getId)));
    }

    @PostMapping("/orders")
    public R<Void> createOrder(@RequestBody ErpOrder order) {
        if (!StringUtils.hasText(order.getOrderNo())) {
            order.setOrderNo("EO" + System.currentTimeMillis());
        }
        orderMapper.insert(order);
        return R.ok();
    }

    @PutMapping("/orders/{id}")
    public R<Void> updateOrder(@PathVariable Long id, @RequestBody ErpOrder order) {
        order.setId(id);
        orderMapper.updateById(order);
        return R.ok();
    }

    @DeleteMapping("/orders/{id}")
    public R<Void> deleteOrder(@PathVariable Long id) {
        orderMapper.deleteById(id);
        return R.ok();
    }
}
