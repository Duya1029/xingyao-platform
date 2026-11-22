package com.xingyao.platform.module.lookup;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingyao.platform.common.R;
import com.xingyao.platform.module.erp.entity.ErpProduct;
import com.xingyao.platform.module.erp.mapper.ErpProductMapper;
import com.xingyao.platform.module.mes.entity.*;
import com.xingyao.platform.module.mes.mapper.*;
import com.xingyao.platform.module.system.entity.SysUser;
import com.xingyao.platform.module.system.mapper.SysUserMapper;
import com.xingyao.platform.module.wms.entity.WmsBrand;
import com.xingyao.platform.module.wms.entity.WmsItemCategory;
import com.xingyao.platform.module.wms.entity.WmsPartner;
import com.xingyao.platform.module.wms.entity.WmsWarehouse;
import com.xingyao.platform.module.wms.mapper.WmsBrandMapper;
import com.xingyao.platform.module.wms.mapper.WmsItemCategoryMapper;
import com.xingyao.platform.module.wms.mapper.WmsPartnerMapper;
import com.xingyao.platform.module.wms.mapper.WmsWarehouseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 表单下拉选项统一出口，避免前端为每个下拉单独建接口 */
@RestController
@RequestMapping("/api/lookup")
@RequiredArgsConstructor
public class LookupController {

    private final SysUserMapper userMapper;
    private final MesWorkstationMapper workstationMapper;
    private final MesWorkshopMapper workshopMapper;
    private final MesRouteMapper routeMapper;
    private final MesRouteCategoryMapper routeCategoryMapper;
    private final MesWorkOrderMapper workOrderMapper;
    private final WmsWarehouseMapper warehouseMapper;
    private final WmsItemCategoryMapper itemCategoryMapper;
    private final WmsBrandMapper brandMapper;
    private final WmsPartnerMapper partnerMapper;
    private final ErpProductMapper productMapper;

    @GetMapping("/options")
    public R<Map<String, Object>> options() {
        Map<String, Object> map = new HashMap<>();
        List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getStatus, 1).orderByAsc(SysUser::getId));
        users.forEach(u -> u.setPassword(null));
        map.put("users", users);
        map.put("workstations", workstationMapper.selectList(null));
        map.put("workshops", workshopMapper.selectList(null));
        map.put("routes", routeMapper.selectList(null));
        map.put("routeCategories", routeCategoryMapper.selectList(null));
        map.put("workOrders", workOrderMapper.selectList(new LambdaQueryWrapper<MesWorkOrder>()
                .orderByDesc(MesWorkOrder::getId)));
        map.put("warehouses", warehouseMapper.selectList((LambdaQueryWrapper<WmsWarehouse>) null));
        map.put("itemCategories", itemCategoryMapper.selectList((LambdaQueryWrapper<WmsItemCategory>) null));
        map.put("brands", brandMapper.selectList((LambdaQueryWrapper<WmsBrand>) null));
        map.put("partners", partnerMapper.selectList((LambdaQueryWrapper<WmsPartner>) null));
        map.put("products", productMapper.selectList(new LambdaQueryWrapper<ErpProduct>()
                .orderByDesc(ErpProduct::getId)));
        return R.ok(map);
    }
}
