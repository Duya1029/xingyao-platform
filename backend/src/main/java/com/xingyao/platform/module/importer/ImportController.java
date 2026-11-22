package com.xingyao.platform.module.importer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingyao.platform.common.R;
import com.xingyao.platform.common.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;
    private final SysImportBatchMapper batchMapper;
    private final SysImportErrorMapper errorMapper;

    @GetMapping("/types")
    public R<List<Map<String, String>>> types() {
        return R.ok(ImportService.TEMPLATES.values().stream()
                .map(t -> Map.of("bizType", t.bizType(), "label", t.label()))
                .toList());
    }

    @GetMapping("/template/{bizType}")
    public ResponseEntity<byte[]> template(@PathVariable String bizType) {
        byte[] bytes = importService.template(bizType);
        String fileName = URLEncoder.encode(bizType + "_模板.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }

    @PostMapping("/upload")
    public R<Map<String, Object>> upload(@RequestParam String bizType,
                                         @RequestParam("file") MultipartFile file) {
        return R.ok(importService.importFile(bizType, file, UserContext.username()));
    }

    @GetMapping("/batches")
    public R<IPage<SysImportBatch>> batches(@RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(batchMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysImportBatch>().orderByDesc(SysImportBatch::getId)));
    }

    @GetMapping("/batches/{id}/errors")
    public R<List<SysImportError>> errors(@PathVariable Long id) {
        return R.ok(errorMapper.selectList(new LambdaQueryWrapper<SysImportError>()
                .eq(SysImportError::getBatchId, id).orderByAsc(SysImportError::getRowNo)));
    }
}
