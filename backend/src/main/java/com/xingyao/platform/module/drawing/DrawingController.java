package com.xingyao.platform.module.drawing;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingyao.platform.common.R;
import com.xingyao.platform.common.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drawing")
@RequiredArgsConstructor
public class DrawingController {

    private final DrawingService drawingService;
    private final DrwTaskMapper taskMapper;
    private final DrwFileMapper fileMapper;

    @PostMapping("/batch")
    public R<Map<String, Object>> upload(@RequestParam("files") List<MultipartFile> files) {
        Long taskId = drawingService.createBatch(files, UserContext.username());
        return R.ok(Map.of("taskId", taskId));
    }

    @GetMapping("/tasks")
    public R<IPage<DrwTask>> tasks(@RequestParam(defaultValue = "1") int pageNum,
                                   @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(taskMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<DrwTask>().orderByDesc(DrwTask::getId)));
    }

    @GetMapping("/tasks/{id}/files")
    public R<List<DrwFile>> files(@PathVariable Long id) {
        return R.ok(fileMapper.selectList(new LambdaQueryWrapper<DrwFile>()
                .eq(DrwFile::getTaskId, id)));
    }

    @PostMapping("/files/{id}/retry")
    public R<Void> retry(@PathVariable Long id) {
        drawingService.retry(id);
        return R.ok();
    }
}
