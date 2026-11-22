package com.xingyao.platform.module.importer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingyao.platform.common.BizException;
import com.xingyao.platform.module.erp.entity.ErpProduct;
import com.xingyao.platform.module.erp.mapper.ErpProductMapper;
import com.xingyao.platform.module.mes.entity.MesRouteCategory;
import com.xingyao.platform.module.mes.entity.MesWorkshop;
import com.xingyao.platform.module.mes.entity.MesWorkstation;
import com.xingyao.platform.module.mes.mapper.MesRouteCategoryMapper;
import com.xingyao.platform.module.mes.mapper.MesWorkshopMapper;
import com.xingyao.platform.module.mes.mapper.MesWorkstationMapper;
import com.xingyao.platform.module.wms.entity.WmsItem;
import com.xingyao.platform.module.wms.entity.WmsItemCategory;
import com.xingyao.platform.module.wms.mapper.WmsItemCategoryMapper;
import com.xingyao.platform.module.wms.mapper.WmsItemMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.*;

/** 导入中心：模板下载 + 行级校验 + 失败明细落库 */
@Service
@RequiredArgsConstructor
public class ImportService {

    private final SysImportBatchMapper batchMapper;
    private final SysImportErrorMapper errorMapper;
    private final WmsItemMapper itemMapper;
    private final WmsItemCategoryMapper itemCategoryMapper;
    private final MesWorkstationMapper workstationMapper;
    private final MesWorkshopMapper workshopMapper;
    private final MesRouteCategoryMapper routeCategoryMapper;
    private final ErpProductMapper productMapper;

    public record TemplateDef(String bizType, String label, String[] headers, String[] sample) {
    }

    public static final Map<String, TemplateDef> TEMPLATES = new LinkedHashMap<>();

    static {
        TEMPLATES.put("WMS_ITEM", new TemplateDef("WMS_ITEM", "WMS 物料基础数据",
                new String[]{"物料编码", "物料名称", "分类名称", "规格型号", "单位"},
                new String[]{"SKU-9001", "示例物料", "原材料", "Φ50*6000mm", "根"}));
        TEMPLATES.put("MES_WORKSTATION", new TemplateDef("MES_WORKSTATION", "MES 工作站基础数据",
                new String[]{"工作站编码", "工作站名称", "所属车间"},
                new String[]{"ST09", "CNC-09 加工中心", "机加车间"}));
        TEMPLATES.put("MES_ROUTE_CATEGORY", new TemplateDef("MES_ROUTE_CATEGORY", "MES 工艺分类",
                new String[]{"分类编码", "分类名称"},
                new String[]{"RC09", "热处理工艺"}));
        TEMPLATES.put("ERP_PRODUCT", new TemplateDef("ERP_PRODUCT", "ERP 产品基础数据",
                new String[]{"产品编码", "产品名称", "单位", "产品分类"},
                new String[]{"EP-9001", "示例产品", "件", "机加工成品"}));
    }

    public byte[] template(String bizType) {
        TemplateDef def = TEMPLATES.get(bizType);
        if (def == null) {
            throw new BizException("不支持的导入类型：" + bizType);
        }
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet(def.label());
            CellStyle headerStyle = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Row header = sheet.createRow(0);
            for (int i = 0; i < def.headers().length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(def.headers()[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 22 * 256);
            }
            Row sample = sheet.createRow(1);
            for (int i = 0; i < def.sample().length; i++) {
                sample.createCell(i).setCellValue(def.sample()[i]);
            }
            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new BizException("模板生成失败：" + e.getMessage());
        }
    }

    @Transactional
    public Map<String, Object> importFile(String bizType, MultipartFile file, String operator) {
        TemplateDef def = TEMPLATES.get(bizType);
        if (def == null) {
            throw new BizException("不支持的导入类型：" + bizType);
        }
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择要导入的文件");
        }
        List<List<String>> rows = readRows(file);
        SysImportBatch batch = new SysImportBatch();
        batch.setBatchNo("IMP" + System.currentTimeMillis());
        batch.setBizType(bizType);
        batch.setFileName(file.getOriginalFilename());
        batch.setTotalRows(rows.size());
        batch.setCreateUser(operator);
        batch.setCreateTime(LocalDateTime.now());
        batch.setStatus("RUNNING");
        batchMapper.insert(batch);

        int success = 0, fail = 0;
        Set<String> seenCodes = new HashSet<>();
        for (int i = 0; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            int rowNo = i + 2;
            List<String[]> errors = validate(bizType, row, seenCodes);
            if (!errors.isEmpty()) {
                fail++;
                for (String[] err : errors) {
                    SysImportError e = new SysImportError();
                    e.setBatchId(batch.getId());
                    e.setRowNo(rowNo);
                    e.setColumnName(err[0]);
                    e.setMessage(err[1]);
                    e.setRawData(String.join(" | ", row));
                    errorMapper.insert(e);
                }
                continue;
            }
            try {
                insertRow(bizType, row);
                success++;
            } catch (Exception e) {
                fail++;
                SysImportError err = new SysImportError();
                err.setBatchId(batch.getId());
                err.setRowNo(rowNo);
                err.setColumnName("整行");
                err.setMessage("写入失败：" + e.getMessage());
                err.setRawData(String.join(" | ", row));
                errorMapper.insert(err);
            }
        }
        batch.setSuccessRows(success);
        batch.setFailRows(fail);
        batch.setStatus(fail == 0 ? "DONE" : (success == 0 ? "FAILED" : "PARTIAL"));
        batchMapper.updateById(batch);

        Map<String, Object> res = new HashMap<>();
        res.put("batchId", batch.getId());
        res.put("batchNo", batch.getBatchNo());
        res.put("total", rows.size());
        res.put("success", success);
        res.put("fail", fail);
        res.put("status", batch.getStatus());
        return res;
    }

    private List<List<String>> readRows(MultipartFile file) {
        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            List<List<String>> rows = new ArrayList<>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                List<String> values = new ArrayList<>();
                boolean empty = true;
                for (int c = 0; c < 10; c++) {
                    Cell cell = row.getCell(c);
                    String value = cell == null ? "" : formatter.formatCellValue(cell).trim();
                    if (!value.isEmpty()) {
                        empty = false;
                    }
                    values.add(value);
                }
                if (!empty) {
                    rows.add(values);
                }
            }
            return rows;
        } catch (Exception e) {
            throw new BizException("文件解析失败，请使用系统提供的 .xlsx 模板");
        }
    }

    private List<String[]> validate(String bizType, List<String> row, Set<String> seenCodes) {
        List<String[]> errors = new ArrayList<>();
        String code = cell(row, 0);
        String name = cell(row, 1);
        if (!StringUtils.hasText(code)) {
            errors.add(new String[]{"编码", "编码不能为空"});
        } else if (!seenCodes.add(code)) {
            errors.add(new String[]{"编码", "文件内编码重复：" + code});
        }
        if (!StringUtils.hasText(name)) {
            errors.add(new String[]{"名称", "名称不能为空"});
        }
        if (!errors.isEmpty()) {
            return errors;
        }
        switch (bizType) {
            case "WMS_ITEM" -> {
                if (!StringUtils.hasText(cell(row, 4))) {
                    errors.add(new String[]{"单位", "单位不能为空"});
                }
                if (itemMapper.selectCount(new LambdaQueryWrapper<WmsItem>().eq(WmsItem::getCode, code)) > 0) {
                    errors.add(new String[]{"物料编码", "编码已存在：" + code});
                }
                String categoryName = cell(row, 2);
                if (StringUtils.hasText(categoryName) && findCategoryId(categoryName) == null) {
                    errors.add(new String[]{"分类名称", "分类不存在：" + categoryName});
                }
            }
            case "MES_WORKSTATION" -> {
                if (workstationMapper.selectCount(new LambdaQueryWrapper<MesWorkstation>()
                        .eq(MesWorkstation::getCode, code)) > 0) {
                    errors.add(new String[]{"工作站编码", "编码已存在：" + code});
                }
                String workshopName = cell(row, 2);
                if (StringUtils.hasText(workshopName) && findWorkshopId(workshopName) == null) {
                    errors.add(new String[]{"所属车间", "车间不存在：" + workshopName});
                }
            }
            case "MES_ROUTE_CATEGORY" -> {
                if (routeCategoryMapper.selectCount(new LambdaQueryWrapper<MesRouteCategory>()
                        .eq(MesRouteCategory::getCode, code)) > 0) {
                    errors.add(new String[]{"分类编码", "编码已存在：" + code});
                }
            }
            case "ERP_PRODUCT" -> {
                if (productMapper.selectCount(new LambdaQueryWrapper<ErpProduct>()
                        .eq(ErpProduct::getCode, code)) > 0) {
                    errors.add(new String[]{"产品编码", "编码已存在：" + code});
                }
            }
            default -> {
            }
        }
        return errors;
    }

    private void insertRow(String bizType, List<String> row) {
        switch (bizType) {
            case "WMS_ITEM" -> {
                WmsItem item = new WmsItem();
                item.setCode(cell(row, 0));
                item.setName(cell(row, 1));
                item.setCategoryId(findCategoryId(cell(row, 2)));
                item.setSpec(cell(row, 3));
                item.setUnit(cell(row, 4));
                item.setStatus(1);
                item.setCreateTime(LocalDateTime.now());
                itemMapper.insert(item);
            }
            case "MES_WORKSTATION" -> {
                MesWorkstation ws = new MesWorkstation();
                ws.setCode(cell(row, 0));
                ws.setName(cell(row, 1));
                ws.setWorkshopId(findWorkshopId(cell(row, 2)));
                ws.setStatus(1);
                workstationMapper.insert(ws);
            }
            case "MES_ROUTE_CATEGORY" -> {
                MesRouteCategory c = new MesRouteCategory();
                c.setCode(cell(row, 0));
                c.setName(cell(row, 1));
                c.setParentId(0L);
                c.setSort(99);
                c.setStatus(1);
                routeCategoryMapper.insert(c);
            }
            case "ERP_PRODUCT" -> {
                ErpProduct p = new ErpProduct();
                p.setCode(cell(row, 0));
                p.setName(cell(row, 1));
                p.setUnit(cell(row, 2));
                p.setCategoryName(cell(row, 3));
                p.setStatus(1);
                productMapper.insert(p);
            }
            default -> {
            }
        }
    }

    private Long findCategoryId(String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        WmsItemCategory category = itemCategoryMapper.selectOne(new LambdaQueryWrapper<WmsItemCategory>()
                .eq(WmsItemCategory::getName, name).last("OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY"));
        return category == null ? null : category.getId();
    }

    private Long findWorkshopId(String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        MesWorkshop workshop = workshopMapper.selectOne(new LambdaQueryWrapper<MesWorkshop>()
                .eq(MesWorkshop::getName, name).last("OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY"));
        return workshop == null ? null : workshop.getId();
    }

    private String cell(List<String> row, int index) {
        return index < row.size() ? row.get(index) : "";
    }
}
